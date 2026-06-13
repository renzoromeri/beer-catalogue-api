# Testing Strategy

Run the complete test suite with:

```bash
./mvnw clean test
```

The Maven Wrapper downloads and runs the configured Maven version, so a local
Maven installation is not required.

## Unit Tests

Focused unit tests cover beer and manufacturer services, entity/domain
mappers, ownership authorization, JWT generation and validation, and demo data
initialization. Mockito isolates collaborators where appropriate.

## REST Integration Tests

`BeerCatalogueApiIntegrationTest` starts the Spring application with the
`test` profile and exercises endpoints through MockMvc. It covers login,
public reads, protected writes, validation, not-found responses, conflicts,
pagination, and database-backed beer queries.

## Security and Authorization Tests

Tests verify that:

- Anonymous users can read but cannot write.
- Admin users can manage all resources.
- Manufacturer users can manage only their own manufacturer and beers.
- Invalid credentials return `401 Unauthorized`.
- Ownership violations return `403 Forbidden`.

## Picture Upload Tests

Integration tests use a temporary filesystem directory and cover admin and
owner uploads, public retrieval, forbidden and anonymous uploads, invalid
content types, empty and oversized files, missing beers and pictures,
replacement cleanup, and picture cleanup when a beer is deleted.

## Architecture Tests

ArchUnit rules protect key boundaries:

- Domain packages do not depend on persistence or frameworks.
- Application packages do not depend on web infrastructure.
- Domain and application code do not access web infrastructure.
- JPA entities remain in persistence infrastructure.

## H2 and Testcontainers

H2 is used for integration tests because it keeps evaluation fast,
self-contained, and deterministic while still exercising Spring Data JPA and
database-backed queries.

Testcontainers was intentionally not added for this challenge. A containerized
production-like database would improve dialect fidelity, but it would also add
Docker availability and startup requirements to the basic test workflow.
PostgreSQL with Testcontainers is a sensible future improvement.
