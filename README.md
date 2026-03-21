# Project Backend

A Reddit-like social platform REST API built with Spring Boot. Features communities (subs), posts, comments, voting, saved items, and JWT authentication.

## Tech Stack

- **Java 21** / **Spring Boot 3.5.7**
- **MySQL 8** — primary database
- **Redis 7** — caching layer
- **Spring Security** — JWT authentication
- **MapStruct** — DTO mapping
- **SpringDoc OpenAPI** — API documentation

## Getting Started

### Prerequisites

- Java 21
- Maven 3.9+
- MySQL 8
- Redis 7

### Run with Docker

```bash
docker compose up --build
```

This starts the app on port `8080` along with MySQL and Redis.

### Run Locally

1. Ensure MySQL is running on `localhost:3306` and Redis on `localhost:6379`
2. Build and run:

```bash
./mvnw spring-boot:run
```

## API Documentation

Swagger UI is available at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) when the app is running.

## API Endpoints

### Auth (`/api/auth`)

| Method | Endpoint             | Description          | Auth |
|--------|----------------------|----------------------|------|
| POST   | `/api/auth/register` | Register a new user  | No   |
| POST   | `/api/auth/login`    | Login                | No   |
| POST   | `/api/auth/refresh`  | Refresh access token | No   |
| POST   | `/api/auth/logout`   | Logout               | Yes  |

### Users (`/api/users`)

| Method | Endpoint                | Description             | Auth |
|--------|-------------------------|-------------------------|------|
| GET    | `/api/users/{username}` | Get user profile        | No   |
| GET    | `/api/users/me`         | Get current user        | Yes  |
| POST   | `/api/users/me/password`| Change password         | Yes  |
| GET    | `/api/users/{id}/posts` | Get posts by user       | No   |

### Subs (`/api/subs`)

| Method | Endpoint                  | Description           | Auth |
|--------|---------------------------|-----------------------|------|
| POST   | `/api/subs`               | Create a sub          | Yes  |
| GET    | `/api/subs/{name}`        | Get sub details       | No   |
| PUT    | `/api/subs/{name}`        | Update sub            | Yes  |
| POST   | `/api/subs/{name}/join`   | Join a sub            | Yes  |
| DELETE | `/api/subs/{name}/leave`  | Leave a sub           | Yes  |
| GET    | `/api/subs/{name}/members`| Get sub members       | No   |
| POST   | `/api/subs/{name}/posts`  | Create post in sub    | Yes  |
| GET    | `/api/subs/{name}/posts`  | Get posts in sub      | No   |

### Posts (`/api/posts`)

| Method | Endpoint           | Description   | Auth |
|--------|--------------------|---------------|------|
| GET    | `/api/posts/{id}`  | Get post      | No   |
| PUT    | `/api/posts/{id}`  | Update post   | Yes  |
| DELETE | `/api/posts/{id}`  | Delete post   | Yes  |

### Comments

| Method | Endpoint                                  | Description       | Auth |
|--------|-------------------------------------------|-----------------  |------|
| GET    | `/api/posts/{id}/comments`                | Get comments      | No   |
| POST   | `/api/posts/{id}/comments`                | Create comment    | Yes  |
| POST   | `/api/posts/{id}/comments/{commentId}`    | Reply to comment  | Yes  |
| GET    | `/api/posts/{id}/comments/{commentId}`    | Get comment       | No   |
| PUT    | `/api/comments/{id}`                      | Update comment    | Yes  |
| DELETE | `/api/comments/{id}`                      | Delete comment    | Yes  |

### Votes

| Method | Endpoint                    | Description          | Auth |
|--------|-----------------------------|----------------------|------|
| POST   | `/api/posts/{id}/vote`      | Vote on post         | Yes  |
| DELETE | `/api/posts/{id}/vote`      | Remove post vote     | Yes  |
| POST   | `/api/comments/{id}/vote`   | Vote on comment      | Yes  |
| DELETE | `/api/comments/{id}/vote`   | Remove comment vote  | Yes  |

### Saved Items (`/api/saved`)

| Method | Endpoint                        | Description            | Auth |
|--------|---------------------------------|------------------------|------|
| GET    | `/api/saved`                    | Get saved items        | Yes  |
| POST   | `/api/saved/posts/{postId}`     | Toggle save post       | Yes  |
| POST   | `/api/saved/comments/{commentId}` | Toggle save comment  | Yes  |
| DELETE | `/api/saved/posts/{postId}`     | Unsave post            | Yes  |
| DELETE | `/api/saved/comments/{commentId}` | Unsave comment       | Yes  |

## Project Structure

```
src/main/java/minhdo/swe/project/
├── controller/     # REST controllers
├── service/        # Business logic
├── repository/     # Data access (Spring Data JPA)
├── entity/         # JPA entities
├── dto/            # Request/response DTOs
├── mapper/         # MapStruct mappers
├── security/       # JWT auth & Spring Security config
├── config/         # Redis, auditing config
└── exception/      # Global exception handling
```

## Key Features

- **JWT Authentication** — access tokens (15 min) + refresh tokens (7 days)
- **Role-based Access** — User, Moderator, Admin roles
- **Redis Caching** — posts (5 min), comments (5 min), user profiles (15 min)
- **Pagination** — paginated responses for lists
- **Voting System** — upvote/downvote on posts and comments with score tracking
- **Nested Comments** — threaded reply support
- **Saved Items** — save/unsave posts and comments
- **Input Validation** — request validation with Jakarta Validation
- **Database Indexing** — indexes on frequently queried columns