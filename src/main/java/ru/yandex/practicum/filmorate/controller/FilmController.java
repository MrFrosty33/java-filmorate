package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private Map<Long, Film> filmsMap = new HashMap<>();

    @GetMapping("/{id}")
    public Film get(@PathVariable Long id) {
        if (filmsMap.containsKey(id)) {
            return filmsMap.get(id);
        } else {
            log.info("Попытка получить несуществующий фильм с id: {}", id);
            throw new NotFoundException("Не существует фильма с id: " + id);
        }
    }

    @GetMapping
    public Collection<Film> getAll() {
        if (!filmsMap.isEmpty()) {
            return filmsMap.values();
        } else {
            log.info("Попытка получить список фильмов, который пуст");
            throw new NotFoundException();
        }
    }

    @PostMapping
    public Film add(@Valid @RequestBody Film film) {
        film.setId(getNextId());
        filmsMap.put(film.getId(), film);
        log.info("Был добавлен фильм с id: {}", film.getId());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        if (filmsMap.containsKey(film.getId())) {
            filmsMap.replace(film.getId(), film);
            log.info("Был обновлён фильм с id: {}", film.getId());
            return film;
        } else {
            log.info("Попытка обновить несуществующий фильм с id: {}", film.getId());
            throw new NotFoundException();
        }
    }


    @PutMapping("/{id}")
    public Film update(@PathVariable Long id, @Valid @RequestBody Film film) {
        if (filmsMap.containsKey(id)) {
            film.setId(id);
            filmsMap.replace(id, film);
            log.info("Был обновлён фильм с id: {}", id);
            return film;
        } else {
            log.info("Попытка обновить несуществующий фильм с id: {}", id);
            throw new NotFoundException();
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        if (filmsMap.containsKey(id)) {
            filmsMap.remove(id);
            log.info("Был удалён фильм с id: {}", id);
        } else {
            log.info("Попытка удалить несуществующий фильм с id: {}", id);
            throw new NotFoundException();
        }
    }

    @DeleteMapping
    public void deleteAll() {
        if (!filmsMap.isEmpty()) {
            filmsMap.clear();
            log.info("Список фильмов был очищен");
        } else {
            log.info("Попытка очистить список фильмов, который и так пуст");
            throw new NotFoundException();
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
