package ru.yandex.practicum.filmorate.storage.dal.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserRowMapper implements RowMapper<User> {
    private final JdbcTemplate jdbc;

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = User.builder()
                .id(rs.getLong("id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();

        user = user.toBuilder()
                .friendStatusMap(getFriendStatusMap(user.getId()))
                .build();

        return user;
    }

//    private Set<Long> getFriends(long id) {
//        String stm = "SELECT f.friend_id " +
//                "FROM \"user\" u " +
//                "INNER JOIN \"friend\" f ON u.id = f.user_id " +
//                "WHERE u.id = " + id;
//        return new HashSet<>(jdbc.queryForList(stm, Long.class));
//    }

    private Map<Long, FriendshipStatus> getFriendStatusMap(long id) {
        String stm = "SELECT f.friend_id AS id, fs.name AS status " +
                "FROM \"user\" u " +
                "INNER JOIN \"friend\" f ON u.id = f.user_id " +
                "INNER JOIN friendship_status fs ON f.friendship_status_id = fs.id " +
                "WHERE u.id = " + id;

        return jdbc.query(stm, rs -> {
            Map<Long, FriendshipStatus> result = new HashMap<>();
            while (rs.next()) {
                Long friendId = rs.getLong("id");
                FriendshipStatus status = FriendshipStatus.valueOf(rs.getString("status"));
                result.put(friendId, status);
            }
            return result;
        });
    }
}
