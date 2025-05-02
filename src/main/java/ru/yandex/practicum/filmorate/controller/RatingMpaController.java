package ru.yandex.practicum.filmorate.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.service.RatingMpaService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/mpa")
public class RatingMpaController {
    private final RatingMpaService ratingService;

    @GetMapping("/{id}")
    public RatingMpa get(@PathVariable Long id) {
        return ratingService.get(id);
    }

    @GetMapping
    public Collection<RatingMpa> getAll() {
        return ratingService.getAll();
    }

    @PostMapping
    public RatingMpa add(@Valid @RequestBody RatingMpa rating) {
        return ratingService.add(rating);
    }

    @PutMapping
    public RatingMpa update(@Valid @RequestBody RatingMpa rating) {
        return ratingService.update(rating);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        ratingService.delete(id);
    }

    @DeleteMapping
    public void deleteAll() {
        ratingService.deleteAll();
    }
}
