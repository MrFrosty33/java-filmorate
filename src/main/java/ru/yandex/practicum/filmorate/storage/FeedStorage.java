package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.UserEvent;

import java.util.List;

public interface FeedStorage {
    List<UserEvent> getFeed(Long id);

    void addEventToFeed(Long userId, EventType eventType, Operation operation, Long entityId);
}