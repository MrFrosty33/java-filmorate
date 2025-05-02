package ru.yandex.practicum.filmorate.storage.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository
public class GenreRepository extends BaseRepository<Genre> implements GenreStorage {
    private static final String GET_ONE = """
            SELECT * FROM genre WHERE id = ?
            """;
    private static final String GET_ALL = """
            SELECT * FROM genre
            """;

    private static final String INSERT_GENRE = """
            INSERT INTO genre (id, name) VALUES (?, ?)
            """;

    private static final String UPDATE_GENRE = """
            UPDATE genre
            SET name = ?
            WHERE id = ?
            """;

    private static final String DELETE_FILM_GENRE_BY_GENRE_ID = """
            DELETE FROM film_genre WHERE genre_id = ?
            """;
    private static final String DELETE_FILM_GENRE = """
            DELETE FROM film_genre
            """;
    private static final String DELETE_GENRE_BY_ID = """
            DELETE FROM genre WHERE id = ?
            """;
    private static final String DELETE_ALL_GENRES = """
            DELETE FROM genre
            """;


    public GenreRepository(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Genre get(Long id) {
        return findOne(GET_ONE, id);
    }

    @Override
    public Collection<Genre> getAll() {
        return findMany(GET_ALL);
    }

    @Override
    public Genre add(Genre genre) {
        genre.setId(nextIdByTable("genre"));

        insert(INSERT_GENRE,
                genre.getId(),
                genre.getName());

        return get(genre.getId());
    }

    @Override
    public Genre update(Genre genre) {
        update(UPDATE_GENRE,
                genre.getName(),
                genre.getId());
        return get(genre.getId());
    }

    @Override
    public boolean delete(Long id) {
        deleteRelated(Optional.of(id));
        boolean deleteGenre = deleteOne(DELETE_GENRE_BY_ID, id);

        if (!deleteGenre) {
            log.info("Произошла ошибка при удалении записи из таблицы genre с id: {}", id);
            throw new InternalServerException("Произошла ошибка при удалении жанра с id: " + id);
        }

        return true;
    }

    @Override
    public boolean deleteAll() {
        deleteRelated(Optional.empty());
        boolean deleteGenre = deleteAll(DELETE_ALL_GENRES);

        if (!deleteGenre) {
            log.info("Произошла ошибка при удалении всех записей из таблицы genre");
            throw new InternalServerException("Произошла ошибка при удалении всех записей из таблицы genre");
        }

        return true;
    }

    private void deleteRelated(Optional<Long> genreId) {
        if (genreId.isPresent()) {
            jdbc.update(DELETE_FILM_GENRE_BY_GENRE_ID, genreId.get());
            log.info("Были удалены все записи из таблицы film_genre у жанра с id: {}", genreId.get());
        } else {
            jdbc.update(DELETE_FILM_GENRE);
            log.info("Была очищена таблица film_genre");
        }
    }
}
