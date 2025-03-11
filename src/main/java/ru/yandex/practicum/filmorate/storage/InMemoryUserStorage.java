package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> usersMap;

    @Override
    public User get(Long id) {
        if (usersMap.containsKey(id)) {
            return usersMap.get(id);
        } else {
            log.info("Попытка получить несуществующего пользователя с id: {}", id);
            throw new NotFoundException("Не существует пользователь с id: " + id);
        }
    }

    @Override
    public Collection<User> getAll() {
        if (!usersMap.isEmpty()) {
            return usersMap.values();
        } else {
            log.info("Попытка получить список пользователей, который пуст");
            throw new NotFoundException("Список пользователей уже пуст");
        }
    }

    @Override
    public Collection<User> getCommonFriends(Long id, Long otherId) {
        User user = get(id);
        User otherUser = get(otherId);
        Collection<User> friends = new ArrayList<>();

        for (Long friendId : user.getFriends()) {
            if (otherUser.getFriends().contains(friendId)) {
                friends.add(usersMap.get(friendId));
            }
        }

        return friends;
    }

    @Override
    public Collection<User> getAllFriends(Long id) {
        User user = get(id);
        if (user.getFriends().isEmpty()) {
            log.info("Попытка получить пустой список друзей у пользователя с id: {}", id);
            throw new NotFoundException("Список друзей пользователя с id: " + id + " пуст");
        }
        Collection<User> friends = new ArrayList<>();

        for (Long friendId : user.getFriends()) {
            friends.add(usersMap.get(friendId));
        }

        return friends;
    }

    @Override
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
                if (!usersMap.containsKey(friendId)) {
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
                usersMap.get(friendId).getFriends().add(user.getId());
            }
        }

        usersMap.put(user.getId(), user);
        log.info("Был добавлен пользователь с id: {}", user.getId());
        return user;
    }

    @Override
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

    @Override
    public User update(User user) {
        if (usersMap.containsKey(user.getId())) {
            updateFriends(usersMap.get(user.getId()), user);
            usersMap.replace(user.getId(), user);
            log.info("Был обновлён пользователь с id: {}", user.getId());
            return user;
        } else {
            log.info("Попытка обновить несуществующего пользователя с id: {}", user.getId());
            throw new NotFoundException("Не существует пользователь с id: " + user.getId());
        }
    }

    @Override
    public User update(Long id, User user) {
        if (usersMap.containsKey(id)) {
            User oldUser = get(id);
            user.setId(id);
            updateFriends(oldUser, user);
            usersMap.replace(id, user);
            log.info("Был обновлён пользователь с id: {}", id);
            return user;
        } else {
            log.info("Попытка обновить несуществующего пользователя с id: {}", id);
            throw new NotFoundException("Не существует пользователь с id: " + id);
        }
    }

    @Override
    public void delete(Long id) {
        if (usersMap.containsKey(id)) {
            User user = usersMap.get(id);
            if (!user.getFriends().isEmpty()) {
                for (Long friendId : user.getFriends()) {
                    usersMap.get(friendId).getFriends().remove(user.getId());
                    log.info("У пользователя с id: {} был удалён друг с id: {}", friendId, user.getId());
                }
            }
            usersMap.remove(id);
            log.info("Был удалён пользователь с id: {}", id);
        } else {
            log.info("Попытка удалить несуществующего пользователя с id: {}", id);
            throw new NotFoundException("Не существует пользователь с id: " + id);
        }
    }

    @Override
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

    @Override
    public void deleteAll() {
        if (!usersMap.isEmpty()) {
            usersMap.clear();
            log.info("Список пользователей был очищен");
        } else {
            log.info("Попытка очистить список пользователей, который и так пуст");
            throw new NotFoundException("Список пользоваталей и так пуст");
        }
    }

    private void updateFriends(User oldUser, User newUser) {
        if (!oldUser.getFriends().equals(newUser.getFriends())) {
            for (Long friendId : oldUser.getFriends()) {
                User friend = usersMap.get(friendId);
                friend.getFriends().remove(oldUser.getId());
            }
            for (Long friendId : newUser.getFriends()) {
                User friend = usersMap.get(friendId);
                friend.getFriends().add(newUser.getId());
            }
        }
    }

    private Long getNextId() {
        Long nextId = usersMap.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++nextId;
    }
}
