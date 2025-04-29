package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface FilmStorage {

    Film get(Long id);

    Collection<Film> getAll();

    Collection<Film> getPopular(int limit);

    Collection<Film> getByDirector(Long directorId, String sortBy);

    Film add(Film film);

    Set<Long> addLike(Long filmId, Long userId);

    Film update(Film film);

    boolean delete(Long id);

    boolean deleteAll();

    boolean deleteLike(Long filmId, Long userId);

    Collection<Film> search(String query, String by);

    List<Film> getCommonFilms(Long userId, Long friendId);
}
