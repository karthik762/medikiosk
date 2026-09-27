-- Phase 11: FHIR Interoperability & Verified Clinical Data Export Schema Enhancements
-- Conforming to docs/FHIR_MAPPING.md, docs/PRODUCT_SPEC.md, and docs/SECURITY.md

-- 1. Enhance fhir_mappings table with session association, resource payload, and validation state
ALTER TABLE fhir_mappings ADD COLUMN session_id VARCHAR(36) REFERENCES intake_sessions(id) ON DELETE CASCADE;
ALTER TABLE fhir_mappings ADD COLUMN resource_payload TEXT;
ALTER TABLE fhir_mappings ADD COLUMN validation_status VARCHAR(32) DEFAULT 'PENDING';
ALTER TABLE fhir_mappings ADD COLUMN validation_errors TEXT;

-- 2. Indexes for efficient FHIR mapping lookups and validation queries
CREATE INDEX idx_fhir_session_resource ON fhir_mappings(session_id, fhir_resource_type);
CREATE INDEX idx_fhir_validation ON fhir_mappings(validation_status);
CREATE INDEX idx_integration_session_type ON integration_records(session_id, integration_type);
