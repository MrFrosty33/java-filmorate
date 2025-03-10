package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Film get(Long id);

    Collection<Film> getAll();

    Collection<Film> getPopular(int count);

    Film add(Film film);

    // Что возвращать в этом методе? Пользователя, что поставил лайк фильму? Или список лайков, в виде id / User?
    void addLike(Long id, Long userId);

    Film update(Film film);

    Film update(Long id, Film film);

    void delete(Long id);

    void deleteAll();
}
