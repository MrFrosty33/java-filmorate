package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.util.Collection;

//TODO новые методы
@Service
public class FilmService {
    private final FilmStorage filmStorage = new InMemoryFilmStorage();

    public Film get(Long id) {
        return filmStorage.get(id);
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film add(Film film) {
        return filmStorage.add(film);
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

    public void deleteAll() {
        filmStorage.deleteAll();
    }
}
