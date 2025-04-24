package ru.yandex.practicum.filmorate.storage.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.dto.GenreDto;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;

@Slf4j
@Repository
public class GenreRepository extends BaseRepository<GenreDto> implements GenreStorage {
    private static final String GET_ONE = "SELECT * FROM genre WHERE id = ?";
    private static final String GET_ALL = "SELECT * FROM genre";

    private static final String INSERT_GENRE = "INSERT INTO genre (id, name) VALUES (?, ?)";

    private static final String UPDATE_GENRE = "UPDATE genre " +
            "SET id = ?, name = ? WHERE id = ?";

    private static final String DELETE_FILM_GENRE_BY_GENRE_ID = "DELETE FROM film_genre WHERE genre_id = ?";
    private static final String DELETE_FILM_GENRE = "DELETE FROM film_genre";
    private static final String DELETE_GENRE_BY_ID = "DELETE FROM genre WHERE id = ?";
    private static final String DELETE_ALL_GENRES = "DELETE FROM genre";

    public GenreRepository(JdbcTemplate jdbc, RowMapper<GenreDto> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public GenreDto get(Long id) {
        return findOne(GET_ONE, id);
    }

    @Override
    public Collection<GenreDto> getAll() {
        return findMany(GET_ALL);
    }

    @Override
    public GenreDto add(GenreDto genre) {
        if (genre.getId() == null) {
            genre.setId(nextIdByTable("genre"));
        }

        insert(INSERT_GENRE,
                genre.getId(),
                genre.getName());

        return get(genre.getId());
    }

    @Override
    public GenreDto update(GenreDto genre) {
        update(UPDATE_GENRE,
                genre.getId(),
                genre.getName(),
                genre.getId());
        return get(genre.getId());
    }

    @Override
    public boolean delete(Long id) {
        boolean deleteGenre = deleteOne(DELETE_GENRE_BY_ID, id);
        boolean deleteFilmGenre = jdbc.update(DELETE_FILM_GENRE_BY_GENRE_ID, id) > 0;

        if (!deleteGenre) {
            log.info("Произошла ошибка при удалении записи из таблицы genre с id: {}", id);
            throw new InternalServerException("Произошла ошибка при удалении жанра с id: " + id);
        }

//        if (!deleteFilmGenre) {
//            log.info("Произошла ошибка при удалении записей из таблицы film_genre с genre_id: {}", id);
//            throw new InternalServerException("Произошла ошибка при удалении связи film_genre с genre_id: " + id);
//        }

        return true;
    }

    @Override
    public boolean deleteAll() {
        // если удалять все жанры, то удалять и все связи фильм-жанр
        boolean deleteGenre = deleteAll(DELETE_ALL_GENRES);
        boolean deleteFilmGenre = jdbc.update(DELETE_FILM_GENRE) > 0;

        if (!deleteGenre) {
            log.info("Произошла ошибка при удалении всех записей из таблицы genre");
            throw new InternalServerException("Произошла ошибка при удалении всех записей из таблицы genre");
        }

        if (!deleteFilmGenre) {
            log.info("Произошла ошибка при удалении всех записей из таблицы film_genre");
            throw new InternalServerException("Произошла ошибка при удалении всех записей из таблицы film_genre");
        }

        return true;
    }
}
