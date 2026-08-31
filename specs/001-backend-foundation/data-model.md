# Data Model & Schema Design: Backend Foundation

This document defines the core persistence models, auditing frameworks, and schema versioning standards for the Fire Management System backend.

---

## 1. Auditable Base Model (Shared Persistent Data)

To support transactional auditability across all future database tables, the system defines an abstract base entity. All subsequent business entities (e.g., `FireIncident`, `FireStation`) will extend this model.

### Key Attributes
- **id**: `UUID` (Primary Key, auto-generated using database-side UUID-v4).
- **created_at**: `Instant` (Timestamp, UTC, indexable, non-nullable). Sets the exact moment the record was inserted.
- **updated_at**: `Instant` (Timestamp, UTC, non-nullable). Sets the exact moment the record was last modified.
- **created_by**: `String` (Non-nullable). The identifier of the user/system that created the record.
- **updated_by**: `String` (Non-nullable). The identifier of the user/system that last modified the record.
- **version**: `Long` (Non-nullable). Enforces **Optimistic Locking** to resolve concurrent modification conflicts.

### Spring Data JPA Configuration
We will use Spring Data JPA Auditing via `@EntityListeners(AuditingEntityListener.class)`.
- `@CreatedBy` and `@LastModifiedBy` will hook into Spring Security's `SecurityContextHolder` to automatically fetch the authenticated user principal's identifier.
- `@CreatedDate` and `@LastModifiedDate` will automatically capture `Instant.now()`.

---

## 2. PostgreSQL Initial Schema Migration (Flyway)

Flyway migrations will manage the physical schema creation in PostgreSQL.

### Migration: `db/migration/V1__initial_schema.sql`

```sql
-- Enable UUID-OSSP extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Base auditing fields will be replicated physically in tables or inherited
-- For validation and local test endpoints, we define a simple dummy entity:
CREATE TABLE IF NOT EXISTS system_health_ping (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ping_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    status VARCHAR(50) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(255) NOT NULL DEFAULT 'SYSTEM'
);

-- Index audit timestamps and system pings for telemetry
CREATE INDEX idx_health_ping_time ON system_health_ping(ping_time);
```

---

## 3. Transient Domain Models

These models exist solely in memory and are populated statelessly from the JWT authentication filter:

### `UserPrincipal`
- **userId**: `String` (Unique ID from the JWT claims, e.g., Supabase user UUID).
- **email**: `String` (User email address from the JWT claims).
- **roles**: `Set<String>` (Roles extracted from JWT authorization claims, e.g. `ROLE_ADMIN`, `ROLE_DISPATCHER`).
