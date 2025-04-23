package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
public class Director {
    @NotNull
    private Long id;
    private String name;
}
