# Tasks: Fire Management System Core Implementation

**Input**: Design documents from `specs/002-fire-management-system/`

**Prerequisites**: [plan.md](plan.md) (required), [spec.md](spec.md) (required for user stories), [data-model.md](data-model.md), [contracts/openapi.yaml](contracts/openapi.yaml)

**Organization**: Tasks are grouped by implementation phase and user story to enable incremental development and independent testing.

## Format: `[ID] [P?] [Story] Description`
- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [x] T001 Verify project compilation and test environment stability by running `mvn clean test`
- [x] T002 Configure console and file logging patterns including Correlation ID in `src/main/resources/application.yml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core database tables, security filtering, and error handling structures that MUST be complete before user stories

- [x] T003 Create database schema migration script in `src/main/resources/db/migration/V2__fire_management_core.sql` for all modules
- [x] T004 Implement global exception handling response structures in `src/main/java/com/company/firemanagement/common/exception/GlobalExceptionHandler.java`
- [x] T005 [P] Setup JWT token generator, parser, and verifier in `src/main/java/com/company/firemanagement/security/jwt/JwtTokenProvider.java`
- [x] T006 [P] Configure security filters, encoder, and permitAll authorization routes in `src/main/java/com/company/firemanagement/config/SecurityConfig.java`

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Regional Jurisdiction and Station Setup (Priority: P1) 🎯 MVP

**Goal**: Establish regional boundaries (Countries, States, Cities) and physical Fire Stations.

**Independent Test**: Create geographical zones and register fire stations, asserting that they map correctly to parent regions and store coordinates.

### Implementation for User Story 1
- [x] T007 [P] [US1] Create entities `Country.java`, `State.java`, `City.java`, and `FireStation.java` in `src/main/java/com/company/firemanagement/domains/geography/entity/`
- [x] T008 [P] [US1] Create repository interfaces `CountryRepository.java`, `StateRepository.java`, `CityRepository.java`, and `FireStationRepository.java` in `src/main/java/com/company/firemanagement/domains/geography/repository/`
- [x] T009 [US1] Implement station creation and geographical lookup services in `src/main/java/com/company/firemanagement/domains/geography/service/FireStationService.java`
- [x] T010 [US1] Create REST mapping controller for stations and regions in `src/main/java/com/company/firemanagement/domains/geography/controller/FireStationController.java`
- [x] T011 [US1] Write Testcontainers-based integration tests verifying station joins and uniqueness constraints in `src/test/java/com/company/firemanagement/domains/geography/FireStationServiceIT.java`

**Checkpoint**: User Story 1 is functional. Fire stations can be created and queried.

---

## Phase 4: User Story 3 - Incident Lifecycle Tracking (Priority: P1)

**Goal**: Submit public complaints, escalate complaints to incidents, assign responding stations, and track active lifecycle states.

**Independent Test**: Submit a complaint, escalate it to an incident assigned to a fire station, and transition status to complete, verifying UTC timestamps and state validity.

### Implementation for User Story 3
- [x] T012 [P] [US3] Create incident entities `IncidentCategory.java`, `Complaint.java`, and `Incident.java` in `src/main/java/com/company/firemanagement/domains/operations/entity/`
- [x] T013 [P] [US3] Create repositories `IncidentCategoryRepository.java`, `ComplaintRepository.java`, and `IncidentRepository.java` in `src/main/java/com/company/firemanagement/domains/operations/repository/`
- [x] T014 [US3] Implement Complaint registration logic in `src/main/java/com/company/firemanagement/domains/operations/service/ComplaintService.java`
- [x] T015 [US3] Implement Incident escalation, assignment, and status updates logic in `src/main/java/com/company/firemanagement/domains/operations/service/IncidentService.java`
- [x] T016 [US3] Create ComplaintController and IncidentController REST mappings in `src/main/java/com/company/firemanagement/domains/operations/controller/`
- [x] T017 [US3] Write Testcontainers-based integration tests validating complaints and incident lifecycle transitions in `src/test/java/com/company/firemanagement/domains/operations/IncidentLifecycleIT.java`

**Checkpoint**: User Story 3 is functional. Complaints can be escalated to incidents and tracked to completion.

---

## Phase 5: User Story 2 - User Onboarding and Shift Logging (Priority: P2)

**Goal**: Register users (Citizens, Firefighters, Admins), authenticate via JWT, and log firefighter shifts with GPS geofencing displacement checks.

**Independent Test**: Register a firefighter, authenticate to retrieve token, check in at a station, check out, and verify shift status is COMPLETED or ABNORMAL based on coordinates displacement.

### Implementation for User Story 2
- [x] T018 [P] [US2] Create entities `User.java`, `Role.java`, `Permission.java`, and `EmployeeProfile.java` in `src/main/java/com/company/firemanagement/domains/user/entity/`
- [x] T019 [P] [US2] Create entity `EmployeeShift.java` in `src/main/java/com/company/firemanagement/domains/shift/entity/`
- [x] T020 [P] [US2] Create repositories for user profiles in `src/main/java/com/company/firemanagement/domains/user/repository/` and shift logs in `src/main/java/com/company/firemanagement/domains/shift/repository/`
- [x] T021 [US2] Implement user registration and token authentication logic in `src/main/java/com/company/firemanagement/domains/user/services/UserService.java`
- [x] T022 [US2] Implement shift check-in and check-out logic with 500m geofencing validation in `src/main/java/com/company/firemanagement/domains/shift/service/ShiftService.java`
- [x] T023 [US2] Build AuthController and ShiftController mappings in `src/main/java/com/company/firemanagement/domains/user/controller/` and `src/main/java/com/company/firemanagement/domains/shift/controller/`
- [x] T024 [US2] Write security authentication and shift geofencing integration tests in `src/test/java/com/company/firemanagement/domains/shift/ShiftManagementIT.java`

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Verify all endpoints, optimize performance, and clean up.

- [x] T025 Run complete system verification using REST endpoints via `api-test.http`
- [x] T026 Harden security configurations, ensuring passwords are never logged, and error tracebacks are suppressed in response objects
- [x] T027 Run code style analysis using checkstyle and linter checks
- [x] T028 Update project documentation and verify building using `mvn package`

---

## Dependencies & Execution Order

### Phase Dependencies
- **Setup (Phase 1)**: Can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion. Blocks all user stories.
- **User Stories (Phases 3-5)**: All depend on Foundational completion.
  - Can proceed sequentially: US1 (MVP) -> US3 -> US2.
- **Polish (Phase 6)**: Depends on all user stories being complete.

---

## Implementation Strategy

### MVP First (User Story 1 Only)
1. Complete Phase 1 and Phase 2 foundations.
2. Complete Phase 3 (User Story 1 - Regional and Station setup).
3. Validate station creation and queries via integrations tests.

### Incremental Delivery
1. Deliver Foundation ready (Phase 1 & 2).
2. Deliver US1 MVP (Phase 3).
3. Deliver US3 Operations (Phase 4).
4. Deliver US2 Auth & Shifts (Phase 5).
5. Verify E2E in Phase 6.
