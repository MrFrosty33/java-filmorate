package ru.yandex.practicum.filmorate.storage.dal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Map;

@Qualifier
public class UserRepository extends BaseRepository<User> implements UserStorage {
    private static final String GET_ONE_QUERY = "SELECT * FROM user WHERE id = ?";
    private static final String GET_ALL_QUERY = "SELECT * FROM user";
    private static final String GET_FRIENDSHIP_STATUS_ID_BY_NAME = "SELECT id FROM friendship_status WHERE name IN (?)";

    private static final String INSERT_QUERY = "INSERT INTO user (id, email, login, name, birthday)" +
            " VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_FRIEND_STATUS_QUERY = "INSERT INTO friend " +
            "(user_id, friend_id, friendship_status_id) VALUES (?, ?, ?)";

    private static final String DELETE_BY_ID_QUERY = "DELETE FROM user WHERE id = ?";
    private static final String DELETE_ALL_QUERY = "DELETE FROM user";

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

    @Override
    public User add(User user) {
        if (user.getId() == null) {
            user.setId(nextIdByTable("user"));
        }
        long insertId = insert(INSERT_QUERY,
                user.getId(),
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday());
        //todo список друзей
        Map<Long, FriendshipStatus> friendStatusMap = user.getFriendStatusMap();

        if (!friendStatusMap.isEmpty()) {
            for (Long friendId : friendStatusMap.keySet()) {
                Long statusId = jdbc.queryForObject(GET_FRIENDSHIP_STATUS_ID_BY_NAME,
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
        // предполагается, что переданный id будет корректен
        user.setId(id);
        return add(user);
    }

    @Override
    public User update(User user) {
        return null;
    }

    @Override
    public User update(Long id, User user) {
        return null;
    }

    @Override
    public void delete(Long id) {
        deleteOne(DELETE_BY_ID_QUERY, id);
    }

    @Override
    public void deleteAll() {
        deleteAll(DELETE_ALL_QUERY);
    }
}
