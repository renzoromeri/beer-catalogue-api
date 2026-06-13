# Technical Decisions and Trade-offs

## Runtime and Framework

Java 21 provides a current LTS runtime. Spring Boot 3 provides a mature,
well-supported foundation for REST APIs, validation, persistence, security,
testing, and operational configuration.

## Database

H2 keeps the challenge simple to run and evaluate without external services.
It is appropriate for the current scope but does not provide production
durability or full PostgreSQL dialect fidelity.

Local, Docker, and Minikube execution use an in-memory H2 database. Hibernate
creates the schema from JPA entities at startup, demo data is loaded for
evaluation, and all database data is lost when the application stops. The test
profile disables demo data to keep tests deterministic.

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
rows and keeps evaluation simple. Stored metadata consists of the internal file
name, content type, and size.

`BeerPictureStorage` separates the application from the current
`FileSystemBeerPictureStorage` adapter. Local filesystem storage is not
appropriate for horizontally scaled or ephemeral production workloads; object
storage or persistent volumes should replace it.

The provided Docker and Minikube configurations do not mount persistent
volumes. Uploaded pictures may be lost when a container or pod is removed or
recreated. This is an explicit trade-off for a self-contained technical
challenge. Production should use a Kubernetes PersistentVolume or object
storage such as S3 or MinIO.

## Docker and Kubernetes

The Dockerfile uses separate build and runtime stages. Docker Compose provides
a simple local container workflow. Kubernetes manifests demonstrate ConfigMap
and Secret separation, probes, and service exposure for Minikube without
introducing production platform complexity.

`k8s/secret.yaml` contains local-development placeholder values and is
committed only to make Minikube evaluation reproducible. Production secrets
must not be committed to source control. They should be supplied through CI/CD
secret injection, Kubernetes External Secrets, Sealed Secrets, AWS Secrets
Manager, HashiCorp Vault, or another secure secret manager.

The Kubernetes ConfigMap holds non-sensitive configuration, while the Secret
holds sensitive or potentially sensitive values such as JWT signing material
and database credentials.

## Generated Files

Build output, runtime picture files, and IDE metadata are intentionally kept
out of source control through `.gitignore`: `target/`, `storage/`, and
`.idea/`. These files are generated locally or at runtime and are not project
source.

## Delivery Note

The project was developed with assistance from ChatGPT and Codex. AI tools were
used for planning, implementation guidance, refactoring support, documentation
drafting, and test strategy. All suggested changes were reviewed, adapted,
executed, tested, and validated by the author. Final design decisions, code
ownership, testing, and delivery remain the responsibility of the author.
`AGENTS.md` records the project-specific architectural boundaries, engineering
conventions, and scope constraints provided to AI-assisted development tools.
