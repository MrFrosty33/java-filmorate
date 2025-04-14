package ru.yandex.practicum.filmorate.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;

public class UserTest {
    private Validator validator;
    private final User validUser = User.builder()
            .id(1L)
            .email("johndoe@example.com")
            .login("john123")
            .name("John Doe")
            .birthday(LocalDate.of(1990, 5, 15))
            .friendStatusMap(new HashMap<>())
            .build();

    @BeforeEach
    public void beforeEach() {
        // Инициализация валидатора
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidUser() {
        var violations = validator.validate(validUser);
        Assertions.assertTrue(violations.isEmpty());
    }

    @Test
    public void testInvalidEmail() {
        User user = validUser.toBuilder()
                .email("invalid?example.com")
                .build();

        var violations = validator.validate(user);
        Assertions.assertFalse(violations.isEmpty());
    }

    @Test
    public void testBlankLogin() {
        User user = validUser.toBuilder()
                .login("")
                .build();

        var violations = validator.validate(user);
        Assertions.assertFalse(violations.isEmpty());
    }

    @Test
    public void testLoginWithSpaces() {
        User user = validUser.toBuilder()
                .login("example login")
                .build();

        var violations = validator.validate(user);
        Assertions.assertFalse(violations.isEmpty());
    }

    @Test
    public void testBirthdayIsInFuture() {
        User user = validUser.toBuilder()
                .birthday(LocalDate.of(2090, 5, 15))
                .build();

        var violations = validator.validate(user);
        Assertions.assertFalse(violations.isEmpty());
    }

}
