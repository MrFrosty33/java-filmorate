package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.storage.dal.RatingMpaRepository;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingMpaService {
    private final RatingMpaRepository ratingMpaRepository;

    public RatingMpa get(Long id) {
        validateRatingExists(Optional.of(id),
                new NotFoundException("Не существует рейтинга с id: " + id),
                "Попытка получить несуществующий рейтинг с id: " + id);

        log.info("Получен рейтинг с id: {}", id);
        return ratingMpaRepository.get(id);
    }

    public Collection<RatingMpa> getAll() {
        log.info("Получен список всех рейтингов");
        return ratingMpaRepository.getAll();
    }

    public RatingMpa add(RatingMpa rating) {
        rating = ratingMpaRepository.add(rating);
        log.info("Был добавлен рейтинг с id: {}", rating.getId());
        return rating;
    }

    public RatingMpa update(RatingMpa rating) {
        Long id = rating.getId();

        validateRatingExists(Optional.of(id),
                new NotFoundException("Рейтинг с id: " + id + " не существует"),
                "Попытка обновить несуществующий рейтинг с id: " + id);

        rating = ratingMpaRepository.update(rating);
        log.info("Был обновлён рейтинг с id: {}", id);
        return rating;
    }

    public void delete(Long id) {
        validateRatingExists(Optional.of(id),
                new NotFoundException("Рейтинг с id: " + id + " не существует"),
                "Попытка удалить несуществующий рейтинг с id: " + id);

        ratingMpaRepository.delete(id);
        log.info("Был удалён рейтинг с id: {}", id);
    }

    public void deleteAll() {
        ratingMpaRepository.deleteAll();
        log.info("Таблица rating была очищена");
    }

    private void validateRatingExists(Optional<Long> id,
                                      RuntimeException e, String logMessage) {
        try {
            if (id.isPresent()) {
                Optional<RatingMpa> result = Optional.ofNullable(ratingMpaRepository.get(id.get()));
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
