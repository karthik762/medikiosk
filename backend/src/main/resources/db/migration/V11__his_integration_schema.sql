-- Phase 13: HIS (Hospital Information System) Integration Foundation Schema Enhancements
-- Conforming to docs/HIS_SPEC.md, docs/DATABASE.md, and docs/SECURITY.md

-- 1. Index on patients.his_id for fast lookup and hospital matching
CREATE INDEX idx_patients_his_id ON patients(his_id);
