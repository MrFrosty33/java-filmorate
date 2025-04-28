-- Стоит ли дополнительно добавлять NOT NULL? В логике приложения все эти случаи обрабатываются,
-- не должно возникнуть случая, когда будут переданы некорректные данные в БД.
CREATE TABLE IF NOT EXISTS genre (
    id BIGINT PRIMARY KEY,
    name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS rating (
    id BIGINT PRIMARY KEY,
    name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS film (
    id BIGINT PRIMARY KEY,
    name VARCHAR NOT NULL,
    description TEXT,
    release_date DATE NOT NULL,
    duration INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS friendship_status (
    id BIGINT PRIMARY KEY,
    name VARCHAR NOT NULL DEFAULT 'unconfirmed'
);

CREATE TABLE IF NOT EXISTS "user" (
    id BIGINT PRIMARY KEY,
    email VARCHAR NOT NULL UNIQUE,
    login VARCHAR NOT NULL UNIQUE,
    name VARCHAR,
    birthday DATE
);

CREATE TABLE IF NOT EXISTS film_genre (
    film_id BIGINT NOT NULL REFERENCES film(id) ON DELETE CASCADE,
    genre_id BIGINT NOT NULL REFERENCES genre(id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS film_rating (
    film_id BIGINT NOT NULL REFERENCES film(id) ON DELETE CASCADE,
    rating_id BIGINT NOT NULL REFERENCES rating(id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, rating_id)
);

CREATE TABLE IF NOT EXISTS "friend" (
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    friend_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    friendship_status_id BIGINT NOT NULL REFERENCES friendship_status(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS "like" (
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    film_id BIGINT NOT NULL REFERENCES film(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, film_id)
);

-- Таблица отзывов
CREATE TABLE IF NOT EXISTS reviews (
    review_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT NOT NULL,
    is_positive BOOLEAN NOT NULL,
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    film_id BIGINT NOT NULL REFERENCES film(id) ON DELETE CASCADE,
    useful INTEGER NOT NULL DEFAULT 0
);

-- Таблица лайков/дизлайков отзывов
CREATE TABLE IF NOT EXISTS review_likes (
    review_id BIGINT NOT NULL REFERENCES reviews(review_id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    is_like BOOLEAN NOT NULL,
    PRIMARY KEY (review_id, user_id)
);