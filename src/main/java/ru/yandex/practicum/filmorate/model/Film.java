package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import ru.yandex.practicum.filmorate.annotation.MinLocalDate;
import ru.yandex.practicum.filmorate.annotation.PositiveDuration;

import java.time.Duration;
import java.time.LocalDate;

// стоит ли работать с final объектами здесь и далее, или лучше заменить @Value на @Data?
@Builder(toBuilder = true)
@Value
public class Film {
    Long id;

    @NotBlank
    String name;

    @NotBlank
    @Size(max = 200)
    String description;

    @NotNull
    @MinLocalDate
    //@PastOrPresent может ли быть дата релиза в будущем?
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate releaseDate;

    @NotNull
    @PositiveDuration
    Duration duration;
}
