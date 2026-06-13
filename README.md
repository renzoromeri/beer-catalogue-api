# Beer Catalogue API

## Project Overview

REST API for managing beers and manufacturers, built as a focused technical
challenge. It provides public catalogue browsing, advanced beer search,
JWT-based authentication, ownership-aware authorization, beer picture
management, API documentation, and container deployment examples.

## Feature Checklist

- [x] Beer CRUD with validation and duplicate protection
- [x] Manufacturer CRUD with protected deletion when beers exist
- [x] Paginated and sorted beer and manufacturer listings
- [x] Database-backed advanced beer query
- [x] Public read endpoints and protected write endpoints
- [x] Admin and manufacturer roles with ownership authorization
- [x] Self-contained JWT authentication with BCrypt passwords
- [x] Consistent API error responses
- [x] Swagger UI and OpenAPI specification
- [x] Unit, REST integration, security, picture, and architecture tests
- [x] Docker, Docker Compose, and Kubernetes/Minikube configuration
- [x] Postman collection and local environment

## Tech Stack

Java 21, Spring Boot 3.3, Spring Web, Spring Data JPA, Spring Security, Bean
Validation, H2, JJWT, Springdoc OpenAPI, Lombok, JUnit 5, Mockito, MockMvc,
ArchUnit, Maven Wrapper, Docker, Docker Compose, and Kubernetes.

## Architecture

The application is a modular monolith using a pragmatic layered architecture
inspired by hexagonal architecture. Packages are organized by feature:
`beer`, `manufacturer`, `security`, `common`, and `config`.

Controllers remain thin, application services contain business and
authorization rules, DTOs define the HTTP contract, and JPA entities remain
inside persistence infrastructure. Beer picture storage is accessed through an
application port with a filesystem adapter.

See [Architecture](docs/ARCHITECTURE.md) and
[Technical decisions](docs/DECISIONS.md).

## Prerequisites

- Java 21
- Git
- Docker Desktop for container execution
- Optional: Minikube and `kubectl` for Kubernetes
- Optional: Postman for the provided API collection

Maven does not need to be installed because the Maven Wrapper is included.

## Quick Start

```bash
git clone https://github.com/renzoromeri/beer-catalogue-api.git
cd beer-catalogue-api
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The API starts at `http://localhost:8080` with demo data enabled.

## Run Locally

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Configuration can be overridden with environment variables documented in
[Deployment](docs/DEPLOYMENT.md).

## Run with Docker

```bash
docker compose up --build
```

Stop and remove the container:

```bash
docker compose down
```

The JWT secret in `docker-compose.yml` is a local-development placeholder only.

## Run with Kubernetes / Minikube

```bash
minikube start --driver=docker
eval $(minikube docker-env)
docker build -t beer-catalogue-api:latest .
kubectl apply -f k8s/
kubectl port-forward service/beer-catalogue-api 8080:8080
```

The values in `k8s/secret.yaml` are local-development placeholders only and
must not be used in production. Cleanup commands and troubleshooting guidance
are in [Deployment](docs/DEPLOYMENT.md).

## API Documentation

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Use `POST /api/auth/login` to obtain a token, then authorize in Swagger with
that bearer token. Practical examples are available in [API usage](docs/API.md).

## Demo Users

| Role | Username | Password |
| --- | --- | --- |
| Admin | `admin` | `admin123` |
| Guinness manufacturer | `guinness_user` | `manufacturer123` |
| Heineken manufacturer | `heineken_user` | `manufacturer123` |

These credentials are for local evaluation only.

## Postman

Import the collection and local environment from `api-client/postman/`, select
`Beer Catalogue Local`, start the application, and run the three authentication
requests. Then run `Get Manufacturers` and `Get Beers` to populate IDs used by
the remaining requests.

Beer picture uploads require manually selecting a local image in
`Body -> form-data -> file`. See the complete workflow in
[Postman testing](docs/POSTMAN.md).

## Run Tests

```bash
./mvnw clean test
```

The suite covers application services, mappings, JWT handling, database query
specifications, REST endpoints, authorization, picture storage behavior, and
architecture boundaries. See [Testing](docs/TESTING.md).

## Design Decisions and Trade-offs

H2 keeps local evaluation fast and self-contained. Authentication uses custom
stateless JWT handling instead of a full OAuth2 provider. Ownership rules are
enforced in the service layer so they apply consistently beyond HTTP routing.

Beer picture metadata is stored in the database while binary content is stored
on the local filesystem. This is suitable for evaluation, but production
deployments should replace it with object storage or persistent volumes.
Docker and Kubernetes examples intentionally remain lightweight.

See [Technical decisions and trade-offs](docs/DECISIONS.md).

## Detailed Documentation

- [Practical API usage](docs/API.md)
- [Postman testing workflow](docs/POSTMAN.md)
- [Testing strategy](docs/TESTING.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Deployment](docs/DEPLOYMENT.md)
- [Technical decisions and trade-offs](docs/DECISIONS.md)

## AI-assisted development disclosure

This project was developed with assistance from ChatGPT and Codex. AI tools
supported planning, implementation guidance, refactoring, documentation
drafting, and test strategy.
All suggested changes were reviewed, adapted, executed, tested, and validated
by the author. Final design decisions, code ownership, testing, and delivery
remain the responsibility of the author.

`AGENTS.md` defines project-specific guidance for AI-assisted development,
including architectural boundaries, engineering conventions, and scope
constraints.
