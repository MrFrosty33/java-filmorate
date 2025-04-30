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
        if (reviewId == null) {
            log.warn("Идентификатор отзыва null");
            throw new IllegalArgumentException("Идентификатор отзыва не может быть null");
        }
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.warn("Отзыв с id {} не найден", reviewId);
                    return new NotFoundException("Отзыв с id " + reviewId + " не найден");
                });
        log.debug("Получен отзыв с id {}", reviewId);
        return review;
    }

    @Override
    public List<Review> getReviewsByFilmId(Long filmId, int count) {
        if (count < 0) {
            log.warn("Количество отзывов {} отрицательное", count);
            throw new IllegalArgumentException("Количество отзывов не может быть отрицательным");
        }
        if (filmId == null) {
            log.debug("Получение всех отзывов с лимитом {}", count);
            return reviewRepository.findAll().stream().limit(count).toList();
        }
        if (filmStorage.get(filmId) == null) {
            log.warn("Фильм с id {} не найден", filmId);
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
        log.debug("Получение отзывов для фильма с id {} с лимитом {}", filmId, count);
        return reviewRepository.findByFilmId(filmId).stream().limit(count).toList();
    }

    @Override
    @Transactional
    public Review addReview(Review review) {
        if (review == null || review.getUserId() == null || review.getFilmId() == null || review.getIsPositive() == null) {
            log.warn("Отзыв или его обязательные поля null");
            throw new IllegalArgumentException("Отзыв или его обязательные поля не могут быть null");
        }
        validateUserAndFilm(review.getUserId(), review.getFilmId());
        review.setUseful(0);
        Review savedReview = reviewRepository.save(review);
        log.info("Создан отзыв с id {}", savedReview.getReviewId());
        return savedReview;
    }

    @Override
    @Transactional
    public Review updateReview(Review review) {
        if (review == null || review.getReviewId() == null) {
            log.warn("Отзыв или его идентификатор null");
            throw new IllegalArgumentException("Отзыв или его идентификатор не могут быть null");
        }
        Review existingReview = getReviewById(review.getReviewId());
        validateUserAndFilm(review.getUserId(), review.getFilmId());
        existingReview.setContent(review.getContent());
        existingReview.setIsPositive(review.getIsPositive());
        existingReview.setUserId(review.getUserId());
        existingReview.setFilmId(review.getFilmId());
        Review updatedReview = reviewRepository.save(existingReview);
        log.info("Обновлён отзыв с id {}", updatedReview.getReviewId());
        return updatedReview;
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        if (reviewId == null) {
            log.warn("Идентификатор отзыва null");
            throw new IllegalArgumentException("Идентификатор отзыва не может быть null");
        }
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
        if (reviewId == null || userId == null) {
            log.warn("Идентификатор отзыва или пользователя null");
            throw new IllegalArgumentException("Идентификатор отзыва или пользователя не может быть null");
        }
        validateReviewAndUser(reviewId, userId);
        try {
            if (reviewRepository.hasLike(reviewId, userId)) {
                log.debug("Лайк уже существует для отзыва с id {} от пользователя с id {}", reviewId, userId);
                return getReviewById(reviewId);
            }
            if (reviewRepository.hasDislike(reviewId, userId)) {
                reviewRepository.deleteDislike(reviewId, userId);
                log.debug("Удалён дизлайк для отзыва с id {} от пользователя с id {}", reviewId, userId);
            }
            reviewRepository.addLike(reviewId, userId);
            log.info("Добавлен лайк для отзыва с id {} от пользователя с id {}", reviewId, userId);
            return updateUsefulAndGetReview(reviewId);
        } catch (DataIntegrityViolationException e) {
            log.warn("Конфликт при добавлении лайка для отзыва с id {} от пользователя с id {}", reviewId, userId, e);
            throw new ConflictException("Пользователь уже оценил этот отзыв");
        } catch (Exception e) {
            log.error("Ошибка при добавлении лайка для отзыва с id {} от пользователя с id {}", reviewId, userId, e);
            throw new RuntimeException("Внутренняя ошибка сервера при добавлении лайка", e);
        }
    }

    @Override
    @Transactional
    public Review addDislike(Long reviewId, Long userId) {
        if (reviewId == null || userId == null) {
            log.warn("Идентификатор отзыва или пользователя null");
            throw new IllegalArgumentException("Идентификатор отзыва или пользователя не могут быть null");
        }
        validateReviewAndUser(reviewId, userId);
        try {
            if (reviewRepository.hasDislike(reviewId, userId)) {
                log.debug("Дизлайк уже существует для отзыва с id {} от пользователя с id {}", reviewId, userId);
                return getReviewById(reviewId);
            }
            if (reviewRepository.hasLike(reviewId, userId)) {
                reviewRepository.deleteLike(reviewId, userId);
                log.debug("Удалён лайк для отзыва с id {} от пользователя с id {}", reviewId, userId);
            }
            reviewRepository.addDislike(reviewId, userId);
            log.info("Добавлен дизлайк для отзыва с id {} от пользователя с id {}", reviewId, userId);
            return updateUsefulAndGetReview(reviewId);
        } catch (DataIntegrityViolationException e) {
            log.warn("Конфликт при добавлении дизлайка для отзыва с id {} от пользователя с id {}", reviewId, userId, e);
            throw new ConflictException("Пользователь уже оценил этот отзыв");
        } catch (Exception e) {
            log.error("Ошибка при добавлении дизлайка для отзыва с id {} от пользователя с id {}", reviewId, userId, e);
            throw new RuntimeException("Внутренняя ошибка сервера при добавлении дизлайка", e);
        }
    }

    @Override
    @Transactional
    public Review deleteLike(Long reviewId, Long userId) {
        if (reviewId == null || userId == null) {
            log.warn("Идентификатор отзыва или пользователя null");
            throw new IllegalArgumentException("Идентификатор отзыва или пользователя не могут быть null");
        }
        validateReviewAndUser(reviewId, userId);
        if (reviewRepository.hasLike(reviewId, userId)) {
            reviewRepository.deleteLike(reviewId, userId);
            log.info("Удалён лайк для отзыва с id {} от пользователя с id {}", reviewId, userId);
            return updateUsefulAndGetReview(reviewId);
        }
        log.debug("Лайк для отзыва с id {} от пользователя с id {} не найден", reviewId, userId);
        return getReviewById(reviewId);
    }

    @Override
    @Transactional
    public Review deleteDislike(Long reviewId, Long userId) {
        if (reviewId == null || userId == null) {
            log.warn("Идентификатор отзыва или пользователя null");
            throw new IllegalArgumentException("Идентификатор отзыва или пользователя не могут быть null");
        }
        validateReviewAndUser(reviewId, userId);
        if (reviewRepository.hasDislike(reviewId, userId)) {
            reviewRepository.deleteDislike(reviewId, userId);
            log.info("Удалён дизлайк для отзыва с id {} от пользователя с id {}", reviewId, userId);
            return updateUsefulAndGetReview(reviewId);
        }
        log.debug("Дизлайк для отзыва с id {} от пользователя с id {} не найден", reviewId, userId);
        return getReviewById(reviewId);
    }

    private void validateReviewAndUser(Long reviewId, Long userId) {
        if (reviewRepository.findById(reviewId).isEmpty()) {
            log.warn("Отзыв с id {} не найден", reviewId);
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }
        if (userStorage.get(userId) == null) {
            log.warn("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }

    private Review updateUsefulAndGetReview(Long reviewId) {
        Review review = getReviewById(reviewId);
        int likes = reviewRepository.countLikes(reviewId);
        int dislikes = reviewRepository.countDislikes(reviewId);
        review.setUseful(likes - dislikes);
        Review updatedReview = reviewRepository.save(review);
        log.debug("Обновлена полезность отзыва с id {}: лайков {}, дизлайков {}, полезность {}",
                reviewId, likes, dislikes, review.getUseful());
        return updatedReview;
    }

    private void validateUserAndFilm(Long userId, Long filmId) {
        if (userId == null || filmId == null) {
            log.warn("Идентификатор пользователя или фильма null");
            throw new IllegalArgumentException("Идентификатор пользователя или фильма не может быть null");
        }
        if (userStorage.get(userId) == null) {
            log.warn("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (filmStorage.get(filmId) == null) {
            log.warn("Фильм с id {} не найден", filmId);
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
    }
}