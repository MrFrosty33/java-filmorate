package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.NoSpaces;

import java.time.LocalDate;
import java.util.Map;

@Builder(toBuilder = true)
@Data
public class User {
    private Long id;

    @Email
    @NotEmpty
    private String email;

    @NotBlank
    @NoSpaces(message = "Login не может содержать пробелы")
    private String login;

    private String name;

    @NotNull
    @PastOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    // Хранит в себе ID друга и статус их дружбы.
    private Map<Long, FriendshipStatus> friendStatusMap;
}
