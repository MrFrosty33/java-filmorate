package ru.yandex.practicum.filmorate.storage.dal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;

@Qualifier
public class FilmRepository extends BaseRepository<Film> implements FilmStorage {
    private static final String GET_ONE_QUERY = "SELECT * FROM film WHERE id = ?";
    private static final String GET_ALL_QUERY = "SELECT * FROM film";
    private static final String INSERT_QUERY = "";
    private static final String DELETE_BY_ID_QUERY = "DELETE FROM film WHERE id = ?";
    private static final String DELETE_ALL_QUERY = "DELETE FROM film";

    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Film get(Long id) {
        return findOne(GET_ONE_QUERY, id);
    }

    @Override
    public Collection<Film> getAll() {
        return findMany(GET_ALL_QUERY);
    }

    @Override
    public Film add(Film film) {
        long id = insert(INSERT_QUERY,
                film.getId(),
                film.getName(),
                film.getReleaseDate(),
                film.getDuration());

        //TODO нужно ещё добавить лайки, жанр и рейтинг в соответствующие смежные таблицы

        return get(id);
    }

    @Override
    public Film add(Long id, Film film) {
        return null;
    }

    @Override
    public Film update(Film film) {
        return null;
    }

    @Override
    public Film update(Long id, Film film) {
        return null;
    }

    @Override
    public void delete(Long id) {
        deleteOne(DELETE_BY_ID_QUERY, id);
    }

    @Override
    public void deleteAll() {
        deleteAll(DELETE_ALL_QUERY);
    }
}
