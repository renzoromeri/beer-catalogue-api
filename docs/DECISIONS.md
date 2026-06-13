# Technical Decisions and Trade-offs

## Runtime and Framework

Java 21 provides a current LTS runtime. Spring Boot 3 provides a mature,
well-supported foundation for REST APIs, validation, persistence, security,
testing, and operational configuration.

## Database

H2 keeps the challenge simple to run and evaluate without external services.
It is appropriate for the current scope but does not provide production
durability or full PostgreSQL dialect fidelity.

Manufacturer deletion is rejected while beers still reference the
manufacturer. This explicit `409 Conflict` avoids accidental cascading loss of
catalogue data.

## Authentication and Authorization

Authentication uses self-contained JWTs signed by the application, with
passwords hashed using BCrypt. This keeps local setup small while demonstrating
stateless authentication. A managed OAuth2/OIDC provider would be preferable
for production.

Spring Security protects routes, while ownership authorization is enforced in
the service layer. Resource ownership depends on loaded domain data and should
not rely only on URL-level rules.

## Picture Storage

Beer picture metadata is stored in the database and binary content is stored
on the local filesystem. This avoids storing large binary values in database
rows and keeps evaluation simple.

`BeerPictureStorage` separates the application from the current
`FileSystemBeerPictureStorage` adapter. Local filesystem storage is not
appropriate for horizontally scaled or ephemeral production workloads; object
storage or persistent volumes should replace it.

## Docker and Kubernetes

The Dockerfile uses separate build and runtime stages. Docker Compose provides
a simple local container workflow. Kubernetes manifests demonstrate ConfigMap
and Secret separation, probes, and service exposure for Minikube without
introducing production platform complexity.

Kubernetes secrets in this repository are local-development placeholders only.

## Delivery Note

ChatGPT and Codex assisted with planning, implementation guidance, refactoring,
documentation drafting, and test strategy. All suggestions were reviewed,
adapted, executed, tested, and validated by the author, who retains
responsibility for final decisions, code ownership, testing, and delivery.
