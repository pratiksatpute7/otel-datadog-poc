# API Documentation

This document reflects the current implementation in `ProductController` and `ProductService`.

## Base URL

```text
http://localhost:8080/api/v1/products
```

## Data Model

`Product` fields in API responses:

| Field | Type | Notes |
|---|---|---|
| `id` | Long | Auto-generated |
| `name` | String | Stored as non-null in DB |
| `price` | Double | Stored as non-null in DB |
| `description` | String | Optional, max DB column length 500 |
| `createdAt` | LocalDateTime | Set automatically on insert |
| `updatedAt` | LocalDateTime | Set on insert and update |

## Endpoints

### 1) Get all products

**Request**

```http
GET /api/v1/products
```

**Example**

```bash
curl http://localhost:8080/api/v1/products
```

**Success response (`200 OK`)**

```json
[
  {
    "id": 1,
    "name": "Wireless Headphones",
    "price": 79.99,
    "description": "High-quality Bluetooth wireless headphones with noise cancellation",
    "createdAt": "2026-02-18T10:30:45.123456",
    "updatedAt": "2026-02-18T10:30:45.123456"
  }
]
```

---

### 2) Get product by id

**Request**

```http
GET /api/v1/products/{id}
```

**Path params**

| Name | Type | Required |
|---|---|---|
| `id` | Long | Yes |

**Example**

```bash
curl http://localhost:8080/api/v1/products/1
```

**Success response (`200 OK`)**

```json
{
  "id": 1,
  "name": "Wireless Headphones",
  "price": 79.99,
  "description": "High-quality Bluetooth wireless headphones with noise cancellation",
  "createdAt": "2026-02-18T10:30:45.123456",
  "updatedAt": "2026-02-18T10:30:45.123456"
}
```

**Not found (`404 Not Found`)**

```text
empty body
```

---

### 3) Create product

**Request**

```http
POST /api/v1/products
Content-Type: application/json
```

**Request body**

```json
{
  "name": "Mechanical Keyboard",
  "price": 149.99,
  "description": "RGB mechanical keyboard with Cherry MX switches"
}
```

**Example**

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mechanical Keyboard",
    "price": 149.99,
    "description": "RGB mechanical keyboard with Cherry MX switches"
  }'
```

**Success response (`201 Created`)**

```json
{
  "id": 4,
  "name": "Mechanical Keyboard",
  "price": 149.99,
  "description": "RGB mechanical keyboard with Cherry MX switches",
  "createdAt": "2026-02-18T10:40:30.456789",
  "updatedAt": "2026-02-18T10:40:30.456789"
}
```

---

### 4) Update product

**Request**

```http
PUT /api/v1/products/{id}
Content-Type: application/json
```

**Path params**

| Name | Type | Required |
|---|---|---|
| `id` | Long | Yes |

**Request body**

```json
{
  "name": "Updated Mechanical Keyboard",
  "price": 159.99,
  "description": "Updated description"
}
```

**Example**

```bash
curl -X PUT http://localhost:8080/api/v1/products/4 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Mechanical Keyboard",
    "price": 159.99,
    "description": "Updated description"
  }'
```

**Success response (`200 OK`)**

Returns the updated product JSON.

**Not found (`404 Not Found`)**

```text
empty body
```

---

### 5) Delete product

**Request**

```http
DELETE /api/v1/products/{id}
```

**Path params**

| Name | Type | Required |
|---|---|---|
| `id` | Long | Yes |

**Example**

```bash
curl -X DELETE http://localhost:8080/api/v1/products/4
```

**Success response (`204 No Content`)**

```text
empty body
```

**Not found (`404 Not Found`)**

```text
empty body
```

## Status Codes Used

| Code | Meaning | Used By |
|---|---|---|
| `200` | OK | GET all, GET by id, PUT update |
| `201` | Created | POST create |
| `204` | No Content | DELETE success |
| `404` | Not Found | GET by id, PUT, DELETE when id does not exist |

## Validation and Error Behavior

- No explicit Bean Validation annotations are currently applied to request payloads.
- Invalid/missing request fields may result in framework or persistence-level errors depending on payload and DB constraints.
- This service does not currently define a custom global exception response shape.

## Quick Test

Run the included script:

```bash
bash test-api.sh
```

## Related Endpoints

- Health: `GET /actuator/health`
- Metrics: `GET /actuator/metrics`
