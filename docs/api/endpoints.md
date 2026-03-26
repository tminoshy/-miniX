# API Endpoints

Base URL: `/api`

All endpoints except Auth require a valid JWT token in the `Authorization` header:
```
Authorization: Bearer <access_token>
```

---

## Auth

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/refresh` | Refresh access token |
| POST | `/api/auth/logout` | Logout (requires `Authorization` header) |

### POST `/api/auth/register`

**Request Body:**
```json
{
  "username": "string",
  "email": "string",
  "password": "string"
}
```

**Response:** `200 OK`
```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "user": {
    "id": 1,
    "username": "string",
    "email": "string",
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

### POST `/api/auth/login`

**Request Body:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Response:** `200 OK` - Same as register response.

### POST `/api/auth/refresh`

**Request Body:**
```json
{
  "refreshToken": "string"
}
```

**Response:** `200 OK` - Same as register response.

### POST `/api/auth/logout`

**Headers:** `Authorization: Bearer <access_token>`

**Response:** `200 OK`
```json
{
  "message": "Logged out successfully"
}
```

---

## Subs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/subs` | Create a new sub |
| GET | `/api/subs/{name}` | Get sub details |
| PUT | `/api/subs/{name}` | Update sub |
| POST | `/api/subs/{name}/join` | Join a sub |
| DELETE | `/api/subs/{name}/leave` | Leave a sub |
| GET | `/api/subs/{name}/members` | Get sub members (paginated) |
| POST | `/api/subs/{name}/posts` | Create a post in a sub |
| GET | `/api/subs/{name}/posts` | Get posts in a sub (paginated) |

### POST `/api/subs`

**Request Body:**
```json
{
  "name": "string (max 21 chars)",
  "description": "string",
  "iconUrl": "string"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "string",
  "description": "string",
  "iconUrl": "string",
  "createdBy": 1,
  "memberCount": 1,
  "createdAt": "2024-01-01T00:00:00"
}
```

### GET `/api/subs/{name}`

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "string",
  "description": "string",
  "iconUrl": "string",
  "createdBy": {
    "id": 1,
    "username": "string"
  },
  "memberCount": 100,
  "member": true,
  "createdAt": "2024-01-01T00:00:00"
}
```

### PUT `/api/subs/{name}`

**Request Body:**
```json
{
  "description": "string",
  "iconUrl": "string"
}
```

**Response:** `200 OK` - Same as GET sub detail response.

### POST `/api/subs/{name}/join`

**Response:** `200 OK` `"Join successfully"`

### DELETE `/api/subs/{name}/leave`

**Response:** `200 OK` `"Leave successfully"`

### GET `/api/subs/{name}/members`

**Query Parameters:** `page`, `size`, `sort`

**Response:** `200 OK` - `Page<UserProfileResponse>`
```json
{
  "content": [
    {
      "id": 1,
      "username": "string",
      "avatarUrl": "string",
      "createdAt": "2024-01-01T00:00:00"
    }
  ],
  "totalElements": 100,
  "totalPages": 10,
  "size": 10,
  "number": 0
}
```

### POST `/api/subs/{name}/posts`

**Request Body:**
```json
{
  "title": "string (max 300 chars)",
  "body": "string"
}
```

**Response:** `200 OK` - See [Post response](#post-response-format).

### GET `/api/subs/{name}/posts`

**Query Parameters:** `page`, `size`, `sort`

**Response:** `200 OK` - `Page<PostResponse>`

---

## Posts

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/posts/{id}` | Get post by ID |
| PUT | `/api/posts/{id}` | Update post |
| DELETE | `/api/posts/{id}` | Delete post |

### Post Response Format

```json
{
  "id": 1,
  "title": "string",
  "body": "string",
  "userInfo": {
    "id": 1,
    "username": "string"
  },
  "subInfo": {
    "id": 1,
    "name": "string"
  },
  "score": 10,
  "createdAt": "2024-01-01T00:00:00"
}
```

### GET `/api/posts/{id}`

**Response:** `200 OK` - Post response.

### PUT `/api/posts/{id}`

**Request Body:**
```json
{
  "title": "string (max 300 chars)",
  "body": "string"
}
```

**Response:** `200 OK` - Post response.

### DELETE `/api/posts/{id}`

**Response:** `204 No Content`

---

## Comments

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/posts/{id}/comments` | Get comments for a post (paginated) |
| POST | `/api/posts/{id}/comments` | Create a comment on a post |
| POST | `/api/posts/{id}/comments/{commentId}` | Reply to a comment |
| GET | `/api/posts/{id}/comments/{commentId}` | Get a specific comment |
| PUT | `/api/comments/{id}` | Update a comment |
| DELETE | `/api/comments/{id}` | Delete a comment |

### Comment Response Format

```json
{
  "id": 1,
  "body": "string",
  "userInfo": {
    "id": 1,
    "username": "string"
  },
  "postId": 1,
  "parentId": null,
  "score": 5,
  "createdAt": "2024-01-01T00:00:00"
}
```

### GET `/api/posts/{id}/comments`

**Query Parameters:** `page`, `size`, `sort`

**Response:** `200 OK` - `Page<CommentResponse>`

### POST `/api/posts/{id}/comments`

**Request Body:**
```json
{
  "body": "string"
}
```

**Response:** `200 OK` - Comment response.

### POST `/api/posts/{id}/comments/{commentId}`

Creates a reply to an existing comment.

**Request Body:**
```json
{
  "body": "string"
}
```

**Response:** `200 OK` - Comment response (with `parentId` set).

### GET `/api/posts/{id}/comments/{commentId}`

**Response:** `200 OK` - Comment response.

### PUT `/api/comments/{id}`

**Request Body:**
```json
{
  "body": "string"
}
```

**Response:** `200 OK` - Comment response.

### DELETE `/api/comments/{id}`

**Response:** `204 No Content`

---

## Votes

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/posts/{id}/vote` | Vote on a post |
| DELETE | `/api/posts/{id}/vote` | Remove vote from a post |
| POST | `/api/comments/{id}/vote` | Vote on a comment |
| DELETE | `/api/comments/{id}/vote` | Remove vote from a comment |

### POST `/api/posts/{id}/vote`

**Request Body:**
```json
{
  "voteType": "UPVOTE"
}
```

`voteType` can be `UPVOTE` (+1) or `DOWNVOTE` (-1).

**Response:** `200 OK` - Post response.

### DELETE `/api/posts/{id}/vote`

**Response:** `204 No Content`

### POST `/api/comments/{id}/vote`

**Request Body:**
```json
{
  "voteType": "UPVOTE"
}
```

**Response:** `200 OK` - Comment response.

### DELETE `/api/comments/{id}/vote`

**Response:** `204 No Content`

---

## Saved Items

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/saved` | Get saved items (paginated) |
| POST | `/api/saved/posts/{postId}` | Save a post |
| POST | `/api/saved/comments/{commentId}` | Save a comment |
| DELETE | `/api/saved/posts/{postId}` | Unsave a post |
| DELETE | `/api/saved/comments/{commentId}` | Unsave a comment |

### GET `/api/saved`

**Query Parameters:** `type` (optional: `POST` or `COMMENT`), `page`, `size` (default 20)

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "type": "POST",
      "post": { ... },
      "comment": null,
      "savedAt": "2024-01-01T00:00:00"
    }
  ],
  "totalElements": 50,
  "totalPages": 3,
  "size": 20,
  "number": 0
}
```

### POST `/api/saved/posts/{postId}`

**Response:** `200 OK`

### POST `/api/saved/comments/{commentId}`

**Response:** `200 OK`

### DELETE `/api/saved/posts/{postId}`

**Response:** `200 OK`

### DELETE `/api/saved/comments/{commentId}`

**Response:** `200 OK`

---

## Users

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/me` | Get current user profile |
| GET | `/api/users/{username}` | Get user profile by username |
| GET | `/api/users/{id}/posts` | Get user's posts (paginated) |
| POST | `/api/users/me/password` | Change password |

### GET `/api/users/me`

**Response:** `200 OK`
```json
{
  "id": 1,
  "username": "string",
  "email": "string",
  "avatarUrl": "string",
  "createdAt": "2024-01-01T00:00:00"
}
```

### GET `/api/users/{username}`

**Response:** `200 OK`
```json
{
  "id": 1,
  "username": "string",
  "avatarUrl": "string",
  "createdAt": "2024-01-01T00:00:00"
}
```

### GET `/api/users/{id}/posts`

**Query Parameters:** `page`, `size`, `sort`

**Response:** `200 OK` - `Page<PostResponse>`

### POST `/api/users/me/password`

**Request Body:**
```json
{
  "oldPassword": "string",
  "newPassowrd": "string"
}
```

**Response:** `200 OK` - User profile detail response.
