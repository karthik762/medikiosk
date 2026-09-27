-- ==============================================================================
-- MediKiosk Database Migration V7: AI Clinical Summary Schema Extension
-- Conforming to docs/DATABASE.md, docs/AI_SPEC.md, and docs/SECURITY.md
-- ==============================================================================

-- 1. Extend clinical_summaries table with AI model tracking and mock indicator
ALTER TABLE clinical_summaries ADD COLUMN model_identifier VARCHAR(128);
ALTER TABLE clinical_summaries ADD COLUMN is_mock BOOLEAN DEFAULT true;

-- 2. Add composite index for session lookup
CREATE INDEX idx_summaries_patient_session ON clinical_summaries(patient_id, session_id);
