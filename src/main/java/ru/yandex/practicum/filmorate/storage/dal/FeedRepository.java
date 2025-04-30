package ru.yandex.practicum.filmorate.storage.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.UserEvent;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.dal.mapper.EventRowMapper;

import java.util.List;

@Repository
public class FeedRepository extends BaseRepository<UserEvent> implements FeedStorage {
    private static final String GET_USER_FEED = """
            SELECT event_id, user_id, event_type, operation, entity_id, timestamp
            FROM user_event
            WHERE user_id = ?
            ORDER BY event_id
            """;

    private static final String INSERT_EVENT = """
            INSERT INTO user_event (user_id, event_type, operation, entity_id)
            VALUES (?, ?, ?, ?)
            """;

    public FeedRepository(JdbcTemplate jdbc, EventRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<UserEvent> getFeed(Long id) {
        return jdbc.query(GET_USER_FEED, mapper, id);
    }

    @Override
    public void addEventToFeed(Long userId, EventType eventType, Operation operation, Long entityId) {
        insert(INSERT_EVENT, userId, eventType.name(), operation.name(), entityId);
    }
}
