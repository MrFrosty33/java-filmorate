package ru.yandex.practicum.filmorate.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;

public class FilmTest {
    private Validator validator;
    private final Film validFilm = Film.builder()
            .id(1L)
            .name("Inception")
            .description("A mind-bending thriller.")
            .releaseDate(LocalDate.of(2010, 7, 16))
            .duration(148)
            .likes(new HashSet<>())
            .genres(new HashSet<>())
            .ratingMpa(RatingMpa.builder().name("R").id(4L).build())
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
        Film film = validFilm.toBuilder().duration(-148).build();

        var violations = validator.validate(film);
        Assertions.assertFalse(violations.isEmpty());
    }

}
