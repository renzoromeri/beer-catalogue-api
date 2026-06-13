# Postman Testing

## Setup

1. Start the application locally or with Docker.
2. Import both files from `api-client/postman/`.
3. Select the `Beer Catalogue Local` environment.
4. Run `Auth / Login Admin`.
5. Run `Auth / Login Guinness Manufacturer`.
6. Run `Auth / Login Heineken Manufacturer`.
7. Run `Manufacturers / Get Manufacturers`.
8. Run `Beers / Get Beers`.

The login requests store tokens in the environment. The list requests populate
manufacturer and beer IDs used by later requests. Confirm that `beerId` belongs
to the expected manufacturer before testing ownership scenarios.

## Suggested Coverage

### Public Endpoints

Run:

- `Manufacturers / Get Manufacturers`
- `Manufacturers / Get Manufacturer`
- `Beers / Get Beers`
- `Beers / Get Beer`
- `Beers / Query Beers`
- `Beers / Get Beer Picture` after uploading a picture

Successful public reads return `200 OK`.

### Admin CRUD

Use the admin token to create manufacturers and to create, update, or delete
beers. Admin create requests return `201 Created`; updates return `200 OK`;
deletes return `204 No Content`.

Manufacturer deletion returns `409 Conflict` when beers still reference the
manufacturer.

### Manufacturer Ownership

Use `guinnessToken` with Guinness IDs and `heinekenToken` with Heineken IDs.
Owners can update their manufacturer and manage their own beers. Attempting to
manage another manufacturer's resource returns `403 Forbidden`.

### Anonymous and Security Scenarios

Protected writes without a bearer token return `401 Unauthorized`. The
`Security Scenarios` folder includes anonymous upload and invalid picture
requests. Ownership violations return `403 Forbidden`; invalid picture content
returns `400 Bad Request`.

### Beer Query

The `Beers` folder includes recommended query, pagination, and sorting cases:

- `Query Beers - No Filters`
- `Query Beers - Empty Body Defaults`
- `Query Beers - Null Filters`
- `Query Beers - Filter by Type`
- `Query Beers - Filter by ABV Range`
- `Query Beers - Filter by Manufacturer`
- `Query Beers - Sort by ABV DESC`
- `Query Beers - Combined Filters`
- `Get Beers - Sort Name ASC`
- `Get Beers - Sort Name DESC`

The `Manufacturers` folder includes:

- `Get Manufacturers - Sort Name DESC`

Run these requests without authentication. Query filters may be omitted or sent
as `null`; blank `name` and `manufacturerName` values also do not apply a
filter. The `Empty Body Defaults` case sends `{}`; a truly absent HTTP body is
not valid. JSON query directions use `ASC` and `DESC`. A successful request
returns `200 OK` with a paginated response. See [API usage](API.md) for the
full query contract and valid values.

### Beer Picture Upload and Retrieval

Picture upload requests require manually selecting a local image:

1. Open an upload request.
2. Select `Body -> form-data`.
3. Keep the key named `file` with type `File`.
4. Select a local JPEG, PNG, or WebP file no larger than 1 MB.
5. Confirm `beerId` and run the request.

Key requests and expected statuses:

| Request | Expected |
| --- | --- |
| `Upload Beer Picture as Admin` | `204 No Content` |
| `Upload Beer Picture as Guinness Owner` | `204 No Content` |
| `Upload Beer Picture as Non Owner - Forbidden` | `403 Forbidden` |
| `Get Beer Picture` | `200 OK` with `Content-Type: image/*` |
| `Anonymous Upload Beer Picture - Unauthorized` | `401 Unauthorized` |
| `Upload Invalid Beer Picture - Bad Request` | `400 Bad Request` |

For the invalid-picture scenario, use the configured text form-data value or
select a `.txt` file if required by the Postman version.
