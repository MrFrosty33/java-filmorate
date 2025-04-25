package ru.yandex.practicum.filmorate.storage.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.exception.InternalServerException;

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

    protected void insert(String query, Object... params) {
        int rowsUpdated = jdbc.update(query, params);

        if (rowsUpdated != 0) {
            // добавился успешно, игнорируем
        } else {
            log.info("Ошибка при добавлении данных в БД");
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    protected void update(String query, Object... params) {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            log.info("Ошибка при обновлении данных в БД");
            throw new InternalServerException("Не удалось обновить данные");
        }
    }

    protected boolean deleteOne(String query, long id) {
        int rowsDeleted = jdbc.update(query, id);
        return rowsDeleted > 0;
    }

    protected boolean deleteAll(String query) {
        int rowsDeleted = jdbc.update(query);
        return rowsDeleted > 0;
    }

    protected Long nextIdByTable(String tableName) {
        String stm = "SELECT MAX(id) FROM ";

        // чтобы работало лишь с таблицами, откуда возможно и может потребоваться получить ID
        // не забывать добавлять новые таблицы сюда!
        switch (tableName) {
            case "user" -> {
                tableName = "\"user\"";
            }
            case "friend" -> {
                tableName = "\"friend\"";
            }
            case "film", "genre", "rating", "friendship_status", "director" -> {
                // игнорируем
            }
            default -> {
                log.info("Ошибка при получении ID из таблицы {}", tableName);
                throw new InternalServerException("Внутренняя ошибка сервера. " +
                        "Можно получить ID только у следующих таблиц: " +
                        "user, film, genre, rating, friendship_status");
            }
        }

        stm += tableName;

        Optional<Number> id = Optional.ofNullable(jdbc.queryForObject(stm, Number.class));
        return id.map(value -> value.longValue() + 1).orElse(1L);
    }
}