package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.MinLocalDate;
import ru.yandex.practicum.filmorate.model.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.dto.RatingMpaDto;

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
    //@PastOrPresent может ли быть дата релиза в будущем?
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    @NotNull
    @Min(1)
    private long duration;

    private Set<Long> likes;
    private Set<GenreDto> genres;
    @JsonProperty("mpa")
    private RatingMpaDto ratingMpa;

    public int getRate() {
        return likes.size();
    }
}
