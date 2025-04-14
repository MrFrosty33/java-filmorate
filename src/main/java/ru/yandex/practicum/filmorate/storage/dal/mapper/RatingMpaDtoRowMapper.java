package ru.yandex.practicum.filmorate.storage.dal.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.model.dto.RatingMpaDto;

import java.sql.ResultSet;
import java.sql.SQLException;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RatingMpaDtoRowMapper implements RowMapper<RatingMpaDto> {
    private final JdbcTemplate jdbc;

    @Override
    public RatingMpaDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        RatingMpaDto result = RatingMpaDto.builder()
                .id(rs.getLong("id"))
                .build();

        result = result.toBuilder()
                .name(getName(result.getId()))
                .build();
        return result;
    }

    private RatingMpa getName(Long id) {
        String stm = "SELECT name FROM rating WHERE id = " + id;
        return jdbc.queryForObject(stm, (rs, rowNum) -> RatingMpa.fromDbName(rs.getString("name")));
    }
}
