# Feature Specification: Backend Foundation

**Feature Branch**: `001-backend-foundation`

**Created**: 2026-08-30

**Status**: Draft

**Input**: User description: "Setup a production-ready Spring Boot backend architecture and foundation for the Fire Management System, featuring Java 21, Spring Boot 3.3.x, Maven, Flyway, Supabase PostgreSQL, Spring Security, JWT, global exception handling, validation, Actuator, logging, Correlation IDs, and Docker containerization."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Developer Setup and Verification (Priority: P1)

As a developer, I want to download, configure, compile, run, and verify the backend database connectivity, migrations, and health checks locally so I can start building business features with confidence.

**Why this priority**: It is the foundation for all future work. Without a functional development environment and verification mechanism, no code can be safely developed or tested.

**Independent Test**: The project can be compiled and verified locally by running standard Maven lifecycle phases (`mvn clean verify`) and the application can run locally utilizing database migrations, responding to health requests.

**Acceptance Scenarios**:

1. **Given** a developer has cloned the repository and has Java 21 and Maven installed, **When** they run `mvn clean verify`, **Then** the build must compile successfully, and all unit/integration tests must pass.
2. **Given** the application is started locally with the `dev` profile, **When** the application initialization finishes, **Then** Flyway database migrations must run automatically, and the application must bind to port 8080.
3. **Given** the application is running, **When** the developer sends a `GET /api/v1/health` request, **Then** the application must respond with `200 OK` and a JSON response showing the server status is UP.

---

### User Story 2 - React Frontend API Integration (Stateless Auth & CORS) (Priority: P2)

As a React frontend application, I want to authenticate with the backend via stateless JWT tokens and perform CORS-approved REST requests so that I can interact securely with the API.

**Why this priority**: Modern frontend-backend systems require secure, cross-origin communication. Establishing CORS and authentication filters early ensures frontend developers can integrate easily and securely without rewriting endpoints.

**Independent Test**: Verify that requests from non-origin domains are handled according to CORS configuration, and verify that secured endpoints reject unauthorized requests while accepting valid JWT bearer tokens.

**Acceptance Scenarios**:

1. **Given** the application is running, **When** a client sends a request from a configured origin (e.g., `http://localhost:3000`), **Then** the backend must respond with the correct CORS headers (e.g., `Access-Control-Allow-Origin: http://localhost:3000`).
2. **Given** a secured endpoint (e.g., `GET /api/v1/secured-test`), **When** a request is sent without a Bearer token or with an invalid token, **Then** the API must reject it with a `401 Unauthorized` HTTP status and a standard error response.
3. **Given** a secured endpoint, **When** a request is sent with a valid Bearer token, **Then** the API must authorize the request and return a `200 OK` response with the requested resource.

---

### User Story 3 - Operations Monitoring and Health Checks (Priority: P3)

As an Operations/Platform engineer, I want to monitor the system's health, liveness, and readiness via Spring Boot Actuator endpoints so I can ensure the deployment is healthy and route traffic correctly.

**Why this priority**: Crucial for cloud and container deployments (e.g., Kubernetes liveness/readiness probes or health checks on ECS/App Runner). Prevents traffic from routing to unhealthy instances.

**Independent Test**: Fetch the Actuator health endpoints and verify that liveness and readiness states are exposed separately.

**Acceptance Scenarios**:

1. **Given** the application is running, **When** the health checker polls `/actuator/health/liveness`, **Then** the response must be `{"status": "UP"}` with `200 OK`.
2. **Given** the application is running but the database connection is lost, **When** the health checker polls `/actuator/health/readiness`, **Then** the response must be `{"status": "DOWN"}` or `{"status": "OUT_OF_SERVICE"}` with a non-200 HTTP status.

---

### Edge Cases

- **Invalid Database Connection**: If the Supabase database is down or connection details are invalid on startup, the application must shut down gracefully or report health degradation immediately (depending on profile) and not log raw database passwords.
- **Malformed JWT Headers**: If the user provides a token with an invalid signature, malformed header, or expired claims, the system must handle the exception within the Security filter chain and return a standardized JSON error instead of standard Tomcat/Spring Security raw HTML pages.
- **Request Validation Failures**: If request bodies fail validations (e.g., empty fields, invalid formats), the system must return a list of all validation errors in a single response payload with a `400 Bad Request` status.
- **CORS Violations**: If a request comes from an origin not in the allowed origins configuration, the server must block it, and in production, wildcards (`*`) must never be used as a fallback.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST version all endpoints under `/api/v1/` and return responses in JSON format.
- **FR-002**: The system MUST use Flyway to version-control the database schema, with migrations automatically executing on application startup.
- **FR-003**: The system MUST implement stateless JWT-based authentication and method-level authorization using Spring Security.
- **FR-004**: The system MUST validate incoming request payloads using Jakarta Bean Validation (`@NotNull`, `@NotBlank`, etc.) and return a consistent validation error structure.
- **FR-005**: The system MUST intercept all unhandled and handled exceptions in a global exception handler, returning a structured JSON response with a unique Correlation/Trace ID.
- **FR-006**: The system MUST expose Prometheus metrics and health check endpoints (liveness, readiness) via Spring Boot Actuator, with public access limited only to health.
- **FR-007**: The system MUST generate and propagate a unique Request/Correlation ID in the MDC logging context and response headers for every HTTP request.
- **FR-008**: The system MUST support environment-based configuration using Spring Profiles (dev, test, prod), externalizing secrets.
- **FR-009**: The system MUST containerize the application using a multi-stage Dockerfile running as a non-root user.
- **FR-010**: The system MUST support a Hybrid JWT validation strategy: validating JWTs issued by external providers (e.g. Supabase Auth) statelessly via JWKS in dev/prod profiles, and validating mock local tokens during unit/integration testing profiles.

### Key Entities *(include if feature involves data)*

- **UserPrincipal**: Represents the authenticated user extracted from the JWT token, containing user ID, roles, and permissions.
- **AuditMetadata**: Embeddable auditing entity containing `createdAt`, `updatedAt`, `createdBy`, and `updatedBy`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A new developer can build and verify the application locally with a single Maven command (`mvn clean verify`) in under 3 minutes.
- **SC-002**: The API always responds with a standardized, predictable error response structure when input validation or security authorization fails.
- **SC-003**: 100% of REST requests carry a traceable Correlation ID in both the application logs and response headers.
- **SC-004**: Integration and unit test execution passes successfully locally and in CI/CD pipelines without needing external pre-configured databases (via Testcontainers).

## Assumptions

- **ASM-001**: The PostgreSQL database is hosted on Supabase (or locally for development).
- **ASM-002**: The React frontend is developed independently and communicates via HTTP REST APIs.
- **ASM-003**: Java 21 and Spring Boot 3.3.x are used.
- **ASM-004**: Security credentials and database credentials are externalized to environment variables and never committed to source control.
