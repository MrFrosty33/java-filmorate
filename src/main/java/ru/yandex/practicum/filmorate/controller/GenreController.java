package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/genres")
public class GenreController {
    private final GenreService genreService;

    @GetMapping("/{id}")
    public Genre get(@PathVariable Long id) {
        return genreService.get(id);
    }

    @GetMapping
    public Collection<Genre> getAll() {
        return genreService.getAll();
    }

    @PostMapping
    public Genre add(@Valid @RequestBody Genre genre) {
        return genreService.add(genre);
    }

    @PutMapping
    public Genre update(@Valid @RequestBody Genre genre) {
        return genreService.update(genre);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        genreService.delete(id);
    }

    @DeleteMapping
    public void deleteAll() {
        genreService.deleteAll();
    }
}
