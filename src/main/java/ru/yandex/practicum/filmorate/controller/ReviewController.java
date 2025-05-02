package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public Review addReview(@Valid @RequestBody @NotNull Review review) {
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody @NotNull Review review) {
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable("id") @NotNull Long id) {
        reviewService.deleteReview(id);
    }

    @GetMapping("/{id}")
    public Review getReviewById(
            @PathVariable("id") @NotNull Long id) {
        return reviewService.getReviewById(id);
    }

    @GetMapping
    public List<Review> getReviews(
            @RequestParam(value = "filmId", required = false) Long filmId,
            @RequestParam(value = "count", defaultValue = "10")
            @Min(value = 1) int count) {
        return reviewService.getReviewsByFilmId(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public Review addLike(
            @PathVariable("id") @NotNull Long id,
            @PathVariable("userId") @NotNull Long userId) {
        return reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public Review addDislike(
            @PathVariable("id") @NotNull Long id,
            @PathVariable("userId") @NotNull Long userId) {
        return reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Review removeLike(
            @PathVariable("id") @NotNull Long id,
            @PathVariable("userId") @NotNull Long userId) {
        return reviewService.deleteLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public Review removeDislike(
            @PathVariable("id") @NotNull Long id,
            @PathVariable("userId") @NotNull Long userId) {
        return reviewService.deleteDislike(id, userId);
    }
}