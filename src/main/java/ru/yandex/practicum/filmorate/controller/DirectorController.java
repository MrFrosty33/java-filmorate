package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping("/{id}")
    public Director get(@PathVariable Long id) {
        return directorService.get(id);
    }

    @GetMapping
    public Collection<Director> getAll() {
        return directorService.getAll();
    }

    @PostMapping
    public Director add(@Valid @RequestBody Director director) {
        return directorService.add(director);
    }

    @PutMapping
    public Director update(@Valid @RequestBody Director director) {
        return directorService.update(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        directorService.delete(id);
    }

    @DeleteMapping
    public void deleteAll() {
        directorService.deleteAll();
    }

}
