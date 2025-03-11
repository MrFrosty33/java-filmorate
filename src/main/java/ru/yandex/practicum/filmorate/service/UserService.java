package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User get(Long id) {
        return userStorage.get(id);
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }

    public Collection<User> getAllFriends(Long id) {
        return userStorage.getAllFriends(id);
    }

    public User add(User user) {
        return userStorage.add(user);
    }

    public User addFriend(Long id, Long friendId) {
        return userStorage.addFriend(id, friendId);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public User update(Long id, User user) {
        return userStorage.update(id, user);
    }

    public void delete(Long id) {
        userStorage.delete(id);
    }

    public void deleteFriend(Long id, Long friendId) {
        userStorage.deleteFriend(id, friendId);
    }

    public void deleteAll() {
        userStorage.deleteAll();
    }
}
