# Feature Specification: Fire Management System

**Feature Branch**: `002-fire-management-system`

**Created**: 2026-08-31

**Status**: Draft

**Input**: User description: "/speckit-specify You are a Principal Data Engineer and Enterprise Solutions Architect. I need you to design a highly scalable, flexible, and modular Fire Management System..."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Regional Jurisdiction and Station Setup (Priority: P1)

As a System Administrator, I want to establish and organize regional jurisdictions (Countries, States, Cities) and register fire stations so that they can act as the central operational anchors for staff, incidents, and equipment tracking.

**Why this priority**: It is the foundation of the entire system. Without defining the geographical regions and stations, it is impossible to assign users, equipment, or dispatch incidents correctly.

**Independent Test**: Register a hierarchy of a country, state, and city, and then create a new fire station within that city. Confirm that the fire station can be queried and successfully maps back to its parent city, state, and country.

**Acceptance Scenarios**:

1. **Given** a clean regional management database, **When** a country "USA", a state "California", a city "San Francisco", and a fire station "SFFD Station 1" are created, **Then** all entities are successfully saved with unique records.
2. **Given** a registered fire station, **When** retrieving its details, **Then** the system returns its geographical coordinates (latitude and longitude) and its correct parent city, state, and country.

---

### User Story 2 - User Onboarding and Shift Logging (Priority: P2)

As a citizen, employee, or administrator, I want to register and authenticate securely, and as an employee (firefighter), I want to record my shift check-ins and check-outs at my assigned station so that my availability and logs are maintained.

**Why this priority**: Secure authentication is required for access control. The shift ledger is critical for dispatchers to know which firefighters are active at a station to respond to incidents.

**Independent Test**: Register a firefighter user, log in to obtain credentials, check in to a fire station with GPS coordinates, and check out at the end of the shift.

**Acceptance Scenarios**:

1. **Given** a user is unregistered, **When** they submit registration details with a specified role (Citizen, Employee, Admin), **Then** their account is created and password hashed.
2. **Given** a registered user, **When** they login with valid credentials, **Then** a secure authorization token containing their role claims is returned.
3. **Given** an authenticated employee, **When** they submit a check-in request with their station ID and current coordinates, **Then** an active shift record is logged with the timestamp and coordinates.
4. **Given** an employee with an active shift record, **When** they submit a check-out request with their current coordinates, **Then** the shift record is updated with a completion status, check-out timestamp, and coordinates.

---

### User Story 3 - Incident Lifecycle Tracking (Priority: P1)

As a citizen, I want to submit a fire-related complaint, and as a dispatcher/admin, I want to escalate the complaint to an active incident, assign it to a fire station, and track it through its lifecycle (Dispatched, In-Progress, Resolved).

**Why this priority**: This represents the core business value of the system—responding to emergencies. It ensures public concerns are addressed and responder actions are tracked.

**Independent Test**: A citizen submits a fire complaint. An admin escalates it to an incident, assigns a station, changes the status to in-progress, and then resolves it.

**Acceptance Scenarios**:

1. **Given** an authenticated citizen, **When** they submit a complaint with description, category, severity, and location coordinates, **Then** the complaint is created with a status of PENDING.
2. **Given** a PENDING complaint, **When** an admin escalates it to an incident and assigns it to a fire station, **Then** an incident is created with a status of DISPATCHED and linked to that complaint.
3. **Given** a DISPATCHED or IN_PROGRESS incident, **When** the assigned firefighters update the state to RESOLVED, **Then** the incident status is updated, and the resolution timestamp is recorded.

---

### Edge Cases

- **Out-of-Geofence Check-in**: A firefighter attempts to check in to a station when their coordinates are significantly far from the station's physical address. The system should flag the shift status as "ABNORMAL" for audit review.
- **Station Deletion Integrity**: A city attempts to deactivate or remove a fire station. The system must prevent deletion if there are active incidents or shift records, ensuring historical logging data remains intact (using strict database foreign key constraint rules or soft-deactivation).
- **Overlapping/Unassigned Incident Jurisdiction**: An incident is reported at coordinates that do not map to an existing city/district boundary. The system must route it to a default regional station or mark it for manual admin assignment.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST support a geographical hierarchy (Countries, States, Cities) representing jurisdictions.
- **FR-002**: System MUST support a master Fire Station registry containing unique station profiles linked to a city and containing latitude/longitude coordinates.
- **FR-003**: System MUST support a single Users table mapping to multiple roles (Public Citizen, Field Employee/Firefighter, Station Admin) via a many-to-many relationship.
- **FR-004**: System MUST record employee shift check-in and check-out actions including timestamps, location coordinates, and status, creating an audit ledger.
- **FR-005**: System MUST allow citizens to submit complaints with coordinates, description, severity, and category.
- **FR-006**: System MUST track incident lifecycle states (Dispatched, In-Progress, Resolved, Cancelled) and link each incident to a complaint, responding station, and incident category.
- **FR-007**: System MUST maintain equipment status tracking linked to fire stations, using reference lookup tables to prevent hardcoding of categories and statuses.

### Key Entities *(include if feature involves data)*

- **Country**: A sovereign state representing the highest level of regional organization.
- **State**: A state, province, or territory within a country.
- **City**: A city, municipality, or district within a state.
- **FireStation**: A physical fire station containing coordinates, address, and status. It is the core operational connector for shifts, equipment, and incidents.
- **User**: A unified account structure representing citizens, firefighters, and admins.
- **Role**: A role (e.g., ROLE_CITIZEN, ROLE_FIREFIGHTER, ROLE_ADMIN) used in RBAC.
- **Permission**: Granular permissions (e.g., READ_INCIDENTS, MANAGE_STATIONS, WRITE_COMPLAINTS) mapped to roles.
- **EmployeeProfile**: Professional metadata for users who are firefighters or admins, establishing their home station.
- **EmployeeShift**: The shift ledger documenting check-in/out timestamps and coordinates.
- **IncidentCategory**: Lookup table defining fire/emergency classifications (e.g., Wildfire, Residential Fire).
- **Complaint**: User-reported incidents containing details and geocoordinates.
- **Incident**: The operational dispatch and response record linked to a station and complaint.
- **EquipmentStatus**: Lookup table for equipment availability states (e.g., Available, Maintenance).
- **Equipment**: Station assets (vehicles, gear) tracked with statuses.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: User registration and login requests must be processed with a response time of under 3 seconds under normal load.
- **SC-002**: Shift check-in and check-out logs must be recorded immediately, ensuring that active firefighter availability is updated in real time.
- **SC-003**: The schema design must support scaling to at least 1,000 cities and 10,000 stations with zero schema changes.
- **SC-004**: 100% of incident status updates and shift ledger logs must preserve audit logs with UTC timestamps.

## Assumptions

- **A-001**: Firefighters use mobile or web applications that have GPS capability enabled to provide coordinates during check-in/out.
- **A-002**: Geographic coordinates (latitude/longitude) use the WGS 84 coordinate reference system.
- **A-003**: The application enforces geofencing validation at the business logic layer using station coordinates.
- **A-004**: JWT tokens are stateless and contain roles and user identity claims.
