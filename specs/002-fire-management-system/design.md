# Technical Design: Fire Management System Core Architecture

This document presents the core database schema design (PostgreSQL DDL) and the RESTful API contract (OpenAPI 3.0 YAML) for the Fire Management System.

---

## SECTION 1: MASTER DATABASE SCHEMA (PostgreSQL DDL)

The database schema is designed for PostgreSQL, highly normalized (3NF), and uses UUIDs as primary keys, TIMESTAMPTZ for tracking time, and NUMERIC(9,6) for geographical coordinates.

```sql
-- PostgreSQL DDL Script
-- Highly Normalized Fire Management System Core Schema

-- ==========================================
-- MODULE 1: GLOBAL GEOGRAPHY & STATIONS
-- ==========================================

CREATE TABLE countries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE NOT NULL,
    iso_code VARCHAR(3) UNIQUE NOT NULL, -- ISO 3166-1 alpha-3 (e.g., 'USA', 'CAN')
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE states (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL, -- State/Province code (e.g., 'CA', 'ON')
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_states_country FOREIGN KEY (country_id) REFERENCES countries(id) ON DELETE CASCADE,
    CONSTRAINT uq_states_country_code UNIQUE (country_id, code)
);

CREATE TABLE cities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    state_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cities_state FOREIGN KEY (state_id) REFERENCES states(id) ON DELETE CASCADE,
    CONSTRAINT uq_cities_state_name UNIQUE (state_id, name)
);

CREATE TABLE fire_stations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    city_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    address TEXT NOT NULL,
    latitude NUMERIC(9, 6) NOT NULL CHECK (latitude BETWEEN -90.0 AND 90.0),
    longitude NUMERIC(9, 6) NOT NULL CHECK (longitude BETWEEN -180.0 AND 180.0),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fire_stations_city FOREIGN KEY (city_id) REFERENCES cities(id) ON DELETE RESTRICT
);

-- ==========================================
-- MODULE 2: USER & ACCESS MANAGEMENT (RBAC)
-- ==========================================

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(30),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) UNIQUE NOT NULL, -- e.g., 'ROLE_CITIZEN', 'ROLE_FIREFIGHTER', 'ROLE_ADMIN'
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE NOT NULL, -- e.g., 'READ_INCIDENTS', 'CREATE_COMPLAINTS', 'MANAGE_STATIONS'
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- employee_profiles connects firefighters/admins to their home station
CREATE TABLE employee_profiles (
    user_id UUID PRIMARY KEY,
    station_id UUID NOT NULL,
    employee_code VARCHAR(50) UNIQUE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_employee_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_employee_profiles_station FOREIGN KEY (station_id) REFERENCES fire_stations(id) ON DELETE RESTRICT
);

CREATE TABLE employee_shifts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL,
    station_id UUID NOT NULL,
    check_in_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    check_out_time TIMESTAMPTZ,
    check_in_latitude NUMERIC(9, 6) NOT NULL CHECK (check_in_latitude BETWEEN -90.0 AND 90.0),
    check_in_longitude NUMERIC(9, 6) NOT NULL CHECK (check_in_longitude BETWEEN -180.0 AND 180.0),
    check_out_latitude NUMERIC(9, 6) CHECK (check_out_latitude BETWEEN -90.0 AND 90.0),
    check_out_longitude NUMERIC(9, 6) CHECK (check_out_longitude BETWEEN -180.0 AND 180.0),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'ABNORMAL')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_employee_shifts_employee FOREIGN KEY (employee_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_employee_shifts_station FOREIGN KEY (station_id) REFERENCES fire_stations(id) ON DELETE RESTRICT
);

-- ==========================================
-- MODULE 3: INCIDENT & COMPLAINT MANAGEMENT
-- ==========================================

CREATE TABLE incident_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE NOT NULL, -- e.g., 'RESIDENTIAL_FIRE', 'WILDFIRE', 'HAZMAT', 'MEDICAL_EMERGENCY'
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE complaints (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID NOT NULL,
    category_id UUID NOT NULL,
    latitude NUMERIC(9, 6) NOT NULL CHECK (latitude BETWEEN -90.0 AND 90.0),
    longitude NUMERIC(9, 6) NOT NULL CHECK (longitude BETWEEN -180.0 AND 180.0),
    severity VARCHAR(50) NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_complaints_reporter FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_complaints_category FOREIGN KEY (category_id) REFERENCES incident_categories(id) ON DELETE RESTRICT
);

CREATE TABLE incidents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    complaint_id UUID UNIQUE, -- 1-to-1 relationship with escalating complaint
    station_id UUID NOT NULL,
    category_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DISPATCHED' CHECK (status IN ('DISPATCHED', 'IN_PROGRESS', 'RESOLVED', 'CANCELLED')),
    severity VARCHAR(50) NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    latitude NUMERIC(9, 6) NOT NULL CHECK (latitude BETWEEN -90.0 AND 90.0),
    longitude NUMERIC(9, 6) NOT NULL CHECK (longitude BETWEEN -180.0 AND 180.0),
    dispatched_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMPTZ,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_incidents_complaint FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE SET NULL,
    CONSTRAINT fk_incidents_station FOREIGN KEY (station_id) REFERENCES fire_stations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_incidents_category FOREIGN KEY (category_id) REFERENCES incident_categories(id) ON DELETE RESTRICT
);

CREATE TABLE equipment_statuses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    status_name VARCHAR(50) UNIQUE NOT NULL, -- e.g., 'AVAILABLE', 'IN_USE', 'MAINTENANCE', 'OUT_OF_SERVICE'
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE equipment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    station_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL, -- e.g., 'Pumper Truck 4', 'Ladder Truck 2'
    type VARCHAR(100) NOT NULL, -- e.g., 'TRUCK', 'PUMP', 'CONTAINMENT_GEAR'
    status_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_equipment_station FOREIGN KEY (station_id) REFERENCES fire_stations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_equipment_status FOREIGN KEY (status_id) REFERENCES equipment_statuses(id) ON DELETE RESTRICT
);

-- ==========================================
-- INDEXES FOR PERFORMANCE OPTIMIZATION
-- ==========================================

CREATE INDEX idx_states_country_id ON states(country_id);
CREATE INDEX idx_cities_state_id ON cities(state_id);
CREATE INDEX idx_fire_stations_city_id ON fire_stations(city_id);
CREATE INDEX idx_employee_profiles_station_id ON employee_profiles(station_id);
CREATE INDEX idx_employee_shifts_employee_id ON employee_shifts(employee_id);
CREATE INDEX idx_employee_shifts_station_id ON employee_shifts(station_id);
CREATE INDEX idx_complaints_reporter_id ON complaints(reporter_id);
CREATE INDEX idx_complaints_category_id ON complaints(category_id);
CREATE INDEX idx_incidents_complaint_id ON incidents(complaint_id);
CREATE INDEX idx_incidents_station_id ON incidents(station_id);
CREATE INDEX idx_incidents_category_id ON incidents(category_id);
CREATE INDEX idx_equipment_station_id ON equipment(station_id);
CREATE INDEX idx_equipment_status_id ON equipment(status_id);
```

### Geographical Hierarchy and Station Joins
- **Fire Stations** act as the central operational hub, joining back to geographical regions via `fire_stations.city_id -> cities.id`.
- The geographic location is fully normalized: `cities.state_id -> states.id` and `states.country_id -> countries.id`. This layout prevents data duplication and allows filtering incidents, active personnel, or equipment by local (City), regional (State), or national (Country) jurisdiction.
- **Incident Escalation**: A `complaint` triggers an `incident`. They join via `incidents.complaint_id -> complaints.id`. Both connect back to the responding fire station via `incidents.station_id -> fire_stations.id` and to the reporter via `complaints.reporter_id -> users.id`.
- **Shift Audit Logs**: Firefighters (`users` with `ROLE_FIREFIGHTER`) check in and out of a specific fire station. These entries join via `employee_shifts.employee_id -> users.id` and `employee_shifts.station_id -> fire_stations.id`, storing exact UTC timestamps and GPS geolocations.

---

## SECTION 2: OPENAPI 3.0 SPECIFICATION (YAML)

This complete, production-grade OpenAPI 3.0 YAML spec models user registration, JWT login, and shift check-in/check-out flows.

```yaml
openapi: 3.0.3
info:
  title: Fire Management System Core API
  version: 1.0.0
  description: Production-grade RESTful API specification for the global Fire Management System, covering onboarding, authentication, and employee shift tracking.
servers:
  - url: /api/v1
    description: v1 Base URL
paths:
  /auth/register:
    post:
      summary: Register a new user
      description: Onboards a new user with dynamic role assignment (Citizen, Employee, Admin).
      operationId: registerUser
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RegisterRequest'
      responses:
        '201':
          description: User registered successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RegisterResponse'
        '400':
          description: Invalid input data
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '409':
          description: Username or email already exists
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /auth/login:
    post:
      summary: Authenticate user
      description: Returns a JWT access token with role claims embedded.
      operationId: loginUser
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/LoginRequest'
      responses:
        '200':
          description: Authentication successful
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/LoginResponse'
        '401':
          description: Invalid credentials
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /shifts/check-in:
    post:
      summary: Employee shift check-in
      description: Logs a new shift check-in event with timestamp, station reference, and GPS coordinates.
      operationId: checkInShift
      security:
        - BearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CheckInRequest'
      responses:
        '201':
          description: Check-in recorded successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ShiftResponse'
        '400':
          description: Invalid check-in request (e.g. employee already checked in)
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '401':
          description: Unauthorized
        '403':
          description: Forbidden (insufficient permissions)

  /shifts/check-out:
    patch:
      summary: Employee shift check-out
      description: Logs the shift check-out event, completing the active shift log.
      operationId: checkOutShift
      security:
        - BearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CheckOutRequest'
      responses:
        '200':
          description: Check-out recorded successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ShiftResponse'
        '400':
          description: Invalid check-out request (e.g. no active shift found)
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '401':
          description: Unauthorized
        '403':
          description: Forbidden (insufficient permissions)

components:
  securitySchemes:
    BearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
  schemas:
    RegisterRequest:
      type: object
      required:
        - email
        - username
        - password
        - first_name
        - last_name
        - role_names
      properties:
        email:
          type: string
          format: email
          example: john.doe@example.com
        username:
          type: string
          example: johndoe
        password:
          type: string
          format: password
          example: SuperSecure123!
        first_name:
          type: string
          example: John
        last_name:
          type: string
          example: Doe
        phone_number:
          type: string
          example: "+15550199"
        role_names:
          type: array
          items:
            type: string
          example: ["ROLE_FIREFIGHTER"]
        station_id:
          type: string
          format: uuid
          example: "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
        employee_code:
          type: string
          example: "EMP-94821"

    RegisterResponse:
      type: object
      required:
        - id
        - email
        - username
        - first_name
        - last_name
        - roles
        - created_at
      properties:
        id:
          type: string
          format: uuid
          example: "d3b07384-d113-4956-b51c-a1141e7d0a22"
        email:
          type: string
          format: email
          example: john.doe@example.com
        username:
          type: string
          example: johndoe
        first_name:
          type: string
          example: John
        last_name:
          type: string
          example: Doe
        phone_number:
          type: string
          nullable: true
          example: "+15550199"
        roles:
          type: array
          items:
            type: string
          example: ["ROLE_FIREFIGHTER"]
        created_at:
          type: string
          format: date-time
          example: "2026-08-31T20:30:00Z"

    LoginRequest:
      type: object
      required:
        - username
        - password
      properties:
        username:
          type: string
          example: johndoe
        password:
          type: string
          format: password
          example: SuperSecure123!

    LoginResponse:
      type: object
      required:
        - access_token
        - token_type
        - expires_in
        - roles
      properties:
        access_token:
          type: string
          example: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huZG9lIiwicm9sZXMiOlsiUk9MRV9GSVJFRklHSFRFUiJdLCJpYXQiOjE3MDk4NTYwMDB9..."
        token_type:
          type: string
          example: Bearer
        expires_in:
          type: integer
          example: 3600
        roles:
          type: array
          items:
            type: string
          example: ["ROLE_FIREFIGHTER"]

    CheckInRequest:
      type: object
      required:
        - employee_id
        - station_id
        - check_in_latitude
        - check_in_longitude
      properties:
        employee_id:
          type: string
          format: uuid
          example: "d3b07384-d113-4956-b51c-a1141e7d0a22"
        station_id:
          type: string
          format: uuid
          example: "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
        check_in_latitude:
          type: number
          format: float
          minimum: -90
          maximum: 90
          example: 37.7749
        check_in_longitude:
          type: number
          format: float
          minimum: -180
          maximum: 180
          example: -122.4194

    CheckOutRequest:
      type: object
      required:
        - shift_id
        - check_out_latitude
        - check_out_longitude
      properties:
        shift_id:
          type: string
          format: uuid
          example: "f2c3a504-20a2-4a0b-9273-04d112d7c044"
        check_out_latitude:
          type: number
          format: float
          minimum: -90
          maximum: 90
          example: 37.7749
        check_out_longitude:
          type: number
          format: float
          minimum: -180
          maximum: 180
          example: -122.4194

    ShiftResponse:
      type: object
      required:
        - id
        - employee_id
        - station_id
        - check_in_time
        - check_in_latitude
        - check_in_longitude
        - status
        - created_at
        - updated_at
      properties:
        id:
          type: string
          format: uuid
          example: "f2c3a504-20a2-4a0b-9273-04d112d7c044"
        employee_id:
          type: string
          format: uuid
          example: "d3b07384-d113-4956-b51c-a1141e7d0a22"
        station_id:
          type: string
          format: uuid
          example: "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
        check_in_time:
          type: string
          format: date-time
          example: "2026-08-31T08:00:00Z"
        check_out_time:
          type: string
          format: date-time
          nullable: true
          example: "2026-08-31T17:00:00Z"
        check_in_latitude:
          type: number
          example: 37.7749
        check_in_longitude:
          type: number
          example: -122.4194
        check_out_latitude:
          type: number
          nullable: true
          example: 37.7750
        check_out_longitude:
          type: number
          nullable: true
          example: -122.4193
        status:
          type: string
          enum: [ACTIVE, COMPLETED, ABNORMAL]
          example: COMPLETED
        created_at:
          type: string
          format: date-time
          example: "2026-08-31T08:00:01Z"
        updated_at:
          type: string
          format: date-time
          example: "2026-08-31T17:00:01Z"

    ErrorResponse:
      type: object
      required:
        - timestamp
        - status
        - error
        - message
        - path
      properties:
        timestamp:
          type: string
          format: date-time
          example: "2026-08-31T20:30:15Z"
        status:
          type: integer
          example: 400
        error:
          type: string
          example: Bad Request
        message:
          type: string
          example: Validation failed for field 'email'
        path:
          type: string
          example: /api/v1/auth/register
```
