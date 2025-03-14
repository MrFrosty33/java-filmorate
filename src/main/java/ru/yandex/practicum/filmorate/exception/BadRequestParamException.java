package ru.yandex.practicum.filmorate.exception;

public class BadRequestParamException extends RuntimeException {
    public BadRequestParamException(String message) {
        super(message);
    }
}
