package ru.yandex.practicum.filmorate.storage.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.dto.RatingMpaDto;
import ru.yandex.practicum.filmorate.storage.RatingMpaStorage;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository
public class RatingMpaRepository extends BaseRepository<RatingMpaDto> implements RatingMpaStorage {
    private static final String GET_ONE = """
            SELECT * FROM rating WHERE id = ?
            """;
    private static final String GET_ALL = """
            SELECT * FROM rating
            """;

    private static final String INSERT_RATING = """
            INSERT INTO rating (id, name) VALUES (?, ?)
            """;

    private static final String UPDATE_RATING = """
            UPDATE rating
            SET name = ?
            WHERE id = ?
            """;

    private static final String DELETE_FILM_RATING_BY_RATING_ID = """
            DELETE FROM film_rating WHERE rating_id = ?
            """;
    private static final String DELETE_FILM_RATING = """
            DELETE FROM film_rating
            """;
    private static final String DELETE_RATING_BY_ID = """
            DELETE FROM rating WHERE id = ?
            """;
    private static final String DELETE_ALL_RATINGS = """
            DELETE FROM rating
            """;


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
                rating.getName().getDbName(),
                rating.getId());
        return get(rating.getId());
    }

    @Override
    public boolean delete(Long id) {
        deleteRelated(Optional.of(id));
        boolean deleteRating = deleteOne(DELETE_RATING_BY_ID, id);

        if (!deleteRating) {
            log.info("Произошла ошибка при удалении записи из таблицы rating с id: {}", id);
            throw new InternalServerException("Произошла ошибка при удалении рейтинга с id: " + id);
        }

        return true;
    }

    @Override
    public boolean deleteAll() {
        deleteRelated(Optional.empty());
        boolean deleteRating = deleteAll(DELETE_ALL_RATINGS);

        if (!deleteRating) {
            log.info("Произошла ошибка при удалении всех записей из таблицы rating");
            throw new InternalServerException("Произошла ошибка при удалении всех записей из таблицы rating");
        }

        return true;
    }

    private void deleteRelated(Optional<Long> ratingId) {
        try {
            if (ratingId.isPresent()) {
                jdbc.update(DELETE_FILM_RATING_BY_RATING_ID, ratingId.get());
                log.info("Были удалены все записи из таблицы film_rating у рейтинга с id: {}", ratingId.get());
            } else {
                jdbc.update(DELETE_FILM_RATING);
                log.info("Была очищена таблица film_rating");
            }
        } catch (NullPointerException e) {
            log.info("В методе deleteRelated в RatingMpaRepository был передан null в качестве параметра");
            throw new InternalServerException("Произошла ошибка при удалении данных из смежных таблиц rating");
        }
    }
}
