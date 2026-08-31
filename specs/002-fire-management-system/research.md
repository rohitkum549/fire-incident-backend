# Research & Design Decisions: Fire Management System

This document captures the key research, architectural trade-offs, and technology design decisions made for the global Fire Management System.

## 1. Database and Schema Migrations

### Decision: PostgreSQL + Flyway Migrations
- **Chosen Technology**: PostgreSQL 15+ with Flyway schema migration tool.
- **Rationale**: Relational integrity is paramount for regional hierarchies (Country -> State -> City -> Station). PostgreSQL provides robust support for transactional consistency, ACID properties, indexing on foreign keys, and precise numeric types (`NUMERIC(9,6)`) for geolocation coordinates. Flyway ensures version-controlled schema evolution without auto-update risk in production.
- **Alternatives Considered**: 
  - *MongoDB*: Considered for flexible document storage, but rejected because spatial relations and rigid foreign keys are required to guarantee relational hierarchy and ledger integrity.

## 2. Authentication & Access Control

### Decision: Stateless JWT Authentication with Role-Based Access Control (RBAC)
- **Chosen Technology**: Spring Security + Stateless JSON Web Tokens (JWT).
- **Rationale**: Meets the system's scalability requirement from a single station to a global setup. Stateless JWTs avoid session clustering overhead. Roles (`ROLE_CITIZEN`, `ROLE_FIREFIGHTER`, `ROLE_ADMIN`) and granular permissions are embedded directly into the token claims, enabling fast, stateless authorization checks.
- **Alternatives Considered**:
  - *Stateful Session Cookies*: Rejected due to scaling complexity across distributed regional instances.

## 3. Shift Logging & Telemetry

### Decision: Shift Audit Ledger with Lat/Long Telemetry
- **Chosen Technology**: JPA Entity Auditing + Geolocation Verification.
- **Rationale**: An immutable ledger pattern (`employee_shifts` table) registers checks-in/out. The application calculates distance between the station and the firefighter's device GPS using the Haversine formula at the business service layer to enforce geofencing. Shift states are tracked using a status field (`ACTIVE`, `COMPLETED`, `ABNORMAL`).
- **Alternatives Considered**:
  - *Real-time continuous GPS tracking (WebSockets)*: Rejected for v1 to prevent battery and network overhead; daily check-in/out telemetry is sufficient for resource availability tracking.

## 4. Integration Testing & Verification

### Decision: JUnit 5 + Testcontainers with PostgreSQL
- **Chosen Technology**: Spring Boot Test with Testcontainers.
- **Rationale**: Testcontainers spins up a real Docker container of PostgreSQL during integration testing, ensuring that Flyway migrations run and database-level triggers, unique constraints, and schema validations are verified against a real database environment rather than an in-memory H2 database.
- **Alternatives Considered**:
  - *H2 In-Memory Database*: Rejected because H2 does not perfectly match PostgreSQL's syntax, constraints, and geographic query capabilities.
