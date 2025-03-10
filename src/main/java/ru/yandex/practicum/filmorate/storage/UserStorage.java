package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    User get(Long id);

    Collection<User> getAll();

    Collection<User> getCommonFriends(Long id, Long otherId);

    Collection<User> getAllFriends(Long id);

    User add(User user);

    // тут тоже вопрос, стоит ли возвращать что-то, и что возвращать?
    User addFriend(Long id, Long friendId);

    User update(User user);

    User update(Long id, User user);

    void delete(Long id);

    void deleteFriend(Long id, Long friendId);

    void deleteAll();
}
