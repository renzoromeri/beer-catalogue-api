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

## Search, Filtering, Pagination, and Sorting

### List Endpoints

`GET /api/beers` and `GET /api/manufacturers` use Spring Pageable query
parameters:

- `page`: zero-based page number; default `0`
- `size`: number of items per page; default `10`
- `sort`: property and direction in the format `property,asc` or
  `property,desc`; default `name,asc`

Documented sort properties are `name`, `abv`, and `type` for beers, and `name`
and `countryOfOrigin` for manufacturers. `manufacturerName` is a supported
`sortBy` value for `POST /api/beers/query`, not a Pageable property for
`GET /api/beers`.

The direction in Pageable URL parameters is conventionally lowercase. Examples:

```http
GET /api/beers?page=0&size=10&sort=name,asc
GET /api/beers?page=0&size=10&sort=name,desc
GET /api/beers?page=0&size=10&sort=abv,desc
GET /api/manufacturers?page=0&size=10&sort=name,asc
GET /api/manufacturers?page=0&size=10&sort=name,desc
```

Equivalent curl examples:

```bash
curl 'http://localhost:8080/api/beers?page=0&size=10&sort=name,desc'
curl 'http://localhost:8080/api/manufacturers?page=0&size=10&sort=name,desc'
```

### Flexible Beer Query

`POST /api/beers/query` accepts optional filters plus pagination and sorting in
its JSON body. Filters can be combined and are applied in the database.

| Field | Behavior |
| --- | --- |
| `name` | Case-insensitive partial match |
| `type` | Exact `BeerType` match |
| `minAbv` | Inclusive minimum ABV |
| `maxAbv` | Inclusive maximum ABV |
| `manufacturerName` | Case-insensitive partial manufacturer-name match |

Every filter is optional. To avoid applying a filter, omit its field or send it
as `null`. For `name` and `manufacturerName`, an empty or blank string also
does not apply a filter.

An empty JSON object `{}` is valid and applies all defaults. A truly absent
HTTP request body is not valid because the endpoint requires a JSON body.

```json
{}
```

Query defaults:

| Field | Default |
| --- | --- |
| `page` | `0` |
| `size` | `10` |
| `sortBy` | `name` |
| `direction` | `ASC` |

Valid `sortBy` values are `name`, `abv`, `type`, and `manufacturerName`.
Valid JSON `direction` values are `ASC` and `DESC`.

Valid `BeerType` values are:

`IPA`, `LAGER`, `STOUT`, `PILSNER`, `WHEAT`, `PALE_ALE`, `PORTER`, `SOUR`,
and `OTHER`.

No filters with explicit pagination and sorting:

```json
{
  "page": 0,
  "size": 10,
  "sortBy": "name",
  "direction": "ASC"
}
```

No filters using `null`:

```json
{
  "name": null,
  "type": null,
  "minAbv": null,
  "maxAbv": null,
  "manufacturerName": null,
  "page": 0,
  "size": 10,
  "sortBy": "name",
  "direction": "ASC"
}
```

Filter by type:

```json
{
  "type": "IPA",
  "page": 0,
  "size": 10,
  "sortBy": "name",
  "direction": "ASC"
}
```

Filter by ABV range and sort descending:

```json
{
  "minAbv": 4.0,
  "maxAbv": 8.0,
  "page": 0,
  "size": 10,
  "sortBy": "abv",
  "direction": "DESC"
}
```

Filter by manufacturer:

```json
{
  "manufacturerName": "Guinness",
  "page": 0,
  "size": 10,
  "sortBy": "manufacturerName",
  "direction": "ASC"
}
```

Combined search:

```json
{
  "name": "stout",
  "type": "STOUT",
  "minAbv": 4.0,
  "maxAbv": 8.0,
  "manufacturerName": "Guinness",
  "page": 0,
  "size": 10,
  "sortBy": "abv",
  "direction": "DESC"
}
```

Run a query with curl:

```bash
curl -X POST http://localhost:8080/api/beers/query \
  -H 'Content-Type: application/json' \
  -d '{"minAbv":4.0,"maxAbv":8.0,"page":0,"size":10,"sortBy":"abv","direction":"DESC"}'
```

### Paginated Response

List and query endpoints return:

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

## Beer Examples

Create a beer:

```bash
curl -X POST http://localhost:8080/api/beers \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{"name":"Punk IPA","abv":5.4,"type":"IPA","description":"Example","manufacturerId":1}'
```

## Manufacturer Examples

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
