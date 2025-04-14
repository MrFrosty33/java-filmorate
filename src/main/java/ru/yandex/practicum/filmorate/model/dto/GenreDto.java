package ru.yandex.practicum.filmorate.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;

@Data
@Builder(toBuilder = true)
public class GenreDto {
    @NotNull
    private Long id;
    private Genre name;
}
