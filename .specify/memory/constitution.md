<!--
SYNC IMPACT REPORT
==================
Version Change: None (Initial) -> 1.0.0
Ratified Date: 2026-08-30
Last Amended Date: 2026-08-30

Core Principles Defined:
- I. Modular Monolith & Clean Architecture
- II. Feature-Based Domain Structure
- III. Stateless Security & Authorization
- IV. Database Integrity & Migrations
- V. REST API Standards & Exception Handling
- VI. Progressive Observability & Traceability
- VII. Automated Testing & Verification
- VIII. Production Readiness & Containerization

Added Sections:
- Technical Constraints & Standards (defined under Section 2)
- Development Workflow & Quality Gates (defined under Section 3)

Governance Rules Defined:
- Authority of Constitution
- Change Management Process
- Stack Version Policy
- Semantic Versioning for Governance

Follow-up TODOs:
- None
-->

# Fire Management System Constitution

## Core Principles

### I. Modular Monolith & Clean Architecture
Implement the backend as a Modular Monolith utilizing Clean Architecture principles. Every contribution must follow SOLID, DRY, KISS, Separation of Concerns, Dependency Inversion, Clean Code, Least Privilege, Fail Secure, Explicit Dependencies, High Cohesion, and Low Coupling. Abstractions must only be introduced for real value. Avoid premature microservices, Kafka, Redis, Elasticsearch, CQRS, event sourcing, or distributed transactions without explicit justification. Do not optimize for writing the most code; optimize for: Correctness → Security → Maintainability → Simplicity → Testability → Performance.

### II. Feature-Based Domain Structure
Organize business functionality primarily by feature/domain (e.g., `user/`, `fire-incident/`, `fire-station/`, etc.) rather than technical roles. Avoid giant global package structures. Controllers must be thin, only handling requests, validation, authentication context, calling services, and responses; they must not contain business logic, database queries, transactions, or complex calculations. Services coordinate use cases and transactions, avoiding giant services. Domain business logic must remain isolated from HTTP, repositories, third-party SDKs, and infrastructure. Avoid God classes and redundant interfaces.

### III. Stateless Security & Authorization
Enforce stateless REST API security via modern Spring Security and JWT validation. Centralize JWT validation. Differentiate Authentication from Authorization, applying least-privilege checks to all endpoints. CORS configurations must be environment-driven, allowing React origins in development and explicit origins in production (never wildcards `*` in production). Do not log passwords, JWTs, secrets, API keys, or Authorization headers, nor expose them in errors.

### IV. Database Integrity & Migrations
Utilize PostgreSQL (hosted on Supabase) via Spring Data JPA with Hibernate. Never use auto-update in production; schema migrations must be version-controlled via Flyway (`V<N>__<name>.sql`). Ensure referential integrity and uniqueness constraints are enforced in PostgreSQL. Map Entities to dedicated Request/Response DTOs via Mappers; never expose internal entities or database secrets directly. Apply optimistic locking, unique constraints, and transaction boundaries carefully to ensure database consistency.

### V. REST API Standards & Exception Handling
All API routes must be versioned under `/api/v1/` and follow REST conventions. Enforce input validation via Jakarta Bean Validation (`@NotNull`, `@NotBlank`, etc.) and standard API pagination (with enforced maximum page sizes) on all query lists. Exceptions must be caught by a centralized global exception handler and return a standardized error JSON structure with stable application error codes. Never expose stack traces, SQL errors, or internal implementation details.

### VI. Progressive Observability & Traceability
Rely on Spring Boot Actuator (securing operational endpoints like health, readiness, liveness, and metrics) and Micrometer for metrics. Every HTTP request must have a Correlation/Request ID propagated through the logging context (SLF4J) and response headers. Do not introduce complex distributed tracing before it is needed.

### VII. Automated Testing & Verification
Every feature must include automated test coverage (Unit, Integration, Security, Controller, and Repository tests) using JUnit 5, Mockito, and Spring Boot Test. Use Testcontainers with a PostgreSQL container for database integration tests to isolate test execution. Tests must not depend on external production databases or credentials, and migrations should run during integration tests.

### VIII. Production Readiness & Containerization
The application must be container-ready (Docker minimal runtime, multi-stage builds, non-root execution) and externalize all configuration/secrets via environment variables or secret managers across DEV, TEST, and PROD profiles. Prevent dependency bloat, ensuring every library is actively maintained and compatible.

## Technical Constraints & Standards

The application enforces strict architectural and physical boundary rules to maintain high quality:

- **Time Representation**: All system timestamps must use UTC. Prefer modern Java time APIs (`Instant`, `OffsetDateTime`, `LocalDate`, `LocalTime`) over legacy date APIs.
- **Auditing**: Support entity auditing with metadata: `createdAt`, `updatedAt`, `createdBy`, `updatedBy`. Differentiate application logging from business audit records.
- **Concurrency & Locking**: For concurrent operations, use optimistic locking, unique database constraints, and transactions instead of application-level checks to guarantee data integrity.
- **External Integration Abstractions**: Isolate third-party integrations and SDK calls behind adapters implementing clean application interfaces, allowing provider swapping without changing business logic.
- **Performance Guidelines**: Avoid premature optimizations, but actively prevent N+1 queries, unlimited result sets, and unnecessary database roundtrips.

## Development Workflow & Quality Gates

All engineering workflow steps must adhere to the following processes:

- **Development Strategy**: Build incrementally in this sequence: Project -> Package Architecture -> Configuration -> DB Connection -> Migrations -> Exceptions -> Validation -> API Standards -> Security -> JWT -> CORS -> DTOs -> Transactions -> Auditing -> Logging -> Request ID -> Actuator -> OpenAPI -> Testing -> Testcontainers -> Code Quality -> Containerization -> CI/CD.
- **Business API Development Rule**: Strictly follow the implementation flow: Requirement -> API Contract -> Security -> Database Migration -> Entity/Domain -> Repository -> Service/Application -> Validation -> Controller -> Mapper -> Response DTO -> Exception Handling -> Tests -> OpenAPI -> Logging -> Metrics/Tracing. Do not skip layers without justification.
- **Pre-Commit & Pre-Completion Checklists**: Before adding new code, verify reuse, layer assignment, and database/security impacts. Before marking work complete, verify compiling, all tests pass, security/validation is in place, no secrets are exposed, and OpenAPI docs are updated.
- **Git & Repository Hygiene**: Maintain a strict `.gitignore` to prevent committing `.env` files, credentials, secrets, target/ directories, or IDE files. Use descriptive commit messages.
- **CI/CD Pipeline Ready**: Ensure the project compiles, unit tests pass, and integration tests run successfully with commands `mvn clean test`, `mvn verify`, and `mvn package`.

## Governance

The enforcement and evolution of this document is subject to the following rules:

- **Constitution Authority**: This constitution is the permanent engineering standard for the project and supersedes all ad-hoc practices.
- **Change Management Process**: Do not silently change the established architecture. If a requirement conflicts with this constitution, identify the conflict, explain the reason, propose the minimal change, and obtain user approval.
- **Version Upgrades & Stack Compatibility**: Always prioritize the latest stable, mutually compatible versions of Java, Spring Boot, Spring Security, Hibernate, and Maven plugins. Verify compatibility before upgrading.

**Version**: 1.0.0 | **Ratified**: 2026-08-30 | **Last Amended**: 2026-08-30
