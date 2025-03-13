package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User get(Long id) {
        validateUserExists(Optional.of(id),
                new NotFoundException("Не существует пользователь с id: " + id),
                "Попытка получить несуществующего пользователя с id: " + id);
        return userStorage.get(id);
    }

    public Collection<User> getAll() {
        validateUserExists(Optional.empty(),
                new NotFoundException("Список пользователей пуст"),
                "Попытка получить список пользователей, который пуст");
        return userStorage.getAll();
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        User user = get(id);
        User otherUser = get(otherId);
        Collection<User> friends = new ArrayList<>();

        /*
        Стоит ли проверять, есть ли вообще друзья у того или иного пользователя
        и в случае отсутствия выбрасывать ошибку?
        Или пускай в этом случае пройдёт цикл и вернёт пустой список?
        if(user.getFriends().isEmpty() || otherUser.getFriends().isEmpty()){
            // ...
        }
         */

        for (Long friendId : user.getFriends()) {
            if (otherUser.getFriends().contains(friendId)) {
                friends.add(userStorage.get(friendId));
            }
        }

        return friends;
    }

    // если список друзей пустой, стоит и возвраать ничего, или же выдавать ошибку?
    // потому что если возвращать ошибку, то Postman тест "add-friends-likes/friends/Friend get" не проходит
    public Collection<User> getAllFriends(Long id) {
        User user = get(id);
        /*
        if (user.getFriends().isEmpty()) {
            log.info("Попытка получить пустой список друзей у пользователя с id: {}", id);
            throw new NotFoundException("Список друзей пользователя с id: " + id + " пуст");
        }
         */
        Collection<User> friends = new ArrayList<>();

        for (Long friendId : user.getFriends()) {
            friends.add(userStorage.get(friendId));
        }

        log.info("Получен список друзей пользователя с id: {}", id);
        return friends;
    }

    public User add(User user) {
        // может ли только что созданный пользователей иметь друзей?
        user.setId(getNextId());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        if (user.getFriends() == null || user.getFriends().isEmpty()) {
            user.setFriends(new HashSet<>());
        } else {
            Set<Long> invalidFriends = new HashSet<>();
            Set<Long> validFriends = new HashSet<>();
            for (Long friendId : user.getFriends()) {
                if (!userStorage.getMap().containsKey(friendId)) {
                    log.info("Попытка добавить пользователя с другом id: {}, который не существует", friendId);
                    invalidFriends.add(friendId);
                } else {
                    validFriends.add(friendId);
                }
            }
            if (!invalidFriends.isEmpty()) {
                log.info("Попытка добавить пользователя с некорректным списком друзей");
                throw new NotFoundException("Друзья с id: " + invalidFriends + " не существуют");
            }
            for (Long friendId : validFriends) {
                log.info("У пользователя с id: {} появился новый друг с id: {}", friendId, user.getId());
                userStorage.get(friendId).getFriends().add(user.getId());
            }
        }

        userStorage.add(user);
        log.info("Был добавлен пользователь с id: {}", user.getId());
        return user;
    }

    public User addFriend(Long id, Long friendId) {
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
        userStorage.update(user.getId(), user);
        log.info("Был обновлён пользователь с id: {}", user.getId());
        return user;
    }

    public User update(Long id, User user) {
        validateUserExists(Optional.of(id),
                new NotFoundException("Не существует пользователь с id: " + id),
                "Попытка обновить несуществующего пользователя с id: " + id);

        user.setId(id);
        updateFriends(get(id), user);
        userStorage.update(id, user);
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

        userStorage.delete(id);
        log.info("Был удалён пользователь с id: {}", id);
    }

    public void deleteFriend(Long id, Long friendId) {
        User user = get(id);
        User friend = get(friendId);
        if (!user.getFriends().contains(friend.getId())) {
            log.info("Попытка убрать из друзей несуществующего друга с id: {}", friend.getId());
            throw new ConflictException("Попытка убрать из друзей несуществующего друга с id: " + friend.getId());
        }
        // Удаление из друзей работает же в обе стороны?
        // А перестал быть другом Б, следовательно Б не является больше другом А?
        user.getFriends().remove(friend.getId());
        friend.getFriends().remove(user.getId());
        log.info("У пользователя с id: {} был удалён друг с id: {}", user.getId(), friend.getId());
    }

    public void deleteAll() {
        validateUserExists(Optional.empty(),
                new NotFoundException("Список пользоваталей и так пуст"),
                "Попытка очистить список пользователей, который и так пуст");

        userStorage.deleteAll();
        log.info("Список пользователей был очищен");

    }

    private void validateUserExists(Optional<Long> id,
                                    RuntimeException e, String logMessage) {
        if (id.isPresent()) {
            if (userStorage.get(id.get()) == null) {
                log.info(logMessage);
                throw e;
            }
        } else {
            if (userStorage.getAll() == null || userStorage.getAll().isEmpty()) {
                log.info(logMessage);
                throw e;
            }
        }
    }

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

    private Long getNextId() {
        Long nextId = userStorage.getMap().keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++nextId;
    }
}
