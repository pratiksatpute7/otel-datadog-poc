# OpenTelemetry Spring Boot POC

A Spring Boot 3 CRUD application used to demonstrate OpenTelemetry-based tracing with Datadog runtime integration.

## What This Project Uses

- Java 17
- Spring Boot 3.2.3 (`web`, `data-jpa`, `actuator`)
- H2 in-memory database
- Micrometer OpenTelemetry bridge (`micrometer-tracing-bridge-otel`)
- OpenTelemetry Java Agent (downloaded in `Dockerfile`)
- Datadog `serverless-init` wrapper in container runtime
- Docker Compose for local orchestration

## How It Is Implemented

### Application Flow

1. API requests hit `ProductController` (`/api/v1/products`).
2. Controller delegates business logic to `ProductService`.
3. Service uses `ProductRepository` (`JpaRepository<Product, Long>`) for persistence.
4. Data is stored in in-memory H2 and seeded at startup by `DataInitializer`.

### Observability Flow

1. `java -javaagent:opentelemetry-javaagent.jar -jar app.jar` enables automatic instrumentation.
2. Spring/Micrometer tracing and OTLP settings are configured in `application.yml`.
3. Traces are exported to `http://datadog-agent:4317` (OTLP gRPC endpoint configured for runtime).
4. Container entrypoint uses Datadog `serverless-init` (`/app/datadog-init`).

End-to-end path: OpenTelemetry Java Agent auto-instruments the app, traces are sent over OTLP to the Datadog serverless wrapper runtime, and that runtime forwards telemetry to Datadog.

## Project Structure

```
otel-datadog-poc/
├── src/main/java/com/example/demo/
│   ├── DemoApplication.java
│   ├── DataInitializer.java
│   ├── controller/ProductController.java
│   ├── service/ProductService.java
│   ├── repository/ProductRepository.java
│   └── model/Product.java
├── src/main/resources/application.yml
├── Dockerfile
├── docker-compose.yml
├── start.sh
├── test-api.sh
├── README.md
├── API.md
└── DEVELOPMENT.md
```

## API Summary

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/v1/products` | List all products |
| GET | `/api/v1/products/{id}` | Get product by id |
| POST | `/api/v1/products` | Create product |
| PUT | `/api/v1/products/{id}` | Update product |
| DELETE | `/api/v1/products/{id}` | Delete product |

Detailed request/response examples: see `API.md`.

## Run the Project

### Prerequisites

- Docker + Docker Compose
- (Optional for local JVM run) Java 17, Maven 3.8+

### Option 1: Docker Compose

Create a local env file for secrets first:

```bash
cp .env.example .env
# then edit .env and set your DD_API_KEY
```

```bash
cd /Users/pratiksatpute/Developer/Projects/otel-datadog-poc
docker-compose up --build
```

Or use helper script:

```bash
./start.sh
```

### Option 2: Local JVM (without containers)

```bash
mvn clean package
java -jar target/otel-datadog-poc-1.0.0.jar
```

## Verify Locally

```bash
# Health
curl http://localhost:8080/actuator/health

# Products
curl http://localhost:8080/api/v1/products

# Run scripted CRUD checks
bash test-api.sh
```

## Runtime Configuration

### `application.yml` highlights

- App name: `otel-datadog-poc`
- Server port: `8080`
- H2 datasource: `jdbc:h2:mem:testdb`
- JPA schema: `ddl-auto: create-drop`
- Actuator exposure: `health`, `metrics`, `prometheus`
- Trace sampling: `1.0` (100%)
- OTLP tracing endpoint: `http://datadog-agent:4317`

### Docker runtime highlights

- Multi-stage build (`maven` builder + `eclipse-temurin` runtime)
- Datadog `serverless-init` copied from `datadog/serverless-init:latest`
- OpenTelemetry Java agent downloaded at build time
- App listens on `8080`

## Notes

- H2 console is enabled at `/h2-console`.
- `404` responses from product-by-id/update/delete return empty bodies.
- No request validation annotations are currently defined on `Product` request payloads.

## Additional Docs

- API contract: `API.md`
- Development details: `DEVELOPMENT.md`
