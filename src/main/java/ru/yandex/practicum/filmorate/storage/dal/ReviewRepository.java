package ru.yandex.practicum.filmorate.storage.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
@Slf4j
public class ReviewRepository extends BaseRepository<Review> implements ReviewStorage {
    private static final String GET_BY_ID = """
                    SELECT review_id, content, is_positive, user_id, film_id, useful \
                    FROM reviews WHERE review_id = ?""";

    private static final String GET_BY_FILM_ID = """
                    SELECT review_id, content, is_positive, user_id, film_id, useful \
                    FROM reviews WHERE film_id = ?""";

    private static final String GET_ALL = """
                    SELECT review_id, content, is_positive, user_id, film_id, useful FROM reviews""";

    private static final String INSERT_REVIEW = """
                    INSERT INTO reviews (content, is_positive, user_id, film_id, useful) \
                    VALUES (?, ?, ?, ?, ?)""";

    private static final String UPDATE_REVIEW = """
                    UPDATE reviews SET content = ?, is_positive = ?, useful = ? \
                    WHERE review_id = ?""";

    private static final String DELETE_BY_ID =
            "DELETE FROM reviews WHERE review_id = ?";

    private static final String COUNT_LIKES = """
                    SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND is_like = true""";

    private static final String COUNT_DISLIKES = """
                    SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND is_like = false""";

    private static final String HAS_LIKE = """
                    SELECT EXISTS(SELECT 1 FROM review_likes \
                    WHERE review_id = ? AND user_id = ? AND is_like = true)""";

    private static final String HAS_DISLIKE = """
                    SELECT EXISTS(SELECT 1 FROM review_likes \
                    WHERE review_id = ? AND user_id = ? AND is_like = false)""";

    private static final String ADD_LIKE =
            "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, true)";

    private static final String ADD_DISLIKE =
            "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, false)";

    private static final String DELETE_LIKE =
            "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = true";

    private static final String DELETE_DISLIKE =
            "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = false";

    private static final String DELETE_ALL_LIKES =
            "DELETE FROM review_likes WHERE review_id = ?";

    public ReviewRepository(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Review get(Long reviewId) {
        return findOne(GET_BY_ID, reviewId);
    }

    @Override
    public List<Review> getAll() {
        return findMany(GET_ALL);
    }

    @Override
    public List<Review> getByFilmId(Long filmId) {
        return findMany(GET_BY_FILM_ID, filmId);
    }

    @Override
    @Transactional
    public Review add(Review review) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    INSERT_REVIEW,
                    new String[]{"review_id"}
            );
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            ps.setInt(5, review.getUseful());
            return ps;
        }, keyHolder);

        review.setReviewId(keyHolder.getKey().longValue());
        return review;
    }

    @Override
    @Transactional
    public Review update(Review review) {
        Review oldReview = get(review.getReviewId());

        update(UPDATE_REVIEW,
                review.getContent(),
                review.getIsPositive(),
                review.getUseful(),
                review.getUserId());

        return get(review.getReviewId());
    }

    @Override
    @Transactional
    public boolean deleteById(Long reviewId) {

        boolean deleteReview = deleteOne(DELETE_BY_ID, reviewId);

        if (!deleteReview) {
            log.info("Произошла ошибка при удалении записи из таблицы reviews с id: {}", reviewId);
            throw new InternalServerException("Произошла ошибка при удалении записи из таблицы reviews с id: " + reviewId);
        }

        return true;
    }

    @Override
    public int countLikes(Long reviewId) {
        return jdbc.queryForObject(COUNT_LIKES, Integer.class, reviewId);
    }

    @Override
    public int countDislikes(Long reviewId) {
        return jdbc.queryForObject(COUNT_DISLIKES, Integer.class, reviewId);
    }

    @Override
    public boolean hasLike(Long reviewId, Long userId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                HAS_LIKE, Boolean.class, reviewId, userId));
    }

    @Override
    public boolean hasDislike(Long reviewId, Long userId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                HAS_DISLIKE, Boolean.class, reviewId, userId));
    }

    @Override
    @Transactional
    public void addLike(Long reviewId, Long userId) {
        insert(ADD_LIKE, reviewId, userId);
    }

    @Override
    @Transactional
    public void addDislike(Long reviewId, Long userId) {
        insert(ADD_DISLIKE, reviewId, userId);

    }

    @Override
    @Transactional
    public void deleteLike(Long reviewId, Long userId) {
        jdbc.update(DELETE_LIKE, reviewId, userId);
    }

    @Override
    @Transactional
    public void deleteDislike(Long reviewId, Long userId) {
        jdbc.update(DELETE_DISLIKE, reviewId, userId);
    }

    @Override
    @Transactional
    public void deleteAllLikesByReviewId(Long reviewId) {
        jdbc.update(DELETE_ALL_LIKES, reviewId);
    }
}
