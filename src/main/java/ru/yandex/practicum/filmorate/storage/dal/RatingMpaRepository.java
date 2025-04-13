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
    private static final String GET_ONE_QUERY = "SELECT * FROM rating WHERE id = ?";
    private static final String GET_ALL_QUERY = "SELECT * FROM rating";

    private static final String INSERT_QUERY = "INSERT INTO rating (id, name) VALUES (?, ?)";

    private static final String UPDATE_QUERY = "UPDATE rating " +
            "SET id = ?, name = ? WHERE id = ?";

    private static final String DELETE_FILM_RATING_BY_RATING_ID = "DELETE FROM film_rating WHERE rating_id = ?";
    private static final String DELETE_FILM_RATING = "DELETE FROM film_rating";
    private static final String DELETE_BY_ID_QUERY = "DELETE FROM rating WHERE id = ?";
    private static final String DELETE_ALL_QUERY = "DELETE FROM rating";

    public RatingMpaRepository(JdbcTemplate jdbc, RowMapper<RatingMpaDto> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public RatingMpaDto get(Long id) {
        return findOne(GET_ONE_QUERY, id);
    }

    @Override
    public Collection<RatingMpaDto> getAll() {
        return findMany(GET_ALL_QUERY);
    }

    @Override
    public RatingMpaDto add(RatingMpaDto rating) {
        if (rating.getId() == null) {
            rating.setId(nextIdByTable("genre"));
        }

        insert(INSERT_QUERY,
                rating.getId(),
                rating.getName().getDbName());

        return get(rating.getId());
    }

    @Override
    public RatingMpaDto update(RatingMpaDto rating) {
        update(UPDATE_QUERY,
                rating.getId(),
                rating.getName().getDbName());
        return get(rating.getId());
    }

    @Override
    public boolean delete(Long id) {
        boolean deleteRating = deleteOne(DELETE_BY_ID_QUERY, id);
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
        boolean deleteRating = deleteAll(DELETE_ALL_QUERY);
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
