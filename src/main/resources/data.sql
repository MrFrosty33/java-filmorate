INSERT INTO rating (id, name) VALUES
(1, 'G'),
(2, 'PG'),
(3, 'PG-13'),
(4, 'R'),
(5, 'NC-17');

INSERT INTO genre (id, name) VALUES
(1, 'Комедия'),
(2, 'Драма'),
(3, 'Мультфильм'),
(4, 'Триллер'),
(5, 'Документальный'),
(6, 'Боевик');

INSERT INTO director (id, name) VALUES
(1, 'Christopher Nolan'),
(2, 'Brosisters Wachowski'),
(3, 'Daniel Kwan'),
(4, 'Daniel Scheinert');

INSERT INTO film (id, name, description, release_date, duration) VALUES
(1, 'Inception', 'Inception description', '2010-07-16', 148),
(2, 'The Matrix', 'The Matrix description', '1999-03-31', 136),
(3, 'The Dark Knight', 'The Dark Knight description', '2008-07-18', 152),
(4, 'Interstellar', 'Interstellar description', '2014-11-07', 169),
(5, 'The Prestige', 'The Prestige description','2006-10-20', 130),
(6, 'Everything Everywhere All At Once','Everything Everywhere All At Once description', '2022-03-11', 139);

INSERT INTO "user" (id, email, login, name, birthday) VALUES
(1, 'john.doe@example.com', 'johndoe', 'John Doe', '1985-02-15'),
(2, 'jane.smith@example.com', 'janesmith', 'Jane Smith', '1990-07-22'),
(3, 'alex.jones@example.com', 'alexjones', 'Alex Jones', '1983-11-10'),
(4, 'emily.white@example.com', 'emilywhite', 'Emily White', '1992-04-05'),
(5, 'michael.green@example.com', 'michaelgreen', 'Michael Green', '1980-06-30');

INSERT INTO film_genre (film_id, genre_id) VALUES
(1, 2),
(2, 6),
(3, 6),
(4, 4),
(5, 2),
(5, 4),
(6, 1),
(6, 6);

INSERT INTO film_rating (film_id, rating_id) VALUES
(1, 1),
(2, 2),
(3, 1),
(4, 1),
(5, 1),
(6, 4);

INSERT INTO film_director (film_id, director_id) VALUES
(1, 1),
(2, 2),
(3, 1),
(4, 1),
(5, 1),
(6, 3),
(6, 4);

INSERT INTO friendship_status (id, name) VALUES
(1, 'UNCONFIRMED'),
(2, 'CONFIRMED');

-- если А дружит с Б, то пока Б не отправил запрос к А, статус дружбы "unconfirmed"
INSERT INTO "friend" (user_id, friend_id, friendship_status_id) VALUES
(1, 2, 2),
(1, 3, 1),
(2, 1, 2),
(2, 3, 2),
(3, 1, 1),
(3, 2, 2),
(3, 4, 2),
(4, 3, 2),
(4, 5, 2),
(5, 4, 2);

INSERT INTO "like" (user_id, film_id) VALUES
(1, 1),
(1, 3),
(2, 2),
(2, 6),
(3, 5),
(4, 1);
