package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
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
    public Review addReview(Review review) {
        log.debug("Adding review: {}", review);
        validateUserAndFilm(review.getUserId(), review.getFilmId());
        review.setUseful(0);
        return reviewRepository.save(review);
    }

    @Override
    public Review updateReview(Review review) {
        log.debug("Updating review: {}", review);
        Review existingReview = getReviewById(review.getReviewId());
        validateUserAndFilm(review.getUserId(), review.getFilmId());
        existingReview.setContent(review.getContent());
        existingReview.setIsPositive(review.getIsPositive());
        existingReview.setUserId(review.getUserId());
        existingReview.setFilmId(review.getFilmId());
        return reviewRepository.save(existingReview);
    }

    @Override
    public void deleteReview(Long reviewId) {
        log.debug("Deleting review with id: {}", reviewId);
        getReviewById(reviewId);
        reviewRepository.deleteById(reviewId);
    }

    @Override
    public Review getReviewById(Long reviewId) {
        log.debug("Getting review with id: {}", reviewId);
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + reviewId + " не найден"));
    }

    @Override
    public List<Review> getReviewsByFilmId(Long filmId, int count) {
        log.debug("Getting reviews for filmId: {}, count: {}", filmId, count);
        if (filmId == null) {
            return reviewRepository.findAll().stream().limit(count).toList();
        }
        if (filmStorage.get(filmId) == null) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
        return reviewRepository.findByFilmId(filmId).stream().limit(count).toList();
    }

    @Override
    public Review addLike(Long reviewId, Long userId) {
        log.debug("Adding like to reviewId: {} by userId: {}", reviewId, userId);
        validateReviewAndUser(reviewId, userId);
        try {
            if (reviewRepository.hasLike(reviewId, userId)) {
                log.debug("User {} already liked review {}", userId, reviewId);
                return getReviewById(reviewId);
            }
            if (reviewRepository.hasDislike(reviewId, userId)) {
                reviewRepository.removeDislike(reviewId, userId);
            }
            reviewRepository.addLike(reviewId, userId);
            return updateUsefulAndGetReview(reviewId);
        } catch (DataIntegrityViolationException e) {
            log.warn("Conflict when adding like to reviewId: {} by userId: {}", reviewId, userId);
            throw new ConflictException("Пользователь уже поставил оценку этому отзыву");
        }
    }

    @Override
    public Review addDislike(Long reviewId, Long userId) {
        log.debug("Adding dislike to reviewId: {} by userId: {}", reviewId, userId);
        validateReviewAndUser(reviewId, userId);
        try {
            if (reviewRepository.hasDislike(reviewId, userId)) {
                log.debug("User {} already disliked review {}", userId, reviewId);
                return getReviewById(reviewId);
            }
            if (reviewRepository.hasLike(reviewId, userId)) {
                reviewRepository.removeLike(reviewId, userId);
            }
            reviewRepository.addDislike(reviewId, userId);
            return updateUsefulAndGetReview(reviewId);
        } catch (DataIntegrityViolationException e) {
            log.warn("Conflict when adding dislike to reviewId: {} by userId: {}", reviewId, userId);
            throw new ConflictException("Пользователь уже поставил оценку этому отзыву");
        }
    }

    @Override
    public Review removeLike(Long reviewId, Long userId) {
        log.debug("Removing like from reviewId: {} by userId: {}", reviewId, userId);
        validateReviewAndUser(reviewId, userId);
        if (!reviewRepository.hasLike(reviewId, userId)) {
            log.debug("No like found for reviewId: {} by userId: {}", reviewId, userId);
            return getReviewById(reviewId);
        }
        reviewRepository.removeLike(reviewId, userId);
        return updateUsefulAndGetReview(reviewId);
    }

    @Override
    public Review removeDislike(Long reviewId, Long userId) {
        log.debug("Removing dislike from reviewId: {} by userId: {}", reviewId, userId);
        validateReviewAndUser(reviewId, userId);
        if (!reviewRepository.hasDislike(reviewId, userId)) {
            log.debug("No dislike found for reviewId: {} by userId: {}", reviewId, userId);
            return getReviewById(reviewId);
        }
        reviewRepository.removeDislike(reviewId, userId);
        return updateUsefulAndGetReview(reviewId);
    }

    private void validateReviewAndUser(Long reviewId, Long userId) {
        if (reviewRepository.findById(reviewId).isEmpty()) {
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }
        if (userStorage.get(userId) == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }

    private Review updateUsefulAndGetReview(Long reviewId) {
        int likes = reviewRepository.countLikes(reviewId);
        int dislikes = reviewRepository.countDislikes(reviewId);
        int useful = likes - dislikes;
        Review review = getReviewById(reviewId);
        review.setUseful(useful);
        reviewRepository.save(review);
        log.debug("Updated useful for reviewId: {}, likes: {}, dislikes: {}, useful: {}", reviewId, likes, dislikes, useful);
        return review;
    }

    private void validateUserAndFilm(Long userId, Long filmId) {
        if (userStorage.get(userId) == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (filmStorage.get(filmId) == null) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
    }
}