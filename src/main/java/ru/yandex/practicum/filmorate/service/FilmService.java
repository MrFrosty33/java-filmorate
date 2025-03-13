package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    // требуется только для методов add & deleteLike
    // если проводить проверки не напрямую, а через userService, то будет не то сообщение об ошибке
    private final UserStorage userStorage;

    public Film get(Long id) {
        validateFilmExists(Optional.of(id),
                new NotFoundException("Не существует фильма с id: " + id),
                "Попытка получить несуществующий фильм с id: " + id);

        log.info("Получен фильм с id: {}", id);
        return filmStorage.get(id);
    }

    public Collection<Film> getAll() {
        validateFilmExists(Optional.empty(),
                new NotFoundException("Список фильмов пуст"),
                "Попытка получить список фильмов, который пуст");

        log.info("Получен список всех фильмов");
        return filmStorage.getAll();
    }

    public Collection<Film> getPopular(int count) {
        int from;
        int bound;
        validateFilmExists(Optional.empty(),
                new NotFoundException("Список фильмов пуст"),
                "Попытка получить список фильмов, который пуст");

        Map<Long, Film> films = filmStorage.getMap();
        if (films.size() > count) {
            from = films.size() - count;
            bound = from + count;
        } else {
            from = 1;
            bound = from + films.size();
        }

        log.info("Получен список из {} наиболее популярных фильмов", count);
        return films.entrySet().stream()
                .filter(entry -> (entry.getKey() >= from && entry.getKey() < bound))
                .sorted((entry1, entry2) ->
                        Integer.compare(entry2.getValue().getLikes().size(), entry1.getValue().getLikes().size()))
                .map(Map.Entry::getValue)
                .toList();
    }

    public Film add(Film film) {
        // может ли только что добавленный фильм иметь лайки?
        film.setId(getNextId());
        if (film.getLikes() == null || film.getLikes().isEmpty()) {
            film.setLikes(new HashSet<>());
        } else {
            Set<Long> invalidLikes = new HashSet<>();
            Set<Long> validLikes = new HashSet<>();
            for (Long likeId : film.getLikes()) {
                if (!userStorage.getAll().contains(userStorage.get(likeId))) {
                    invalidLikes.add(likeId);
                } else {
                    validLikes.add(likeId);
                }
            }
            if (!invalidLikes.isEmpty()) {
                log.info("Попытка добавить фильм с некорректным списком лайков");
                throw new ConflictException("Пользователи с id: " + invalidLikes
                        + " не существуют, следовательно не могут поставить лайк");
            }
            for (Long likeId : validLikes) {
                film.getLikes().add(likeId);
            }
        }
        filmStorage.add(film.getId(), film);
        log.info("Был добавлен фильм с id: {}", film.getId());
        return film;
    }

    public Set<Long> addLike(Long id, Long userId) {
        Film film = get(id);
        User user = userService.get(userId);
        film.getLikes().add(userId);
        log.info("Фильму с id: {} был поставлен лайк от пользователя с id: {}", id, userId);
        return film.getLikes();
    }

    public Film update(Film film) {
        validateFilmExists(Optional.of(film.getId()),
                new NotFoundException("Фильм с id: " + film.getId() + " не существует"),
                "Попытка обновить несуществующий фильм с id: " + film.getId());

        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        validateLikes(get(film.getId()), film);

        log.info("Был обновлён фильм с id: {}", film.getId());
        filmStorage.update(film);
        return film;
    }

    public Film update(Long id, Film film) {
        validateFilmExists(Optional.of(id),
                new NotFoundException("Фильм с id: " + id + " не существует"),
                "Попытка обновить несуществующий фильм с id: " + id);
        validateLikes(get(id), film);
        // на всякий случай, вдруг id объекта отличается от заданного
        film.setId(id);

        log.info("Был обновлён фильм с id: {}", id);
        filmStorage.update(id, film);
        return film;
    }

    public void delete(Long id) {
        validateFilmExists(Optional.of(id),
                new NotFoundException("Фильм с id: " + id + " не существует"),
                "Попытка удалить несуществующий фильм с id: " + id);


        filmStorage.delete(id);
        log.info("Был удалён фильм с id: {}", id);
    }

    public void deleteLike(Long id, Long userId) {
        Film film = get(id);
        User user = userStorage.get(userId);

        if (film.getLikes().contains(userId)) {
            film.getLikes().remove(userId);
            log.info("У фильма с id: {} был удалён лайк от пользователя с id: {}",
                    id, userId);
        } else {
            log.info("Попытка удалить у фильма с id: {} несуществующий лайк от пользователя с id: {}",
                    id, userId);
            throw new NotFoundException("У фильма с id: " + id + " нет лайка от пользователя с id: " + userId);
        }
    }

    public void deleteAll() {
        filmStorage.deleteAll();
    }

    private void validateFilmExists(Optional<Long> id,
                                    RuntimeException e, String logMessage) {
        if (id.isPresent()) {
            if (filmStorage.get(id.get()) == null) {
                log.info(logMessage);
                throw e;
            }
        } else {
            if (filmStorage.getAll() == null || filmStorage.getAll().isEmpty()) {
                log.info(logMessage);
                throw e;
            }
        }
    }

    private void validateLikes(Film oldFilm, Film newFilm) {
        if (!oldFilm.getLikes().equals(newFilm.getLikes())) {
            for (Long likeId : newFilm.getLikes()) {
                // проверка, существует ли пользователь
                User user = userService.get(likeId);
            }
        }
    }

    private Long getNextId() {
        Long nextId = filmStorage.getMap().keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++nextId;
    }
}
