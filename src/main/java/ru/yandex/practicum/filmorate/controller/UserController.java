package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    Map<Long, User> usersMap = new HashMap<>();

    @GetMapping("/{id}")
    public User get(@PathVariable Long id) {
        if (usersMap.containsKey(id)) {
            return usersMap.get(id);
        } else {
            log.info("Попытка получить несуществующего пользователя с id: {}", id);
            throw new NotFoundException("Не существует пользователь с id: " + id);
        }
    }

    @GetMapping
    public Collection<User> getAll() {
        if (!usersMap.isEmpty()) {
            return usersMap.values();
        } else {
            log.info("Попытка получить список пользователей, который пуст");
            throw new NotFoundException();
        }
    }

    @PostMapping
    public User add(@Valid @RequestBody User userRaw) {
        User user = userRaw.toBuilder().id(nextId()).build();
        usersMap.put(user.getId(), user);
        log.info("Был добавлен пользователь с id: {}", user.getId());
        return user;
    }

    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @Valid @RequestBody User rawuser) {
        if (usersMap.containsKey(id)) {
            User user = rawuser.toBuilder().id(id).build();
            usersMap.replace(id, user);
            log.info("Был обновлён пользователь с id: {}", id);
            return user;
            // return usersMap.get(user.getId()); можно и так, но более ресурсоёмко, а результат тот же
        } else {
            log.info("Попытка обновить несуществующего пользователя с id: {}", id);
            throw new NotFoundException();
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        if (usersMap.containsKey(id)) {
            usersMap.remove(id);
            log.info("Был удалён пользователь с id: {}", id);
        } else {
            log.info("Попытка удалить несуществующего пользователя с id: {}", id);
            throw new NotFoundException();
        }
    }

    @DeleteMapping
    public void deleteAll() {
        if (!usersMap.isEmpty()) {
            usersMap.clear();
            log.info("Список пользователей был очищен");
        } else {
            log.info("Попытка очистить список пользователей, который и так пуст");
            throw new NotFoundException();
        }
    }

    private Long nextId() {
        Long nextId = usersMap.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++nextId;
    }

}
