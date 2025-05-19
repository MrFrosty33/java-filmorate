package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;
import java.util.List;

public interface ReviewStorage {
    /*List<Review> findByFilmId(Long filmId);

    @Query(value = "SELECT COUNT(*) FROM review_likes WHERE review_id = :reviewId AND is_like = true", nativeQuery = true)
    int countLikes(Long reviewId);

    @Query(value = "SELECT COUNT(*) FROM review_likes WHERE review_id = :reviewId AND is_like = false", nativeQuery = true)
    int countDislikes(Long reviewId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM review_likes WHERE review_id = :reviewId AND user_id = :userId AND is_like = true)", nativeQuery = true)
    boolean hasLike(Long reviewId, Long userId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM review_likes WHERE review_id = :reviewId AND user_id = :userId AND is_like = false)", nativeQuery = true)
    boolean hasDislike(Long reviewId, Long userId);

    @Modifying
    @Query(value = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (:reviewId, :userId, true)", nativeQuery = true)
    void addLike(Long reviewId, Long userId);

    @Modifying
    @Query(value = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (:reviewId, :userId, false)", nativeQuery = true)
    void addDislike(Long reviewId, Long userId);

    @Modifying
    @Query(value = "DELETE FROM review_likes WHERE review_id = :reviewId AND user_id = :userId AND is_like = true", nativeQuery = true)
    void deleteLike(Long reviewId, Long userId);

    @Modifying
    @Query(value = "DELETE FROM review_likes WHERE review_id = :reviewId AND user_id = :userId AND is_like = false", nativeQuery = true)
    void deleteDislike(Long reviewId, Long userId);

    @Modifying
    @Query(value = "DELETE FROM review_likes WHERE review_id = :reviewId", nativeQuery = true)
    void deleteAllLikesByReviewId(Long reviewId);*/
    Review get(Long reviewId);

    List<Review> getByFilmId(Long filmId);

    List<Review> getAll();

    Review add(Review review);

    Review update(Review review);

    boolean deleteById(Long reviewId);

    int countLikes(Long reviewId);

    int countDislikes(Long reviewId);

    boolean hasLike(Long reviewId, Long userId);

    boolean hasDislike(Long reviewId, Long userId);

    void addLike(Long reviewId, Long userId);

    void addDislike(Long reviewId, Long userId);

    void deleteLike(Long reviewId, Long userId);

    void deleteDislike(Long reviewId, Long userId);

    void deleteAllLikesByReviewId(Long reviewId);
}