package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.MinLocalDate;

import java.time.LocalDate;
import java.util.Set;

@Builder(toBuilder = true)
@Data
public class Film {
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    @Size(max = 200)
    private String description;

    @NotNull
    @MinLocalDate
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    @NotNull
    @Min(1)
    private long duration;

    private Set<Long> likes;
    private Set<Genre> genres;
    @JsonProperty("mpa")
    private RatingMpa ratingMpa;
    private Set<Director> directors;

    public int getRate() {
        return likes.size();
    }
}
