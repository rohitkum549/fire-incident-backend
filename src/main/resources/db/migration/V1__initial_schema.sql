-- Fire Management System Backend - Initial DB Migration
-- Schema Version: V1

CREATE TABLE health_check_log (
    id UUID PRIMARY KEY,
    checked_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL
);

-- Add database performance index on status and checked_at
CREATE INDEX idx_health_check_status_time ON health_check_log (status, checked_at DESC);
