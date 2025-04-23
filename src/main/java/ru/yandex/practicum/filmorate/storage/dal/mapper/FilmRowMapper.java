package ru.yandex.practicum.filmorate.storage.dal.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.model.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.dto.RatingMpaDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
                .directors(getDirectors(film.getId()))
                .build();
        return film;
    }

    private Set<Long> getLikes(long id) {
        String stm = "SELECT l.user_id " +
                "FROM film f " +
                "INNER JOIN \"like\" l ON f.id = l.film_id " +
                "WHERE l.film_id = " + id;
        return new LinkedHashSet<>(jdbc.queryForList(stm, Long.class));
        // Перешёл на LinkedHasSet дабы сохранять порядок
    }

    private Set<GenreDto> getGenres(long id) {
        String stm = "SELECT g.id AS id, g.name AS name " +
                "FROM film f " +
                "INNER JOIN film_genre fg ON fg.film_id = f.id " +
                "INNER JOIN genre g ON fg.genre_id = g.id " +
                "WHERE fg.film_id = ? ORDER BY g.id ASC";

        return new LinkedHashSet<>(jdbc.query(stm,
                (rs, rowNum) -> GenreDto.builder()
                        .id(rs.getLong("id"))
                        .name(Genre.valueOf(rs.getString("name")))
                        .build(),
                id
        ));
    }

    private RatingMpaDto getRating(long id) {
        String stm = "SELECT r.id AS id, r.name AS name " +
                "FROM film f " +
                "INNER JOIN film_rating fr ON f.id = fr.film_id INNER JOIN rating r ON r.id = fr.rating_id " +
                "WHERE f.id = ?";

        try {
            return jdbc.queryForObject(stm,
                    (rs, rowNum) -> RatingMpaDto.builder()
                            .id(rs.getLong("id"))
                            .name(RatingMpa.fromDbName(rs.getString("name")))
                            .build(),
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private Set<Director> getDirectors(long id){
        String stm = "SELECT d.id AS id, d.name AS name " +
                "FROM film f " +
                "INNER JOIN film_director fd ON f.id = fd.film_id INNER JOIN director d ON d.id = fd.director_id " +
                "WHERE f.id = ?";

        try {
            return new HashSet<>(jdbc.query(stm,
                    (rs, rowNum) -> Director.builder()
                            .id(rs.getLong("id"))
                            .name(rs.getString("name"))
                            .build(),
                    id
            ));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
