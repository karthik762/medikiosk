-- ==============================================================================
-- MediKiosk Database Migration V3: Seed Approved Question Bank & Conditions
-- Conforming to docs/USER_FLOWS.md and docs/DATABASE.md
-- ==============================================================================

-- 1. Insert Core Approved Intake Questions
INSERT INTO questions (id, question_code, question_text, question_type, category, language, required, active, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111101', 'Q_PRIMARY_COMPLAINT', 'What is the primary reason for your visit today?', 'TEXT', 'CHIEF_COMPLAINT', 'en', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('11111111-1111-1111-1111-111111111102', 'Q_PAST_CONDITIONS', 'Do you have any existing diagnosed chronic medical conditions?', 'YES_NO', 'PAST_HISTORY', 'en', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('11111111-1111-1111-1111-111111111103', 'Q_PAST_CONDITIONS_DETAIL', 'Please describe your diagnosed medical conditions.', 'TEXT', 'PAST_HISTORY', 'en', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('11111111-1111-1111-1111-111111111104', 'Q_CURRENT_MEDS', 'Are you currently taking any prescription or regular medications?', 'YES_NO', 'MEDICATIONS', 'en', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('11111111-1111-1111-1111-111111111105', 'Q_KNOWN_ALLERGIES', 'Do you have any known drug, food, or environmental allergies?', 'YES_NO', 'ALLERGIES', 'en', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('11111111-1111-1111-1111-111111111106', 'Q_KNOWN_ALLERGIES_DETAIL', 'Please list your known allergies and the reactions they cause.', 'TEXT', 'ALLERGIES', 'en', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2. Insert Adaptive Question Branching Conditions
INSERT INTO question_conditions (id, question_id, dependent_question_id, condition_type, condition_value, action, created_at)
VALUES
    ('22222222-2222-2222-2222-222222222201', '11111111-1111-1111-1111-111111111102', '11111111-1111-1111-1111-111111111103', 'EQUALS', 'YES', 'SHOW', CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222202', '11111111-1111-1111-1111-111111111105', '11111111-1111-1111-1111-111111111106', 'EQUALS', 'YES', 'SHOW', CURRENT_TIMESTAMP);
