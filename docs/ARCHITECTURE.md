# Architecture

## Overview

The application is a modular monolith with a pragmatic layered architecture
inspired by hexagonal architecture. It keeps feature ownership clear without
introducing microservice or framework abstraction overhead.

## Package Structure

- `beer`: beer domain, application logic, persistence, web API, and picture storage
- `manufacturer`: manufacturer domain, application logic, persistence, and web API
- `security`: authentication, JWT handling, authorization, users, and security configuration
- `common`: shared pagination, error responses, and exceptions
- `config`: application properties, OpenAPI configuration, and demo data

Feature packages use these layers where applicable:

- `domain`: framework-independent domain models and enums
- `application`: use cases, business rules, and mapping
- `application.port`: boundaries for replaceable infrastructure
- `infrastructure.persistence`: JPA entities, repositories, and specifications
- `infrastructure.storage`: external storage adapters
- `infrastructure.web`: controllers, request DTOs, response DTOs, and web mappers

## Responsibilities

Controllers are thin: they validate and map HTTP input, delegate to services,
and shape HTTP responses. Application services contain business rules,
transactions, conflict checks, ownership authorization, and orchestration.
Repositories are limited to persistence concerns.

API requests and responses use DTOs. JPA entities are not exposed directly,
which keeps persistence details out of the public contract.

## Picture Storage Port

`BeerPictureStorage` is an application port that defines storing, loading, and
deleting picture bytes. `FileSystemBeerPictureStorage` is the current adapter.
This separation allows a future object-storage adapter without changing the
controller or picture use cases.

Picture metadata is stored with the beer record; binary content is stored
outside the database. The metadata consists of the internal file name, content
type, and size. Replacement and beer deletion remove obsolete files.

The filesystem adapter is intentionally simple for evaluation. Its storage
directory is configurable, but the provided Docker and Minikube deployments do
not make it persistent. A production adapter should use object storage or
durable mounted storage.

## Security Boundaries

Spring Security authenticates requests and protects broad HTTP routes.
`OwnershipAuthorizationService` applies resource-level authorization in the
service layer. This ensures ownership rules remain active when use cases are
called from entry points other than HTTP.

## Architecture Tests

ArchUnit tests prevent domain dependencies on Spring/JPA, application
dependencies on web infrastructure, and misplaced JPA entities. These tests
turn important architectural conventions into executable checks.

## SOLID in Practice

- **Single responsibility:** controllers, services, mappers, repositories, and
  storage adapters have focused roles.
- **Open/closed and dependency inversion:** `BeerPictureStorage` permits new
  storage implementations behind a stable application contract.
- **Interface segregation:** the storage port exposes only operations required
  by picture use cases.
- **Liskov substitution:** alternative storage adapters can satisfy the same
  storage contract.

Interfaces are introduced only where they provide a meaningful replaceable
boundary; the rest of the application favors straightforward concrete classes.
