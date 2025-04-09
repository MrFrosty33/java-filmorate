package ru.yandex.practicum.filmorate.storage.dal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Qualifier
public class FilmRepository extends BaseRepository<Film> implements FilmStorage {
    private static final String GET_ONE_QUERY = "SELECT * FROM film WHERE id = ?";
    private static final String GET_ALL_QUERY = "SELECT * FROM film";
    private static final String GET_GENRE_ID_BY_NAME = "SELECT id FROM genre WHERE name in (?)";
    private static final String GET_RATING_ID_BY_NAME = "SELECT id FROM rating WHERE name IN (?)";

    //TODO работает ли, и требуются ли кавычки для текстовых переменных?
    private static final String INSERT_FILM_QUERY = "INSERT INTO film (id, name, description, release_date, duration)" +
            " VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_LIKE_QUERY = "INSERT INTO like (user_id, film_id) VALUES (?, ?)";
    private static final String INSERT_FILM_GENRE_QUERY = "INSERT INTO film_genre (film_id, genre_id) VALUES (?,?)";
    private static final String INSERT_FILM_RATING_QUERY = "INSERT INTO film_rating (film_id, rating_id) VALUES (?,?)";

    private static final String UPDATE_QUERY = "UPDATE film " +
            "SET id = ?, name = ?, description = ?, release_date = ?, duration = ? WHERE id = ?";

    private static final String DELETE_BY_ID_QUERY = "DELETE FROM film WHERE id = ?";
    private static final String DELETE_ALL_QUERY = "DELETE FROM film";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM like WHERE film_id =?";
    private static final String DELETE_FILM_GENRE_QUERY = "DELETE FROM film_genre WHERE film_id =?";
    private static final String DELETE_FILM_RATING_QUERY = "DELETE FROM film_rating WHERE film_id =?";


    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    //TODO тест всех методов!!!

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
        //TODO если не правильно работают эти переменные, также исправить в update
        final Long ratingId = jdbc.queryForObject(GET_RATING_ID_BY_NAME, Long.class, film.getRatingMpa());
        final Set<Long> genreId =
                new HashSet<>(jdbc.queryForList(GET_GENRE_ID_BY_NAME, Long.class, film.getGenres()));
        final Set<Long> likeUserId = film.getLikes();

        if (film.getId() == null) {
            film.setId(nextIdByTable("film"));
        }

        long insertId = insert(INSERT_FILM_QUERY,
                film.getId(),
                film.getName(),
                film.getReleaseDate(),
                film.getDuration());

        if (!likeUserId.isEmpty()) {
            for (Long id : likeUserId) {
                insert(INSERT_LIKE_QUERY, id, film.getId());
            }
        }

        if (!genreId.isEmpty()) {
            for (Long id : genreId) {
                insert(INSERT_FILM_GENRE_QUERY, film.getId(), id);
            }
        }

        insert(INSERT_FILM_RATING_QUERY, film.getId(), ratingId);

        return get(insertId);
    }

    // нужен ли этот метод вообще теперь?
    @Override
    public Film add(Long id, Film film) {
        // предполагается, что переданный id будет корректен
        film.setId(id);
        return add(film);
    }

    @Override
    public Film update(Film film) {
        final Long ratingId = jdbc.queryForObject(GET_RATING_ID_BY_NAME, Long.class, film.getRatingMpa());
        final Set<Long> genreId =
                new HashSet<>(jdbc.queryForList(GET_GENRE_ID_BY_NAME, Long.class, film.getGenres()));
        final Set<Long> likeUserId = film.getLikes();
        // Допустим, переданный нам фильм полностью корректен.
        // Если одно из полей isEmpty \ isBlank, значит таково пожелание обновления

        // сам фильм можно сразу обновить
        update(UPDATE_QUERY,
                film.getId(),
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getId());

        // остальные связи в смежных таблицах будто проще удалить старые и внести новые
        jdbc.update(DELETE_LIKE_QUERY, film.getId());
        jdbc.update(DELETE_FILM_GENRE_QUERY, film.getId());
        jdbc.update(DELETE_FILM_RATING_QUERY, film.getId());

        if (!likeUserId.isEmpty()) {
            for (Long userLikeId : likeUserId) {
                insert(INSERT_LIKE_QUERY, userLikeId, film.getId());
            }
        }

        if (!genreId.isEmpty()) {
            for (Long id : genreId) {
                insert(INSERT_FILM_GENRE_QUERY, film.getId(), id);
            }
        }

        insert(INSERT_FILM_RATING_QUERY, film.getId(), ratingId);

        return get(film.getId());
    }

    // и этот метод, нужен ли?
    @Override
    public Film update(Long id, Film film) {
        film.setId(id);
        return update(film);
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
