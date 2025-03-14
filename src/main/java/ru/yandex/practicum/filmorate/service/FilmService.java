package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.BadRequestParamException;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final UserStorage userStorage;

    public static final Comparator<Film> FILM_COMPARATOR =
            Comparator.comparingLong(Film::getRate).reversed();

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
        validateFilmExists(Optional.empty(),
                new NotFoundException("Список фильмов пуст"),
                "Попытка получить список фильмов, который пуст");

        if (count <= 0) {
            log.info("Попытка получить список популярных фильмов c count = {}", count);
            throw new BadRequestParamException("count не может быть меньше или равен 0");
        }

        if (count > filmStorage.getAll().size()) {
            count = filmStorage.getAll().size();
        }

        log.info("Получен список из {} наиболее популярных фильмов", count);
        return filmStorage.getAll().stream()
                .sorted(FILM_COMPARATOR)
                .limit(count)
                .collect(Collectors.toList());
    }

    public Film add(Film film) {
        film.setId(getNextId());
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }

        if (!film.getLikes().isEmpty()) {
            log.info("Попытка добавить новый фильм с уже поставленными ему лайками");
            throw new ConflictException("Новый фильм не может содержать лайки");
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
