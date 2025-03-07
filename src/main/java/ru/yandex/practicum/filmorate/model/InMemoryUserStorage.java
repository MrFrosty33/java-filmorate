package ru.yandex.practicum.filmorate.model;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.storage.UserStorage;

@Component
public class InMemoryUserStorage implements UserStorage {
}
