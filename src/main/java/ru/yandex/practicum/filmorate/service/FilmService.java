package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.BadRequestParamException;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dal.FilmRepository;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmRepository filmRepository;
    private final UserService userService;
    private final DirectorService directorService;

    public Film get(Long id) {
        validateFilmExists(Optional.of(id),
                new NotFoundException("Не существует фильма с id: " + id),
                "Попытка получить несуществующий фильм с id: " + id);

        log.info("Получен фильм с id: {}", id);
        return filmRepository.get(id);
    }

    public Collection<Film> getAll() {
        validateFilmExists(Optional.empty(),
                new NotFoundException("Таблица film пуста"),
                "Попытка получить данные из таблицы film, которая пуста");

        log.info("Получен список всех фильмов");
        return filmRepository.getAll();
    }

    public Collection<Film> getPopular(int limit) {
        validateFilmExists(Optional.empty(),
                new NotFoundException("Таблица film пуста"),
                "Попытка получить данные из таблицы film, которая пуста");

        if (limit <= 0) {
            log.info("Попытка получить список популярных фильмов c limit = {}", limit);
            throw new BadRequestParamException("limit не может быть меньше или равен 0");
        }

        Collection<Film> result = filmRepository.getPopular(limit);
        log.info("Получен список из {} наиболее популярных фильмов", result.size());
        return result;
    }

    public Collection<Film> getByDirector(Long directorId, String sortBy) {
        Director director = directorService.get(directorId);
        Collection<Film> result;

        switch (sortBy) {
            case "year", "likes" -> result = filmRepository.getByDirector(directorId, sortBy);
            default -> {
                log.info("Попытка получить список фильмов по режиссёру с sortBy = {}", sortBy);
                throw new BadRequestParamException("Был передан sortBy с неподдерживаемым типом сортировки: " + sortBy +
                        ". Поддерживаются только year, likes");
            }
        }

        return result;
    }

    public Film add(Film film) {
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }

        if (film.getGenres() == null) {
            film.setGenres(new HashSet<>());
        }

        if (film.getDirectors() == null) {
            film.setDirectors(new HashSet<>());
        }

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
        Long id = film.getId();

        validateFilmExists(Optional.of(id),
                new NotFoundException("Фильм с id: " + id + " не существует"),
                "Попытка обновить несуществующий фильм с id: " + id);

        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }

        if (film.getGenres() == null) {
            film.setGenres(new HashSet<>());
        }

        if (film.getDirectors() == null) {
            film.setDirectors(new HashSet<>());
        }

        film = filmRepository.update(film);
        log.info("Был обновлён фильм с id: {}", id);
        return film;
    }

    // В репозитории оставил один метод update(Film film)
    public Film update(Long id, Film film) {
        film.setId(id);
        return update(film);
    }

    public void delete(Long id) {
        validateFilmExists(Optional.of(id),
                new NotFoundException("Фильм с id: " + id + " не существует"),
                "Попытка удалить несуществующий фильм с id: " + id);


        filmRepository.delete(id);
        log.info("Был удалён фильм с id: {}", id);
    }

    public void deleteAll() {
        validateFilmExists(Optional.empty(),
                new NotFoundException("Таблица film пуста"),
                "Попытка очистить таблицу film, которая и так пуста");

        filmRepository.deleteAll();
        log.info("Таблица film была очищена");
    }

    public void deleteLike(Long filmId, Long userId) {
        Film film = get(filmId);
        User user = userService.get(userId);

        if (film.getLikes().contains(userId)) {
            filmRepository.deleteLike(filmId, userId);
            log.info("У фильма с id: {} был удалён лайк от пользователя с id: {}",
                    filmId, userId);
        } else {
            log.info("Попытка удалить у фильма с id: {} несуществующий лайк от пользователя с id: {}",
                    filmId, userId);
            throw new NotFoundException("У фильма с id: " + filmId + " нет лайка от пользователя с id: " + userId);
        }
    }

    private void validateFilmExists(Optional<Long> id,
                                    RuntimeException e, String logMessage) {
        try {
            if (id.isPresent()) {
                Optional<Film> result = Optional.ofNullable(filmRepository.get(id.get()));
                if (result.isEmpty()) {
                    log.info(logMessage);
                    throw e;
                }
            } else {
                Optional<Collection<Film>> result = Optional.ofNullable(filmRepository.getAll());
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

    public List<Film> getCommonFilms(long userId, long friendId) {
        userService.get(userId);
        userService.get(friendId);
        return filmRepository.getCommonFilms(userId, friendId);
    }
}
