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
    Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getAll() {
        if (!films.isEmpty()) {
            return films.values();
        } else {
            log.info("Список фильмов на данный момент пуст.");
            throw new NotFoundException();
        }
    }

    @GetMapping("films/{id}")
    public Film get(@PathVariable Long id) {
        if (films.containsKey(id)) {
            return films.get(id);
        } else {
            log.info("Не существует фильма с id: {}.", id);
            throw new NotFoundException();
        }
    }

    // каким образом должны выставляться id? передаётся объект уже с id, или же это работа контроллера?
    // если передаётся с id, должны ли id в мапе и в объекте быть одинаковыми?
    @PostMapping
    public Film add(@Valid @RequestBody Film filmRaw) {
        Film film = filmRaw.toBuilder().id(nextId()).build();
        films.put(film.getId(), film);
        log.info("Был добавлен фильм с id: {}.", film.getId());
        return film;
    }


    @PostMapping
    public void addAll(@Valid @RequestBody Collection<Film> film) {
        film.forEach(value -> films.put(nextId(), value));
    }

    @PutMapping("/films/{id}")
    public void update(@Valid @RequestBody @PathVariable Long id, Film film) {
        if (films.containsKey(id)) {
            films.replace(id, film);
        } else {
            log.info("Не существует фильма с id: {}.", id);
            throw new NotFoundException();
        }
    }

    // стоит ли возвращать удалённый фильм?
    @DeleteMapping("films/{id}")
    public void delete(@PathVariable Long id) {
        if (films.containsKey(id)) {
            films.remove(id);
        } else {
            log.info("Не существует фильма с id: {}.", id);
            throw new NotFoundException();
        }
    }


    private Long nextId() {
        Long nextId = films.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++nextId;
    }
}
