package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dal.UserRepository;

import java.util.Collection;
import java.util.Optional;

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
                new NotFoundException("Таблица user пуста"),
                "Попытка получить данные из таблицы user, которая пуста");

        log.info("Получен список всех пользователей");
        return userRepository.getAll();
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        validateUserExists(Optional.of(id),
                new NotFoundException("Не существует пользователь с id: " + id),
                "Попытка получить общий список друзей, но не существует пользователь с id:  " + id);

        validateUserExists(Optional.of(otherId),
                new NotFoundException("Не существует пользователь с id: " + otherId),
                "Попытка получить общий список друзей, но не существует пользователь с id: " + otherId);

        log.info("Получен список совместных друзей между пользователем с id: {} и другим пользователем с id: {}",
                id, otherId);
        return userRepository.getCommonFriends(id, otherId);
    }

    public FriendshipStatus getFriendshipStatus(Long id, Long otherId) {
        validateUserExists(Optional.of(id),
                new NotFoundException("Не существует пользователь с id: " + id),
                "Попытка получить статус дружбы, но не существует пользователь с id: " + id);

        validateUserExists(Optional.of(otherId),
                new NotFoundException("Не существует пользователь с id: " + otherId),
                "Попытка получить статус дружбы, но не существует пользователь с id: " + otherId);

        log.info("Получен статус дружбы между пользователем с id: {} и другим пользователем с id: {}", id, otherId);
        return userRepository.getFriendshipStatus(id, otherId);
    }

    public Collection<User> getAllFriends(Long id) {
        validateUserExists(Optional.of(id),
                new NotFoundException("Не существует пользователь с id: " + id),
                "Попытка получить список друзей, но не существует пользователь с id:  " + id);

        log.info("Получен список друзей пользователя с id: {}", id);
        return userRepository.getAllFriends(id);
    }

    public User add(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        if (!user.getFriendStatusMap().isEmpty()) {
            log.info("Попытка добавить нового пользователя с уже имеющимися друзьями");
            throw new ConflictException("Новый пользователь не может иметь друзей");
        }

        userRepository.add(user);
        log.info("Был добавлен пользователь с id: {}", user.getId());
        return userRepository.get(user.getId());
    }

    public User add(Long id, User user) {
        user.setId(id);
        return add(user);
    }

    // Стоит возвращать целиком пользователя, или же только статус дружбы, полученный в ходе выполнения метода?
    public User addFriend(Long id, Long friendId) {
        // если А дружит с Б, то пока Б не отправил запрос к А, статус дружбы UNCONFIRMED
        FriendshipStatus status;

        if (id.equals(friendId)) {
            log.info("Попытка добавить самого себя в друзья");
            throw new ConflictException("Невозможно добавить самого себя в друзья");
        }

        if (userRepository.getAllFriends(id).contains(userRepository.get(friendId))) {
            log.info("Попытка добавить в друзья пользователя, который уже находится в друзьях");
            throw new ConflictException("Пользователь с id: " + friendId + " уже находится в друзьях");
        }

        // Проверяем, дружит ли пользователь Б с А
        // Если да, меняем статус дружбы на CONFIRMED и добавляем дружбу с другой стороны.
        try {
            status = userRepository.getFriendshipStatus(friendId, id);
            status = FriendshipStatus.CONFIRMED;

            log.info("В таблице friend изменён статус дружбы между пользователями с id: {} и id: {} на статус {}",
                    friendId, id, status);
            userRepository.updateFriendshipStatus(friendId, id, status);
            log.info("В таблице friend была добавлена запись с id: {} и id: {}, со статусом: {}", id, friendId, status);
            userRepository.addFriend(id, friendId, status);
        }
        // если же нет таких записей, то добавляем дружбу пользователя А с Б со статусом UNCONFIRMED
        catch (EmptyResultDataAccessException e) {
            status = FriendshipStatus.UNCOFIRMED;
            log.info("В таблице friend была добавлена запись с id: {} и id: {}, со статусом: {}", id, friendId, status);
            userRepository.addFriend(id, friendId, status);
        }

        return userRepository.get(id);
    }

    public User update(User user) {
        validateUserExists(Optional.of(user.getId()),
                new NotFoundException("Не существует пользователь с id: " + user.getId()),
                "Попытка обновить несуществующего пользователя с id: " + user.getId());

        userRepository.update(user);
        log.info("Был обновлён пользователь с id: {}", user.getId());
        return userRepository.get(user.getId());
    }

    public User update(Long id, User user) {
        user.setId(id);
        return update(user);
    }

    public void delete(Long id) {
        validateUserExists(Optional.of(id),
                new NotFoundException("Не существует пользователь с id: " + id),
                "Попытка удалить несуществующего пользователя с id: " + id);

        userRepository.delete(id);
        log.info("Был удалён пользователь с id: {}", id);
    }

    public void deleteFriend(Long id, Long friendId) {
        validateUserExists(Optional.of(id),
                new NotFoundException("Не существует пользователь с id: " + id),
                "Попытка удалить друга у несуществующего пользователя с id: " + id);

        validateUserExists(Optional.of(friendId),
                new NotFoundException("Не существует пользователь с id: " + friendId),
                "Попытка удалить из друзей несуществующего пользователя с id: " + friendId);

        if (!getAllFriends(id).contains(get(friendId))) {
            log.info("Попытка удалить из друзей несуществующего друга с id: {}", friendId);
            throw new NotFoundException("У пользователя с id: " + id + " нет друга с id: " + friendId);
        }

        userRepository.deleteFriend(id, friendId);
        log.info("У пользователя с id: {} был удалён друг с id: {}", id, friendId);
    }

    public void deleteAll() {
        validateUserExists(Optional.empty(),
                new NotFoundException("Таблица user пуста"),
                "Попытка очистить таблицу user, которая и так пуста");

        userRepository.deleteAll();
        log.info("Таблица user была очищена");
    }

    private void validateUserExists(Optional<Long> id,
                                    RuntimeException e, String logMessage) {
        if (id.isPresent()) {
            try {
                userRepository.get(id.get());
            } catch (EmptyResultDataAccessException ex) {
                log.info(logMessage);
                throw e;
            }
        } else {
            try {
                userRepository.getAll();
            } catch (EmptyResultDataAccessException ex) {
                log.info(logMessage);
                throw e;
            }
        }
    }
}
