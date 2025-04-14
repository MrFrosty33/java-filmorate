package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.dto.GenreDto;
import ru.yandex.practicum.filmorate.storage.dal.GenreRepository;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    public GenreDto get(Long id) {
        validateGenreExists(Optional.of(id),
                new NotFoundException("Не существует жанра с id: " + id),
                "Попытка получить несуществующий жанр с id: " + id);

        log.info("Получен жанр с id: {}", id);
        return genreRepository.get(id);
    }

    public Collection<GenreDto> getAll() {
        validateGenreExists(Optional.empty(),
                new NotFoundException("Таблица genre пуста"),
                "Попытка получить данные из таблицы genre, которая пуста");

        log.info("Получен список всех жанров");
        return genreRepository.getAll();
    }

    public GenreDto add(GenreDto genre) {
        genre = genreRepository.add(genre);
        log.info("Был добавлен жанр с id: {}", genre.getId());
        return genre;
    }

    public GenreDto update(GenreDto genre) {
        Long id = genre.getId();

        validateGenreExists(Optional.of(id),
                new NotFoundException("Жанр с id: " + id + " не существует"),
                "Попытка обновить несуществующий жанр с id: " + id);

        log.info("Был обновлён жанр с id: {}", id);
        genreRepository.update(genre);
        return genreRepository.get(id);
    }

    public void delete(Long id) {
        validateGenreExists(Optional.of(id),
                new NotFoundException("Жанр с id: " + id + " не существует"),
                "Попытка удалить несуществующий жанр с id: " + id);

        genreRepository.delete(id);
        log.info("Был удалён жанр с id: {}", id);
    }

    public void deleteAll() {
        validateGenreExists(Optional.empty(),
                new NotFoundException("Таблица genre пуста"),
                "Попытка очистить таблицу genre, которая и так пуста");

        genreRepository.deleteAll();
        log.info("Таблица genre была очищена");
    }

    private void validateGenreExists(Optional<Long> id,
                                     RuntimeException e, String logMessage) {
        try {
            if (id.isPresent()) {
                Optional<GenreDto> result = Optional.ofNullable(genreRepository.get(id.get()));
                if (result.isEmpty()) {
                    log.info(logMessage);
                    throw e;
                }
            } else {
                Optional<Collection<GenreDto>> result = Optional.ofNullable(genreRepository.getAll());
                if (result.isEmpty()) {
                    log.info(logMessage);
                    throw e;
                }
            }
        } catch (EmptyResultDataAccessException ex) {
            log.info(logMessage);
            throw e;
        }
    }
}
