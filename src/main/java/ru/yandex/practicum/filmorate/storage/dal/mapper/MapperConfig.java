package ru.yandex.practicum.filmorate.storage.dal.mapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;

@Configuration
public class MapperConfig {

    @Bean
    public RowMapper<Film> filmRowMapper(JdbcTemplate jdbc) {
        return new FilmRowMapper(jdbc);
    }

    @Bean
    public UserRowMapper userRowMapper(JdbcTemplate jdbc) {
        return new UserRowMapper(jdbc);
    }

    @Bean
    public GenreRowMapper genreDtoRowMapper(JdbcTemplate jdbc) {
        return new GenreRowMapper(jdbc);
    }

    @Bean
    public RatingMpaRowMapper ratingMpaDtoRowMapper(JdbcTemplate jdbc) {
        return new RatingMpaRowMapper(jdbc);
    }

    @Bean
    public DirectorRowMapper directorRowMapper() {
        return new DirectorRowMapper();
    }
}
