package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService{
    public Director get(Long id) {
        return null;
    }

    public Collection<Director> getAll() {
        return List.of();
    }

    public Director add(Director director) {
        return null;
    }

    public Director update(Director director) {
        return null;
    }

    public boolean delete(Long id) {
        return false;
    }

    public boolean deleteAll() {
        return false;
    }
}
