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
    Map<Long, Film> filmsMap = new HashMap<>();

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

    // каким образом должны выставляться id? передаётся объект уже с id, или же это работа контроллера?
    // если id всегда назначает controller, стоит ли пометить аннотацией @Null поле id у Film для валидации?
    // если передаётся с id, должны ли id в мапе и в объекте быть одинаковыми?
    @PostMapping
    public Film add(@Valid @RequestBody Film filmRaw) {
        Film film = filmRaw.toBuilder().id(nextId()).build();
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
            // return filmsMap.get(film.getId()); можно и так, но более ресурсоёмко, а результат тот же
        } else {
            log.info("Попытка обновить несуществующий фильм с id: {}", film.getId());
            throw new NotFoundException();
        }
    }

    // для обновления объекта нужно, чтобы передавался объект со всеми полями, проходящими валидацию?
    // не требуется возможность обновить одно только поле?
    @PutMapping("/{id}")
    public Film update(@PathVariable Long id, @Valid @RequestBody Film rawFilm) {
        if (filmsMap.containsKey(id)) {
            Film film = rawFilm.toBuilder().id(id).build();
            filmsMap.replace(id, film);
            log.info("Был обновлён фильм с id: {}", id);
            return film;
            // return filmsMap.get(film.getId()); можно и так, но более ресурсоёмко, а результат тот же
        } else {
            log.info("Попытка обновить несуществующий фильм с id: {}", id);
            throw new NotFoundException();
        }
    }

    // стоит ли возвращать удалённый фильм?
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

    private Long nextId() {
        Long nextId = filmsMap.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++nextId;
    }
}
