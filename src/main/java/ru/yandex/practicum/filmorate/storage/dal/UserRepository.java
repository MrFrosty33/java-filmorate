package ru.yandex.practicum.filmorate.storage.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.dal.mapper.UserRowMapper;

import java.util.*;

@Slf4j
@Repository
public class UserRepository extends BaseRepository<User> implements UserStorage {
    private static final String GET_USER_BY_ID = "SELECT * FROM \"user\"  WHERE id = ?";
    private static final String GET_ALL = "SELECT * FROM \"user\" ";
    private static final String GET_FRIENDSHIP_STATUS_ID_BY_NAME = "SELECT id FROM friendship_status " +
            "WHERE name IN (?)";
    private static final String GET_FRIENDSHIP_STATUS_NAME_BY_ID = "SELECT name FROM friendship_status " +
            "WHERE id = ?";
    private static final String GET_FRIENDSHIP_STATUS_ID_BETWEEN_USERS = "SELECT friendship_status_id " +
            "FROM \"friend\" WHERE user_id = ? AND friend_id = ? ";
    private static final String GET_COMMON_FRIENDS_BETWEEN_USERS = "SELECT u.* FROM \"user\" u " +
            "JOIN (SELECT friend_id FROM \"friend\" WHERE user_id = ? " +
            "INTERSECT SELECT friend_id FROM \"friend\" WHERE user_id = ?) " +
            "common ON u.id = common.friend_id";
    private static final String GET_ALL_FRIENDS_BY_USER_ID = "SELECT friend_id FROM \"friend\" WHERE user_id = ?";

    private static final String INSERT_USER = "INSERT INTO \"user\" (id, email, login, name, birthday)" +
            " VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_FRIEND_STATUS = "INSERT INTO \"friend\" " +
            "(user_id, friend_id, friendship_status_id) VALUES (?, ?, ?)";

    private static final String UPDATE_USER = "UPDATE \"user\" " +
            "SET id = ?, email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String UPDATE_FRIENDSHIP_STATUS = "UPDATE \"friend\" " +
            "SET user_id = ?, friend_id = ?, friendship_status_id = ? WHERE user_id = ? AND friend_id = ?";

    private static final String DELETE_USER_BY_ID = "DELETE FROM \"user\" WHERE id = ?";
    private static final String DELETE_ALL_USERS = "DELETE FROM \"user\" ";
    private static final String DELETE_ALL_FRIENDS_BY_USER_ID = "DELETE FROM \"friend\" WHERE user_id = ?";
    private static final String DELETE_FRIENDS_BY_USER_AND_FRIEND_ID = "DELETE FROM \"friend\" " +
            "WHERE user_id = ? AND friend_id = ?";
    private static final String DELETE_ALL_FRIENDS = "DELETE FROM \"friend\" ";

    public UserRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public User get(Long id) {
        return findOne(GET_USER_BY_ID, id);
    }

    @Override
    public Collection<User> getAll() {
        return findMany(GET_ALL);
    }

    @Override
    public Collection<User> getCommonFriends(Long id, Long otherId) {
        UserRowMapper userRowMapper = new UserRowMapper(jdbc);
        return jdbc.query(GET_COMMON_FRIENDS_BETWEEN_USERS, userRowMapper, id, otherId);
    }

    @Override
    public FriendshipStatus getFriendshipStatus(Long id, Long otherId) {
        // возвращает нынешний статус дружбы
        // если нет записи - EmptyResultDataAccessException, который должен отлавливаться далее
        Long statusId = jdbc.queryForObject(GET_FRIENDSHIP_STATUS_ID_BETWEEN_USERS, Long.class, id, otherId);
        return jdbc.queryForObject(
                GET_FRIENDSHIP_STATUS_NAME_BY_ID,
                (rs, rowNum) -> FriendshipStatus.valueOf(rs.getString("name")),
                statusId
        );
    }

    public Collection<User> getAllFriends(Long id) {
        Set<Long> friendIds = new HashSet<>(jdbc.queryForList(GET_ALL_FRIENDS_BY_USER_ID, Long.class, id));
        Collection<User> result = new ArrayList<>();

        for (Long friendId : friendIds) {
            result.add(get(friendId));
        }

        return result;
    }

    @Override
    public User add(User user) {
        if (user.getId() == null) {
            // Для БД нужно передавать значение с кавычками "user"
            // Добавляю кавычки и проверяю возможность получения id из таблицы в методе BaseRepository
            // Здесь, дабы читалось чуть проще, можно передавать просто user
            user.setId(nextIdByTable("user"));
        }

        insert(INSERT_USER,
                user.getId(),
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday());

        return get(user.getId());
    }

    @Override
    public User addFriend(Long id, Long friendId, FriendshipStatus status) {
        Long statusId = jdbc.queryForObject(GET_FRIENDSHIP_STATUS_ID_BY_NAME, Long.class, status.name());
        jdbc.update(INSERT_FRIEND_STATUS, id, friendId, statusId);
        return get(id);
    }

    @Override
    @Transactional
    public User update(User user) {
        if (user.getId() == null) {
            user.setId(nextIdByTable("user"));
        }

        User oldUser = get(user.getId());

        update(UPDATE_USER,
                user.getId(),
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        if (!oldUser.getFriendStatusMap().equals(user.getFriendStatusMap())) {
            jdbc.update(DELETE_ALL_FRIENDS_BY_USER_ID, oldUser.getId());

            Map<Long, FriendshipStatus> friendStatusMap = user.getFriendStatusMap();
            List<Object[]> batchInsertUserFriend = new ArrayList<>();
            List<Object[]> batchInsertFriendUser = new ArrayList<>();
            List<Object[]> batchDeleteFriendUserRelation = new ArrayList<>();

            for (Long friendId : friendStatusMap.keySet()) {
                // нужно проверить, есть ли заявка о дружбе с другой стороны
                try {
                    // как здесь избежать циклического запроса, я не знаю
                    // тут либо получать список всех друзей по friendId и смотреть, есть ли там user.getId()
                    // либо менее трудоёмко - получать статус дружбы между friendId и user.getId()
                    // таким образом сократил с трёх запросов за каждую итерацию цикла до одного - уже лучше
                    getFriendshipStatus(friendId, user.getId());

                    batchInsertUserFriend.add(new Object[]{user.getId(), friendId, FriendshipStatus.CONFIRMED});
                    batchInsertFriendUser.add(new Object[]{friendId, user.getId(), FriendshipStatus.CONFIRMED});
                    batchDeleteFriendUserRelation.add(new Object[]{friendId, user.getId()});
                } catch (EmptyResultDataAccessException e) {
                    batchInsertUserFriend.add(new Object[]{user.getId(), friendId, FriendshipStatus.UNCONFIRMED});
                }
            }

            jdbc.batchUpdate(DELETE_FRIENDS_BY_USER_AND_FRIEND_ID, batchDeleteFriendUserRelation);
            jdbc.batchUpdate(INSERT_FRIEND_STATUS, batchInsertUserFriend);
            jdbc.batchUpdate(INSERT_FRIEND_STATUS, batchInsertFriendUser);
        }

        return get(user.getId());
    }

    @Override
    public FriendshipStatus updateFriendshipStatus(Long id, Long friendId, FriendshipStatus friendshipStatus) {
        update(UPDATE_FRIENDSHIP_STATUS,
                id,
                friendId,
                friendshipStatus,
                id,
                friendId);

        return getFriendshipStatus(id, friendId);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        boolean deleteUser = deleteOne(DELETE_USER_BY_ID, id);
        boolean deleteFriend = jdbc.update(DELETE_ALL_FRIENDS_BY_USER_ID, id) > 0;

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
    @Transactional
    public boolean deleteAll() {
        boolean deleteUser = deleteAll(DELETE_ALL_USERS);
        boolean deleteFriend = jdbc.update(DELETE_ALL_FRIENDS) > 0;

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
        boolean deleteFriend = jdbc.update(DELETE_FRIENDS_BY_USER_AND_FRIEND_ID, id, friendId) > 0;

        if (!deleteFriend) {
            log.info("Произошла ошибка при удалении записи из таблицы friend " +
                    "с user_id: {} и friend_id: {} ", id, friendId);
            throw new InternalServerException("Произошла ошибка при удалении записи из таблицы friend " +
                    "с user_id: " + id + " и friend_id: " + friendId);
        }

        return true;
    }
}
