# Practical API Usage

The local base URL is `http://localhost:8080`. Swagger UI at
`http://localhost:8080/swagger-ui/index.html` is the interactive API reference;
this document focuses on common usage.

## Authentication

Obtain a JWT:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
```

The response contains `accessToken` and `tokenType`. Send protected requests
with `Authorization: Bearer <token>`.

## Endpoint Summary

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/auth/login` | Public | Obtain a JWT |
| `GET` | `/api/beers` | Public | List beers |
| `GET` | `/api/beers/{id}` | Public | Get one beer |
| `POST` | `/api/beers/query` | Public | Query beers |
| `POST` | `/api/beers` | Authenticated owner/admin | Create a beer |
| `PUT` | `/api/beers/{id}` | Authenticated owner/admin | Update a beer |
| `DELETE` | `/api/beers/{id}` | Authenticated owner/admin | Delete a beer |
| `POST` | `/api/beers/{id}/picture` | Authenticated owner/admin | Upload or replace picture |
| `GET` | `/api/beers/{id}/picture` | Public | Retrieve picture bytes |
| `GET` | `/api/manufacturers` | Public | List manufacturers |
| `GET` | `/api/manufacturers/{id}` | Public | Get one manufacturer |
| `POST` | `/api/manufacturers` | Admin | Create a manufacturer |
| `PUT` | `/api/manufacturers/{id}` | Authenticated owner/admin | Update manufacturer |
| `DELETE` | `/api/manufacturers/{id}` | Admin | Delete manufacturer |

## Beer Examples

List with pagination and sorting:

```bash
curl 'http://localhost:8080/api/beers?page=0&size=10&sort=name,asc'
```

Create a beer:

```bash
curl -X POST http://localhost:8080/api/beers \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{"name":"Punk IPA","abv":5.4,"type":"IPA","description":"Example","manufacturerId":1}'
```

Query beers using any combination of optional filters:

```bash
curl -X POST http://localhost:8080/api/beers/query \
  -H 'Content-Type: application/json' \
  -d '{"name":"stout","minAbv":4.0,"maxAbv":8.0,"manufacturerName":"Guinness","page":0,"size":10,"sortBy":"name","direction":"ASC"}'
```

## Manufacturer Examples

```bash
curl 'http://localhost:8080/api/manufacturers?page=0&size=10&sort=name,asc'
```

```bash
curl -X POST http://localhost:8080/api/manufacturers \
  -H 'Authorization: Bearer <admin-token>' \
  -H 'Content-Type: application/json' \
  -d '{"name":"Brewdog","countryOfOrigin":"Scotland"}'
```

Deleting a manufacturer that still has beers returns `409 Conflict`.

## Beer Pictures

Uploads accept `image/jpeg`, `image/png`, and `image/webp`, with a default
maximum size of 1 MB.

```bash
curl -X POST http://localhost:8080/api/beers/1/picture \
  -H 'Authorization: Bearer <token>' \
  -F 'file=@/path/to/picture.png'
```

Successful upload or replacement returns `204 No Content`.

```bash
curl http://localhost:8080/api/beers/1/picture --output beer-picture
```

Picture retrieval is public. Missing pictures return `404 Not Found`.

## Pagination

List endpoints accept Spring-style `page`, `size`, and `sort` query parameters.
Page numbering starts at zero. Responses use:

```json
{
  "items": [],
  "page": 0,
  "size": 10,
  "totalElements": 0,
  "totalPages": 0,
  "last": true
}
```

The beer query endpoint accepts pagination and sorting in its JSON body.

## Errors and Status Codes

Error responses use this shape:

```json
{
  "timestamp": "2026-06-13T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed",
  "path": "/api/beers",
  "fieldErrors": [
    {"field": "name", "message": "must not be blank"}
  ]
}
```

Main status codes are `200 OK`, `201 Created`, `204 No Content`,
`400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`,
`409 Conflict`, and `500 Internal Server Error`.
