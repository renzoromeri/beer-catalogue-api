# Technical Decisions

These are lightweight working decisions and may evolve during implementation.

## Architecture and Data

- Build a modular monolith using lightweight hexagonal architecture.
- Use an H2 in-memory database initially.
- Store users and roles in the database.
- Initialize demo data with an `ApplicationRunner`.
- Use Lombok where it reduces useful boilerplate.

## Security

- Authenticate with JWT and hash passwords with BCrypt.
- Externalize `JWT_SECRET` through environment variables and a Kubernetes
  `Secret`.
- Do not implement a full OAuth2 provider initially.

## Configuration and Deployment

- Keep one `application.yml`, split into `local`, `docker`, `k8s`, and `test`
  Spring profiles.
- Use Docker and Docker Compose for containerized local execution.
- Provide Kubernetes manifests for Minikube.
- Use a `ConfigMap` for non-sensitive configuration and a `Secret` for
  sensitive technical values.
- Do not implement AWS RDS initially.

## Search and Pagination

- Listing endpoints use `page`, `size`, and `sort` query parameters.
- Advanced beer search uses `POST /api/beers/query`.

## Current Trade-offs

- Picture upload is optional if time allows.
- Prefer a focused implementation over infrastructure or query-language
  complexity.
