package ru.yandex.practicum.filmorate.storage.dal.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.model.dto.RatingMpaDto;

import java.sql.ResultSet;
import java.sql.SQLException;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RatingMpaDtoRowMapper implements RowMapper<RatingMpaDto> {
    @Override
    public RatingMpaDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return RatingMpaDto.builder()
                .id(rs.getLong("id"))
                .name(RatingMpa.valueOf(rs.getString("name")))
                .build();
    }
}
