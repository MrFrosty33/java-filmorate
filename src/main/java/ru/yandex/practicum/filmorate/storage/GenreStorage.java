package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.dto.GenreDto;

import java.util.Collection;

public interface GenreStorage {
    GenreDto get(Long id);

    Collection<GenreDto> getAll();

    GenreDto add(GenreDto genre);

    GenreDto update(GenreDto genre);

    boolean delete(Long id);

    boolean deleteAll();
}
