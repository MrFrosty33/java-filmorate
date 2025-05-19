package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FeedStorage feedRepository;

    @Override
    public Review getReviewById(Long reviewId) {
        validateReviewExists(reviewId,
                new NotFoundException("Не существует отзыва с id: " + reviewId),
                "Попытка получить несуществующий отзыв с id: " + reviewId);

        log.info("Получен отзыв с id: {}", reviewId);
        return reviewStorage.get(reviewId);
    }

    @Override
    public List<Review> getReviewsByFilmId(Long filmId, int count) {
        if (filmId != null && filmStorage.get(filmId) == null) {
            log.warn("Отзыв с id {} не найден", filmId);
            throw new NotFoundException("Отзыв с id " + filmId + " не найден");
        }

        List<Review> all = (filmId == null)
                ? reviewStorage.getAll()
                : reviewStorage.getByFilmId(filmId);

        return all.stream()
                .sorted(Comparator.comparingInt(Review::getUseful).reversed())
                .limit(count)
                .toList();
    }

    @Override
    @Transactional
    public Review addReview(Review review) {
        validateUserAndFilm(review.getUserId(), review.getFilmId());
        review.setUseful(0);
        Review saved = reviewStorage.add(review);
        log.info("Создан отзыв с id {}", saved.getReviewId());

        feedRepository.addEventToFeed(saved.getUserId(), EventType.REVIEW, Operation.ADD, saved.getReviewId());
        log.info("Событие добавлено в ленту: пользователь с id: {} добавил отзыв с id: {}",
                saved.getUserId(), saved.getReviewId());

        return saved;
    }

    @Override
    @Transactional
    public Review updateReview(Review review) {
        Long id = review.getReviewId();
        Review existing = getReviewById(id);
        validateUserAndFilm(review.getUserId(), review.getFilmId());

        existing.setContent(review.getContent());
        existing.setIsPositive(review.getIsPositive());
        existing.setUseful(reviewStorage.countLikes(id) - reviewStorage.countDislikes(id));

        Review updated = reviewStorage.update(existing);
        log.info("Обновлён отзыв с id {}", id);

        feedRepository.addEventToFeed(updated.getUserId(), EventType.REVIEW, Operation.UPDATE, updated.getReviewId());
        log.info("Событие добавлено в ленту: пользователь с id: {} обновил отзыв с id: {}",
                updated.getUserId(), id);

        return updated;
    }

    private Review updateUsefulAndGetReview(Long reviewId) {
        Review review = getReviewById(reviewId);
        int likes = reviewStorage.countLikes(reviewId);
        int dislikes = reviewStorage.countDislikes(reviewId);
        review.setUseful(likes - dislikes);
        Review updated = reviewStorage.update(review);
        log.debug("Полезность отзыва {} обновлена: {}", reviewId, review.getUseful());
        return updated;
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        Review existing = reviewStorage.get(reviewId);

        reviewStorage.deleteAllLikesByReviewId(reviewId);
        feedRepository.addEventToFeed(existing.getUserId(), EventType.REVIEW, Operation.REMOVE, existing.getReviewId());
        log.info("Событие добавлено в ленту: пользователь с id: {} удалил отзыв с id: {}",
                existing.getUserId(), reviewId);

        reviewStorage.deleteById(reviewId);
        log.info("Удалён отзыв с id {}", reviewId);

    }

    @Override
    @Transactional
    public Review addLike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);
        if (reviewStorage.hasLike(reviewId, userId)) {
            log.debug("Лайк уже существует для отзыва {} от пользователя {}", reviewId, userId);
            return getReviewById(reviewId);
        }
        if (reviewStorage.hasDislike(reviewId, userId)) {
            reviewStorage.deleteDislike(reviewId, userId);
            log.debug("Удалён дизлайк для отзыва {} от пользователя {}", reviewId, userId);
            return getReviewById(reviewId);
        }
        reviewStorage.addLike(reviewId, userId);
        log.info("Добавлен лайк для отзыва {} от пользователя {}", reviewId, userId);

        return updateUsefulAndGetReview(reviewId);
    }

    @Override
    @Transactional
    public Review addDislike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);

        try {
            if (reviewStorage.hasDislike(reviewId, userId)) {
                log.debug("Дизлайк уже существует для отзыва {} от пользователя {}", reviewId, userId);
                return getReviewById(reviewId);
            }
            if (reviewStorage.hasLike(reviewId, userId)) {
                reviewStorage.deleteLike(reviewId, userId);
                log.debug("Удалён лайк для отзыва {} от пользователя {}", reviewId, userId);
            }
            reviewStorage.addDislike(reviewId, userId);
            log.info("Добавлен дизлайк для отзыва {} от пользователя {}", reviewId, userId);
            return updateUsefulAndGetReview(reviewId);

        } catch (DataIntegrityViolationException e) {
            log.warn("Пользователь {} уже оценил отзыв {}", userId, reviewId, e);
            throw new ConflictException("Пользователь уже оценил этот отзыв");
        }
    }

    @Override
    @Transactional
    public Review deleteLike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);

        if (reviewStorage.hasLike(reviewId, userId)) {
            reviewStorage.deleteLike(reviewId, userId);
            log.info("Удалён лайк для отзыва {} от пользователя {}", reviewId, userId);

            feedRepository.addEventToFeed(userId, EventType.LIKE, Operation.REMOVE, reviewId);
            log.info("Событие добавлено в ленту: пользователь с id: {} удалил лайк с отзыва с id: {}",
                    userId, reviewId);
        }
        log.debug("Лайк не найден для отзыва {} от пользователя {}", reviewId, userId);
        return updateUsefulAndGetReview(reviewId);
    }

    @Override
    @Transactional
    public Review deleteDislike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);

        if (reviewStorage.hasDislike(reviewId, userId)) {
            reviewStorage.deleteDislike(reviewId, userId);
            log.info("Удалён дизлайк для отзыва {} от пользователя {}", reviewId, userId);
        }
        log.debug("Дизлайк не найден для отзыва {} от пользователя {}", reviewId, userId);
        return updateUsefulAndGetReview(reviewId);
    }

    private void validateReviewExists(Long id,
                                    RuntimeException e, String logMessage) {
        try {
                Optional<Review> result = Optional.ofNullable(reviewStorage.get(id));
                if (result.isEmpty()) {
                    log.info(logMessage);
                    throw e;
                }
            } catch (EmptyResultDataAccessException ex) {
            log.info(logMessage);
            throw e;
        }
    }

    private void validateReviewAndUser(Long reviewId, Long userId) {
        validateReviewExists(reviewId,
                new NotFoundException("Не существует отзыва с id: " + reviewId),
                "Попытка получить несуществующий отзыв с id: " + reviewId);
        if (userStorage.get(userId) == null) {
            log.warn("Пользователь {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
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