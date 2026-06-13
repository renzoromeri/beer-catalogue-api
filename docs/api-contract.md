# Planned API Contract

This contract describes the initial plan and may evolve during implementation.

## Authentication

- `POST /api/auth/login`

## Manufacturers

- `GET /api/manufacturers`
- `GET /api/manufacturers/{id}`
- `POST /api/manufacturers`
- `PUT /api/manufacturers/{id}`
- `DELETE /api/manufacturers/{id}`

## Beers

- `GET /api/beers`
- `GET /api/beers/{id}`
- `POST /api/beers`
- `PUT /api/beers/{id}`
- `DELETE /api/beers/{id}`
- `POST /api/beers/query`

## Security Rules

- Anonymous users can read.
- Manufacturer users can manage only their own manufacturer and beers.
- Admin users can manage everything.

## Planned Demo Users

| Username | Password |
| --- | --- |
| `admin` | `admin123` |
| `guinness_user` | `manufacturer123` |
| `heineken_user` | `manufacturer123` |

Demo credentials are for local and test use only.

## Search Examples

```http
GET /api/beers?page=0&size=10&sort=name,asc
GET /api/manufacturers?page=0&size=10&sort=name,asc
```

Advanced beer search:

```http
POST /api/beers/query
Content-Type: application/json

{
  "name": "stout",
  "type": "STOUT",
  "minAbv": 4.0,
  "maxAbv": 8.0,
  "manufacturerName": "Guinness",
  "page": 0,
  "size": 10,
  "sortBy": "name",
  "direction": "asc"
}
```

All advanced-search fields are optional.

## Response Strategy

- Use appropriate HTTP status codes.
- Return a standard error response.
- Return paginated responses for listing and query endpoints.
