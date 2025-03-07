package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserService {
    private final Map<Long, User> usersMap = new HashMap<>();

    public User get(Long id) {
        if (usersMap.containsKey(id)) {
            return usersMap.get(id);
        } else {
            log.info("Попытка получить несуществующего пользователя с id: {}", id);
            throw new NotFoundException("Не существует пользователь с id: " + id);
        }
    }

    public Collection<User> getAll() {
        if (!usersMap.isEmpty()) {
            return usersMap.values();
        } else {
            log.info("Попытка получить список пользователей, который пуст");
            throw new NotFoundException("Список пользователей уже пуст");
        }
    }

    public User add(User user) {
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        usersMap.put(user.getId(), user);
        log.info("Был добавлен пользователь с id: {}", user.getId());
        return user;
    }

    public User update(User user) {
        if (usersMap.containsKey(user.getId())) {
            usersMap.replace(user.getId(), user);
            log.info("Был обновлён пользователь с id: {}", user.getId());
            return user;
        } else {
            log.info("Попытка обновить несуществующего пользователя с id: {}", user.getId());
            throw new NotFoundException("Не существует пользователь с id: " + user.getId());
        }
    }

    public User update(Long id, User user) {
        if (usersMap.containsKey(id)) {
            user.setId(id);
            usersMap.replace(id, user);
            log.info("Был обновлён пользователь с id: {}", id);
            return user;
        } else {
            log.info("Попытка обновить несуществующего пользователя с id: {}", id);
            throw new NotFoundException("Не существует пользователь с id: " + id);
        }
    }

    public void delete(Long id) {
        if (usersMap.containsKey(id)) {
            usersMap.remove(id);
            log.info("Был удалён пользователь с id: {}", id);
        } else {
            log.info("Попытка удалить несуществующего пользователя с id: {}", id);
            throw new NotFoundException("Не существует пользователь с id: " + id);
        }
    }

    public void deleteAll() {
        if (!usersMap.isEmpty()) {
            usersMap.clear();
            log.info("Список пользователей был очищен");
        } else {
            log.info("Попытка очистить список пользователей, который и так пуст");
            throw new NotFoundException("Список пользоваталей и так пуст");
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
