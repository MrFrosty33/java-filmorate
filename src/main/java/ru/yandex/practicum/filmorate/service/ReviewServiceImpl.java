package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewRepository;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @Override
    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.warn("Отзыв с id {} не найден", reviewId);
                    return new NotFoundException("Отзыв с id " + reviewId + " не найден");
                });
    }

    @Override
    public List<Review> getReviewsByFilmId(Long filmId, int count) {
        if (filmId != null && filmStorage.get(filmId) == null) {
            log.warn("Фильм с id {} не найден", filmId);
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }

        List<Review> all = (filmId == null)
                ? reviewRepository.findAll()
                : reviewRepository.findByFilmId(filmId);

        return all.stream()
                .limit(count)
                .toList();
    }

    @Override
    @Transactional
    public Review addReview(Review review) {
        validateUserAndFilm(review.getUserId(), review.getFilmId());
        review.setUseful(0);
        Review saved = reviewRepository.save(review);
        log.info("Создан отзыв с id {}", saved.getReviewId());
        return saved;
    }

    @Override
    @Transactional
    public Review updateReview(Review review) {
        Review existing = getReviewById(review.getReviewId());
        validateUserAndFilm(review.getUserId(), review.getFilmId());

        existing.setContent(review.getContent());
        existing.setIsPositive(review.getIsPositive());
        existing.setUserId(review.getUserId());
        existing.setFilmId(review.getFilmId());

        Review updated = reviewRepository.save(existing);
        log.info("Обновлён отзыв с id {}", updated.getReviewId());
        return updated;
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            log.warn("Отзыв с id {} не найден", reviewId);
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }
        reviewRepository.deleteById(reviewId);
        log.info("Удалён отзыв с id {}", reviewId);
    }

    @Override
    @Transactional
    public Review addLike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);

        try {
            if (reviewRepository.hasLike(reviewId, userId)) {
                log.debug("Лайк уже существует для отзыва {} от пользователя {}", reviewId, userId);
                return getReviewById(reviewId);
            }
            if (reviewRepository.hasDislike(reviewId, userId)) {
                reviewRepository.deleteDislike(reviewId, userId);
                log.debug("Удалён дизлайк для отзыва {} от пользователя {}", reviewId, userId);
            }
            reviewRepository.addLike(reviewId, userId);
            log.info("Добавлен лайк для отзыва {} от пользователя {}", reviewId, userId);
            return updateUsefulAndGetReview(reviewId);

        } catch (DataIntegrityViolationException e) {
            log.warn("Пользователь {} уже оценил отзыв {}", userId, reviewId, e);
            throw new ConflictException("Пользователь уже оценил этот отзыв");
        } catch (Exception e) {
            log.error("Ошибка при добавлении лайка", e);
            throw new RuntimeException("Внутренняя ошибка при добавлении лайка", e);
        }
    }

    @Override
    @Transactional
    public Review addDislike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);

        try {
            if (reviewRepository.hasDislike(reviewId, userId)) {
                log.debug("Дизлайк уже существует для отзыва {} от пользователя {}", reviewId, userId);
                return getReviewById(reviewId);
            }
            if (reviewRepository.hasLike(reviewId, userId)) {
                reviewRepository.deleteLike(reviewId, userId);
                log.debug("Удалён лайк для отзыва {} от пользователя {}", reviewId, userId);
            }
            reviewRepository.addDislike(reviewId, userId);
            log.info("Добавлен дизлайк для отзыва {} от пользователя {}", reviewId, userId);
            return updateUsefulAndGetReview(reviewId);

        } catch (DataIntegrityViolationException e) {
            log.warn("Пользователь {} уже оценил отзыв {}", userId, reviewId, e);
            throw new ConflictException("Пользователь уже оценил этот отзыв");
        } catch (Exception e) {
            log.error("Ошибка при добавлении дизлайка", e);
            throw new RuntimeException("Внутренняя ошибка при добавлении дизлайка", e);
        }
    }

    @Override
    @Transactional
    public Review deleteLike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);

        if (reviewRepository.hasLike(reviewId, userId)) {
            reviewRepository.deleteLike(reviewId, userId);
            log.info("Удалён лайк для отзыва {} от пользователя {}", reviewId, userId);
            return updateUsefulAndGetReview(reviewId);
        }
        log.debug("Лайк не найден для отзыва {} от пользователя {}", reviewId, userId);
        return getReviewById(reviewId);
    }

    @Override
    @Transactional
    public Review deleteDislike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);

        if (reviewRepository.hasDislike(reviewId, userId)) {
            reviewRepository.deleteDislike(reviewId, userId);
            log.info("Удалён дизлайк для отзыва {} от пользователя {}", reviewId, userId);
            return updateUsefulAndGetReview(reviewId);
        }
        log.debug("Дизлайк не найден для отзыва {} от пользователя {}", reviewId, userId);
        return getReviewById(reviewId);
    }

    private void validateReviewAndUser(Long reviewId, Long userId) {
        if (reviewRepository.findById(reviewId).isEmpty()) {
            log.warn("Отзыв {} не найден", reviewId);
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }
        if (userStorage.get(userId) == null) {
            log.warn("Пользователь {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }

    private Review updateUsefulAndGetReview(Long reviewId) {
        Review review = getReviewById(reviewId);
        int likes    = reviewRepository.countLikes(reviewId);
        int dislikes = reviewRepository.countDislikes(reviewId);
        review.setUseful(likes - dislikes);
        Review updated = reviewRepository.save(review);
        log.debug("Полезность отзыва {} обновлена: {}", reviewId, review.getUseful());
        return updated;
    }

    private void validateUserAndFilm(Long userId, Long filmId) {
        if (userStorage.get(userId) == null) {
            log.warn("Пользователь {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (filmStorage.get(filmId) == null) {
            log.warn("Фильм {} не найден", filmId);
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
    }
}
