package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

public interface DirectorStorage {

    Director get(Long id);

    Collection<Director> getAll();

    Director add(Director director);

    Director update(Director director);

    boolean delete(Long id);

    boolean deleteAll();
}
