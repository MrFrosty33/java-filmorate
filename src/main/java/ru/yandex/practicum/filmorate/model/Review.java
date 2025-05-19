package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    private Long reviewId;

    @NotBlank(message = "Content cannot be blank")
    private String content;

    @NotNull(message = "isPositive cannot be null")
    private Boolean isPositive;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Film ID cannot be null")
    private Long filmId;

    @Builder.Default
    private Integer useful = 0;
}