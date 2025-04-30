package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @NotBlank(message = "Content cannot be blank")
    @Column(name = "content", nullable = false)
    private String content;

    @NotNull(message = "isPositive cannot be null")
    @Column(name = "is_positive", nullable = false)
    private Boolean isPositive;

    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull(message = "Film ID cannot be null")
    @Column(name = "film_id", nullable = false)
    private Long filmId;

    @Builder.Default
    @Column(name = "useful", nullable = false)
    private Integer useful = 0;
}