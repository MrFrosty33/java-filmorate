package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class InMemoryFilmStorage implements FilmStorage {
    /**
     * В этом классе предполагается, что все ему передаваемые данные корректны.
     * Проверки на корректность данных должны быть проведены в других классах.
     */

    private final Map<Long, Film> filmsMap;

    @Override
    public Film get(Long id) {
        return filmsMap.getOrDefault(id, null);
    }

    @Override
    public Collection<Film> getAll() {
        if (!filmsMap.isEmpty()) {
            return filmsMap.values();
        } else {
            return null;
        }
    }

    @Override
    public Map<Long, Film> getMap() {
        return filmsMap;
    }

    @Override
    public Film add(Film film) {
        return filmsMap.put(film.getId(), film);
    }

    @Override
    public Film add(Long id, Film film) {
        return filmsMap.put(id, film);
    }

    @Override
    public Film update(Film film) {
        return filmsMap.replace(film.getId(), film);
    }

    @Override
    public Film update(Long id, Film film) {
        return filmsMap.replace(id, film);
    }

    @Override
    public void delete(Long id) {
        filmsMap.remove(id);
    }

    @Override
    public void deleteAll() {
        filmsMap.clear();
    }
}
