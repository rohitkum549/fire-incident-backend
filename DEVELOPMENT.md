# Developer Guide: Fire Management System Backend

This guide is designed to help engineers quickly configure, run, and develop business modules in the backend repository.

---

## 1. Quickstart Development Setup

### Local Prerequisites
- **Java JDK 21**
- **Maven 3.9+**
- **Docker Engine** (For Testcontainers during verification)

### Environment File
Copy the environment template and configure local overrides:
```bash
cp .env.example .env
```
Ensure your database details are valid (e.g. localhost or Supabase dev instance).

---

## 2. Environment Profiles

The application uses Spring Profiles to load specific properties files:

- **`dev`**: Default profile for active development. Enables detailed error logs and connects to the development database (local or Supabase dev).
- **`test`**: Automatically loaded during unit/integration tests. Sets Hibernate DDL configurations and activates Testcontainers for database isolation.
- **`prod`**: Strictly enforced settings for production deployments. Disables debug logging, enforces secure headers, and requires production database credentials.

To run the application with a specific profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 3. Database Migrations (Flyway)

We manage the database schema versioning using Flyway. 

### Guidelines & Conventions
1. **No Manual Alterations**: Never alter database structures manually in any environment (including local dev).
2. **Version Files**: Place all migration scripts under `src/main/resources/db/migration/`.
3. **Naming Convention**: Use the format `V<N>__<description>.sql`, where `<N>` is a sequential integer:
   - `V1__initial_schema.sql`
   - `V2__create_users_table.sql`
4. **Immutability**: Once a migration is committed to main/git, it **MUST NOT** be modified. If an issue is found, write a new sequential migration script (e.g. `V3__fix_users_table.sql`) to apply corrections.

---

## 4. Implementation Workflow for New Domains

When implementing a new domain module (e.g. `incidents`), follow this strict layer sequence:

```text
Requirement 
   ↓
API Contract (docs)
   ↓
Flyway Database Migration
   ↓
JPA Entity (extends BaseAuditEntity)
   ↓
Spring Data JPA Repository
   ↓
Request/Response DTOs
   ↓
MapStruct Mapper interface
   ↓
Service Layer (enforces transactions & business logic)
   ↓
REST Controller (routes, validation, auth context)
   ↓
Unit / Integration Tests
```

### Example Domain Package Structure

```text
domains/incidents/
├── controller/
│   └── IncidentController.java
├── service/
│   └── IncidentService.java
├── repository/
│   └── IncidentRepository.java
├── entity/
│   └── Incident.java
├── mapper/
│   └── IncidentMapper.java
└── dto/
    ├── request/
    │   └── CreateIncidentRequest.java
    └── response/
        └── IncidentResponse.java
```

---

## 5. Testing Guidelines

A complete pull request must include tests demonstrating feature verification:

### A. Unit Tests (Mockito)
Verify isolated business rules inside services or utility beans:
```java
@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {
    @Mock private IncidentRepository repository;
    @InjectMocks private IncidentService service;

    @Test
    void shouldCreateIncident() { ... }
}
```

### B. Controller & Validation Tests (MockMvc)
Verify route binding, CORS, validation filters, exception responses, and security permissions without loading the database:
```java
@WebMvcTest(IncidentController.class)
@AutoConfigureMockMvc(addFilters = true) // Test security filters
class IncidentControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private IncidentService service;

    @Test
    void shouldRejectInvalidPayload() { ... }
}
```

### C. E2E/Persistence Integration Tests (Testcontainers)
Validate database constraints, indexing, transactions, and custom JPA queries against a real PostgreSQL instance:
```java
class IncidentRepositoryIT extends BaseIntegrationTest {
    @Autowired private IncidentRepository repository;

    @Test
    void shouldPersistAndAuditIncident() { ... }
}
```
*Note: Make sure your integration tests extend [BaseIntegrationTest](file:///home/rohit/Desktop/fire-incident-backend/src/test/java/com/company/firemanagement/BaseIntegrationTest.java).*
