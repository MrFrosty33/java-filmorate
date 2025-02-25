package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.annotation.PositiveDuration;

import java.time.Duration;

@Slf4j
public class PositiveDurationValidator implements ConstraintValidator<PositiveDuration, Duration> {
    @Override
    public void initialize(PositiveDuration constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Duration value, ConstraintValidatorContext constraintValidatorContext) {
        boolean result = value.isPositive();
        if (!result) log.info("PositiveDurationValidator не прошёл валидацию");
        return result;
    }
}
