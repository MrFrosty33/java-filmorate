package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<Review> addReview(@Valid @RequestBody Review review) {
        return ResponseEntity.ok(reviewService.addReview(review));
    }

    @PutMapping
    public ResponseEntity<Review> updateReview(@Valid @RequestBody Review review) {
        return ResponseEntity.ok(reviewService.updateReview(review));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @GetMapping
    public ResponseEntity<List<Review>> getReviews(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(reviewService.getReviewsByFilmId(filmId, count));
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Review> addLike(@PathVariable Long id, @PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.addLike(id, userId));
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Review> addDislike(@PathVariable Long id, @PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.addDislike(id, userId));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Review> removeLike(@PathVariable Long id, @PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.deleteLike(id, userId));
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Review> removeDislike(@PathVariable Long id, @PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.deleteDislike(id, userId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleNotFoundException(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<String> handleConflictException(ConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Внутренняя ошибка сервера: " + e.getMessage());
    }
}