# REST API Design Guidelines: Fire Management System

This document outlines the API design rules, request-response standards, error formats, and versioning strategies for the Fire Management System REST endpoints.

---

## 1. Versioning & Routing

All public-facing API routes must start with a version prefix:
- **Default Route Prefix**: `/api/v1/`
- **Plural Resource Names**: Use plural nouns for resource representation (e.g. `/api/v1/incidents` rather than `/api/v1/incident`).
- **Path Case**: Use lower kebab-case for multiple words in URIs (e.g. `/api/v1/fire-stations`).

---

## 2. HTTP Method Semantics

Follow REST guidelines strictly for endpoint behaviors:

- **`GET`**: Retrieve a resource or collection. Must be safe and idempotent. Returns `200 OK` (or `404 Not Found`).
- **`POST`**: Create a new resource. Returns `201 Created` with the newly created resource in the body and a `Location` header.
- **`PUT`**: Replace an existing resource completely. Idempotent. Returns `200 OK` or `204 No Content`.
- **`PATCH`**: Partially update a resource. Returns `200 OK`.
- **`DELETE`**: Remove a resource. Idempotent. Returns `204 No Content`.

---

## 3. Standard HTTP Status Codes

Do not invent custom status codes. Stick to the standard definitions:

- **`200 OK`**: Request succeeded.
- **`201 Created`**: Resource created successfully.
- **`204 No Content`**: Request succeeded, no payload returned (e.g. delete actions).
- **`400 Bad Request`**: Request validation failed or payload was malformed.
- **`401 Unauthorized`**: Authentication credentials are missing or invalid.
- **`403 Forbidden`**: Authenticated but lacks required roles/permissions.
- **`404 Not Found`**: Resource does not exist.
- **`409 Conflict`**: Conflict with existing state (e.g. duplicate email, unique constraint failure).
- **`500 Internal Server Error`**: Unhandled system exception.
- **`503 Service Unavailable`**: Database or critical downstream service is offline.

---

## 4. Input Validation & Error Payloads

All incoming request payloads must be validated using **Jakarta Bean Validation** annotations (`@NotNull`, `@NotBlank`, `@Size`, `@Min`, `@Max`, `@Pattern`, `@Email`).

### Validation Error Response (HTTP 400)
When validation constraints fail, the Global Exception Handler intercepts the error and returns this structured payload:

```json
{
  "timestamp": "2026-08-30T17:15:30.125Z",
  "status": 400,
  "errorCode": "VAL_001",
  "message": "Request validation failed",
  "path": "/api/v1/fire-stations",
  "correlationId": "91a82f3c-1b7d-41a3-834c-628dcfba291d",
  "errors": [
    {
      "field": "stationName",
      "message": "Station name cannot be blank"
    },
    {
      "field": "maxCapacity",
      "message": "Max capacity must be a positive integer"
    }
  ]
}
```

---

## 5. Standard Error Format (HTTP 4xx / 5xx)
For non-validation errors (e.g. not found, unauthorized, internal errors), the payload omits the `errors` array but includes details:

```json
{
  "timestamp": "2026-08-30T17:15:30.125Z",
  "status": 404,
  "errorCode": "RES_001",
  "message": "The fire incident with ID 91a82f3c-1b7d-41a3-834c-628dcfba291d was not found",
  "path": "/api/v1/incidents/91a82f3c-1b7d-41a3-834c-628dcfba291d",
  "correlationId": "91a82f3c-1b7d-41a3-834c-628dcfba291d"
}
```

---

## 6. Pagination, Filtering, & Sorting

Querying lists of entities must use standard request parameter mappings:

- **Pagination**: Use `page` (0-indexed) and `size`.
  - Example: `?page=0&size=20`
  - *Constraint*: Default size must be `20`. The maximum allowed size must be `100` to prevent database memory exhaustion.
- **Sorting**: Format is `sort=<propertyName>,<direction>` (e.g., `asc` or `desc`).
  - Example: `?sort=createdAt,desc&sort=stationName,asc`
- **Filtering**: Use simple query parameters matching field filters.
  - Example: `/api/v1/incidents?status=ACTIVE&severity=HIGH`
- **Response Format**: Paginated results must return standard Spring `Page` structure (containing content list, page number, total pages, and total elements) to allow frontends to build pagination bars.
