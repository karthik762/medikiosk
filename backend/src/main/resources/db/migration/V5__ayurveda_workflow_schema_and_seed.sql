-- ==============================================================================
-- MediKiosk Database Migration V5: Ayurveda Workflow Schema & Seed
-- Conforming to docs/AYURVEDA_SPEC.md, docs/DATABASE.md, and docs/USER_FLOWS.md
-- ==============================================================================

-- 1. Extend ayurveda_histories table with status, patient confirmation, and notes
ALTER TABLE ayurveda_histories ADD COLUMN status VARCHAR(32) DEFAULT 'DRAFT' NOT NULL;
ALTER TABLE ayurveda_histories ADD COLUMN patient_confirmed BOOLEAN DEFAULT false NOT NULL;
ALTER TABLE ayurveda_histories ADD COLUMN confirmed_at TIMESTAMP NULL;
ALTER TABLE ayurveda_histories ADD COLUMN notes TEXT;

CREATE INDEX idx_ayurveda_status ON ayurveda_histories(status);

-- 2. Seed Approved Ayurveda Intake Questions
INSERT INTO questions (id, question_code, question_text, question_type, category, language, required, active, created_at, updated_at)
VALUES
    ('55555555-5555-5555-5555-555555555501', 'Q_AYURVEDA_OPT_IN', 'Would you like to include an Ayurvedic health assessment as part of your consultation?', 'YES_NO', 'AYURVEDA_INTAKE', 'en', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('55555555-5555-5555-5555-555555555502', 'Q_PRAKRITI_BODY_FRAME', 'How would you describe your general physical build / body frame?', 'CHOICE', 'PRAKRITI', 'en', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('55555555-5555-5555-5555-555555555503', 'Q_PRAKRITI_WEATHER', 'Which climate or temperature do you find most uncomfortable?', 'CHOICE', 'PRAKRITI', 'en', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('55555555-5555-5555-5555-555555555504', 'Q_PRAKRITI_DIGESTION', 'How is your usual appetite and digestive pattern?', 'CHOICE', 'PRAKRITI', 'en', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('55555555-5555-5555-5555-555555555505', 'Q_PRAKRITI_SLEEP', 'How would you describe your usual sleep pattern?', 'CHOICE', 'PRAKRITI', 'en', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('55555555-5555-5555-5555-555555555506', 'Q_NIDANA_DIET', 'What is your predominant dietary preference and timing habit?', 'CHOICE', 'NIDANA', 'en', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('55555555-5555-5555-5555-555555555507', 'Q_NIDANA_ACTIVITY', 'How would you describe your daily routine and physical activity level?', 'CHOICE', 'NIDANA', 'en', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('55555555-5555-5555-5555-555555555508', 'Q_NIDANA_STRESS', 'Have you experienced persistent psychological stress, anxiety, or emotional strain recently?', 'YES_NO', 'NIDANA', 'en', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('55555555-5555-5555-5555-555555555509', 'Q_PARIKSHA_BOWEL', 'What is your typical bowel elimination pattern (Koshtha)?', 'CHOICE', 'PARIKSHA', 'en', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('55555555-5555-5555-5555-555555555510', 'Q_PARIKSHA_ENERGY', 'How is your overall daily energy and physical stamina (Bala)?', 'CHOICE', 'PARIKSHA', 'en', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('55555555-5555-5555-5555-555555555511', 'Q_PARIKSHA_TONGUE', 'Do you observe a thick or sticky white/yellow coating on your tongue in the morning (Jihva)?', 'YES_NO', 'PARIKSHA', 'en', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 3. Seed Deterministic Branching Conditions for Ayurveda Section
INSERT INTO question_conditions (id, question_id, dependent_question_id, condition_type, condition_value, action, created_at)
VALUES
    ('55555555-5555-5555-5555-555555555591', '55555555-5555-5555-5555-555555555501', '55555555-5555-5555-5555-555555555502', 'EQUALS', 'YES', 'SHOW', CURRENT_TIMESTAMP),
    ('55555555-5555-5555-5555-555555555592', '55555555-5555-5555-5555-555555555501', '55555555-5555-5555-5555-555555555506', 'EQUALS', 'YES', 'SHOW', CURRENT_TIMESTAMP),
    ('55555555-5555-5555-5555-555555555593', '55555555-5555-5555-5555-555555555501', '55555555-5555-5555-5555-555555555509', 'EQUALS', 'YES', 'SHOW', CURRENT_TIMESTAMP);
