package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.ReviewRepository;

import java.util.Comparator;
import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository, UserStorage userStorage, FilmStorage filmStorage) {
        this.reviewRepository = reviewRepository;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    @Override
    public Review addReview(Review review) {
        if (userStorage.get(review.getUserId()) == null) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        if (filmStorage.get(review.getFilmId()) == null) {
            throw new IllegalArgumentException("Фильм не найден");
        }
        review.setUseful(0);
        return reviewRepository.save(review);
    }

    @Override
    public Review updateReview(Review review) {
        if (!reviewRepository.existsById(review.getReviewId())) {
            throw new IllegalArgumentException("Отзыв не найден");
        }
        return reviewRepository.save(review);
    }

    @Override
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    @Override
    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));
    }

    @Override
    public List<Review> getReviewsByFilmId(Long filmId, int count) {
        List<Review> reviews;
        if (filmId != null) {
            reviews = reviewRepository.findByFilmId(filmId);
        } else {
            reviews = reviewRepository.findAll();
        }
        // Сортируем по рейтингу полезности (по убыванию)
        reviews.sort(Comparator.comparing(Review::getUseful).reversed());
        // Ограничиваем количество отзывов
        if (count > 0 && count < reviews.size()) {
            reviews = reviews.subList(0, count);
        }
        return reviews;
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        Review review = getReviewById(reviewId);
        review.setUseful(review.getUseful() + 1);
        reviewRepository.save(review);
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        Review review = getReviewById(reviewId);
        review.setUseful(review.getUseful() - 1);
        reviewRepository.save(review);
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        Review review = getReviewById(reviewId);
        review.setUseful(review.getUseful() - 1);
        reviewRepository.save(review);
    }

    @Override
    public void removeDislike(Long reviewId, Long userId) {
        Review review = getReviewById(reviewId);
        review.setUseful(review.getUseful() + 1);
        reviewRepository.save(review);
    }
}