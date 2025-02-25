package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Value;
import ru.yandex.practicum.filmorate.annotation.NoSpaces;

import java.time.LocalDate;

@Builder(toBuilder = true)
@Value
public class User {
    Long id;

    @Email
    String email;

    @NotBlank
    @NoSpaces(message = "Login не может содержать пробелы")
    String login;

    // не требуется ли заодно и пароль, раз есть логин?

    String name;

    @NotNull
    @PastOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate birthday;
}
