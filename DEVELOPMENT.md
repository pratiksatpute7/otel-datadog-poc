# Development Guide

This guide documents the current implementation for local development and code changes.

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker + Docker Compose (for containerized run)
- `curl` for quick endpoint checks

## Local Setup

### 1) Build and run on JVM

```bash
cd /Users/pratiksatpute/Developer/Projects/otel-datadog-poc
mvn clean package
java -jar target/otel-datadog-poc-1.0.0.jar
```

### 2) Build and run with Docker Compose

```bash
cd /Users/pratiksatpute/Developer/Projects/otel-datadog-poc
docker-compose up --build
```

or

```bash
./start.sh
```

## Implementation Overview

### Package layout

```
src/main/java/com/example/demo/
├── DemoApplication.java
├── DataInitializer.java
├── controller/ProductController.java
├── service/ProductService.java
├── repository/ProductRepository.java
└── model/Product.java
```

### Current design

- `ProductController`: REST endpoints under `/api/v1/products`
- `ProductService`: transactional create/update/delete operations
- `ProductRepository`: Spring Data JPA repository
- `Product`: JPA entity with lifecycle callbacks (`@PrePersist`, `@PreUpdate`)
- `DataInitializer`: seeds 3 products on startup

### Request handling behavior

- `GET /{id}`, `PUT /{id}`, `DELETE /{id}` return `404` with empty body when ID does not exist.
- Create/update use `@RequestBody Product` directly.
- There is no custom exception handler class currently.

## Observability Details

### Dependencies in use

- `io.micrometer:micrometer-tracing-bridge-otel`
- `io.opentelemetry:opentelemetry-api`
- `org.springframework.boot:spring-boot-starter-actuator`

### Runtime instrumentation

- Docker runtime launches app with:

```bash
java -javaagent:opentelemetry-javaagent.jar -jar app.jar
```

- Container entrypoint is Datadog `serverless-init` (`/app/datadog-init`).

Telemetry path in this project: OpenTelemetry Java Agent performs auto-instrumentation, traces are emitted via OTLP to the Datadog serverless wrapper runtime, and that runtime forwards data to Datadog.

### Configuration (`application.yml`)

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  otlp:
    tracing:
      endpoint: http://datadog-agent:4317
  metrics:
    export:
      otlp:
        enabled: true
```

Also present:

```yaml
otel:
  exporter:
    otlp:
      protocol: grpc
      endpoint: http://datadog-agent:4317
```

## Database and Actuator

- DB: H2 in-memory (`jdbc:h2:mem:testdb`)
- Schema mode: `create-drop`
- H2 console: `http://localhost:8080/h2-console`
- Actuator exposed: `health`, `metrics`, `prometheus`

## Useful Commands

```bash
# Build
mvn clean package

# Run tests
mvn test

# Run app locally
java -jar target/otel-datadog-poc-1.0.0.jar

# Run API smoke script
bash test-api.sh

# Docker logs
docker-compose logs app
docker-compose logs -f app

# Stop stack
docker-compose down
```

## How to Extend the API

1. Add method in `ProductService`.
2. Add repository query in `ProductRepository` (if needed).
3. Add endpoint mapping in `ProductController`.
4. Test with `curl` or `test-api.sh`.

## Troubleshooting

### App does not start

```bash
docker-compose logs app
```

### Health endpoint not UP

```bash
curl http://localhost:8080/actuator/health
```

### Endpoint returns 404 unexpectedly

- Verify requested product ID exists.
- Re-check seeded data by calling:

```bash
curl http://localhost:8080/api/v1/products
```

### Port conflict on 8080

- Change host mapping in `docker-compose.yml` (for example `8081:8080`) and use the updated host port for requests.
