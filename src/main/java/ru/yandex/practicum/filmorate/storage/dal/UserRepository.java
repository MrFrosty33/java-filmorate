package ru.yandex.practicum.filmorate.storage.dal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Qualifier
public class UserRepository extends BaseRepository<User> implements UserStorage {
    private static final String GET_ONE_QUERY = "SELECT * FROM user WHERE id = ?";
    private static final String GET_ALL_QUERY = "SELECT * FROM user";
    private static final String INSERT_QUERY = "";
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
        return null;
    }

    @Override
    public User add(Long id, User user) {
        return null;
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
