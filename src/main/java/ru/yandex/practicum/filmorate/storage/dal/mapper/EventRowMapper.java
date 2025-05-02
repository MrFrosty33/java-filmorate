package ru.yandex.practicum.filmorate.storage.dal.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.UserEvent;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class EventRowMapper implements RowMapper<UserEvent> {

    @Override
    public UserEvent mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return UserEvent.builder()
                .eventId(resultSet.getLong("event_id"))
                .userId(resultSet.getLong("user_id"))
                .eventType(EventType.valueOf(resultSet.getString("event_type")))
                .operation(Operation.valueOf(resultSet.getString("operation")))
                .entityId(resultSet.getLong("entity_id"))
                .timestamp(resultSet.getTimestamp("timestamp").getTime())
                .build();
    }
}
