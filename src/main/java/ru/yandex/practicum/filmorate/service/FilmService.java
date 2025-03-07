package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class FilmService {
    private final Map<Long, Film> filmsMap = new HashMap<>();

    public Film get(Long id) {
        if (filmsMap.containsKey(id)) {
            return filmsMap.get(id);
        } else {
            log.info("Попытка получить несуществующий фильм с id: {}", id);
            throw new NotFoundException("Не существует фильма с id: " + id);
        }
    }

    public Collection<Film> getAll() {
        if (!filmsMap.isEmpty()) {
            return filmsMap.values();
        } else {
            log.info("Попытка получить список фильмов, который пуст");
            throw new NotFoundException("Список фильмов пуст");
        }
    }

    public Film add(Film film) {
        film.setId(getNextId());
        filmsMap.put(film.getId(), film);
        log.info("Был добавлен фильм с id: {}", film.getId());
        return film;
    }

    public Film update(Film film) {
        if (filmsMap.containsKey(film.getId())) {
            filmsMap.replace(film.getId(), film);
            log.info("Был обновлён фильм с id: {}", film.getId());
            return film;
        } else {
            log.info("Попытка обновить несуществующий фильм с id: {}", film.getId());
            throw new NotFoundException("Не существует фильм с id: " + film.getId());
        }
    }

    public Film update(Long id, Film film) {
        if (filmsMap.containsKey(id)) {
            film.setId(id);
            filmsMap.replace(id, film);
            log.info("Был обновлён фильм с id: {}", id);
            return film;
        } else {
            log.info("Попытка обновить несуществующий фильм с id: {}", id);
            throw new NotFoundException("Не существует фильм с id: " + id);
        }
    }

    public void delete(Long id) {
        if (filmsMap.containsKey(id)) {
            filmsMap.remove(id);
            log.info("Был удалён фильм с id: {}", id);
        } else {
            log.info("Попытка удалить несуществующий фильм с id: {}", id);
            throw new NotFoundException("Не существует фильм с id: " + id);
        }
    }

    public void deleteAll() {
        if (!filmsMap.isEmpty()) {
            filmsMap.clear();
            log.info("Список фильмов был очищен");
        } else {
            log.info("Попытка очистить список фильмов, который и так пуст");
            throw new NotFoundException("Список фильмов уже пуст");
        }
    }

    private Long getNextId() {
        Long nextId = filmsMap.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++nextId;
    }
}
