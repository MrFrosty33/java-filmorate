package ru.yandex.practicum.filmorate.storage.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.filmorate.exception.InternalServerException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class BaseRepository<T> {
    protected final JdbcTemplate jdbc;
    protected final RowMapper<T> mapper;

    protected T findOne(String query, Object... params) {
        try {
            return jdbc.queryForObject(query, mapper, params);
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    protected List<T> findMany(String query, Object... params) {
        return jdbc.query(query, mapper, params);
    }

    protected boolean deleteOne(String query, long id) {
        int rowsDeleted = jdbc.update(query, id);
        return rowsDeleted > 0;
    }

    protected boolean deleteAll(String query) {
        int rowsDeleted = jdbc.update(query);
        return rowsDeleted > 0;
    }

    protected void update(String query, Object... params) {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            log.info("Ошибка при обновлении данных в БД");
            throw new InternalServerException("Не удалось обновить данные");
        }
    }

    protected long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);

        // Возвращаем id нового пользователя
        if (id != null) {
            return id;
        } else {
            log.info("Ошибка при добавлении данных в БД");
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    protected Long nextIdByTable(String tableName) {
        String GET_CURRENT_MAX_ID = "SELECT MAX(id) FROM ?";

        // чтобы работало лишь с таблицами, откуда возможно и может потребоваться получить ID
        switch (tableName) {
            case "user" -> {
                tableName = "\"user\"";
            }
            case "friend" -> {
                tableName = "\"friend\"";
            }
            case "film", "genre", "rating", "friendship_status" -> {
                // игнорируем
            }
            default -> {
                log.info("Ошибка при получении ID из таблицы {}", tableName);
                throw new InternalServerException("Внутренняя ошибка сервера. " +
                        "Можно получить ID только у следующих таблиц: " +
                        "user, film, genre, rating, friendship_status");
            }
        }

        Optional<Long> id = Optional.ofNullable(jdbc.queryForObject(GET_CURRENT_MAX_ID, Long.class, tableName));
        return id.map(value -> value + 1).orElse(1L);
    }
}