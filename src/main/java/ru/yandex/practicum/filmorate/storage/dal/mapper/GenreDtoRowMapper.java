package ru.yandex.practicum.filmorate.storage.dal.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.dto.GenreDto;

import java.sql.ResultSet;
import java.sql.SQLException;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreDtoRowMapper implements RowMapper<GenreDto> {
    @Override
    public GenreDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return GenreDto.builder()
                .id(rs.getLong("id"))
                .name(Genre.valueOf(rs.getString("name")))
                .build();
    }
}
