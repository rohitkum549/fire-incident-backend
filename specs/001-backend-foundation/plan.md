# Implementation Plan: Backend Foundation

**Branch**: `001-backend-foundation` | **Date**: 2026-08-30 | **Spec**: [spec.md](file:///home/rohit/Desktop/fire-incident-backend/specs/001-backend-foundation/spec.md)

**Input**: Feature specification from `/specs/001-backend-foundation/spec.md`

## Summary

This plan outlines the implementation of the core modular monolith backend foundation for the Fire Management System. It establishes a secure, testable, and containerized Spring Boot application featuring database integration (PostgreSQL on Supabase with Flyway), security (stateless JWT), observability (Actuator + Correlation IDs), global validation, exception handling, and robust testing standards (JUnit 5 + Testcontainers).

## Technical Context

**Language/Version**: Java 21 (LTS)

**Primary Dependencies**: Spring Boot 3.3.3, Spring Security, Spring Data JPA, Flyway Migration, MapStruct 1.5.5, Lombok 1.18.30, Springdoc OpenAPI 2.6.0

**Storage**: PostgreSQL (Supabase) + local PostgreSQL (development/testing)

**Testing**: JUnit 5, Mockito, Spring Boot Test, Testcontainers (PostgreSQL 16)

**Target Platform**: Linux Runtime / Docker Container

**Project Type**: REST Web Service

**Performance Goals**: API response time under 100ms for health check; latency under 200ms p95 for standard unauthenticated endpoints.

**Constraints**: Stateless API sessions, UTC timezone at system level, strict CORS config, no credentials in source control.

**Scale/Scope**: High-quality skeleton template with standard modular-monolith structures ready for rapid domain additions.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Principle I: Modular Monolith & Clean Architecture** — **PASS**. Package structure divides core infrastructure (`config/`, `security/`, `common/`) from business domains. Interfaces and DTOs avoid leaky database entities.
- **Principle II: Feature-Based Domain Structure** — **PASS**. Future business domains will be organized under `domains/` containing their own controller, service, repository, and DTO layers.
- **Principle III: Stateless Security & Authorization** — **PASS**. Authentication uses stateless JWT filters in Spring Security. CORS is environment-driven.
- **Principle IV: Database Integrity & Migrations** — **PASS**. Flyway is utilized. Entities mapped to DTOs.
- **Principle V: REST API Standards & Centralized Exceptions** — **PASS**. Route versioning `/api/v1/` enforced. Global exception handler returns consistent error JSON.
- **Principle VI: Progressive Observability** — **PASS**. Standard Spring Actuator liveness/readiness, correlation ID generation, and SLF4J MDC logging context.
- **Principle VII: Automated Testing & Testcontainers** — **PASS**. Database tests run against real PostgreSQL instances via Testcontainers.
- **Principle VIII: Production Readiness** — **PASS**. Configuration and secrets externalized to environment variables. Minimal multi-stage non-root Docker build.

## Project Structure

### Documentation (this feature)

```text
specs/001-backend-foundation/
├── plan.md              # This file
├── research.md          # Research & Stack Decisions
├── data-model.md        # Data models & schemas
├── quickstart.md        # Runnable verification scenarios
├── checklists/
│   └── requirements.md  # Specification Quality Checklist
└── contracts/
    └── health-api.md    # API contract for testing/health
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/com/company/firemanagement/
│   │   ├── FireManagementApplication.java
│   │   ├── config/               # Global configurations (Database, OpenAPI, WebCors)
│   │   ├── security/             # Stateless security context & JWT filters
│   │   ├── common/               # Shared utilities, validation, error formats
│   │   │   ├── exception/        # Global Exception Handler and custom exception types
│   │   │   ├── response/         # Standard response types and page models
│   │   │   └── logging/          # MdcCorrelationIdFilter and tracing utils
│   │   └── domains/              # Business domain modules
│   │       └── health/           # Health Check API domain module
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-test.yml
│       ├── application-prod.yml
│       └── db/
│           └── migration/        # Flyway schema versioning
│
└── test/
    ├── java/com/company/firemanagement/
    │   ├── common/               # Base test configurations, Testcontainers configurations
    │   └── domains/health/       # Controller & integration tests for health
    └── resources/
```

**Structure Decision**: A single Maven module structure with clear packages. Domain logic is encapsulated under `domains/<feature-name>/` to allow easy isolation, refactoring, or eventual microservice extraction.

## Complexity Tracking

*No constitution violations present. Design adheres cleanly to guidelines.*
