package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @GetMapping("/{id}")
    public Film get(@PathVariable Long id) {
        return filmService.get(id);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopular(count);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getByDirector(@PathVariable Long directorId, @RequestParam String sortBy) {
        return filmService.getByDirector(directorId, sortBy);
    }

    @GetMapping
    public Collection<Film> getAll() {
        return filmService.getAll();
    }

    @PostMapping
    public Film add(@Valid @RequestBody Film film) {
        return filmService.add(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public Collection<Long> addLike(@PathVariable Long id, @PathVariable Long userId) {
        return filmService.addLike(id, userId);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        return filmService.update(film);
    }


    @PutMapping("/{id}")
    public Film update(@PathVariable Long id, @Valid @RequestBody Film film) {
        return filmService.update(id, film);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        filmService.delete(id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.deleteLike(id, userId);
    }

    @DeleteMapping
    public void deleteAll() {
        filmService.deleteAll();
    }

    @GetMapping(value = "/common", params = {"userId", "friendId"})
    public List<Film> getCommonFilms(long userId, long friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }
}
