# Beer Catalogue API Guidance

## Project Goal

Build a Beer Catalogue API as a focused technical challenge.

## Stack

Java 21, Spring Boot 3.3+, Maven, Spring Web, Spring Data JPA, Spring Security,
JWT, H2, Bean Validation, Lombok, Springdoc OpenAPI, JUnit 5, Mockito, MockMvc,
Docker, Docker Compose, Kubernetes/Minikube, and Bruno.

## Architecture

Use a pragmatic modular monolith with lightweight hexagonal architecture.
Initial intended modules are `beer`, `manufacturer`, `security`, `common`, and
`config`.

## Coding Guidelines

- Keep controllers thin and put business logic in services.
- Use DTOs for API input and output, with Bean Validation for inputs.
- Prefer records for simple DTOs when appropriate.
- Use Lombok where it removes useful boilerplate.
- Use constructor injection.
- Add focused tests for core logic.
- Keep commits small and focused.

## Initially Out of Scope

- Microservices
- Kafka or asynchronous messaging
- CQRS or event sourcing
- Full OAuth2 provider setup
- AWS RDS deployment
- OData-like query parser
