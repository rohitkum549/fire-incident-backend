# Backend System Architecture: Fire Management System

This document outlines the architectural patterns, request-response lifecycles, and technical boundaries governing the Fire Management System backend.

---

## 1. High-Level Design Patterns

The backend is built as a **Modular Monolith** applying **Clean Architecture** and **SOLID** principles. 
The system emphasizes strict separation of concerns, high cohesion, and low coupling:

- **Modular Monolith**: Core business domains (e.g., Users, Incidents, Stations) are organized as independent, self-contained packages under `domains/`. They communicate via clean method calls (or events), facilitating eventual extraction into microservices if scale warrants it.
- **Stateless REST**: The system has zero-session-state memory. All states are verified per request using cryptographically signed JSON Web Tokens (JWTs).
- **Database Isolation**: Direct database entity exposure is prohibited. Entities are transformed into dedicated Request/Response DTOs using compile-time MapStruct mappers before entering or leaving the API layer.

---

## 2. Technical Components Diagram

The diagram below shows the high-level architecture from the React Frontend down to PostgreSQL:

```mermaid
graph TD
    React["React Frontend"] -->|HTTPS + JWT| Security["Spring Security Filter Chain"]
    Security -->|validate JWT| TokenProvider["JwtTokenProvider"]
    Security -->|route request| Controller["Controller Layer"]
    Controller -->|DTO Validation| Service["Service Layer (Transactions)"]
    Service -->|business logic| Domain["Domain Entities"]
    Service -->|repository interface| Repository["Repository (Spring Data JPA)"]
    Repository -->|HikariCP Connection| Database["PostgreSQL Database (Supabase)"]
```

---

## 3. Package & Directory Organization

```text
src/main/java/com/company/firemanagement/
├── FireManagementApplication.java
│
├── config/                     # Shared system-wide configurations
│   ├── DatabaseConfig.java     # Auditing configuration and Transaction support
│   ├── OpenApiConfig.java     # Swagger API documentation configuration
│   └── SecurityConfig.java     # CORS, HTTP Filter rules, and OAuth2 security definitions
│
├── security/                   # Authentication and Authorization middleware
│   ├── jwt/                    # JwtAuthenticationFilter & TokenProvider
│   └── principal/              # UserPrincipal representation
│
├── common/                     # Cross-cutting system modules
│   ├── entity/                 # BaseAuditEntity
│   ├── exception/              # GlobalExceptionHandler and Error codes
│   └── logging/                # CorrelationFilter
│
└── domains/                    # Business domain packages (Modular Monolith Boundaries)
    └── health/                 # Health check domain
        ├── controller/
        ├── entity/
        └── repository/
```

---

## 4. Key Lifecycles

### A. HTTP Request & Correlation Tracking

Each incoming request is stamped with a unique Correlation/Trace ID to ensure traceability across multithreaded log structures.

```mermaid
sequenceDiagram
    participant Client as Client (React)
    participant CorrFilter as CorrelationFilter
    participant Security as Security Config
    participant Controller as REST Controller
    participant Response as Client Response

    Client->>CorrFilter: HTTP Request (Optional X-Correlation-Id)
    alt Correlation ID present in header
        CorrFilter->>CorrFilter: Extract and bind to MDC
    else Correlation ID missing
        CorrFilter->>CorrFilter: Generate new UUID and bind to MDC
    end
    CorrFilter->>Security: Forward HTTP Request
    Security->>Controller: Forward to matched Route
    Controller-->>CorrFilter: Return HTTP Response (Body/Payload)
    CorrFilter->>Response: Add 'X-Correlation-Id' to Headers & return
```

### B. Security & Authentication Flow

Standard stateless verification of incoming Bearer credentials:

```mermaid
sequenceDiagram
    participant Client as Client (React)
    participant Filter as JwtAuthenticationFilter
    participant Provider as JwtTokenProvider
    participant Context as SecurityContextHolder
    participant Endpoint as Protected Endpoint

    Client->>Filter: Request with 'Authorization: Bearer <JWT>'
    alt Authorization Header matches prefix
        Filter->>Provider: Validate token signature and expiration
        alt Token is valid
            Provider->>Provider: Parse claims (userId, email, roles)
            Provider->>Filter: Build UsernamePasswordAuthenticationToken
            Filter->>Context: Set Authentication context
            Filter->>Endpoint: Route to controller action
        else Token is invalid/expired
            Provider->>Filter: Return false
            Filter->>Client: 401 Unauthorized Response (via SecurityConfig Error Handler)
        end
    else Header missing/malformed
        Filter->>Endpoint: Bypass filter (fails later if route is secured)
    end
```

### C. Auditing & Database Flow

All persistent modifications are timestamped and tagged with the active editor context:

```mermaid
sequenceDiagram
    participant Controller as Controller
    participant Service as Service (@Transactional)
    participant Auditor as DatabaseConfig (AuditorAware)
    participant Repository as Repository
    participant DB as PostgreSQL

    Controller->>Service: Pass validated Request DTO
    Service->>Auditor: Retrieve current UserPrincipal username
    Auditor-->>Service: Return Active Username (or 'SYSTEM')
    Service->>Repository: Save Entity (extends BaseAuditEntity)
    Note over Repository,DB: Spring Data JPA inserts: <br/>createdAt, updatedAt, createdBy, updatedBy
    Repository->>DB: SQL INSERT/UPDATE with Transaction
```
