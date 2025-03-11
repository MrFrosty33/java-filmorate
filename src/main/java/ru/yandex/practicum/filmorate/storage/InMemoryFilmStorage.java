package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryFilmStorage implements FilmStorage {

    /*
        Правильно ли я понимаю, что этот и ему подобные классы существует ради того, чтобы хранить в себе мапу
        с объектами? Но если так и он реализует методы из интерфейса, то получается несколько сумбурно:
        Запрос приходит в FilmController -> контроллер вызывает соответсвующий метод в FilmService ->
        сервис вызывает такой же метод в InMemoryFilmStorage. Будто можно обойтись без отдельного хранилища,
        ибо код из контроллера буквально дублируется в сервисе. FilmService может реализовывать интерфейс FilmStorage
        и хранить в себе мапу.
     */

    /*
        На данный момент во многих методах использую для валидации внутренние методы get(), которые
        в случае чего выбрасывают ошибку. Но текст ошибок может не идеально подходить ситуации.
        Если в каждом методе делать проверки, код станет более громоздкий. Стоит ли сделать отдельный метод
        для валидации? Из-за этого также пришлось добавить UserStorage, чтобы иметь возможность проверять,
        существует ли пользователь, который пытается поставить лайк.
     */
    private final Map<Long, Film> filmsMap;
    private final UserStorage userStorage;

    @Override
    public Film get(Long id) {
        if (filmsMap.containsKey(id)) {
            return filmsMap.get(id);
        } else {
            log.info("Попытка получить несуществующий фильм с id: {}", id);
            throw new NotFoundException("Не существует фильма с id: " + id);
        }
    }

    @Override
    public Collection<Film> getAll() {
        if (!filmsMap.isEmpty()) {
            return filmsMap.values();
        } else {
            log.info("Попытка получить список фильмов, который пуст");
            throw new NotFoundException("Список фильмов пуст");
        }
    }

    @Override
    public Collection<Film> getPopular(int count) {
        int from;
        int bound;

        if (filmsMap.size() > count) {
            from = filmsMap.size() - count;
            bound = from + count;
        } else {
            from = 1;
            bound = from + filmsMap.size();
        }

        log.info("Получен список из {} наиболее популярных фильмов", count);
        return filmsMap.entrySet().stream()
                .filter(entry -> (entry.getKey() >= from && entry.getKey() < bound))
                .sorted((entry1, entry2) ->
                        Integer.compare(entry2.getValue().getLikes().size(), entry1.getValue().getLikes().size()))
                .map(Map.Entry::getValue)
                .toList();
    }

    @Override
    public Film add(Film film) {
        // может ли только что добавленный фильм иметь лайки?
        film.setId(getNextId());
        filmsMap.put(film.getId(), film);
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
                throw new NotFoundException("Пользователи с id: " + invalidLikes
                        + " не существуют, следовательно не могут поставить лайк");
            }
            for (Long likeId : validLikes) {
                film.getLikes().add(likeId);
            }
        }
        log.info("Был добавлен фильм с id: {}", film.getId());
        return film;
    }

    @Override
    public Set<Long> addLike(Long id, Long userId) {
        Film film = get(id);
        User user = userStorage.get(userId);

        film.getLikes().add(userId);
        log.info("Фильму с id: {} был поставлен лайк от пользователя с id: {}", id, userId);
        return film.getLikes();
    }

    @Override
    public Film update(Film film) {
        if (filmsMap.containsKey(film.getId())) {
            filmsMap.replace(film.getId(), film);
            log.info("Был обновлён фильм с id: {}", film.getId());
            return film;
        } else {
            log.info("Попытка обновить несуществующий фильм с id: {}", film.getId());
            throw new NotFoundException("Не существует фильм с id: " + film.getId());
        }
    }

    @Override
    public Film update(Long id, Film film) {
        if (filmsMap.containsKey(id)) {
            film.setId(id);
            filmsMap.replace(id, film);
            log.info("Был обновлён фильм с id: {}", id);
            return film;
        } else {
            log.info("Попытка обновить несуществующий фильм с id: {}", id);
            throw new NotFoundException("Не существует фильм с id: " + id);
        }
    }

    @Override
    public void delete(Long id) {
        if (filmsMap.containsKey(id)) {
            filmsMap.remove(id);
            log.info("Был удалён фильм с id: {}", id);
        } else {
            log.info("Попытка удалить несуществующий фильм с id: {}", id);
            throw new NotFoundException("Не существует фильм с id: " + id);
        }
    }

    @Override
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

    @Override
    public void deleteAll() {
        if (!filmsMap.isEmpty()) {
            filmsMap.clear();
            log.info("Список фильмов был очищен");
        } else {
            log.info("Попытка очистить список фильмов, который и так пуст");
            throw new NotFoundException("Список фильмов уже пуст");
        }
    }

    private Long getNextId() {
        Long nextId = filmsMap.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++nextId;
    }
}
