# Quickstart & Verification Guide: Fire Management System

This guide outlines the concrete validation scenarios and commands to verify the onboarding, authentication, shift management, and incident lifecycles.

## Prerequisites
- **Runtime**: Java 17 or higher.
- **Build Tool**: Maven 3.8+.
- **Database**: PostgreSQL 15+ (configured via `.env` or system variables) or Docker (required for Testcontainers-based integration tests).
- **Environment**: A valid `.env` file containing local PostgreSQL database configurations, JWT secrets, and PORT.

## 1. Local Build and Automated Tests

Before running manual validation, execute the automated tests (which spin up Flyway migrations and run integration test scenarios using Docker-based Testcontainers):

```bash
# Clean project and compile resources
mvn clean compile

# Execute unit and integration tests (including Flyway and security validation)
mvn test
```

Expected output:
```text
[INFO] Tests run: XX, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 2. API Validation Scenarios

Once the server is running (`mvn spring-boot:run` or running the packaged jar), verify the endpoints using curl or a REST client (like Intellij HTTP Client referencing [api-test.http](../../api-test.http)).

### Scenario 2.1: Citizen Registration & Login
1. **Register citizen**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/register \
     -H "Content-Type: application/json" \
     -d '{
       "username": "citizen_jane",
       "email": "jane@example.com",
       "password": "SecurePassword123!",
       "first_name": "Jane",
       "last_name": "Doe",
       "role_names": ["ROLE_CITIZEN"]
     }'
   ```
   *Expected Response*: `201 Created` containing citizen's database ID and role array.

2. **Authenticate citizen**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "username": "citizen_jane",
       "password": "SecurePassword123!"
     }'
   ```
   *Expected Response*: `200 OK` containing a JWT `access_token`.

### Scenario 2.2: Firefighter Shift Lifecycle (Check-in & Check-out)
*Prerequisite: Register a firefighter profile associated with a valid station ID (e.g. `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11`).*

1. **Firefighter check-in**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/shifts/check-in \
     -H "Authorization: Bearer <FIREFIGHTER_JWT_TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{
       "employee_id": "d3b07384-d113-4956-b51c-a1141e7d0a22",
       "station_id": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
       "check_in_latitude": 37.7749,
       "check_in_longitude": -122.4194
     }'
   ```
   *Expected Response*: `201 Created` with shift ID (e.g. `f2c3a504-20a2-4a0b-9273-04d112d7c044`), timestamp, and active status.

2. **Firefighter check-out**:
   ```bash
   curl -X PATCH http://localhost:8080/api/v1/shifts/check-out \
     -H "Authorization: Bearer <FIREFIGHTER_JWT_TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{
       "shift_id": "f2c3a504-20a2-4a0b-9273-04d112d7c044",
       "check_out_latitude": 37.7750,
       "check_out_longitude": -122.4193
     }'
   ```
   *Expected Response*: `200 OK` showing updated status `COMPLETED` and the end timestamp.
