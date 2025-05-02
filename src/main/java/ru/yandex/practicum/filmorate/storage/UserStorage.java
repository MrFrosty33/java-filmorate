package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface UserStorage {

    User get(Long id);

    Collection<User> getAll();

    Collection<User> getCommonFriends(Long id, Long otherId);

    public Collection<User> getAllFriends(Long id);

    FriendshipStatus getFriendshipStatus(Long id, Long otherId);

    User add(User user);

    User addFriend(Long id, Long friendId, FriendshipStatus status);

    User update(User user);

    FriendshipStatus updateFriendshipStatus(Long id, Long friendId, FriendshipStatus friendshipStatus);

    boolean delete(Long id);

    boolean deleteAll();

    boolean deleteFriend(Long id, Long friendId);

    Map<Long, Set<Long>> getSimilarUserLikes(Long id);
}
