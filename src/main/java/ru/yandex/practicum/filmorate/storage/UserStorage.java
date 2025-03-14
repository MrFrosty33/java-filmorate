package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Map;

public interface UserStorage {

    User get(Long id);

    Collection<User> getAll();

    Map<Long, User> getMap();

    User add(User user);

    User add(Long id, User user);

    User update(User user);

    User update(Long id, User user);

    void delete(Long id);

    void deleteAll();
}
