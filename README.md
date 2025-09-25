# Filmorate — Movie Rating Service

Filmorate is a backend service built with Java and Spring Boot that manages movies, users, and their ratings. This project demonstrates solid understanding of backend development with Java, including REST API design, layered architecture, database interaction (H2 and PostgreSQL), data validation, using Spring Boot. It also reflects experience with version control and collaborative development, as part of the functionality was implemented in a team setting. Developed as part of the Yandex Practicum - Java developer program.

---

## Technologies Used

- **Java 21** – Core language version  
- **Spring Boot** – Framework
- **H2 Database** – Lightweight, in-memory SQL database for integration testing  
- **PostgreSQL** – Relational database for production environments  
- **JUnit 5** – Framework for a few simple test cases
- **Lombok** – Library to reduce boilerplate code (e.g., getters/setters)  
- **Maven** – Build automation and dependency management  

---

## Features

- **User Management**  
  - CRUD operations
  - Add/remove friends and view mutual friends
  - Get film recommendation according on user and user's friends liked films
  - Get feed of user's actions

- **Film Management**  
  - CRUD operations 
  - Search film by title and / or director
  - Get most popular films with optional parameters, such as likes count, genre and production year
  - Get films, that liked both user and his friend
  - Add and delete like operations
 
- **Director Management**  
  - CRUD operations  

- **Genre Management**  
  - CRUD operations  

- **MPA Rating Management**  
  - CRUD operations 

---

## Project Structure
- `src/main/java/ru/yandex/practicum/filmorate/`
  - `annotation/` – Custom annotation to constraint and validate certant variables
  - `controller/` – REST controllers handling HTTP requests
  - `exception/` – Custom exceptions and Exception Handler
  - `model/` – Domain models (User, Film, etc.)
  - `service/` – Business logic layer
  - `storage/` – Data access layer (repositories) and mappers
  - `validator/` – Classes, that inherit ConstraintValidator 
  - `resources/` - Spring Boot configuration, Database schema, initial data and test data for DB

