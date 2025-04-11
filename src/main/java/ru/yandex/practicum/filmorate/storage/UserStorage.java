package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    User get(Long id);

    Collection<User> getAll();

    Collection<User> getCommonFriends(Long id, Long otherId);

    User add(User user);

    User add(Long id, User user);

    User addFriend(Long id, Long friendId, FriendshipStatus status);

    User update(User user);

    User update(Long id, User user);

    boolean delete(Long id);

    boolean deleteAll();

    boolean deleteFriend(Long id, Long friendId);
}
