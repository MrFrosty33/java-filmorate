package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewService {

    Review addReview(Review review);

    Review updateReview(Review review);

    void deleteReview(Long reviewId);

    Review getReviewById(Long reviewId);

    List<Review> getReviewsByFilmId(Long filmId, int count);

    Review addLike(Long reviewId, Long userId);

    Review addDislike(Long reviewId, Long userId);

    Review removeLike(Long reviewId, Long userId);

    Review removeDislike(Long reviewId, Long userId);
}