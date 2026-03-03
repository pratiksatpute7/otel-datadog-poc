# Java + .NET Event Workflow POC

This repository runs a distributed workflow across one Java Spring Boot service and three .NET 8 services with Azure Service Bus Emulator and OpenTelemetry tracing exported through Datadog `serverless-init`.

## Architecture at a Glance

1. Client calls Java workflow API.
2. Java calls Job Manager (`.NET`) via HTTP.
3. Job Manager publishes `JobCreated` to topic `job-created`.
4. Job Processing Worker (`.NET`) consumes `job-created`, then publishes `JobProcessed` to `job-processed`.
5. Java consumes `job-processed` and calls Pricing Service (`.NET`) via HTTP.

## Services and Ports

- Java app (`app`): `http://localhost:8080`
- Job Manager API (`job-manager`): `http://localhost:8081`
- Job Processing Worker health endpoint (`dps-worker`): `http://localhost:8082/health`
- Pricing Service API (`pricer`): `http://localhost:8083`
- Azure Service Bus Emulator (`servicebus-emulator`) + SQL Edge (`sqledge`)

## Prerequisites

- Docker
- Docker Compose (`docker compose` or `docker-compose`)

## Quick Start

1. Create env file:

```bash
cp .env.example .env
```

2. Add required values to `.env`:

```dotenv
DD_API_KEY=your_datadog_api_key_here
DD_SITE=datadoghq.com
ACCEPT_EULA=Y
SQL_PASSWORD=YourStrong(!)Password
SQL_WAIT_INTERVAL=15
EMULATOR_HTTP_PORT=5300
```

3. Start the full stack:

```bash
docker-compose up --build -d
```

Or use the helper script:

```bash
./start.sh
```

## Health Checks

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/health
curl http://localhost:8082/health
curl http://localhost:8083/health
```

## Trigger the Workflow

```bash
curl -X POST http://localhost:8080/api/v1/workflow/start \
  -H "Content-Type: application/json" \
  -d '{"jobName":"pricing-job","amount":100.0}'
```

Expected response (`202 Accepted`) from Java API:

```json
{
  "jobId": "<uuid>",
  "correlationId": "<uuid>",
  "status": "JOB_SUBMITTED"
}
```

## View Logs

```bash
docker-compose logs -f app job-manager dps-worker pricer servicebus-emulator
```

## Stop the Stack

```bash
docker-compose down
```

## Notes

- Service Bus emulator entities are defined in `infra/servicebus/Config.json`.
- Runtime connection strings use emulator mode (`UseDevelopmentEmulator=true`).
- Product CRUD endpoints are available at `/api/v1/products`.
