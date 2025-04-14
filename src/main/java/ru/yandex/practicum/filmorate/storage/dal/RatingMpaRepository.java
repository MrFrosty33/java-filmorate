package ru.yandex.practicum.filmorate.storage.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.dto.RatingMpaDto;
import ru.yandex.practicum.filmorate.storage.RatingMpaStorage;

import java.util.Collection;

@Slf4j
@Repository
public class RatingMpaRepository extends BaseRepository<RatingMpaDto> implements RatingMpaStorage {
    private static final String GET_ONE = "SELECT * FROM rating WHERE id = ?";
    private static final String GET_ALL = "SELECT * FROM rating";

    private static final String INSERT_RATING = "INSERT INTO rating (id, name) VALUES (?, ?)";

    private static final String UPDATE_RATING = "UPDATE rating " +
            "SET id = ?, name = ? WHERE id = ?";

    private static final String DELETE_FILM_RATING_BY_RATING_ID = "DELETE FROM film_rating WHERE rating_id = ?";
    private static final String DELETE_FILM_RATING = "DELETE FROM film_rating";
    private static final String DELETE_RATING_BY_ID = "DELETE FROM rating WHERE id = ?";
    private static final String DELETE_ALL_RATINGS = "DELETE FROM rating";

    public RatingMpaRepository(JdbcTemplate jdbc, RowMapper<RatingMpaDto> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public RatingMpaDto get(Long id) {
        return findOne(GET_ONE, id);
    }

    @Override
    public Collection<RatingMpaDto> getAll() {
        return findMany(GET_ALL);
    }

    @Override
    public RatingMpaDto add(RatingMpaDto rating) {
        if (rating.getId() == null) {
            rating.setId(nextIdByTable("genre"));
        }

        insert(INSERT_RATING,
                rating.getId(),
                rating.getName().getDbName());

        return get(rating.getId());
    }

    @Override
    public RatingMpaDto update(RatingMpaDto rating) {
        update(UPDATE_RATING,
                rating.getId(),
                rating.getName().getDbName());
        return get(rating.getId());
    }

    @Override
    public boolean delete(Long id) {
        boolean deleteRating = deleteOne(DELETE_RATING_BY_ID, id);
        boolean deleteFilmRating = jdbc.update(DELETE_FILM_RATING_BY_RATING_ID, id) > 0;

        if (!deleteRating) {
            log.info("Произошла ошибка при удалении записи из таблицы rating с id: {}", id);
            throw new InternalServerException("Произошла ошибка при удалении рейтинга с id: " + id);
        }

        if (!deleteFilmRating) {
            log.info("Произошла ошибка при удалении записей из таблицы film_rating с rating_id: {}", id);
            throw new InternalServerException("Произошла ошибка при удалении связи film_rating с rating_id: " + id);
        }

        return true;
    }

    @Override
    public boolean deleteAll() {
        boolean deleteRating = deleteAll(DELETE_ALL_RATINGS);
        boolean deleteFilmRating = jdbc.update(DELETE_FILM_RATING) > 0;

        if (!deleteRating) {
            log.info("Произошла ошибка при удалении всех записей из таблицы rating");
            throw new InternalServerException("Произошла ошибка при удалении всех записей из таблицы rating");
        }

        if (!deleteFilmRating) {
            log.info("Произошла ошибка при удалении всех записей из таблицы film_rating");
            throw new InternalServerException("Произошла ошибка при удалении всех записей из таблицы film_rating");
        }

        return true;
    }
}
