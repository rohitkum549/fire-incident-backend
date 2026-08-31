-- Fire Management System Core Database Migration
-- Version: V2

-- ==========================================
-- MODULE 1: GLOBAL GEOGRAPHY & STATIONS
-- ==========================================

CREATE TABLE countries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE NOT NULL,
    iso_code VARCHAR(3) UNIQUE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE states (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL,
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
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE NOT NULL,
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
    name VARCHAR(100) UNIQUE NOT NULL,
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
    complaint_id UUID UNIQUE,
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
    status_name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE equipment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    station_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(100) NOT NULL,
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
