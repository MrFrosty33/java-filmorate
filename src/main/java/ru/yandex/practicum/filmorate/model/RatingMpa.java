package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class RatingMpa {
    @NotNull
    private Long id;
    private String name;
}
