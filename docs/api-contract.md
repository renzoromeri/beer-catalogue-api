# API Contract

This document summarizes the implemented API contract. Practical request
examples are available in [API usage](API.md).

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

## Demo Users

| Username | Password |
| --- | --- |
| `admin` | `admin123` |
| `guinness_user` | `manufacturer123` |
| `heineken_user` | `manufacturer123` |

Demo credentials are for local and test use only.

## Search Examples

List endpoints use Spring Pageable query parameters. Page numbering starts at
zero, and sort uses `property,asc` or `property,desc`:

```http
GET /api/beers?page=0&size=10&sort=name,asc
GET /api/beers?page=0&size=10&sort=name,desc
GET /api/beers?page=0&size=10&sort=abv,desc
GET /api/manufacturers?page=0&size=10&sort=name,asc
GET /api/manufacturers?page=0&size=10&sort=name,desc
```

All advanced beer-search filters are optional. Omitted or `null` filters are
not applied. Empty or blank `name` and `manufacturerName` values are also not
applied. `{}` is valid and uses these defaults:

- `page`: `0`
- `size`: `10`
- `sortBy`: `name`
- `direction`: `ASC`

Valid `sortBy` values are `name`, `abv`, `type`, and `manufacturerName`.
Valid JSON `direction` values are `ASC` and `DESC`.

Valid `BeerType` values are `IPA`, `LAGER`, `STOUT`, `PILSNER`, `WHEAT`,
`PALE_ALE`, `PORTER`, `SOUR`, and `OTHER`.

Advanced combined beer search:

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
  "sortBy": "abv",
  "direction": "DESC"
}
```

## Response Strategy

- Use appropriate HTTP status codes.
- Return a standard error response.
- Return paginated responses for listing and query endpoints.
