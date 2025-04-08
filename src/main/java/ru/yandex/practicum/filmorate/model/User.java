package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.NoSpaces;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@Builder(toBuilder = true)
@Data
public class User {
    private Long id;

    @Email
    private String email;

    @NotBlank
    @NoSpaces(message = "Login не может содержать пробелы")
    private String login;

    private String name;

    @NotNull
    @PastOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    // висит до тех пор, пока логика во всех методах не переделана под мапу
    // TODO упразднить
    private Set<Long> friends;

    // Хранит в себе ID друга и статус их дружбы.
    private Map<Long, FriendshipStatus> friendStatusMap;
}
