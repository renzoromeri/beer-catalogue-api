# Deployment

## Required Tools

- Java 21
- Git
- Docker Desktop for Docker and Docker Compose
- Optional: Minikube and `kubectl`
- Optional: Postman

Maven is not required because the repository includes the Maven Wrapper.

## macOS/Linux vs Windows Command Reference

The setup steps are the same across operating systems, but some shell commands
use different syntax.

### Maven Wrapper

| Task | macOS/Linux | Windows PowerShell |
| --- | --- | --- |
| Run tests | `./mvnw clean test` | `.\mvnw.cmd clean test` |
| Run locally | `./mvnw spring-boot:run -Dspring-boot.run.profiles=local` | `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"` |

### Docker Compose

| Task | macOS/Linux | Windows PowerShell |
| --- | --- | --- |
| Start | `docker compose up --build` | `docker compose up --build` |
| Stop | `docker compose down` | `docker compose down` |
| Check Docker | `docker ps` | `docker ps` |

### Minikube

| Task | macOS/Linux | Windows PowerShell |
| --- | --- | --- |
| Start Minikube | `minikube start --driver=docker` | `minikube start --driver=docker` |
| Use Minikube Docker daemon | `eval $(minikube docker-env)` | `minikube docker-env \| Invoke-Expression` |
| Reset Docker daemon | `eval $(minikube docker-env -u)` | `minikube docker-env -u \| Invoke-Expression` |
| Build image | `docker build -t beer-catalogue-api:latest .` | `docker build -t beer-catalogue-api:latest .` |
| Apply manifests | `kubectl apply -f k8s/` | `kubectl apply -f k8s/` |
| Port-forward service | `kubectl port-forward service/beer-catalogue-api 8080:8080` | `kubectl port-forward service/beer-catalogue-api 8080:8080` |

### Environment Variables

| Task | macOS/Linux | Windows PowerShell |
| --- | --- | --- |
| Set variable | `export JWT_SECRET=local-secret` | `$env:JWT_SECRET="local-secret"` |

### curl

On Windows PowerShell, `curl` may be an alias for `Invoke-WebRequest`. If a
documented curl example behaves differently, use `curl.exe` explicitly:

```powershell
curl.exe -i http://localhost:8080/api/beers
```

## Local Execution

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The API listens on `http://localhost:8080`. Demo data is enabled by default.
Without configuration overrides, H2 runs in memory and pictures are written to
`./storage/beer-pictures`.

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
local-development placeholders only. The file is committed to make the
Minikube deployment reproducible for evaluation purposes.

Production secrets must not be committed to source control. They should be
provided through CI/CD secret injection, Kubernetes External Secrets, Sealed
Secrets, AWS Secrets Manager, HashiCorp Vault, or another secure secret
manager.

Cleanup:

```bash
kubectl delete -f k8s/
eval $(minikube docker-env -u)
minikube stop
```

## H2 In-Memory Database

Local, Docker, and Minikube evaluation use the H2 in-memory database. Hibernate
creates the schema from JPA entities at startup using `create-drop`. Database
data is lost when the application, container, or pod stops.

Demo users, manufacturers, and beers are loaded automatically in the local,
Docker, and Kubernetes profiles. Demo data is disabled in the `test` profile
to keep tests isolated and deterministic.

This behavior keeps evaluation self-contained. Production should use a durable
database such as PostgreSQL with schema migrations and managed backups.

## Profiles and Environment Variables

Properties declared before the profile-specific sections in
`src/main/resources/application.yml` apply to every profile. A profile-specific
section overrides only the properties it redefines.

Environment variable placeholders use `${ENV_VAR:defaultValue}`. The
application uses the environment variable when present and otherwise falls
back to the default value. For example, the YAML property:

```yaml
app:
  storage:
    beer-pictures-dir: ${BEER_PICTURES_DIR:./storage/beer-pictures}
```

means local execution without `BEER_PICTURES_DIR` uses
`./storage/beer-pictures`, while Docker and Kubernetes provide
`BEER_PICTURES_DIR=/app/storage/beer-pictures`.

## ConfigMap and Secret

The Kubernetes ConfigMap contains non-sensitive runtime configuration:

- `SPRING_PROFILES_ACTIVE`
- `SERVER_PORT`
- `SPRING_DATASOURCE_URL`
- `BEER_PICTURES_DIR`
- `BEER_PICTURE_MAX_SIZE_BYTES`

The Kubernetes Secret contains sensitive or potentially sensitive values:

- `JWT_SECRET`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

The Secret values committed in this repository are placeholders for local
Minikube evaluation only. Production values must be supplied through a secure
secret manager or CI/CD pipeline.

## Configuration Reference

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

Uploaded beer pictures are not stored as binary data in H2. H2 stores only the
internal file name, content type, and size. The binary file is stored on the
local filesystem through the picture storage adapter.

- Local default path: `./storage/beer-pictures`
- Docker and Kubernetes path: `/app/storage/beer-pictures`
- Configurable storage path: `BEER_PICTURES_DIR`
- Configurable maximum upload size: `BEER_PICTURE_MAX_SIZE_BYTES`

Docker Compose and Minikube do not configure persistent volumes. Pictures are
stored inside the container or pod filesystem and may be lost when that
container or pod is removed or recreated. This is acceptable for a
self-contained technical challenge. Production should use durable storage,
such as a Kubernetes PersistentVolume or object storage such as S3 or MinIO.

## Generated and Runtime Files

Generated build output, runtime picture files, and local IDE configuration are
not part of the repository. `.gitignore` intentionally excludes:

- `target/`
- `storage/`
- `.idea/`

Runtime-generated storage files must not be committed to Git.

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
