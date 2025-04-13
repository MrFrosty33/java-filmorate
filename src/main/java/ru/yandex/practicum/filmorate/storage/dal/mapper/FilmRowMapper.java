package ru.yandex.practicum.filmorate.storage.dal.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmRowMapper implements RowMapper<Film> {
    private final JdbcTemplate jdbc;

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = Film.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .description(resultSet.getString("description"))
                .duration(resultSet.getLong("duration"))
                .build();

        film = film.toBuilder()
                .likes(getLikes(film.getId()))
                .genres(getGenres(film.getId()))
                .ratingMpa(getRating(film.getId()))
                .build();
        return film;
    }

    private Set<Long> getLikes(long id) {
        String stm = "SELECT l.user_id " +
                "FROM film f " +
                "INNER JOIN \"like\" l ON f.id = l.film_id " +
                "WHERE l.film_id = " + id;
        return new HashSet<>(jdbc.queryForList(stm, Long.class));
    }

    private Set<Genre> getGenres(long id) {
        String stm = "SELECT g.name AS genre " +
                "FROM film f " +
                "INNER JOIN film_genre fg ON fg.film_id = f.id " +
                "INNER JOIN genre g ON fg.genre_id = g.id " +
                "WHERE fg.film_id = " + id;
        return new HashSet<>(jdbc.query(stm, (rs, rowNum) -> Genre.valueOf(rs.getString("genre"))));
    }

    private RatingMpa getRating(long id) {
        String stm = "SELECT r.name AS rating " +
                "FROM film f " +
                "INNER JOIN film_rating fr ON f.id = fr.film_id INNER JOIN rating r ON r.id = fr.rating_id " +
                "WHERE f.id = " + id;
        return jdbc.queryForObject(stm, (rs, rowNum) -> RatingMpa.valueOf(rs.getString("rating")));
    }
}
