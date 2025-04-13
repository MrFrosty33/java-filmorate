package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.dto.GenreDto;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/genres")
public class GenreController {
    private final GenreService genreService;

    @GetMapping("/{id}")
    public GenreDto get(@PathVariable Long id) {
        return genreService.get(id);
    }

    @GetMapping
    public Collection<GenreDto> getAll() {
        return genreService.getAll();
    }

    @PostMapping
    public GenreDto add(@Valid @RequestBody GenreDto genre) {
        return genreService.add(genre);
    }

    @PutMapping
    public GenreDto update(@Valid @RequestBody GenreDto genre) {
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
