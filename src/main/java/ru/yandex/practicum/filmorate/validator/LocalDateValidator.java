package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.annotation.MinLocalDate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class LocalDateValidator implements ConstraintValidator<MinLocalDate, LocalDate> {
    private LocalDate minDate;

    @Override
    public void initialize(MinLocalDate constraintAnnotation) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        this.minDate = LocalDate.parse(constraintAnnotation.value(), formatter);
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext constraintValidatorContext) {
        boolean result = !value.isBefore(minDate);
        if (!result) log.info("Не прошёл валидацию");
        return result;
    }
}
