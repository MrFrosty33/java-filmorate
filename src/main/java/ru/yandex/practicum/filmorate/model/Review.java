package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "reviews")
@Data
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_positive", nullable = false)
    private Boolean isPositive;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "film_id", nullable = false)
    private Long filmId;

    @Column(name = "useful", nullable = false)
    private Integer useful;

    public Review() {
    }
}