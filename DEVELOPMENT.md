# Development Guide

## Prerequisites

- Docker
- Docker Compose (`docker compose` or `docker-compose`)
- Java 17 + Maven (only needed for local Java builds outside containers)
- .NET 8 SDK (only needed for local .NET builds outside containers)

## System Overview

- Java Spring Boot app (`app`) orchestrates the workflow.
- Job Manager API (`job-manager`) publishes `JobCreated` events.
- Job Processing Worker (`dps-worker`) consumes `job-created` and publishes `job-processed`.
- Pricing Service API (`pricer`) calculates and returns a quote.
- Azure Service Bus Emulator + SQL Edge run in Docker Compose.

## Environment Setup

Create a local env file:

```bash
cp .env.example .env
```

Set these values in `.env`:

```dotenv
DD_API_KEY=your_datadog_api_key_here
DD_SITE=datadoghq.com
ACCEPT_EULA=Y
SQL_PASSWORD=YourStrong(!)Password
SQL_WAIT_INTERVAL=15
EMULATOR_HTTP_PORT=5300
```

## Run Locally with Docker Compose

```bash
docker-compose up --build -d
```

Or use:

```bash
./start.sh
```

## Service Endpoints

- Java app: `http://localhost:8080`
- Job Manager: `http://localhost:8081`
- Job Processing Worker health: `http://localhost:8082/health`
- Pricing Service: `http://localhost:8083`

## Common Commands

```bash
# Health checks
curl http://localhost:8080/actuator/health
curl http://localhost:8081/health
curl http://localhost:8082/health
curl http://localhost:8083/health

# Trigger workflow
curl -X POST http://localhost:8080/api/v1/workflow/start \
  -H "Content-Type: application/json" \
  -d '{"jobName":"pricing-job","amount":100.0}'

# Run product CRUD smoke test script
bash test-api.sh

# Follow logs
docker-compose logs -f app job-manager dps-worker pricer servicebus-emulator

# Stop stack
docker-compose down
```

## Tracing Notes

- OpenTelemetry spans are emitted by Java and .NET services.
- Azure SDK spans are enabled through `Azure.Messaging.*` activity source in .NET services.
- Service Bus tracing context is propagated through message metadata and correlation IDs.

## Service Bus Emulator Notes

- Runtime connection strings use `UseDevelopmentEmulator=true`.
- Topics/subscriptions are declared in `infra/servicebus/Config.json`:
  - `job-created` / `dps-sub`
  - `job-processed` / `java-sub`
- Emulator setup is for development/testing only.
