# API Interface Contract: Health Check API

This contract specifies the REST endpoint for verifying system operational health.

---

## 1. System Health Status

Verify the application instance is fully operational and database connectivity is functional.

- **URL**: `/api/v1/health`
- **HTTP Method**: `GET`
- **Authentication**: None (Public Endpoint)
- **CORS Scope**: Allowed origins configured in Spring Profiles (e.g. localhost for development)

### Headers
- **Accept**: `application/json`

---

## 2. Success Response

Returned when the server is online and successfully connects to the database.

- **HTTP Status**: `200 OK`
- **Content-Type**: `application/json`
- **Response Headers**:
  - `X-Correlation-Id`: `[UUIDv4]` (Request tracing token)

### Response Body
```json
{
  "status": "UP",
  "timestamp": "2026-08-30T17:15:30.450Z",
  "database": "CONNECTED"
}
```

---

## 3. Degradation / Unhealthy Response

Returned when the server is online, but the database connection has failed or timed out.

- **HTTP Status**: `503 Service Unavailable`
- **Content-Type**: `application/json`
- **Response Headers**:
  - `X-Correlation-Id`: `[UUIDv4]`

### Response Body
```json
{
  "timestamp": "2026-08-30T17:15:30.450Z",
  "status": 503,
  "error": "SERVICE_UNAVAILABLE",
  "message": "Database connectivity failed: Connection refused",
  "path": "/api/v1/health",
  "traceId": "91a82f3c-1b7d-41a3-834c-628dcfba291d"
}
```
