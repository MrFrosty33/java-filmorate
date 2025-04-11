package ru.yandex.practicum.filmorate.storage.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Map;

@Slf4j
@Qualifier
public class UserRepository extends BaseRepository<User> implements UserStorage {
    private static final String GET_ONE_QUERY = "SELECT * FROM \"user\"  WHERE id = ?";
    private static final String GET_ALL_QUERY = "SELECT * FROM \"user\" ";
    private static final String GET_FRIENDSHIP_STATUS_ID_BY_NAME_QUERY = "SELECT id FROM friendship_status " +
            "WHERE name IN (?)";
    private static final String GET_FRIENDSHIP_STATUS_NAME_BY_ID_QUERY = "SELECT name FROM friendship_status " +
            "WHERE id = ?";
    private static final String GET_FRIENDSHIP_STATUS_ID_BETWEEN_USERS_QUERY = "SELECT friendship_status_id " +
            "FROM \"friend\" WHERE user_id = ? AND friend_id = ? ";
    private static final String GET_COMMON_FRIENDS_BETWEEN_USERS_QUERY = "SELECT friend_id FROM \"friend\" " +
            "WHERE user_id = ? INTERSECT SELECT friend_id FROM \"friend\" WHERE user_id = ?";

    private static final String INSERT_QUERY = "INSERT INTO \"user\" (id, email, login, name, birthday)" +
            " VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_FRIEND_STATUS_QUERY = "INSERT INTO \"friend\" " +
            "(user_id, friend_id, friendship_status_id) VALUES (?, ?, ?)";

    private static final String UPDATE_QUERY = "UPDATE \"user\" " +
            "SET id = ?, email = ?, login = ?, name = ?, birthday = ? ";

    private static final String DELETE_BY_ID_QUERY = "DELETE FROM \"user\" WHERE id = ?";
    private static final String DELETE_ALL_QUERY = "DELETE FROM \"user\" ";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM \"friend\" WHERE user_id = ?";
    private static final String DELETE_ALL_FRIENDS_QUERY = "DELETE FROM \"friend\" ";

    public UserRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }


    @Override
    public User get(Long id) {
        return findOne(GET_ONE_QUERY, id);
    }

    @Override
    public Collection<User> getAll() {
        return findMany(GET_ALL_QUERY);
    }

    //todo интересно, правильно ли написал метод
    public FriendshipStatus getFriendshipStatus(Long id, Long otherId) {
        // метод проверяет, существует ли уже заявка о дружбе с одной стороны
        // если есть - возвращает CONFIRMED, иначе - UNCONFIRMED
        Long statusId = jdbc.queryForObject(GET_FRIENDSHIP_STATUS_ID_BETWEEN_USERS_QUERY, Long.class, id, otherId);
        return jdbc.queryForObject(
                GET_FRIENDSHIP_STATUS_NAME_BY_ID_QUERY,
                (rs, rowNum) -> FriendshipStatus.valueOf(rs.getString("name")),
                statusId
        );
    }

    //todo сработает ли?
    @Override
    public Collection<User> getCommonFriends(Long id, Long otherId) {
        return findMany(GET_COMMON_FRIENDS_BETWEEN_USERS_QUERY, id, otherId);
    }

    @Override
    public User add(User user) {
        if (user.getId() == null) {
            // Для БД нужно передавать значение с кавычками "user"
            // Добавляю кавычки и проверяю возможность получения id из таблицы в методе BaseRepository
            // Здесь, дабы читалось чуть проще, можно передавать просто user
            user.setId(nextIdByTable("user"));
        }
        long insertId = insert(INSERT_QUERY,
                user.getId(),
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday());

        // Заполняем таблицу friend
        Map<Long, FriendshipStatus> friendStatusMap = user.getFriendStatusMap();
        if (!friendStatusMap.isEmpty()) {
            for (Long friendId : friendStatusMap.keySet()) {
                Long statusId = jdbc.queryForObject(GET_FRIENDSHIP_STATUS_ID_BY_NAME_QUERY,
                        Long.class,
                        friendStatusMap.get(friendId));
                insert(INSERT_FRIEND_STATUS_QUERY, user.getId(), friendId, statusId);
                insert(INSERT_FRIEND_STATUS_QUERY, friendId, user.getId(), statusId);
            }
        }

        return get(insertId);
    }

    @Override
    public User add(Long id, User user) {
        // Предполагается, что переданный id будет корректен
        user.setId(id);
        return add(user);
    }

    @Override
    public User addFriend(Long id, Long friendId, FriendshipStatus status) {
        jdbc.update(INSERT_FRIEND_STATUS_QUERY, id, friendId, status);
        return get(id);
    }

    @Override
    public User update(User user) {
        if (user.getId() == null) {
            user.setId(nextIdByTable("user"));
        }

        User oldUser = get(user.getId());

        update(UPDATE_QUERY,
                user.getId(),
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday());

        if (!oldUser.getFriendStatusMap().equals(user.getFriendStatusMap())) {
            jdbc.update(DELETE_FRIEND_QUERY, oldUser.getId());

            Map<Long, FriendshipStatus> friendStatusMap = user.getFriendStatusMap();

            for (Long friendId : friendStatusMap.keySet()) {
                Long statusId = jdbc.queryForObject(GET_FRIENDSHIP_STATUS_ID_BY_NAME_QUERY,
                        Long.class,
                        friendStatusMap.get(friendId));
                insert(INSERT_FRIEND_STATUS_QUERY, user.getId(), friendId, statusId);
                insert(INSERT_FRIEND_STATUS_QUERY, friendId, user.getId(), statusId);
            }
        }

        return get(user.getId());
    }

    @Override
    public User update(Long id, User user) {
        user.setId(id);
        return update(user);
    }

    @Override
    public boolean delete(Long id) {
        boolean deleteUser = deleteOne(DELETE_BY_ID_QUERY, id);
        boolean deleteFriend = jdbc.update(DELETE_FRIEND_QUERY, id) > 0;

        if (!deleteUser) {
            log.info("Произошла ошибка при удалении записи из таблицы user с id: {}", id);
            throw new InternalServerException("Произошла ошибка при удалении записи из таблицы user с id: " + id);
        }
        if (!deleteFriend) {
            log.info("Произошла ошибка при удалении записи из таблицы friend с user_id: {}", id);
            throw new InternalServerException("Произошла ошибка при удалении записи из таблицы friend с user_id: " + id);
        }

        return true;
    }

    @Override
    public boolean deleteAll() {
        boolean deleteUser = deleteAll(DELETE_ALL_QUERY);
        boolean deleteFriend = jdbc.update(DELETE_ALL_FRIENDS_QUERY) > 0;

        if (!deleteUser) {
            log.info("Произошла ошибка при удалении всех записей из таблицы user");
            throw new InternalServerException("Произошла ошибка при очистке таблицы user");
        }
        if (!deleteFriend) {
            log.info("Произошла ошибка при удалении всех записей из таблицы friend");
            throw new InternalServerException("Произошла ошибка при очистке таблицы friend");
        }

        return true;
    }

    @Override
    public boolean deleteFriend(Long id, Long friendId) {
        return false;
    }
}
