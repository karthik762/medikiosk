-- ==============================================================================
-- MediKiosk Database Migration V1: Initial Foundation Setup
-- Verifies Flyway migration infrastructure and stores foundational system metadata.
-- ==============================================================================

CREATE TABLE IF NOT EXISTS system_metadata (
    id VARCHAR(64) PRIMARY KEY,
    property_value TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO system_metadata (id, property_value, description)
VALUES 
    ('schema_version', '1.0.0', 'Initial database schema foundation version'),
    ('system_name', 'MediKiosk', 'AI-Powered Clinical Intake Kiosk'),
    ('status', 'INITIALIZED', 'Initial database migration applied successfully');
