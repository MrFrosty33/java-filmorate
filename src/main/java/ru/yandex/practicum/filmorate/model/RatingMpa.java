package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RatingMpa {
    G("G"),
    PG("PG"),
    PG_13("PG-13"),
    R("R"),
    NC_17("NC-17");

    private final String dbName;

    RatingMpa(String dbName) {
        this.dbName = dbName;
    }

    @JsonValue
    public String getDbName() {
        return dbName;
    }

    @JsonCreator
    public static RatingMpa fromDbName(String dbName) {
        return RatingMpa.valueOf(dbName.replace('-', '_'));
    }
}
