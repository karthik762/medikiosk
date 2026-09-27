-- ==============================================================================
-- MediKiosk Database Migration V2: Core Domain Schema
-- Implements complete domain schema conforming to docs/DATABASE.md
-- ==============================================================================

-- 1. users
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- 2. patients
CREATE TABLE IF NOT EXISTS patients (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) REFERENCES users(id) ON DELETE SET NULL,
    patient_identifier VARCHAR(64) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(32),
    phone VARCHAR(32),
    email VARCHAR(255),
    abha_id VARCHAR(64),
    his_id VARCHAR(64),
    healthlake_ref VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_patients_identifier ON patients(patient_identifier);
CREATE INDEX idx_patients_phone ON patients(phone);
CREATE INDEX idx_patients_user_id ON patients(user_id);

-- 3. intake_sessions
CREATE TABLE IF NOT EXISTS intake_sessions (
    id VARCHAR(36) PRIMARY KEY,
    patient_id VARCHAR(36) NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    language VARCHAR(32) DEFAULT 'en',
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sessions_patient ON intake_sessions(patient_id);
CREATE INDEX idx_sessions_status ON intake_sessions(status);
CREATE INDEX idx_sessions_created_at ON intake_sessions(created_at);

-- 4. consents
CREATE TABLE IF NOT EXISTS consents (
    id VARCHAR(36) PRIMARY KEY,
    patient_id VARCHAR(36) NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    session_id VARCHAR(36) REFERENCES intake_sessions(id) ON DELETE SET NULL,
    consent_status VARCHAR(32) NOT NULL,
    consent_type VARCHAR(64) NOT NULL,
    consent_version VARCHAR(32) NOT NULL,
    external_reference VARCHAR(255),
    consented_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_consents_patient ON consents(patient_id);
CREATE INDEX idx_consents_session ON consents(session_id);

-- 5. questions
CREATE TABLE IF NOT EXISTS questions (
    id VARCHAR(36) PRIMARY KEY,
    question_code VARCHAR(64) NOT NULL UNIQUE,
    question_text TEXT NOT NULL,
    question_type VARCHAR(32) NOT NULL,
    category VARCHAR(64),
    language VARCHAR(32) DEFAULT 'en',
    required BOOLEAN DEFAULT true,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_questions_code ON questions(question_code);
CREATE INDEX idx_questions_category ON questions(category);

-- 6. question_conditions
CREATE TABLE IF NOT EXISTS question_conditions (
    id VARCHAR(36) PRIMARY KEY,
    question_id VARCHAR(36) NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    dependent_question_id VARCHAR(36) NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    condition_type VARCHAR(64) NOT NULL,
    condition_value TEXT,
    action VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_qcond_question ON question_conditions(question_id);
CREATE INDEX idx_qcond_dep_question ON question_conditions(dependent_question_id);

-- 7. answers
CREATE TABLE IF NOT EXISTS answers (
    id VARCHAR(36) PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL REFERENCES intake_sessions(id) ON DELETE CASCADE,
    question_id VARCHAR(36) NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    answer_text TEXT NOT NULL,
    input_method VARCHAR(32),
    original_transcription TEXT,
    confidence_score DECIMAL(5, 4),
    confirmed_by_patient BOOLEAN DEFAULT true,
    answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_answers_session ON answers(session_id);
CREATE INDEX idx_answers_question ON answers(question_id);

-- 8. symptoms
CREATE TABLE IF NOT EXISTS symptoms (
    id VARCHAR(36) PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL REFERENCES intake_sessions(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    duration VARCHAR(100),
    severity INTEGER,
    frequency VARCHAR(100),
    onset VARCHAR(100),
    source VARCHAR(32) DEFAULT 'PATIENT',
    verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_symptoms_session ON symptoms(session_id);

-- 9. triage_results
CREATE TABLE IF NOT EXISTS triage_results (
    id VARCHAR(36) PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL UNIQUE REFERENCES intake_sessions(id) ON DELETE CASCADE,
    priority VARCHAR(32) NOT NULL,
    triggered_rules LONGTEXT,
    explanation TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_triage_session ON triage_results(session_id);
CREATE INDEX idx_triage_priority ON triage_results(priority);

-- 10. medical_history
CREATE TABLE IF NOT EXISTS medical_history (
    id VARCHAR(36) PRIMARY KEY,
    patient_id VARCHAR(36) NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    session_id VARCHAR(36) REFERENCES intake_sessions(id) ON DELETE SET NULL,
    category VARCHAR(64) NOT NULL,
    description TEXT NOT NULL,
    source VARCHAR(32) DEFAULT 'PATIENT',
    verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_med_history_patient ON medical_history(patient_id);
CREATE INDEX idx_med_history_session ON medical_history(session_id);

-- 11. medications
CREATE TABLE IF NOT EXISTS medications (
    id VARCHAR(36) PRIMARY KEY,
    patient_id VARCHAR(36) NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    session_id VARCHAR(36) REFERENCES intake_sessions(id) ON DELETE SET NULL,
    medication_name VARCHAR(255) NOT NULL,
    dosage VARCHAR(100),
    frequency VARCHAR(100),
    duration VARCHAR(100),
    source VARCHAR(32) DEFAULT 'PATIENT',
    verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_medications_patient ON medications(patient_id);
CREATE INDEX idx_medications_session ON medications(session_id);

-- 12. allergies
CREATE TABLE IF NOT EXISTS allergies (
    id VARCHAR(36) PRIMARY KEY,
    patient_id VARCHAR(36) NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    session_id VARCHAR(36) REFERENCES intake_sessions(id) ON DELETE SET NULL,
    allergen VARCHAR(255) NOT NULL,
    reaction TEXT,
    severity VARCHAR(64),
    source VARCHAR(32) DEFAULT 'PATIENT',
    verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_allergies_patient ON allergies(patient_id);
CREATE INDEX idx_allergies_session ON allergies(session_id);

-- 13. ayurveda_histories
CREATE TABLE IF NOT EXISTS ayurveda_histories (
    id VARCHAR(36) PRIMARY KEY,
    patient_id VARCHAR(36) NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    session_id VARCHAR(36) NOT NULL UNIQUE REFERENCES intake_sessions(id) ON DELETE CASCADE,
    prakriti_data LONGTEXT,
    nidana_data LONGTEXT,
    pariksha_data LONGTEXT,
    terminology_data LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ayurveda_patient ON ayurveda_histories(patient_id);
CREATE INDEX idx_ayurveda_session ON ayurveda_histories(session_id);

-- 14. documents
CREATE TABLE IF NOT EXISTS documents (
    id VARCHAR(36) PRIMARY KEY,
    patient_id VARCHAR(36) NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    session_id VARCHAR(36) REFERENCES intake_sessions(id) ON DELETE SET NULL,
    document_type VARCHAR(64) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100),
    storage_reference TEXT NOT NULL,
    file_hash VARCHAR(128),
    processing_status VARCHAR(32) NOT NULL DEFAULT 'UPLOADED',
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL
);

CREATE INDEX idx_documents_patient ON documents(patient_id);
CREATE INDEX idx_documents_session ON documents(session_id);
CREATE INDEX idx_documents_status ON documents(processing_status);

-- 15. document_extractions
CREATE TABLE IF NOT EXISTS document_extractions (
    id VARCHAR(36) PRIMARY KEY,
    document_id VARCHAR(36) NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    extracted_text TEXT,
    extraction_data LONGTEXT,
    confidence_score DECIMAL(5, 4),
    processing_engine VARCHAR(64),
    status VARCHAR(32) NOT NULL DEFAULT 'SUCCESS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_extractions_document ON document_extractions(document_id);

-- 16. clinical_summaries
CREATE TABLE IF NOT EXISTS clinical_summaries (
    id VARCHAR(36) PRIMARY KEY,
    patient_id VARCHAR(36) NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    session_id VARCHAR(36) NOT NULL UNIQUE REFERENCES intake_sessions(id) ON DELETE CASCADE,
    summary_data LONGTEXT,
    summary_text TEXT,
    generated_by VARCHAR(32) DEFAULT 'AI',
    status VARCHAR(32) NOT NULL DEFAULT 'AI_GENERATED',
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_summaries_patient ON clinical_summaries(patient_id);
CREATE INDEX idx_summaries_session ON clinical_summaries(session_id);
CREATE INDEX idx_summaries_status ON clinical_summaries(status);

-- 17. doctor_reviews
CREATE TABLE IF NOT EXISTS doctor_reviews (
    id VARCHAR(36) PRIMARY KEY,
    summary_id VARCHAR(36) NOT NULL REFERENCES clinical_summaries(id) ON DELETE CASCADE,
    doctor_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action VARCHAR(32) NOT NULL,
    comments TEXT,
    summary_version INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reviews_summary ON doctor_reviews(summary_id);
CREATE INDEX idx_reviews_doctor ON doctor_reviews(doctor_id);

-- 18. fhir_mappings
CREATE TABLE IF NOT EXISTS fhir_mappings (
    id VARCHAR(36) PRIMARY KEY,
    entity_type VARCHAR(64) NOT NULL,
    entity_id VARCHAR(36) NOT NULL,
    fhir_resource_type VARCHAR(64) NOT NULL,
    fhir_resource_id VARCHAR(128) NOT NULL,
    fhir_version VARCHAR(32) DEFAULT 'R4',
    sync_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    last_synced_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_fhir_entity ON fhir_mappings(entity_type, entity_id);
CREATE INDEX idx_fhir_resource ON fhir_mappings(fhir_resource_type, fhir_resource_id);

-- 19. integration_records
CREATE TABLE IF NOT EXISTS integration_records (
    id VARCHAR(36) PRIMARY KEY,
    patient_id VARCHAR(36) REFERENCES patients(id) ON DELETE SET NULL,
    session_id VARCHAR(36) REFERENCES intake_sessions(id) ON DELETE SET NULL,
    integration_type VARCHAR(32) NOT NULL,
    operation VARCHAR(32) NOT NULL,
    external_reference VARCHAR(255),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    attempt_count INTEGER DEFAULT 1,
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL
);

CREATE INDEX idx_integration_status ON integration_records(status);
CREATE INDEX idx_integration_type ON integration_records(integration_type);
CREATE INDEX idx_integration_patient ON integration_records(patient_id);
CREATE INDEX idx_integration_session ON integration_records(session_id);

-- 20. audit_logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(64) NOT NULL,
    entity_type VARCHAR(64),
    entity_id VARCHAR(36),
    metadata LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_created_at ON audit_logs(created_at);
CREATE INDEX idx_audit_action ON audit_logs(action);

-- 21. notifications
CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    priority VARCHAR(32) DEFAULT 'NORMAL',
    `read` BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_unread ON notifications(user_id, `read`);
