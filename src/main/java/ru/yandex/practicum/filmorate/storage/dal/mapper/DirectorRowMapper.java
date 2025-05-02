package ru.yandex.practicum.filmorate.storage.dal.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DirectorRowMapper implements RowMapper<Director> {

    @Override
    public Director mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Director.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .build();
    }
}
