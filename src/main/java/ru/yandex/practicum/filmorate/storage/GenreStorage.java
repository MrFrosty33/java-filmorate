package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

public interface GenreStorage {
    Genre get(Long id);

    Collection<Genre> getAll();

    Genre add(Genre genre);

    Genre update(Genre genre);

    boolean delete(Long id);

    boolean deleteAll();
}
