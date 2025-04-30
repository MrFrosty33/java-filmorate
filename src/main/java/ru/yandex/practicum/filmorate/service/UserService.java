package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dal.FilmRepository;
import ru.yandex.practicum.filmorate.storage.dal.UserRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;

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

        if (user.getFriendStatusMap() == null) {
            user.setFriendStatusMap(new HashMap<>());
        }

        if (!user.getFriendStatusMap().isEmpty()) {
            log.info("Попытка добавить нового пользователя с уже имеющимися друзьями");
            throw new ConflictException("Новый пользователь не может иметь друзей");
        }

        user = userRepository.add(user);
        log.info("Был добавлен пользователь с id: {}", user.getId());
        return user;
    }

    // Стоит возвращать целиком пользователя, или же только статус дружбы, полученный в ходе выполнения метода?
    public User addFriend(Long id, Long friendId) {
        // если А дружит с Б, то пока Б не отправил запрос к А, статус дружбы UNCONFIRMED
        FriendshipStatus status;
        validateUserExists(Optional.of(id),
                new NotFoundException("Не существует пользователь с id: " + id),
                "Попытка добавить друга к несуществующему пользователю с id:  " + id);

        validateUserExists(Optional.of(friendId),
                new NotFoundException("Не существует пользователь с id: " + friendId),
                "Попытка в друзья несуществующего пользователя с id:  " + friendId);

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
            userRepository.getFriendshipStatus(friendId, id);
            status = FriendshipStatus.CONFIRMED;

            log.info("В таблице friend изменён статус дружбы между пользователями с id: {} и id: {} на статус {}",
                    friendId, id, status);
            userRepository.updateFriendshipStatus(friendId, id, status);
            log.info("В таблице friend была добавлена запись с id: {} и id: {}, со статусом: {}", id, friendId, status);
            userRepository.addFriend(id, friendId, status);
        } catch (EmptyResultDataAccessException e) {
            // если же нет таких записей, то добавляем дружбу пользователя А с Б со статусом UNCONFIRMED
            status = FriendshipStatus.UNCONFIRMED;
            log.info("В таблице friend была добавлена запись с id: {} и id: {}, со статусом: {}", id, friendId, status);
            userRepository.addFriend(id, friendId, status);
        }

        return userRepository.get(id);
    }

    public User update(User user) {
        Long id = user.getId();

        validateUserExists(Optional.of(id),
                new NotFoundException("Не существует пользователь с id: " + id),
                "Попытка обновить несуществующего пользователя с id: " + id);

        if (user.getFriendStatusMap() == null) {
            user.setFriendStatusMap(new HashMap<>());
        }

        user = userRepository.update(user);
        log.info("Был обновлён пользователь с id: {}", id);
        return user;
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
            return;
            //throw new NotFoundException("У пользователя с id: " + id + " нет друга с id: " + friendId);
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
        try {
            if (id.isPresent()) {
                Optional<User> result = Optional.ofNullable(userRepository.get(id.get()));
                if (result.isEmpty()) {
                    log.info(logMessage);
                    throw e;
                }
            } else {
                Optional<Collection<User>> result = Optional.ofNullable(userRepository.getAll());
                if (result.isPresent() && result.get().isEmpty()) {
                    log.info(logMessage);
                    throw e;
                }
            }
        } catch (EmptyResultDataAccessException ex) {
            log.info(logMessage);
            throw e;
        }
    }

    public List<Film> getRecommendations(Long id) {
        validateUserExists(Optional.of(id),
                new NotFoundException("Не существует пользователь с id: " + id),
                "Попытка получить несуществующего пользователя с id: " + id);

        Map<Long, Set<Long>> similarUserLikes = userRepository.getSimilarUserLikes(id);
        Set<Long> userLikes = similarUserLikes.get(id);
        if (userLikes == null) {
            return Collections.emptyList();
        }

        Set<Long> mostSimilarUsers = getMostSimilarUsers(id, similarUserLikes);
        Set<Long> recommendations = new HashSet<>();
        for (Long userId : mostSimilarUsers) {
            Set<Long> otherUserLikes = similarUserLikes.get(userId);
            for (Long otherUserLike : otherUserLikes) {
                if (!userLikes.contains(otherUserLike)) {
                    recommendations.add(otherUserLike);
                }
            }
        }
        log.info("Получен список рекомендаций с id: {}", id);
        return filmRepository.getByListIds(recommendations);
    }

    private Set<Long> getMostSimilarUsers(Long userId, Map<Long, Set<Long>> userLikes) {
        int maxIntersectionSize = 0;

        Set<Long> mostSimilarUsers = new HashSet<>();
        for (Map.Entry<Long, Set<Long>> entry : userLikes.entrySet()) {
            if (entry.getKey().equals(userId)) continue;
            Set<Long> otherUserLikes = entry.getValue();
            Set<Long> intersection = new HashSet<>(userLikes.get(userId));
            intersection.retainAll(otherUserLikes);
            if (intersection.size() > maxIntersectionSize) {
                maxIntersectionSize = intersection.size();
                mostSimilarUsers.clear();
                mostSimilarUsers.add(entry.getKey());
            } else if (intersection.size() == maxIntersectionSize) {
                mostSimilarUsers.add(entry.getKey());
            }
        }
        return mostSimilarUsers;
    }
}
