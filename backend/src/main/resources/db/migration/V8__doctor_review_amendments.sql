-- Phase 10: Doctor Review, Clinical Amendments, and Verification Schema Enhancements
-- Conforming to docs/PRODUCT_SPEC.md, docs/DATABASE.md, and docs/SECURITY.md

-- 1. Enhance doctor_reviews table to support detailed amendment tracking and verification timestamps
ALTER TABLE doctor_reviews ADD COLUMN amendments LONGTEXT;
ALTER TABLE doctor_reviews ADD COLUMN verified_at TIMESTAMP NULL;

-- 2. Enhance clinical_summaries table to preserve the original AI snapshot across doctor edits
ALTER TABLE clinical_summaries ADD COLUMN original_ai_data LONGTEXT;

-- 3. Performance index for review history queries
CREATE INDEX idx_reviews_summary_action ON doctor_reviews(summary_id, action);
