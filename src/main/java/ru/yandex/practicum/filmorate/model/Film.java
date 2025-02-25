package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Value;
import ru.yandex.practicum.filmorate.annotation.MinLocalDate;

import java.time.Duration;
import java.time.LocalDate;

// стоит ли работать с final объектами здесь и далее, или лучше заменить @Value на @Data?
@Builder(toBuilder = true)
@Value
public class Film {
    Long id;

    @NotBlank
    @NotNull
    String name;

    @NotBlank
    @NotNull
    @Max(value = 200)
    String description;

    @NotNull
    @MinLocalDate
    LocalDate releaseDate;

    @NotNull
    @Positive
    Duration duration;
}
