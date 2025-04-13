package ru.yandex.practicum.filmorate.storage.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Repository
public class FilmRepository extends BaseRepository<Film> implements FilmStorage {
    // Достаточно ли понятные названия? Стоит ли над ними ещё подумать?
    private static final String GET_ONE_QUERY = "SELECT * FROM film WHERE id = ?";
    private static final String GET_ALL_QUERY = "SELECT * FROM film";
    private static final String GET_GENRE_ID_BY_NAME = "SELECT id FROM genre WHERE name in (?)";
    private static final String GET_RATING_ID_BY_NAME = "SELECT id FROM rating WHERE name IN (?)";
    private static final String GET_POPULAR_ID = "SELECT film_id FROM \"like\" " +
            "GROUP BY film_id ORDER BY COUNT(user_id) DESC LIMIT ?";

    private static final String INSERT_FILM_QUERY = "INSERT INTO film (id, name, description, release_date, duration)" +
            " VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_LIKE_QUERY = "INSERT INTO \"like\" (user_id, film_id) VALUES (?, ?)";
    private static final String INSERT_FILM_GENRE_QUERY = "INSERT INTO film_genre (film_id, genre_id) VALUES (?,?)";
    private static final String INSERT_FILM_RATING_QUERY = "INSERT INTO film_rating (film_id, rating_id) VALUES (?,?)";

    private static final String UPDATE_QUERY = "UPDATE film " +
            "SET id = ?, name = ?, description = ?, release_date = ?, duration = ? WHERE id = ?";

    private static final String DELETE_BY_ID_QUERY = "DELETE FROM film WHERE id = ?";
    private static final String DELETE_ALL_QUERY = "DELETE FROM film";
    private static final String DELETE_ALL_LIKE_BY_FILM_ID_QUERY = "DELETE FROM \"like\" WHERE film_id =?";
    private static final String DELETE_LIKE_BY_USER_ID_AND_FILM_ID =
            "DELETE FROM \"like\" WHERE user_id = ? AND film_id = ? ";
    private static final String DELETE_ALL_LIKES_QUERY = "DELETE FROM \"like\" ";
    private static final String DELETE_ALL_FILM_GENRE_BY_FILM_ID_QUERY = "DELETE FROM film_genre WHERE film_id =?";
    private static final String DELETE_ALL_FILMS_GENRES_QUERY = "DELETE FROM film_genre ";
    private static final String DELETE_ALL_FILM_RATING_BY_FILM_ID_QUERY = "DELETE FROM film_rating WHERE film_id =?";
    private static final String DELETE_ALL_FILMS_RATINGS_QUERY = "DELETE FROM film_rating ";


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
    public Collection<Film> getPopular(int limit) {
        Collection<Long> popularIds = jdbc.queryForList(GET_POPULAR_ID, Long.class, limit);
        Collection<Film> result = new ArrayList<>();

        for (Long id : popularIds) {
            result.add(get(id));
        }

        return result;
    }

    @Override
    public Film add(Film film) {
        final Long ratingId;
        final Set<Long> genreId;

        if (film.getId() == null) {
            film.setId(nextIdByTable("film"));
        }

        insert(INSERT_FILM_QUERY,
                film.getId(),
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration());

        if (!film.getGenres().isEmpty()) {
            genreId = new HashSet<>(jdbc.queryForList(GET_GENRE_ID_BY_NAME, Long.class, film.getGenres()));
            if (!genreId.isEmpty()) {
                for (Long id : genreId) {
                    insert(INSERT_FILM_GENRE_QUERY, film.getId(), id);
                }
            }
        }

        if (film.getRatingMpa() != null) {
            ratingId = jdbc.queryForObject(GET_RATING_ID_BY_NAME, Long.class, film.getRatingMpa().getDbName());
            insert(INSERT_FILM_RATING_QUERY, film.getId(), ratingId);
        }

        return get(film.getId());
    }

    @Override
    public Set<Long> addLike(Long filmId, Long userId) {
        insert(INSERT_LIKE_QUERY, userId, filmId);
        return get(filmId).getLikes();
    }

    @Override
    public Film update(Film film) {
        final Long ratingId = jdbc.queryForObject(GET_RATING_ID_BY_NAME, Long.class, film.getRatingMpa());
        final Set<Long> genreId =
                new HashSet<>(jdbc.queryForList(GET_GENRE_ID_BY_NAME, Long.class, film.getGenres()));
        final Set<Long> likeUserId = film.getLikes();
        // Допустим, переданный нам фильм полностью корректен
        // Если одно из полей isEmpty \ isBlank, значит таково пожелание обновления

        // Сам фильм можно сразу обновить
        update(UPDATE_QUERY,
                film.getId(),
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getId());

        // Остальные записи в смежных таблицах будто проще удалить и внести новые.
        // Существуют они или нет - не играет большой роли. Если они есть - удалятся, если нет, ничего не произойдёт
        jdbc.update(DELETE_ALL_LIKE_BY_FILM_ID_QUERY, film.getId());
        jdbc.update(DELETE_ALL_FILM_GENRE_BY_FILM_ID_QUERY, film.getId());
        jdbc.update(DELETE_ALL_FILM_RATING_BY_FILM_ID_QUERY, film.getId());

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

    @Override
    public boolean delete(Long id) {
        boolean deleteFilm = deleteOne(DELETE_BY_ID_QUERY, id);
        boolean deleteLike = jdbc.update(DELETE_ALL_LIKE_BY_FILM_ID_QUERY, id) > 0;
        boolean deleteFilmGenre = jdbc.update(DELETE_ALL_FILM_GENRE_BY_FILM_ID_QUERY, id) > 0;
        boolean deleteFilmRating = jdbc.update(DELETE_ALL_FILM_RATING_BY_FILM_ID_QUERY, id) > 0;

        if (!deleteFilm) {
            log.info("Произошла ошибка при удалении записи из таблицы film с id: {}", id);
            throw new InternalServerException("Произошла ошибка при удалении фильма с id: " + id);
        }
        if (!deleteLike) {
            log.info("Произошла ошибка при удалении записи из таблицы like с film_id: {}", id);
            throw new InternalServerException("Произошла ошибка при удалении лайка у фильма с id: " + id);
        }
        if (!deleteFilmGenre) {
            log.info("Произошла ошибка при удалении записи из таблицы film_genre с film_id: {}", id);
            throw new InternalServerException("Произошла ошибка при удалении жанра у фильма с id: " + id);
        }
        if (!deleteFilmRating) {
            log.info("Произошла ошибка при удалении записи из таблицы film_rating с film_id: {}", id);
            throw new InternalServerException("Произошла ошибка при удалении рейтинга фильма с id: " + id);
        }

        return true;
    }

    @Override
    public boolean deleteAll() {
        boolean deleteFilm = deleteAll(DELETE_ALL_QUERY);
        boolean deleteLike = jdbc.update(DELETE_ALL_LIKES_QUERY) > 0;
        boolean deleteFilmGenre = jdbc.update(DELETE_ALL_FILMS_GENRES_QUERY) > 0;
        boolean deleteFilmRating = jdbc.update(DELETE_ALL_FILMS_RATINGS_QUERY) > 0;

        if (!deleteFilm) {
            log.info("Произошла ошибка при удалении всех записей из таблицы film");
            throw new InternalServerException("Произошла ошибка при очистке таблицы фильмов");
        }
        if (!deleteLike) {
            log.info("Произошла ошибка при удалении всех записей из таблицы like");
            throw new InternalServerException("Произошла ошибка при очистке таблицы лайков");
        }
        if (!deleteFilmGenre) {
            log.info("Произошла ошибка при удалении всех записей из таблицы film_genre");
            throw new InternalServerException("Произошла ошибка при очистке таблицы жанров");
        }
        if (!deleteFilmRating) {
            log.info("Произошла ошибка при удалении всех записей из таблицы film_rating");
            throw new InternalServerException("Произошла ошибка при очистке таблицы рейтингов");
        }

        return true;
    }

    @Override
    public boolean deleteLike(Long filmId, Long userId) {
        boolean deleteLike = jdbc.update(DELETE_LIKE_BY_USER_ID_AND_FILM_ID, userId, filmId) > 0;

        if (!deleteLike) {
            log.info("Произошла ошибка при удалении лайка из таблицы like с film_id: {} и user_id: {}", filmId, userId);
            throw new InternalServerException("Произошла ошибка при удалении лайка у фильма с id: " + filmId +
                    " от пользователя с id: " + userId);
        }

        return true;
    }
}
