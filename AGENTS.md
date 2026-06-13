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

## Engineering guidelines

- Follow SOLID principles pragmatically and keep each class focused on one
  responsibility.
- Keep controllers thin and free of business logic; put business rules in
  application services.
- Use repositories only for persistence concerns.
- Use DTOs for API input and output; do not expose JPA entities directly.
- Use Bean Validation for requests and constructor injection for dependencies.
- Prefer records for simple DTOs and Lombok where they reduce useful
  boilerplate.
- Prefer clear names over clever abstractions.
- Avoid unnecessary interfaces unless they improve testability or architecture.
- Add focused tests for core logic.
- Keep commits small and focused.

## Performance guidelines

- Use pagination for listing endpoints and controlled request parameters for
  sorting.
- Filter searches in the database; do not filter large lists in memory.
- Use JPA Specifications for advanced beer search.
- Be aware of N+1 queries when loading Beer with Manufacturer.
- Use fetch joins or `EntityGraph` only when needed.
- Avoid logging sensitive data.
- Keep the implementation simple and measurable.

## Initially Out of Scope

- Microservices
- Kafka or asynchronous messaging
- CQRS or event sourcing
- Full OAuth2 provider setup
- AWS RDS deployment
- OData-like query parser
