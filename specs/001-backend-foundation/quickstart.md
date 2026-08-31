# Quickstart Validation Guide: Backend Foundation

This guide outlines the steps required to run, test, and verify the backend foundation end-to-end.

---

## 1. Prerequisites

Before running the application, ensure the following are installed:
- **Java Development Kit (JDK) 21** (LTS)
- **Apache Maven 3.9+**
- **Docker Engine** (For running Testcontainers database integration tests)
- **HTTP Client Tool** (e.g. `curl`, Postman, or VS Code HTTP Client extension)

---

## 2. Environment Setup

Copy `.env.example` to `.env` in the project root:
```bash
cp .env.example .env
```
Fill in the database connection details. For local development, you can spin up a local PostgreSQL container or configure your Supabase development database credentials:
```env
DATABASE_URL=jdbc:postgresql://localhost:5432/fire_management
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_secure_password
JWT_ISSUER=http://localhost:8080/auth
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

---

## 3. Build & Test Commands

To compile the codebase and run all unit and integration tests (including Testcontainers-based persistence tests), execute:
```bash
mvn clean verify
```
To run the Spring Boot application locally:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 4. End-to-End Validation Scenarios

### Scenario A: Public Health API Verification
Check that the application has booted correctly and database connectivity is online.
```bash
curl -X GET http://localhost:8080/api/v1/health
```
**Expected Output**:
- **HTTP Status**: `200 OK`
- **Headers**:
  - `Content-Type: application/json`
  - `X-Correlation-Id: [UUID-format-string]`
- **Body**:
  ```json
  {
    "status": "UP",
    "timestamp": "2026-08-30T11:45:00Z",
    "database": "CONNECTED"
  }
  ```

### Scenario B: Security Authorization Check
Verify that a secured endpoint is protected by JWT validation.
```bash
curl -X GET http://localhost:8080/api/v1/secured-test
```
**Expected Output**:
- **HTTP Status**: `401 Unauthorized`
- **Body**:
  ```json
  {
    "timestamp": "2026-08-30T11:45:05Z",
    "status": 401,
    "error": "UNAUTHORIZED",
    "message": "Full authentication is required to access this resource",
    "path": "/api/v1/secured-test",
    "traceId": "some-correlation-uuid"
  }
  ```

### Scenario C: Correlation ID Log Verification
Check application logs (console/stdout) after sending the requests:
```text
2026-08-30 17:15:30.412 [correlation-id-uuid-12345] INFO  c.c.f.d.h.c.HealthController - Health check requested
```
Confirm that:
1. The logging prefix contains the matching correlation UUID enclosed in square brackets.
2. The same UUID is returned in the HTTP Response header `X-Correlation-Id`.
