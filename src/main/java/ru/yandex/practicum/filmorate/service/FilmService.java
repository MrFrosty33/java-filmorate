package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;

    public Film get(Long id) {
        return filmStorage.get(id);
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Collection<Film> getPopular(int count) {
        return filmStorage.getPopular(count);
    }

    public Film add(Film film) {
        return filmStorage.add(film);
    }

    public Set<Long> addLike(Long id, Long userId) {
        return filmStorage.addLike(id, userId);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Film update(Long id, Film film) {
        return filmStorage.update(id, film);
    }

    public void delete(Long id) {
        filmStorage.delete(id);
    }

    public void deleteLike(Long id, Long userId) {
        filmStorage.deleteLike(id, userId);
    }

    public void deleteAll() {
        filmStorage.deleteAll();
    }
}
