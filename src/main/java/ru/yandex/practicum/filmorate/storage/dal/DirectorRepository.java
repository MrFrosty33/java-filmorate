package ru.yandex.practicum.filmorate.storage.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.Collection;

@Slf4j
@Repository
public class DirectorRepository extends BaseRepository<Director> implements DirectorStorage {
    private static final String GET_ONE = "SELECT * FROM director WHERE id = ?";
    private static final String GET_ALL = "SELECT * FROM director";

    private static final String INSERT_DIRECTOR = "INSERT INTO director (id, name) VALUES (?, ?)";

    private static final String UPDATE_DIRECTOR = "UPDATE director " +
            "SET id = ?, name = ? WHERE id = ?";

    private static final String DELETE_FILM_DIRECTOR_BY_DIRECTOR_ID = "DELETE FROM film_director WHERE director_id = ?";
    private static final String DELETE_FILM_DIRECTOR = "DELETE FROM film_director";
    private static final String DELETE_DIRECTOR_BY_ID = "DELETE FROM director WHERE id = ?";
    private static final String DELETE_ALL_DIRECTORS = "DELETE FROM director";

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
                director.getId(),
                director.getName(),
                director.getId());
        return get(director.getId());
    }

    @Override
    public boolean delete(Long id) {
        boolean deleteFilmDirector = jdbc.update(DELETE_FILM_DIRECTOR_BY_DIRECTOR_ID, id) > 0;
        boolean deleteDirector = deleteOne(DELETE_DIRECTOR_BY_ID, id);

        if (!deleteDirector) {
            log.info("Произошла ошибка при удалении записи из таблицы director с id: {}", id);
            throw new InternalServerException("Произошла ошибка при удалении режиссёра с id: " + id);
        }

        if (!deleteFilmDirector) {
            log.info("Произошла ошибка при удалении записей из таблицы film_director с director_id: {}", id);
            throw new InternalServerException("Произошла ошибка при удалении связи film_director с director_id: " + id);
        }

        return true;
    }

    @Override
    public boolean deleteAll() {
        boolean deleteFilmDirector = jdbc.update(DELETE_FILM_DIRECTOR) > 0;
        boolean deleteDirector = deleteAll(DELETE_ALL_DIRECTORS);

        if (!deleteDirector) {
            log.info("Произошла ошибка при удалении всех записей из таблицы director");
            throw new InternalServerException("Произошла ошибка при удалении всех записей из таблицы director");
        }

        if (!deleteFilmDirector) {
            log.info("Произошла ошибка при удалении всех записей из таблицы film_director");
            throw new InternalServerException("Произошла ошибка при удалении всех записей из таблицы film_director");
        }

        return true;
    }
}
