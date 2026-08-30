# Tasks: Backend Foundation

**Input**: Design documents from `/specs/001-backend-foundation/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/health-api.md

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions
- **Single project**: `src/main/java/com/company/firemanagement/`, `src/test/java/com/company/firemanagement/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [ ] T001 Verify baseline Maven `pom.xml` dependencies and properties configuration in pom.xml
- [ ] T002 Configure Checkstyle and JaCoCo Maven plugins inside build plugins section of pom.xml
- [ ] T003 [P] Add checkstyle-suppressions.xml in the project root to ignore strict Javadoc style checks

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T004 Setup Flyway schema migrations file src/main/resources/db/migration/V1__initial_schema.sql
- [ ] T005 Setup JPA Auditing and DatabaseConfig in src/main/java/com/company/firemanagement/config/DatabaseConfig.java
- [ ] T006 [P] Implement CorrelationFilter servlet filter in src/main/java/com/company/firemanagement/common/logging/CorrelationFilter.java
- [ ] T007 Configure GlobalExceptionHandler for REST error mappings in src/main/java/com/company/firemanagement/common/exception/GlobalExceptionHandler.java
- [ ] T008 Configure application.yml profile settings (dev, test, prod) in src/main/resources/application.yml

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Developer Setup and Verification (Priority: P1) 🎯 MVP

**Goal**: Establish a functional local development setup with database E2E read/write validations.

**Independent Test**: Run `mvn clean verify` to ensure compilation and integration test passes, and poll `/api/v1/health` for Status UP.

### Implementation for User Story 1

- [ ] T009 [P] [US1] Create HealthCheckLog JPA Entity in src/main/java/com/company/firemanagement/domains/health/entity/HealthCheckLog.java
- [ ] T010 [P] [US1] Create HealthCheckLogRepository JPA Repository in src/main/java/com/company/firemanagement/domains/health/repository/HealthCheckLogRepository.java
- [ ] T011 [US1] Implement HealthCheckController database E2E ping endpoint in src/main/java/com/company/firemanagement/domains/health/controller/HealthCheckController.java
- [ ] T012 [US1] Write HealthCheckControllerIT test case verifying database write-read flow using Testcontainers in src/test/java/com/company/firemanagement/domains/health/controller/HealthCheckControllerIT.java

**Checkpoint**: At this point, User Story 1 is fully functional and testable independently.

---

## Phase 4: User Story 2 - React Frontend API Integration (Stateless Auth & CORS) (Priority: P2)

**Goal**: Enforce stateless token security and allow React dev/prod CORS requests.

**Independent Test**: Verify endpoints return 401 when Bearer token is missing, and verify CORS headers match permitted origins.

### Implementation for User Story 2

- [ ] T013 [P] [US2] Create UserPrincipal model representing security context in src/main/java/com/company/firemanagement/security/principal/UserPrincipal.java
- [ ] T014 [US2] Implement JwtTokenProvider parsing and validating bearer claims in src/main/java/com/company/firemanagement/security/jwt/JwtTokenProvider.java
- [ ] T015 [US2] Configure JwtAuthenticationFilter in Spring Security chain in src/main/java/com/company/firemanagement/security/jwt/JwtAuthenticationFilter.java
- [ ] T016 [US2] Configure Web CORS allowed origins mapping in src/main/java/com/company/firemanagement/config/SecurityConfig.java

**Checkpoint**: At this point, User Stories 1 AND 2 work independently.

---

## Phase 5: User Story 3 - Operations Monitoring and Health Checks (Priority: P3)

**Goal**: Expose actuator metrics, readiness, and liveness endpoints for operational orchestration.

**Independent Test**: Poll `/actuator/health/liveness` and `/actuator/health/readiness` to verify probe reporting states.

### Implementation for User Story 3

- [ ] T017 [US3] Enable Actuator Health readiness and liveness probes in src/main/resources/application.yml
- [ ] T018 [US3] Verify prometheus scraper configuration metrics endpoint in src/main/resources/application.yml and security rules

**Checkpoint**: All user stories are independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize packaging, automated compilation workflows, and developer guides.

- [ ] T019 Uncomment and complete multi-stage Docker build configurations in Dockerfile
- [ ] T020 Create GitHub Actions CI/CD workflows run verification in .github/workflows/ci-cd.yml
- [ ] T021 [P] Write ARTIFACTS documentation (ARCHITECTURE.md, DEVELOPMENT.md, SECURITY.md, API_GUIDELINES.md)
- [ ] T022 Overwrite README.md with unified developer quickstart guides in README.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories.
- **User Stories (Phase 3+)**: All depend on Foundational phase completion.
  - User stories can then proceed in parallel or sequentially (US1 → US2 → US3).
- **Polish (Final Phase)**: Depends on all desired user stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories.
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Independently testable.
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - Independently testable.

---

## Parallel Example: Setup Phase

```bash
# Developers can work on formatting configuration and dependencies in parallel:
Task: "Add checkstyle-suppressions.xml in the project root to ignore strict Javadoc style checks"
Task: "Configure Checkstyle and JaCoCo Maven plugins inside build plugins section of pom.xml"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational (blocks all stories).
3. Complete Phase 3: User Story 1.
4. **STOP and VALIDATE**: Test User Story 1 database connectivity ping locally.
5. Deploy/demo if ready.

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready.
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!).
3. Add User Story 2 → Test independently → Deploy/Demo.
4. Add User Story 3 → Test independently → Deploy/Demo.
5. Apply Phase 6 Polish (Docker, CI/CD, Documentation).
