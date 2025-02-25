package ru.yandex.practicum.filmorate.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;

public class FilmTest {
    private Validator validator;
    private final Film validFilm = Film.builder()
            .id(1L)
            .name("Inception")
            .description("A mind-bending thriller.")
            .releaseDate(LocalDate.of(2010, 7, 16))
            .duration(Duration.ofMinutes(148))
            .build();

    @BeforeEach
    public void beforeEach() {
        // Инициализация валидатора
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidFilm() {
        var violations = validator.validate(validFilm);
        Assertions.assertTrue(violations.isEmpty());
    }

    @Test
    public void testBlankName() {
        Film film = validFilm.toBuilder().name("").build();

        var violations = validator.validate(film);
        Assertions.assertFalse(violations.isEmpty());
    }

    @Test
    public void testBlankDescription() {
        Film film = validFilm.toBuilder().description("").build();

        var violations = validator.validate(film);
        Assertions.assertFalse(violations.isEmpty());
    }

    @Test
    public void testOver200SymbolsDescription() {
        Film film = validFilm.toBuilder().description("Inception is a mind-bending thriller where Dom Cobb, " +
                "a skilled thief who steals secrets from dreams, is tasked with planting an idea in someone's mind. " +
                "As he dives through layers of dreams, reality blurs, creating a thrilling " +
                "journey of time and memory.").build();

        var violations = validator.validate(film);
        Assertions.assertFalse(violations.isEmpty());
    }

    @Test
    public void testReleaseDateIsBeforeMinLocalDate() {
        Film film = validFilm.toBuilder().releaseDate(LocalDate.of(1895, 12, 27)).build();

        var violations = validator.validate(film);
        Assertions.assertFalse(violations.isEmpty());
    }

    @Test
    public void testDurationIsNegative() {
        Film film = validFilm.toBuilder().duration(Duration.ofMinutes(-148)).build();

        var violations = validator.validate(film);
        Assertions.assertFalse(violations.isEmpty());
    }

}
