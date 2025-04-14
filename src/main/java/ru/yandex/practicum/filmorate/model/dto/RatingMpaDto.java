package ru.yandex.practicum.filmorate.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.RatingMpa;

@Data
@Builder(toBuilder = true)
public class RatingMpaDto {
    @NotNull
    private Long id;
    private RatingMpa name;
}
