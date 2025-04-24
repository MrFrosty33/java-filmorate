package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.dal.DirectorRepository;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorRepository directorRepository;

    public Director get(Long id) {
        validateDirectorExists(Optional.of(id),
                new NotFoundException("Не существует режиссёра с id: " + id),
                "Попытка получить несуществующего режиссёра с id: " + id);

        log.info("Получен режиссёр с id: {}", id);
        return directorRepository.get(id);
    }

    public Collection<Director> getAll() {
        validateDirectorExists(Optional.empty(),
                new NotFoundException("Таблица director пуста"),
                "Попытка получить данные из таблицы director, которая пуста");

        log.info("Получен список всех режиссёров");
        return directorRepository.getAll();
    }

    public Director add(Director director) {
        director = directorRepository.add(director);
        log.info("Был добавлен режиссёр с id: {}", director.getId());
        return director;
    }

    public Director update(Director director) {
        Long id = director.getId();

        validateDirectorExists(Optional.of(id),
                new NotFoundException("Режиссёр с id: " + id + " не существует"),
                "Попытка обновить несуществующего режиссёра с id: " + id);

        log.info("Был обновлён режиссёр с id: {}", id);
        return directorRepository.update(director);
    }

    public void delete(Long id) {
        validateDirectorExists(Optional.of(id),
                new InternalServerException("Режиссёр с id: " + id + " не существует"),
                "Попытка удалить несуществующего режиссёра с id: " + id);

        directorRepository.delete(id);
        log.info("Был удалён режиссёр с id: {}", id);
    }

    public void deleteAll() {
        validateDirectorExists(Optional.empty(),
                new InternalServerException("Таблица director пуста"),
                "Попытка очистить таблицу director, которая и так пуста");

        directorRepository.deleteAll();
        log.info("Таблица director была очищена");
    }

    private void validateDirectorExists(Optional<Long> id,
                                        RuntimeException e, String logMessage) {
        try {
            if (id.isPresent()) {
                Optional<Director> result = Optional.ofNullable(directorRepository.get(id.get()));
                if (result.isEmpty()) {
                    log.info(logMessage);
                    throw e;
                }
            } else {
                Optional<Collection<Director>> result = Optional.ofNullable(directorRepository.getAll());
                if (result.isPresent() && result.get().isEmpty()) {
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
