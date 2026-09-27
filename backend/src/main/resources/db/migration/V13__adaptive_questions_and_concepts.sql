-- ==============================================================================
-- MediKiosk Database Migration V13: Adaptive Clinical Questions & Concepts
-- Conforming to docs/AI_SPEC.md, docs/TRIAGE_RULES.md and Phase 20-21 requirements
-- ==============================================================================

-- 1. Alter questions table to support clinical concept binding and adaptive ranking
ALTER TABLE questions ADD COLUMN clinical_concept_code VARCHAR(64);
ALTER TABLE questions ADD COLUMN safety_critical BOOLEAN DEFAULT FALSE;
ALTER TABLE questions ADD COLUMN priority INT DEFAULT 50;
ALTER TABLE questions ADD COLUMN purpose VARCHAR(128);
ALTER TABLE questions ADD COLUMN version INT DEFAULT 1;

CREATE INDEX idx_questions_concept ON questions(clinical_concept_code);
CREATE INDEX idx_questions_safety ON questions(safety_critical);

-- 2. Update existing questions with clinical concept metadata
UPDATE questions SET clinical_concept_code = 'CHIEF_COMPLAINT', priority = 10, purpose = 'PRIMARY_REASON' WHERE question_code = 'Q_PRIMARY_COMPLAINT';
UPDATE questions SET clinical_concept_code = 'PAST_HISTORY', priority = 80, purpose = 'CHRONIC_CONDITIONS' WHERE question_code = 'Q_PAST_CONDITIONS';
UPDATE questions SET clinical_concept_code = 'PAST_HISTORY', priority = 85, purpose = 'CHRONIC_CONDITIONS_DETAIL' WHERE question_code = 'Q_PAST_CONDITIONS_DETAIL';
UPDATE questions SET clinical_concept_code = 'MEDICATION', priority = 90, purpose = 'CURRENT_MEDICATIONS' WHERE question_code = 'Q_CURRENT_MEDS';
UPDATE questions SET clinical_concept_code = 'ALLERGY', safety_critical = TRUE, priority = 70, purpose = 'KNOWN_ALLERGIES' WHERE question_code = 'Q_KNOWN_ALLERGIES';
UPDATE questions SET clinical_concept_code = 'ALLERGY', safety_critical = TRUE, priority = 75, purpose = 'KNOWN_ALLERGIES_DETAIL' WHERE question_code = 'Q_KNOWN_ALLERGIES_DETAIL';

-- 3. Seed Adaptive Red-Flag and Concept-Targeted Questions
INSERT INTO questions (id, question_code, question_text, question_type, category, language, required, active, clinical_concept_code, safety_critical, priority, purpose, version, created_at, updated_at)
VALUES
    -- Chest Pain Specific Follow-ups
    ('33333333-3333-3333-3333-333333333301', 'Q_CP_SEVERITY_SCALE', 'On a scale from 0 (no pain) to 10 (worst pain imaginable), how severe is your chest discomfort right now?', 'SCALE_0_10', 'SYMPTOM_SEVERITY', 'en', true, true, 'CHEST_PAIN', true, 1, 'SEVERITY_ASSESSMENT', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333302', 'Q_CHEST_PAIN_DURATION', 'Approximately how long have you been experiencing this chest pain or pressure?', 'TEXT', 'SYMPTOM_DURATION', 'en', true, true, 'CHEST_PAIN', true, 2, 'DURATION_ASSESSMENT', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333303', 'Q_CHEST_PAIN_RADIATION', 'Does the pain spread or radiate anywhere (such as to your left arm, jaw, neck, or back)?', 'YES_NO', 'RED_FLAG_SCREENING', 'en', true, true, 'CHEST_PAIN', true, 3, 'RADIATION_SCREENING', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333304', 'Q_CHEST_PAIN_BREATHING', 'Are you also having any difficulty breathing or shortness of breath?', 'YES_NO', 'RED_FLAG_SCREENING', 'en', true, true, 'SHORTNESS_OF_BREATH', true, 4, 'DYSPNEA_CO_OCCURRENCE', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333305', 'Q_CHEST_PAIN_SWEATING', 'Are you experiencing unexplained cold sweats, clamminess, or profuse sweating?', 'YES_NO', 'RED_FLAG_SCREENING', 'en', true, true, 'SWEATING', true, 5, 'DIAPHORESIS_SCREENING', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Shortness of Breath Specific Follow-ups
    ('33333333-3333-3333-3333-333333333310', 'Q_SOB_ONSET', 'Did your breathing difficulty start suddenly or develop gradually?', 'CHOICE', 'SYMPTOM_CHARACTERIZATION', 'en', true, true, 'SHORTNESS_OF_BREATH', true, 6, 'ONSET_SCREENING', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333311', 'Q_SOB_REST', 'Are you feeling short of breath even while resting or lying completely flat?', 'YES_NO', 'RED_FLAG_SCREENING', 'en', true, true, 'SHORTNESS_OF_BREATH', true, 7, 'ORTHOPNEA_REST_DYSPNEA', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Dizziness / Syncope Follow-ups
    ('33333333-3333-3333-3333-333333333320', 'Q_DIZZINESS_SYNCOPE', 'Have you passed out, fainted, or momentarily lost consciousness?', 'YES_NO', 'RED_FLAG_SCREENING', 'en', true, true, 'DIZZINESS', true, 8, 'SYNCOPE_SCREENING', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Fever Follow-ups
    ('33333333-3333-3333-3333-333333333330', 'Q_FEVER_DURATION', 'For how many days have you had a fever?', 'TEXT', 'SYMPTOM_DURATION', 'en', true, true, 'FEVER', false, 15, 'FEVER_DURATION', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333331', 'Q_FEVER_CHILLS', 'Are you experiencing shaking chills or rigors?', 'YES_NO', 'ASSOCIATED_SYMPTOMS', 'en', false, true, 'FEVER', false, 20, 'CHILLS_SCREENING', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Headache Follow-ups
    ('33333333-3333-3333-3333-333333333340', 'Q_HEADACHE_SEVERITY', 'On a scale from 0 to 10, how severe is your headache?', 'SCALE_0_10', 'SYMPTOM_SEVERITY', 'en', true, true, 'HEADACHE', false, 16, 'HEADACHE_SEVERITY', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333341', 'Q_HEADACHE_VISION', 'Are you experiencing any vision changes, stiff neck, or extreme sensitivity to light?', 'YES_NO', 'RED_FLAG_SCREENING', 'en', true, true, 'HEADACHE', true, 9, 'NEURO_RED_FLAGS', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Abdominal Pain Follow-ups
    ('33333333-3333-3333-3333-333333333350', 'Q_ABDOMINAL_LOCATION', 'Where in your abdomen is the pain located (e.g. upper right, lower right, generalized)?', 'TEXT', 'SYMPTOM_CHARACTERIZATION', 'en', true, true, 'ABDOMINAL_PAIN', false, 18, 'ABDOMINAL_LOCATION', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333351', 'Q_ABDOMINAL_VOMITING', 'Are you experiencing persistent vomiting or inability to keep fluids down?', 'YES_NO', 'RED_FLAG_SCREENING', 'en', true, true, 'VOMITING', true, 10, 'VOMITING_SCREENING', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Cough Follow-ups
    ('33333333-3333-3333-3333-333333333360', 'Q_COUGH_DURATION', 'How long have you been coughing?', 'TEXT', 'SYMPTOM_DURATION', 'en', true, true, 'COUGH', false, 22, 'COUGH_DURATION', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333361', 'Q_COUGH_BLOOD', 'Have you coughed up any blood or rust-colored phlegm?', 'YES_NO', 'RED_FLAG_SCREENING', 'en', true, true, 'COUGH', true, 11, 'HEMOPTYSIS_SCREENING', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

