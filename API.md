# API Documentation

This document reflects the current Java + .NET workflow and product APIs.

## Base URLs

- Java app: `http://localhost:8080`
- Job Manager: `http://localhost:8081`
- Job Processing Worker health: `http://localhost:8082`
- Pricing Service: `http://localhost:8083`

## Workflow API (Java)

### Start Workflow

**Request**

```http
POST /api/v1/workflow/start
Content-Type: application/json
```

**Body**

```json
{
  "jobName": "pricing-job",
  "amount": 100.0
}
```

**Response (`202 Accepted`)**

```json
{
  "jobId": "d2d95a2c-9b9d-4f24-bdb3-c2f4d2a11db5",
  "correlationId": "70284642-f8bb-48f9-a50f-171bcb1d8248",
  "status": "JOB_SUBMITTED"
}
```

## Internal .NET APIs

These endpoints are primarily called by internal services.

### Job Manager

#### Create Job

**Request**

```http
POST /api/v1/jobs
Content-Type: application/json
```

**Body**

```json
{
  "jobId": "optional-string",
  "correlationId": "optional-string",
  "jobName": "pricing-job",
  "amount": 100.0
}
```

If `jobId` or `correlationId` is missing/blank, Job Manager generates them.

**Response (`202 Accepted`)**

```json
{
  "jobId": "generated-or-provided-id",
  "correlationId": "generated-or-provided-correlation-id",
  "status": "JOB_CREATED_PUBLISHED"
}
```

#### Health

```http
GET /health
```

Response example:

```json
{
  "status": "UP"
}
```

### Pricing Service

#### Quote

**Request**

```http
POST /api/v1/pricer/quote
Content-Type: application/json
```

**Body**

```json
{
  "jobId": "d2d95a2c-9b9d-4f24-bdb3-c2f4d2a11db5",
  "correlationId": "70284642-f8bb-48f9-a50f-171bcb1d8248",
  "amount": 100.0
}
```

**Response (`200 OK`)**

```json
{
  "jobId": "d2d95a2c-9b9d-4f24-bdb3-c2f4d2a11db5",
  "correlationId": "70284642-f8bb-48f9-a50f-171bcb1d8248",
  "baseAmount": 100.0,
  "finalPrice": 118.0,
  "currency": "USD",
  "calculatedAt": "2026-03-02T11:10:55.354623+00:00"
}
```

#### Health

```http
GET /health
```

### Job Processing Worker Health

```http
GET /health
```

Response example:

```json
{
  "status": "UP"
}
```

## Event Topics

- `job-created` with subscription `dps-sub`
- `job-processed` with subscription `java-sub`

Flow:

- Job Manager publishes `JobCreated` to `job-created`.
- Job Processing Worker consumes `job-created` and publishes `JobProcessed` to `job-processed`.
- Java consumes `job-processed` and invokes Pricing Service.

---

## Product CRUD API (Java)

Base URL:

```text
http://localhost:8080/api/v1/products
```

### Data Model

| Field | Type | Notes |
|---|---|---|
| `id` | Long | Auto-generated |
| `name` | String | Non-null in DB |
| `price` | Double | Non-null in DB |
| `description` | String | Optional, max length 500 |
| `createdAt` | LocalDateTime | Set on insert |
| `updatedAt` | LocalDateTime | Set on insert/update |

### Endpoints

#### 1) Get all products

```http
GET /api/v1/products
```

Example:

```bash
curl http://localhost:8080/api/v1/products
```

Success: `200 OK`

#### 2) Get product by ID

```http
GET /api/v1/products/{id}
```

Example:

```bash
curl http://localhost:8080/api/v1/products/1
```

Success: `200 OK`  
Not found: `404 Not Found` (empty body)

#### 3) Create product

```http
POST /api/v1/products
Content-Type: application/json
```

Example:

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mechanical Keyboard",
    "price": 149.99,
    "description": "RGB mechanical keyboard with Cherry MX switches"
  }'
```

Success: `201 Created`

#### 4) Update product

```http
PUT /api/v1/products/{id}
Content-Type: application/json
```

Success: `200 OK`  
Not found: `404 Not Found` (empty body)

#### 5) Delete product

```http
DELETE /api/v1/products/{id}
```

Success: `204 No Content`  
Not found: `404 Not Found` (empty body)

## Status Codes Used

| Code | Meaning | Used By |
|---|---|---|
| `200` | OK | Product GET/PUT, Pricing Service quote, health endpoints |
| `201` | Created | Product create |
| `202` | Accepted | Workflow start, Job create |
| `204` | No Content | Product delete |
| `404` | Not Found | Product by ID/update/delete when ID does not exist |

## Validation and Error Behavior

- No explicit Bean Validation annotations are applied to product/workflow request payloads in Java controllers.
- Invalid or missing fields may surface as framework- or persistence-level errors.
- No custom global error response contract is currently defined.

## Quick Test

Run the included script:

```bash
bash test-api.sh
```

## Java Actuator Endpoints

- `GET /actuator/health`
- `GET /actuator/metrics`
- `GET /actuator/prometheus`
