package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PastOrPresent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.BadRequestParamException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.Year;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @GetMapping("/{id}")
    public Film get(@PathVariable Long id) {
        return filmService.get(id);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopular(@RequestParam(required = false)
                                       Long genreId,

                                       @RequestParam(required = false)
                                       @PastOrPresent(message = "year не может быть в будущем")
                                       Year year) {
        return filmService.getPopular(genreId, year);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getByDirector(@PathVariable
                                          Long directorId,

                                          @RequestParam
                                          String sortBy) {
        // я попытался задать ограничение через
        // @Pattern(regexp = "year|likes", message = "Поддерживаются только значения: year, likes")
        // но почему-то в ApplicationExceptionHandler исключение приземляется в handleOther
        // и ответ получается неинформативным. На данный момент не разобрался ещё с этим, поэтому сделал так
        switch (sortBy) {
            case "year", "likes" -> {
                return filmService.getByDirector(directorId, sortBy);
            }
            default -> {
                log.info("Попытка получить список фильмов по режиссёру с неподдерживаемым sortBy: {}", sortBy);
                throw new BadRequestParamException("Поддерживаются только year, likes в качестве метода сортировки");
            }
        }
    }

    @GetMapping
    public Collection<Film> getAll() {
        return filmService.getAll();
    }

    @PostMapping
    public Film add(@Valid @RequestBody Film film) {
        return filmService.add(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public Collection<Long> addLike(@PathVariable Long id, @PathVariable Long userId) {
        return filmService.addLike(id, userId);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        return filmService.update(film);
    }


    @PutMapping("/{id}")
    public Film update(@PathVariable Long id, @Valid @RequestBody Film film) {
        return filmService.update(id, film);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        filmService.delete(id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.deleteLike(id, userId);
    }

    @DeleteMapping
    public void deleteAll() {
        filmService.deleteAll();
    }

    @GetMapping("/search")
    public Collection<Film> search(@RequestParam @NotNull String query, @RequestParam @NotNull String by) {
        return filmService.search(query, by);
    }

    @GetMapping(value = "/common")
    public List<Film> getCommonFilms(@RequestParam long userId, @RequestParam long friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }
}
