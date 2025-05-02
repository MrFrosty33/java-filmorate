package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.RatingMpa;

import java.util.Collection;

public interface RatingMpaStorage {
    RatingMpa get(Long id);

    Collection<RatingMpa> getAll();

    RatingMpa add(RatingMpa rating);

    RatingMpa update(RatingMpa rating);

    boolean delete(Long id);

    boolean deleteAll();

}
