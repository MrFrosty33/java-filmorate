package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dal.UserRepository;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User get(Long id) {
        validateUserExists(Optional.of(id),
                new NotFoundException("Не существует пользователь с id: " + id),
                "Попытка получить несуществующего пользователя с id: " + id);

        log.info("Получен пользователь с id: {}", id);
        return userRepository.get(id);
    }

    public Collection<User> getAll() {
        validateUserExists(Optional.empty(),
                new NotFoundException("Список пользователей пуст"),
                "Попытка получить список пользователей, который пуст");

        log.info("Получен список всех пользователей");
        return userRepository.getAll();
    }


    //todo перелопатить всё тут под новую логику
    public Collection<User> getCommonFriends(Long id, Long otherId) {
        final User user = userRepository.get(id);
        final User other = userRepository.get(otherId);
        final Set<Long> friends = user.getFriends();
        final Set<Long> otherFriends = other.getFriends();

        log.info("Получен список совместных друзей между пользоваталем с id: {} и другим пользователем с id: {}",
                id, otherId);
        return friends.stream()
                .filter(otherFriends::contains)
                .map(userRepository::get)
                .toList();
    }

    public Collection<User> getAllFriends(Long id) {
        User user = get(id);
        Collection<User> friends = new ArrayList<>();

        for (Long friendId : user.getFriends()) {
            friends.add(userRepository.get(friendId));
        }

        log.info("Получен список друзей пользователя с id: {}", id);
        return friends;
    }

    public User add(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }

        if (!user.getFriends().isEmpty()) {
            log.info("Попытка добавить нового пользователя с уже имеющимися друзьями");
            throw new ConflictException("Новый пользователь не может иметь друзей");
        }

        userRepository.add(user);
        log.info("Был добавлен пользователь с id: {}", user.getId());
        return user;
    }

    public User addFriend(Long id, Long friendId) {
        // если А дружит с Б, то пока Б не отправил запрос к А, статус дружбы "unconfirmed"
        //todo доработать эту логику
        User user = get(id);
        User friend = get(friendId);
        if (id.equals(friendId)) {
            log.info("Попытка добавить самого себя в друзья");
            throw new ConflictException("Невозможно добавить самого себя в друзья");
        }
        if (user.getFriends().contains(friend.getId())) {
            log.info("Попытка добавить в друзья пользователя, который уже находится в друзьях");
            throw new ConflictException("Пользователь с id: " + user.getId() + " уже находится в друзьях");
        }
        user.getFriends().add(friend.getId());
        log.info("У пользователя с id: {} появился новый друг с id: {}", user.getId(), friend.getId());
        friend.getFriends().add(user.getId());
        log.info("У пользователя с id: {} появился новый друг с id: {}", friend.getId(), user.getId());
        return user;
    }

    public User update(User user) {
        validateUserExists(Optional.of(user.getId()),
                new NotFoundException("Не существует пользователь с id: " + user.getId()),
                "Попытка обновить несуществующего пользователя с id: " + user.getId());

        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        updateFriends(get(user.getId()), user);
        userRepository.update(user.getId(), user);
        log.info("Был обновлён пользователь с id: {}", user.getId());
        return user;
    }

    public User update(Long id, User user) {
        validateUserExists(Optional.of(id),
                new NotFoundException("Не существует пользователь с id: " + id),
                "Попытка обновить несуществующего пользователя с id: " + id);

        user.setId(id);
        updateFriends(get(id), user);
        userRepository.update(id, user);
        log.info("Был обновлён пользователь с id: {}", id);
        return user;
    }

    public void delete(Long id) {
        validateUserExists(Optional.of(id),
                new NotFoundException("Не существует пользователь с id: " + id),
                "Попытка удалить несуществующего пользователя с id: " + id);

        User user = get(id);
        if (!user.getFriends().isEmpty()) {
            for (Long friendId : user.getFriends()) {
                get(friendId).getFriends().remove(user.getId());
                log.info("У пользователя с id: {} был удалён друг с id: {}", friendId, user.getId());
            }
        }

        userRepository.delete(id);
        log.info("Был удалён пользователь с id: {}", id);
    }

    public void deleteFriend(Long id, Long friendId) {
        User user = get(id);
        User friend = get(friendId);

        user.getFriends().remove(friend.getId());
        friend.getFriends().remove(user.getId());
        log.info("У пользователя с id: {} был удалён друг с id: {}", user.getId(), friend.getId());
    }

    public void deleteAll() {
        validateUserExists(Optional.empty(),
                new NotFoundException("Список пользоваталей и так пуст"),
                "Попытка очистить список пользователей, который и так пуст");

        userRepository.deleteAll();
        log.info("Список пользователей был очищен");

    }

    //todo подправить, как в filmService
    private void validateUserExists(Optional<Long> id,
                                    RuntimeException e, String logMessage) {
        if (id.isPresent()) {
            if (userRepository.get(id.get()) == null) {
                log.info(logMessage);
                throw e;
            }
        } else {
            if (userRepository.getAll() == null || userRepository.getAll().isEmpty()) {
                log.info(logMessage);
                throw e;
            }
        }
    }

    //todo подправить
    private void updateFriends(User oldUser, User newUser) {
        if (!oldUser.getFriends().equals(newUser.getFriends())) {
            for (Long friendId : oldUser.getFriends()) {
                User friend = get(friendId);
                friend.getFriends().remove(oldUser.getId());
            }
            for (Long friendId : newUser.getFriends()) {
                User friend = get(friendId);
                friend.getFriends().add(newUser.getId());
            }
        }
    }
}
