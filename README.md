# Beer Catalogue API

A Spring Boot backend for a Beer Catalogue API technical challenge.

## Tech Stack

- Java 21
- Spring Boot 3.3
- Maven
- Spring Web, Spring Data JPA, Spring Security, and Bean Validation
- H2
- Springdoc OpenAPI
- JJWT
- Lombok

## Current Status

Beer and manufacturer management, JWT authentication, authorization, and API
documentation are implemented.

## Run Tests

```bash
mvn clean test
```

## Run Locally

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## API Documentation

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI docs: http://localhost:8080/v3/api-docs

To authorize in Swagger:

1. Call `POST /api/auth/login`.
2. Copy the returned `accessToken`.
3. Click **Authorize**.
4. Paste the token as the Bearer token requested by the UI.

## Run with Docker Compose

The JWT secret in `docker-compose.yml` is for local development only.

```bash
./mvnw clean test
docker compose up --build
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html

## Run on Minikube

The values in `k8s/secret.yaml` are local-development placeholders only.

```bash
minikube start --driver=docker
eval $(minikube docker-env)
docker build -t beer-catalogue-api:latest .
kubectl apply -f k8s/
kubectl get pods
kubectl port-forward service/beer-catalogue-api 8080:8080
```

Test the API with `curl http://localhost:8080/api/beers`.

- Swagger UI: http://localhost:8080/swagger-ui/index.html

Cleanup:

```bash
kubectl delete -f k8s/
eval $(minikube docker-env -u)
```
