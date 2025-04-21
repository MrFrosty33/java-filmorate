package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.dto.RatingMpaDto;

import java.util.Collection;

public interface RatingMpaStorage {
    RatingMpaDto get(Long id);

    Collection<RatingMpaDto> getAll();

    RatingMpaDto add(RatingMpaDto rating);

    RatingMpaDto update(RatingMpaDto rating);

    boolean delete(Long id);

    boolean deleteAll();

}
