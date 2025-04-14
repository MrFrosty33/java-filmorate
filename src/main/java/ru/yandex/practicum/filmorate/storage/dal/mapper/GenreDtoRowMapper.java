package ru.yandex.practicum.filmorate.storage.dal.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.dto.GenreDto;

import java.sql.ResultSet;
import java.sql.SQLException;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreDtoRowMapper implements RowMapper<GenreDto> {
    private final JdbcTemplate jdbc;

    @Override
    public GenreDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        GenreDto result = GenreDto.builder()
                .id(rs.getLong("id"))
                .build();

        result = result.toBuilder()
                .name(getName(result.getId()))
                .build();
        return result;
    }

    private Genre getName(Long id) {
        String stm = "SELECT name FROM genre WHERE id = " + id;
        return jdbc.queryForObject(stm, (rs, rowNum) -> Genre.valueOf(rs.getString("name")));
    }
}
