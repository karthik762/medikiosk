-- Phase 12: ABDM / ABHA Integration Foundation Schema Enhancements
-- Conforming to docs/ABDM_SPEC.md, docs/DATABASE.md, and docs/SECURITY.md

-- 1. Enhance patients table with ABHA verification status and verification timestamp
ALTER TABLE patients ADD COLUMN abha_status VARCHAR(32) DEFAULT 'NOT_PROVIDED';
ALTER TABLE patients ADD COLUMN abha_verified_at TIMESTAMP NULL;

CREATE INDEX idx_patients_abha_status ON patients(abha_status);
CREATE INDEX idx_patients_abha_id ON patients(abha_id);

-- 2. Enhance consents table with revocation timestamp and compound index for fast consent verification
ALTER TABLE consents ADD COLUMN revoked_at TIMESTAMP NULL;
CREATE INDEX idx_consents_patient_type ON consents(patient_id, consent_type);

-- 3. Enhance integration_records table for ABDM idempotency and response tracking
ALTER TABLE integration_records ADD COLUMN idempotency_key VARCHAR(128);
ALTER TABLE integration_records ADD COLUMN response_payload TEXT;

CREATE INDEX idx_integration_idempotency ON integration_records(idempotency_key);
