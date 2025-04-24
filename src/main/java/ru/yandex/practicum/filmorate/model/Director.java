package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
public class Director {
    private Long id;
    @NotBlank
    private String name;
}
