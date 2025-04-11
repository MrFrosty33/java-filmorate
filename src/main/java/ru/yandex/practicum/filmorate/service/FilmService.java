package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.BadRequestParamException;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dal.FilmRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmRepository filmRepository;
    private final UserService userService;

    public Film get(Long id) {
        validateFilmExists(Optional.of(id),
                new NotFoundException("Не существует фильма с id: " + id),
                "Попытка получить несуществующий фильм с id: " + id);

        log.info("Получен фильм с id: {}", id);
        return filmRepository.get(id);
    }

    public Collection<Film> getAll() {
        validateFilmExists(Optional.empty(),
                new NotFoundException("Список фильмов пуст"),
                "Попытка получить список фильмов, который пуст");

        log.info("Получен список всех фильмов");
        return filmRepository.getAll();
    }

    public Collection<Film> getPopular(int limit) {
        validateFilmExists(Optional.empty(),
                new NotFoundException("Список фильмов пуст"),
                "Попытка получить список фильмов, который пуст");

        if (limit <= 0) {
            log.info("Попытка получить список популярных фильмов c limit = {}", limit);
            throw new BadRequestParamException("limit не может быть меньше или равен 0");
        }

        Collection<Film> result = filmRepository.getPopular(limit);
        log.info("Получен список из {} наиболее популярных фильмов", result.size());
        return result;
    }

    public Film add(Film film) {
        if (!film.getLikes().isEmpty()) {
            log.info("Попытка добавить новый фильм с уже поставленными ему лайками");
            throw new ConflictException("Новый фильм не может содержать лайки");
        }

        film = filmRepository.add(film);
        log.info("Был добавлен фильм с id: {}", film.getId());
        return film;
    }

    public Set<Long> addLike(Long filmId, Long userId) {
        Film film = get(filmId);
        User user = userService.get(userId);
        Set<Long> likes = filmRepository.addLike(filmId, userId);

        log.info("Фильму с id: {} был поставлен лайк от пользователя с id: {}", filmId, userId);
        return likes;
    }

    public Film update(Film film) {
        validateFilmExists(Optional.of(film.getId()),
                new NotFoundException("Фильм с id: " + film.getId() + " не существует"),
                "Попытка обновить несуществующий фильм с id: " + film.getId());

        validateLikes(get(film.getId()), film);

        log.info("Был обновлён фильм с id: {}", film.getId());
        filmRepository.update(film);
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
        filmRepository.update(id, film);
        return film;
    }

    public void delete(Long id) {
        validateFilmExists(Optional.of(id),
                new NotFoundException("Фильм с id: " + id + " не существует"),
                "Попытка удалить несуществующий фильм с id: " + id);


        filmRepository.delete(id);
        log.info("Был удалён фильм с id: {}", id);
    }

    public void deleteLike(Long filmId, Long userId) {
        Film film = get(filmId);
        User user = userService.get(userId);

        if (film.getLikes().contains(userId)) {
            film.getLikes().remove(userId);
            log.info("У фильма с id: {} был удалён лайк от пользователя с id: {}",
                    filmId, userId);
        } else {
            log.info("Попытка удалить у фильма с id: {} несуществующий лайк от пользователя с id: {}",
                    filmId, userId);
            throw new NotFoundException("У фильма с id: " + filmId + " нет лайка от пользователя с id: " + userId);
        }
    }

    public void deleteAll() {
        filmRepository.deleteAll();
    }

    private void validateFilmExists(Optional<Long> id,
                                    RuntimeException e, String logMessage) {
        if (id.isPresent()) {
            try {
                filmRepository.get(id.get());
            } catch (EmptyResultDataAccessException ex) {
                log.info(logMessage);
                throw e;
            }
        } else {
            if (filmRepository.getAll() == null || filmRepository.getAll().isEmpty()) {
                log.info(logMessage);
                throw e;
            }
        }
    }

    //todo переделать под репозиторий потом
    private void validateLikes(Film oldFilm, Film newFilm) {
        if (!oldFilm.getLikes().equals(newFilm.getLikes())) {
            for (Long likeId : newFilm.getLikes()) {
                // проверка, существует ли пользователь
                User user = userService.get(likeId);
            }
        }
    }
}
