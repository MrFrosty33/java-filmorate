-- Стоит ли дополнительно добавлять NOT NULL? В логике приложения все эти случаи обрабатываются,
-- не должно возникнуть случая, когда будут переданы некорректные данные в БД.
CREATE TABLE IF NOT EXISTS genre (
    id SERIAL PRIMARY KEY,
    name VARCHAR
);

CREATE TABLE IF NOT EXISTS rating (
    id SERIAL PRIMARY KEY,
    name VARCHAR
);

CREATE TABLE IF NOT EXISTS film (
    id SERIAL PRIMARY KEY,
    name VARCHAR,
    description TEXT,
    release_date DATE,
    duration INTEGER
);

CREATE TABLE IF NOT EXISTS friendship_status (
    id SERIAL PRIMARY KEY,
    name VARCHAR DEFAULT 'unconfirmed'
);

-- Стоит ли делать поля email & login UNIQUE?
-- На данный момент логика приложения позволяет добавлять одинаковые e-mail & login
CREATE TABLE IF NOT EXISTS "user" (
    id SERIAL PRIMARY KEY,
    email VARCHAR,
    login VARCHAR,
    name VARCHAR,
    birthday DATE
);

CREATE TABLE IF NOT EXISTS film_genre (
    film_id INTEGER REFERENCES film(id) ON DELETE CASCADE,
    genre_id INTEGER REFERENCES genre(id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS film_rating (
    film_id INTEGER REFERENCES film(id) ON DELETE CASCADE,
    rating_id INTEGER REFERENCES rating(id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, rating_id)
);

CREATE TABLE IF NOT EXISTS "friend" (
    user_id INTEGER REFERENCES "user"(id) ON DELETE CASCADE,
    friend_id INTEGER REFERENCES "user"(id) ON DELETE CASCADE,
    friendship_status_id INTEGER REFERENCES friendship_status(id),
    PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS "like" (
    user_id INTEGER REFERENCES "user"(id) ON DELETE CASCADE,
    film_id INTEGER REFERENCES film(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, film_id)
);
