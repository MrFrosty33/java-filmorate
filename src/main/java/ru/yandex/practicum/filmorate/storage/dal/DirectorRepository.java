package ru.yandex.practicum.filmorate.storage.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository
public class DirectorRepository extends BaseRepository<Director> implements DirectorStorage {
    private static final String GET_ONE = """
            SELECT * FROM director WHERE id = ?
            """;
    private static final String GET_ALL = """
            SELECT * FROM director
            """;

    private static final String INSERT_DIRECTOR = """
            INSERT INTO director (id, name) VALUES (?, ?)
            """;

    private static final String UPDATE_DIRECTOR = """
            UPDATE director
            SET name = ?
            WHERE id = ?
            """;

    private static final String DELETE_FILM_DIRECTOR_BY_DIRECTOR_ID = """
            DELETE FROM film_director WHERE director_id = ?
            """;
    private static final String DELETE_FILM_DIRECTOR = """
            DELETE FROM film_director
            """;
    private static final String DELETE_DIRECTOR_BY_ID = """
            DELETE FROM director WHERE id = ?
            """;
    private static final String DELETE_ALL_DIRECTORS = """
            DELETE FROM director
            """;



    public DirectorRepository(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Director get(Long id) {
        return findOne(GET_ONE, id);
    }

    @Override
    public Collection<Director> getAll() {
        return findMany(GET_ALL);
    }

    @Override
    public Director add(Director director) {
        if (director.getId() == null) {
            director.setId(nextIdByTable("director"));
        }

        insert(INSERT_DIRECTOR,
                director.getId(),
                director.getName());

        return get(director.getId());
    }

    @Override
    public Director update(Director director) {
        update(UPDATE_DIRECTOR,
                director.getName(),
                director.getId());
        return get(director.getId());
    }

    @Override
    public boolean delete(Long id) {
        deleteRelated(Optional.of(id));
        boolean deleteDirector = deleteOne(DELETE_DIRECTOR_BY_ID, id);

        if (!deleteDirector) {
            log.info("Произошла ошибка при удалении записи из таблицы director с id: {}", id);
            throw new InternalServerException("Произошла ошибка при удалении режиссёра с id: " + id);
        }

        return true;
    }

    @Override
    public boolean deleteAll() {
        deleteRelated(Optional.empty());
        boolean deleteDirector = deleteAll(DELETE_ALL_DIRECTORS);

        if (!deleteDirector) {
            log.info("Произошла ошибка при удалении всех записей из таблицы director");
            throw new InternalServerException("Произошла ошибка при удалении всех записей из таблицы director");
        }

        return true;
    }

    private void deleteRelated(Optional<Long> directorId) {
        try {
            if (directorId.isPresent()) {
                jdbc.update(DELETE_FILM_DIRECTOR_BY_DIRECTOR_ID, directorId.get());
                log.info("Были удалены все записи из таблицы film_director у режиссёра с id: {}", directorId.get());
            } else {
                jdbc.update(DELETE_FILM_DIRECTOR);
                log.info("Была очищена таблица film_director");
            }
        } catch (NullPointerException e) {
            log.info("В методе deleteRelated в DirectorRepository был передан null в качестве параметра");
            throw new InternalServerException("Произошла ошибка при удалении данных из смежных таблиц director");
        }
    }
}
