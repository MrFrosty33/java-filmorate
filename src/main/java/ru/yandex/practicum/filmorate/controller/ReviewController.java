package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
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
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<Review> addReview(
            @Valid @RequestBody @NotNull Review review
    ) {
        return ResponseEntity.ok(reviewService.addReview(review));
    }

    @PutMapping
    public ResponseEntity<Review> updateReview(
            @Valid @RequestBody @NotNull Review review
    ) {
        return ResponseEntity.ok(reviewService.updateReview(review));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable("id") @NotNull Long id
    ) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(
            @PathVariable("id") @NotNull Long id
    ) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @GetMapping
    public ResponseEntity<List<Review>> getReviews(
            @RequestParam(value = "filmId", required = false) Long filmId,
            @RequestParam(value = "count", defaultValue = "10")
            @Min(value = 1) int count
    ) {
        return ResponseEntity.ok(reviewService.getReviewsByFilmId(filmId, count));
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Review> addLike(
            @PathVariable("id") @NotNull Long id,
            @PathVariable("userId") @NotNull Long userId
    ) {
        return ResponseEntity.ok(reviewService.addLike(id, userId));
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Review> addDislike(
            @PathVariable("id") @NotNull Long id,
            @PathVariable("userId") @NotNull Long userId
    ) {
        return ResponseEntity.ok(reviewService.addDislike(id, userId));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Review> removeLike(
            @PathVariable("id") @NotNull Long id,
            @PathVariable("userId") @NotNull Long userId
    ) {
        Review updated = reviewService.deleteLike(id, userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Review> removeDislike(
            @PathVariable("id") @NotNull Long id,
            @PathVariable("userId") @NotNull Long userId
    ) {
        Review updated = reviewService.deleteDislike(id, userId);
        return ResponseEntity.ok(updated);
    }
}