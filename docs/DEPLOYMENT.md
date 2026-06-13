# Deployment

## Required Tools

- Java 21
- Git
- Docker Desktop for Docker and Docker Compose
- Optional: Minikube and `kubectl`
- Optional: Postman

Maven is not required because the repository includes the Maven Wrapper.

## Local Execution

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The API listens on `http://localhost:8080`. Demo data is enabled by default.

## Docker Compose

```bash
docker compose up --build
```

```bash
docker compose down
```

Docker Compose builds the multi-stage Dockerfile and publishes port `8080`.
Its JWT secret is a local-development placeholder only.

## Kubernetes / Minikube

```bash
minikube start --driver=docker
eval $(minikube docker-env)
docker build -t beer-catalogue-api:latest .
kubectl apply -f k8s/
kubectl get pods
kubectl port-forward service/beer-catalogue-api 8080:8080
```

The manifests define a Deployment, ClusterIP Service, ConfigMap, Secret,
readiness probe, and liveness probe. Values in `k8s/secret.yaml` are
local-development placeholders only and must be replaced for real deployments.

Cleanup:

```bash
kubectl delete -f k8s/
eval $(minikube docker-env -u)
minikube stop
```

## Configuration

The ConfigMap contains non-sensitive runtime configuration. The Secret contains
sensitive technical values such as the JWT secret and database credentials.

| Environment variable | Default / purpose |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | Selects `local`, `docker`, `k8s`, or `test` |
| `SERVER_PORT` | `8080` |
| `JWT_SECRET` | JWT signing secret; replace outside local development |
| `JWT_EXPIRATION_MINUTES` | `60` |
| `INIT_DEMO_DATA_ENABLED` | Enables demo users and catalogue data |
| `SPRING_DATASOURCE_URL` | H2 JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `BEER_PICTURES_DIR` | Picture binary storage directory |
| `BEER_PICTURE_MAX_SIZE_BYTES` | Default `1048576` bytes |

## Picture Storage

Local execution defaults to `./storage/beer-pictures`. Docker and Kubernetes
configure `/app/storage/beer-pictures`. The provided container and Kubernetes
examples do not configure persistent storage, so picture files can be lost
when workloads are replaced. Production should use object storage or a
persistent volume.

## Troubleshooting

- Confirm Java 21 with `java -version`.
- Confirm port `8080` is free or override `SERVER_PORT`.
- Check local logs from the terminal running the application.
- Check Docker logs with `docker compose logs beer-catalogue-api`.
- Check Kubernetes state with `kubectl get pods` and
  `kubectl logs deployment/beer-catalogue-api`.
- If Minikube cannot find the image, rebuild it after
  `eval $(minikube docker-env)`.
- Verify the API with `curl http://localhost:8080/v3/api-docs`.
