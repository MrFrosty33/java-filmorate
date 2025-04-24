package ru.yandex.practicum.filmorate.storage.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.dto.RatingMpaDto;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;

@Slf4j
@Repository
public class FilmRepository extends BaseRepository<Film> implements FilmStorage {
    private static final String GET_ONE = "SELECT * FROM film WHERE id = ?";
    private static final String GET_ALL = "SELECT * FROM film";
    private static final String GET_GENRE_ID_BY_NAME = "SELECT id FROM genre WHERE name in (?)";
    private static final String GET_RATING_ID_BY_NAME = "SELECT id FROM rating WHERE name IN (?)";
    private static final String GET_ALL_RATING_ID = "SELECT id FROM rating";
    private static final String GET_ALL_GENRE_ID = "SELECT id FROM genre";
    private static final String GET_POPULAR_ID = "SELECT film_id FROM \"like\" " +
            "GROUP BY film_id ORDER BY COUNT(user_id) DESC LIMIT ?";

    private static final String INSERT_FILM = "INSERT INTO film (id, name, description, release_date, duration)" +
            " VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_LIKE = "INSERT INTO \"like\" (user_id, film_id) VALUES (?, ?)";
    private static final String INSERT_FILM_GENRE = "INSERT INTO film_genre (film_id, genre_id) VALUES (?,?)";
    private static final String INSERT_FILM_RATING = "INSERT INTO film_rating (film_id, rating_id) VALUES (?,?)";

    private static final String UPDATE_FILM = "UPDATE film " +
            "SET id = ?, name = ?, description = ?, release_date = ?, duration = ? WHERE id = ?";

    private static final String DELETE_FILM_BY_ID = "DELETE FROM film WHERE id = ?";
    private static final String DELETE_ALL_FILMS = "DELETE FROM film";
    private static final String DELETE_ALL_LIKE_BY_FILM_ID = "DELETE FROM \"like\" WHERE film_id =?";
    private static final String DELETE_LIKE_BY_USER_ID_AND_FILM_ID =
            "DELETE FROM \"like\" WHERE user_id = ? AND film_id = ? ";
    private static final String DELETE_ALL_LIKES = "DELETE FROM \"like\" ";
    private static final String DELETE_ALL_FILM_GENRE_BY_FILM_ID = "DELETE FROM film_genre WHERE film_id =?";
    private static final String DELETE_ALL_FILMS_GENRES = "DELETE FROM film_genre ";
    private static final String DELETE_ALL_FILM_RATING_BY_FILM_ID = "DELETE FROM film_rating WHERE film_id =?";
    private static final String DELETE_ALL_FILMS_RATINGS = "DELETE FROM film_rating ";


    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Film get(Long id) {
        return findOne(GET_ONE, id);
    }

    @Override
    public Collection<Film> getAll() {
        return findMany(GET_ALL);
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
    @Transactional
    public Film add(Film film) {
        if (film.getId() == null) {
            film.setId(nextIdByTable("film"));
        }

        insert(INSERT_FILM,
                film.getId(),
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> genreIds = new HashSet<>(jdbc.queryForList(GET_ALL_GENRE_ID, Long.class));
            List<Object[]> batchArgs = new ArrayList<>();

            for (GenreDto genre : film.getGenres()) {
                if (genreIds.contains(genre.getId())) {
                    batchArgs.add(new Object[]{film.getId(), genre.getId()});
                } else {
                    log.info("Попытка добавить несуществующий жанр с id: {}", genre.getId());
                    throw new NotFoundException("Не найден жанр с id: " + genre.getId());
                }
            }

            jdbc.batchUpdate(INSERT_FILM_GENRE, batchArgs);
        }

        if (film.getRatingMpa() != null) {
            Set<Long> ratingIds = new HashSet<>(jdbc.queryForList(GET_ALL_RATING_ID, Long.class));
            if (ratingIds.contains(film.getRatingMpa().getId())) {
                insert(INSERT_FILM_RATING, film.getId(), film.getRatingMpa().getId());
            } else {
                log.info("Попытка добавить несуществующий рейтинг с id: {}", film.getRatingMpa().getId());
                throw new NotFoundException("Не найден рейтинг с id: " + film.getRatingMpa().getId());
            }
        }

        return get(film.getId());
    }

    @Override
    public Set<Long> addLike(Long filmId, Long userId) {
        insert(INSERT_LIKE, userId, filmId);
        return get(filmId).getLikes();
    }

    @Override
    @Transactional
    public Film update(Film film) {
        // Допустим, переданный нам фильм полностью корректен
        // Если одно из полей isEmpty \ isBlank, значит таково пожелание обновления
        Set<Long> likes = film.getLikes();
        Set<GenreDto> genres = film.getGenres();
        RatingMpaDto rating = film.getRatingMpa();

        // Сам фильм можно сразу обновить
        update(UPDATE_FILM,
                film.getId(),
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getId());

        // Остальные записи в смежных таблицах будто проще удалить и внести новые.
        // Существуют они или нет - не играет большой роли. Если они есть - удалятся, если нет, ничего не произойдёт
        jdbc.update(DELETE_ALL_LIKE_BY_FILM_ID, film.getId());
        jdbc.update(DELETE_ALL_FILM_GENRE_BY_FILM_ID, film.getId());
        jdbc.update(DELETE_ALL_FILM_RATING_BY_FILM_ID, film.getId());

        if (!likes.isEmpty()) {
            List<Object[]> batchArgs = new ArrayList<>();

            for (Long userLikeId : likes) {
                batchArgs.add(new Object[]{userLikeId, film.getId()});
            }

            jdbc.batchUpdate(INSERT_LIKE, batchArgs);
        }

        if (!genres.isEmpty()) {
            List<Object[]> batchArgs = new ArrayList<>();

            for (GenreDto genre : genres) {
                batchArgs.add(new Object[]{film.getId(), genre.getId()});
            }

            jdbc.batchUpdate(INSERT_FILM_GENRE, batchArgs);
        }

        insert(INSERT_FILM_RATING, film.getId(), rating.getId());

        return get(film.getId());
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        boolean deleteLike = jdbc.update(DELETE_ALL_LIKE_BY_FILM_ID, id) > 0;
        boolean deleteFilmGenre = jdbc.update(DELETE_ALL_FILM_GENRE_BY_FILM_ID, id) > 0;
        boolean deleteFilmRating = jdbc.update(DELETE_ALL_FILM_RATING_BY_FILM_ID, id) > 0;
        boolean deleteFilm = deleteOne(DELETE_FILM_BY_ID, id);

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
    @Transactional
    public boolean deleteAll() {
        boolean deleteLike = jdbc.update(DELETE_ALL_LIKES) > 0;
        boolean deleteFilmGenre = jdbc.update(DELETE_ALL_FILMS_GENRES) > 0;
        boolean deleteFilmRating = jdbc.update(DELETE_ALL_FILMS_RATINGS) > 0;
        boolean deleteFilm = deleteAll(DELETE_ALL_FILMS);

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
