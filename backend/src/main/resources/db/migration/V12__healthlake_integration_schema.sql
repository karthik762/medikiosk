-- Phase 14: AWS HealthLake Integration Foundation Schema Enhancements
-- Conforming to docs/HEALTHLAKE_SPEC.md, docs/DATABASE.md, and docs/SECURITY.md

-- 1. Index on patients.healthlake_ref for fast lookup and AWS HealthLake Patient matching
CREATE INDEX idx_patients_healthlake_ref ON patients(healthlake_ref);
