package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Film get(Long id);

    Collection<Film> getAll();


    Film add(Film film);

    Film add(Long id, Film film);

    Film update(Film film);

    Film update(Long id, Film film);

    void delete(Long id);

    void deleteAll();
}
