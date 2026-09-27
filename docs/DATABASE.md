# MediKiosk — Database Specification

## 1. Database Overview

MediKiosk uses MySQL 8.x as its primary relational database.

The database stores:

- User accounts
- Patient information
- Intake sessions
- Clinical questions and answers
- Triage results
- Medical history
- Ayurveda history
- Medications
- Allergies
- Medical documents
- OCR results
- AI-generated summaries
- Doctor reviews and verification
- FHIR-related identifiers/data
- Integration status
- Audit logs

The database must maintain clear relationships between patients, intake sessions, clinical information, documents, AI outputs, and doctor verification.

---

# 2. Database Principles

The database must:

- Use MySQL 8.x.
- Use UUIDs for primary identifiers where appropriate.
- Maintain foreign-key relationships.
- Enforce data integrity using constraints.
- Store timestamps for important records.
- Keep AI-generated information separate from doctor-verified information.
- Preserve original patient-provided information.
- Preserve original uploaded documents.
- Support FHIR-compatible data mapping.
- Support future ABDM, HIS, and AWS HealthLake integration.
- Maintain audit records for important actions.

---

# 3. Core Entity Relationship

Patient
→ Many Intake Sessions

Intake Session
→ Many Answers

Intake Session
→ One Triage Result

Intake Session
→ One Clinical Summary

Intake Session
→ One Ayurveda History

Patient
→ Many Medical History Records

Patient
→ Many Medications

Patient
→ Many Allergies

Patient
→ Many Documents

Document
→ One or More Extractions

Clinical Summary
→ Doctor Review / Verification

User
→ Doctor / Patient / Admin role

Important actions
→ Audit Log

---

# 4. users

Purpose:

Stores authenticated application users.

Fields:

| Field | Type | Description |
|---|---|---|
| id | UUID | Primary key |
| email | VARCHAR | Unique login email |
| password_hash | VARCHAR | Hashed password |
| first_name | VARCHAR | First name |
| last_name | VARCHAR | Last name |
| role | ENUM | PATIENT / DOCTOR / ADMIN |
| status | ENUM | ACTIVE / INACTIVE / LOCKED |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update |

Constraints:

- Email must be unique.
- Passwords must never be stored in plain text.
- Role must be restricted to approved values.

---

# 5. patients

Purpose:

Stores patient-specific information.

Fields:

| Field | Type | Description |
|---|---|---|
| id | UUID | Primary key |
| user_id | UUID | Optional reference to users |
| patient_identifier | VARCHAR | Internal patient identifier |
| first_name | VARCHAR | Patient first name |
| last_name | VARCHAR | Patient last name |
| date_of_birth | DATE | Date of birth |
| gender | VARCHAR | Gender |
| phone | VARCHAR | Contact number |
| email | VARCHAR | Email |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update |

Relationships:

- One patient can have many intake sessions.
- One patient can have many documents.
- One patient can have many medical history records.

---

# 6. intake_sessions

Purpose:

Represents one MediKiosk intake session.

Fields:

| Field | Type | Description |
|---|---|---|
| id | UUID | Primary key |
| patient_id | UUID | Patient reference |
| status | ENUM | CREATED / IN_PROGRESS / PROCESSING / COMPLETED / EXPIRED |
| language | VARCHAR | Selected language |
| started_at | TIMESTAMP | Session start |
| completed_at | TIMESTAMP | Completion time |
| expires_at | TIMESTAMP | Session expiration |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update |

Relationships:

- Belongs to one patient.
- Contains many answers.
- Has one triage result.
- Has one clinical summary.
- Has one Ayurveda history.
- Can contain many documents.

---

# 7. consents

Purpose:

Stores patient consent for information collection and processing.

Fields:

| Field | Type | Description |
|---|---|---|
| id | UUID | Primary key |
| patient_id | UUID | Patient reference |
| session_id | UUID | Intake session reference |
| consent_status | ENUM | ACCEPTED / DECLINED |
| consent_version | VARCHAR | Version of consent text |
| consented_at | TIMESTAMP | Consent timestamp |
| created_at | TIMESTAMP | Creation timestamp |

Rules:

- Consent must be recorded before clinical information is collected.
- Declined consent must prevent the applicable intake workflow from proceeding.

---

# 8. questions

Purpose:

Stores approved clinical questions used by the adaptive question engine.

Fields:

| Field | Type | Description |
|---|---|---|
| id | UUID | Primary key |
| question_code | VARCHAR | Unique question identifier |
| question_text | TEXT | Question |
| question_type | VARCHAR | TEXT / YES_NO / SINGLE_CHOICE / MULTI_CHOICE / SCALE |
| category | VARCHAR | Clinical category |
| language | VARCHAR | Question language |
| required | BOOLEAN | Whether answer is required |
| active | BOOLEAN | Whether question is active |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update |

The question engine must use approved questions.

AI must not modify the clinical question definitions.

---

# 9. question_conditions

Purpose:

Defines conditional logic for adaptive questioning.

Fields:

| Field | Type | Description |
|---|---|---|
| id | UUID | Primary key |
| question_id | UUID | Current question |
| dependent_question_id | UUID | Related question |
| condition_type | VARCHAR | Condition type |
| condition_value | TEXT | Expected value |
| action | VARCHAR | SHOW / SKIP |
| created_at | TIMESTAMP | Creation timestamp |

Purpose:

Allows the system to determine which questions should appear based on previous answers.

---

# 10. answers

Purpose:

Stores patient responses.

Fields:

| Field | Type | Description |
|---|---|---|
| id | UUID | Primary key |
| session_id | UUID | Intake session |
| question_id | UUID | Question |
| answer_text | TEXT | Patient answer |
| input_method | VARCHAR | VOICE / TEXT / TOUCH |
| original_transcription | TEXT | Original voice transcription |
| confidence_score | DECIMAL | Confidence where available |
| confirmed_by_patient | BOOLEAN | Patient confirmation |
| answered_at | TIMESTAMP | Answer timestamp |

Rules:

- Original patient response must be preserved.
- AI-generated interpretation must not overwrite the original answer.
- Voice responses must be identifiable as voice input.

---

# 11. symptoms

Purpose:

Stores structured symptoms associated with a patient intake.

Fields:

| Field | Type | Description |
|---|---|---|
| id | UUID | Primary key |
| session_id | UUID | Intake session |
| name | VARCHAR | Symptom |
| location | VARCHAR | Location |
| duration | VARCHAR | Duration |
| severity | INTEGER | Severity |
| frequency | VARCHAR | Frequency |
| onset | VARCHAR | Onset |
| source | VARCHAR | PATIENT / AI / DOCUMENT |
| verified | BOOLEAN | Verification status |
| created_at | TIMESTAMP | Creation timestamp |

---

# 12. triage_results

Purpose:

Stores the result of the approved rule-based triage engine.

Fields:

| Field | Type | Description |
|---|---|---|
| id | UUID | Primary key |
| session_id | UUID | Intake session |
| priority | ENUM | URGENT / PRIORITY / STABLE |
| triggered_rules | JSONB | Rules that were triggered |
| explanation | TEXT | System explanation |
| created_at | TIMESTAMP | Evaluation timestamp |

Rules:

- Triage must be generated by the approved rule engine.
- AI must not independently determine triage priority.
- Triage status must be visible to authorized doctors/clinical staff.

---

# 13. medical_history

Purpose:

Stores conventional medical history.

Fields:

| Field | Type | Description |
|---|---|---|
| id | UUID | Primary key |
| patient_id | UUID | Patient |
| session_id | UUID | Related session |
| category | VARCHAR | Medical history category |
| description | TEXT | Information |
| source | VARCHAR | PATIENT / DOCUMENT / AI |
| verified | BOOLEAN | Verification status |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update |

Possible categories:

- Previous conditions
- Surgeries
- Hospitalizations
- Family history
- Lifestyle
- Other relevant history

---

# 14. medications

Purpose:

Stores medication information.

Fields:

| Field | Type | Description |
|---|---|---|
| id | UUID | Primary key |
| patient_id | UUID | Patient |
| session_id | UUID | Session |
| medication_name | VARCHAR | Medication |
| dosage | VARCHAR | Dosage |
| frequency | VARCHAR | Frequency |
| source | VARCHAR | PATIENT / DOCUMENT / AI |
| verified | BOOLEAN | Verification status |
| created_at | TIMESTAMP | Creation timestamp |

---

# 15. allergies

Purpose:

Stores patient allergy information.

Fields:

| Field | Type | Description |
|---|---|---|
| id | UUID | Primary key |
| patient_id | UUID | Patient |
| session_id | UUID | Session |
| allergen | VARCHAR | Allergen |
| reaction | TEXT | Reported reaction |
| source | VARCHAR | Information source |
| verified | BOOLEAN | Verification status |
| created_at | TIMESTAMP | Creation timestamp |

---

# 16. ayurveda_histories

Purpose:

Stores structured Ayurveda information.

Fields:

| Field | Type | Description |
|---|---|---|
| id | UUID | Primary key |
| patient_id | UUID | Patient |
| session_id | UUID | Session |
| prakriti_data | JSONB | Prakriti information |
| nidana_data | JSONB | Nidana information |
| pariksha_data | JSONB | Pariksha information |
| terminology_data | JSONB | Ayurveda terminology |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update |

Flexible JSONB fields may be used because Ayurveda questionnaire structures may evolve.

---

# 17. documents

Purpose:

Stores metadata for uploaded or scanned medical documents.

Fields:

| Field | Type | Description |
|---|---|---|
| id | UUID | Primary key |
| patient_id | UUID | Patient |
| session_id | UUID | Session |
| document_type | VARCHAR | PRESCRIPTION / LAB_REPORT / MEDICAL_RECORD / DISCHARGE / OTHER |
| file_name | VARCHAR | Original file name |
| file_type | VARCHAR | MIME type |
| storage_reference | TEXT | Secure storage reference |
| file_hash | VARCHAR | File integrity hash |
| processing_status | ENUM | UPLOADED / PROCESSING / COMPLETED / FAILED |
| uploaded_at | TIMESTAMP | Upload timestamp |
| processed_at | TIMESTAMP | Processing completion |

The database should store document metadata and secure references rather than unnecessarily storing large binary files directly in relational tables.

---

# 18. document_extractions

Purpose:

Stores OCR and document-processing results.

Fields:

| Field | Type | Description |
|---|---|---|
| id | UUID | Primary key |
| document_id | UUID | Document |
| extracted_text | TEXT | OCR text |
| extraction_data | JSONB | Structured extracted information |
| confidence_score | DECIMAL | Overall confidence |
| processing_engine | VARCHAR | OCR/processing engine |
| status | ENUM | SUCCESS / LOW_CONFIDENCE / FAILED |
| created_at | TIMESTAMP | Processing timestamp |

Rules:

- Original document must remain available.
- Low-confidence extraction must be flagged.
- Extracted data must not automatically become doctor-verified data.

---

# 19. clinical_summaries

Purpose:

Stores AI-generated and doctor-verified clinical summaries.

Fields:

| Field | Type | Description |
|---|---|---|
| id | UUID | Primary key |
| patient_id | UUID | Patient |
| session_id | UUID | Session |
| summary_data | JSONB | Structured summary |
| summary_text | TEXT | Human-readable summary |
| generated_by | VARCHAR | AI / MANUAL |
| status | ENUM | AI_GENERATED / DOCTOR_REVIEWED / VERIFIED |
| version | INTEGER | Summary version |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update |

Important:

AI-generated summaries must remain distinguishable from doctor-verified summaries.

---

# 20. doctor_reviews

Purpose:

Stores doctor review and verification actions.

Fields:

| Field | Type | Description |
|---|---|---|
| id | UUID | Primary key |
| summary_id | UUID | Clinical summary |
| doctor_id | UUID | Doctor user |
| action | ENUM | REVIEWED / EDITED / VERIFIED |
| comments | TEXT | Doctor comments |
| summary_version | INTEGER | Version reviewed |
| created_at | TIMESTAMP | Action timestamp |

Doctor verification must always record the doctor and timestamp.

---

# 21. fhir_mappings

Purpose:

Stores references between MediKiosk records and FHIR resources.

Fields:

| Field | Type | Description |
|---|---|---|
| id | UUID | Primary key |
| entity_type | VARCHAR | Internal entity |
| entity_id | UUID | Internal record |
| fhir_resource_type | VARCHAR | FHIR resource |
| fhir_resource_id | VARCHAR | FHIR resource identifier |
| fhir_version | VARCHAR | FHIR version |
| sync_status | VARCHAR | PENDING / SYNCED / FAILED |
| last_synced_at | TIMESTAMP | Last synchronization |
| created_at | TIMESTAMP | Creation timestamp |

---

# 22. integration_records

Purpose:

Tracks external integration operations.

Fields:

| Field | Type | Description |
|---|---|---|
| id | UUID | Primary key |
| patient_id | UUID | Patient |
| integration_type | VARCHAR | ABDM / HIS / HEALTHLAKE |
| operation | VARCHAR | READ / CREATE / UPDATE / SYNC |
| external_reference | VARCHAR | External system identifier |
| status | VARCHAR | PENDING / SUCCESS / FAILED |
| error_message | TEXT | Error information |
| attempted_at | TIMESTAMP | Attempt timestamp |
| completed_at | TIMESTAMP | Completion timestamp |

---

# 23. audit_logs

Purpose:

Records important security and application actions.

Fields:

| Field | Type | Description |
|---|---|---|
| id | UUID | Primary key |
| user_id | UUID | User performing action |
| action | VARCHAR | Action performed |
| entity_type | VARCHAR | Affected entity |
| entity_id | UUID | Affected record |
| metadata | JSONB | Additional information |
| created_at | TIMESTAMP | Action timestamp |

Examples:

- LOGIN
- LOGOUT
- PATIENT_VIEW
- PATIENT_UPDATE
- DOCUMENT_VIEW
- DOCUMENT_UPLOAD
- AI_SUMMARY_GENERATED
- SUMMARY_EDITED
- SUMMARY_VERIFIED
- FHIR_SYNC
- ABDM_SYNC
- HIS_SYNC
- HEALTHLAKE_SYNC

---

# 24. notifications

Purpose:

Stores system notifications and workflow alerts.

Fields:

| Field | Type | Description |
|---|---|---|
| id | UUID | Primary key |
| user_id | UUID | Recipient |
| type | VARCHAR | Notification type |
| title | VARCHAR | Notification title |
| message | TEXT | Notification message |
| priority | VARCHAR | LOW / NORMAL / HIGH / URGENT |
| read | BOOLEAN | Read status |
| created_at | TIMESTAMP | Creation timestamp |

Used for events such as:

- Urgent triage
- New patient intake
- Processing completion
- Integration failure
- Doctor review required

---

# 25. Database Relationships

Core relationships:

Patient
→ Intake Sessions

Patient
→ Medical History

Patient
→ Medications

Patient
→ Allergies

Patient
→ Documents

Patient
→ Ayurveda History

Intake Session
→ Answers

Intake Session
→ Symptoms

Intake Session
→ Triage Result

Intake Session
→ Clinical Summary

Document
→ Document Extractions

Clinical Summary
→ Doctor Reviews

User
→ Doctor Reviews

Internal Records
→ FHIR Mappings

External Operations
→ Integration Records

Users / System Actions
→ Audit Logs

---

# 26. JSONB Usage

MySQL 8.x LONGTEXT (JSON) should be used for flexible or evolving structures such as:

- AI structured output
- OCR extraction data
- Ayurveda assessment data
- FHIR-related payloads where appropriate
- Integration metadata
- Audit metadata

Relational columns should be preferred for frequently queried core fields.

Do not put the entire database into JSONB.

---

# 27. Data Integrity

The database must enforce:

- Foreign-key relationships
- Unique patient identifiers where required
- Unique user emails
- Valid enum/status values
- Non-null required fields
- Timestamp consistency
- Referential integrity

Records should not be silently deleted when they are required for auditability or clinical traceability.

Use appropriate soft-delete/archive strategies where required.

---

# 28. Indexing

Indexes should be created for frequently queried fields.

Initial candidates:

- users.email
- patients.patient_identifier
- patients.phone
- intake_sessions.patient_id
- intake_sessions.status
- intake_sessions.created_at
- triage_results.priority
- documents.patient_id
- documents.session_id
- clinical_summaries.patient_id
- clinical_summaries.session_id
- audit_logs.user_id
- audit_logs.created_at
- integration_records.status

Indexes should be added based on actual query patterns.

---

# 29. Versioning

AI-generated clinical summaries must support versioning.

Example:

Version 1
→ AI Generated

Version 2
→ Doctor Edited

Version 3
→ Doctor Verified

Previous versions must remain traceable where required.

---

# 30. Data Source Tracking

Clinical information should record its source where applicable.

Possible sources:

- PATIENT
- VOICE
- TOUCH
- DOCUMENT
- OCR
- AI
- DOCTOR
- EXTERNAL_SYSTEM

This allows doctors to distinguish between:

Patient-provided information
vs.
Document-extracted information
vs.
AI-structured information
vs.
Doctor-verified information.

---

# 31. FHIR Readiness

The internal database must support mapping to FHIR resources.

Initial mapping targets include:

Patient
→ FHIR Patient

Intake Session
→ FHIR Encounter

Symptoms / Clinical findings
→ FHIR Condition / Observation

Medications
→ Appropriate FHIR medication resource

Allergies
→ FHIR AllergyIntolerance

Documents
→ FHIR DocumentReference

Consent
→ FHIR Consent

Clinical Summary
→ FHIR Composition where appropriate

FHIR mapping logic must remain in the FHIR service layer rather than being embedded throughout the database layer.

---

# 32. External Integration Independence

The MySQL 8.x database must remain the primary application data store.

ABDM, HIS, and AWS HealthLake integrations must not directly control core database operations.

Architecture:

MySQL 8.x
→ FHIR Service
→ Integration Adapter
→ External System

External integration failure must not cause patient intake data loss.

---

# 33. Database Security

The application must:

- Never store plain-text passwords.
- Use hashed passwords.
- Restrict database access.
- Use environment variables for credentials.
- Avoid exposing database credentials to the frontend.
- Use parameterized queries / ORM protections.
- Restrict access based on application roles.
- Maintain audit records for sensitive operations.

---

# 34. Database Deployment

MySQL 8.x

Implementation choices:

- Managed relational database (AWS RDS MySQL / GCP Cloud SQL)
- Containerized MySQL
- Native host installation

For development:

MySQL 8.x will run locally or through Docker.

---

# 35. Data Migration & Versioning

Flyway (or Liquibase)
→ MySQL 8.x

OR

Spring Boot DDL auto-management (development only)
→ MySQL 8.x
→ Backend
→ Supporting services

Production infrastructure must be configured separately from development.

---

# 35. Database Completion Criteria

The database design is considered complete when:

1. All MVP entities are represented.
2. Relationships are defined.
3. Foreign keys are implemented.
4. Required constraints are implemented.
5. Important fields are indexed.
6. AI-generated data is distinguishable from verified data.
7. Source information can be traced.
8. Documents can be linked to patients and sessions.
9. Doctor verification can be recorded.
10. Audit logs can be recorded.
11. FHIR mappings can be stored.
12. External integration status can be tracked.
13. The database can support the complete patient-to-doctor workflow.
14. The schema remains compatible with future ABDM, HIS, and AWS HealthLake integration.

---

# 36. Flyway Schema Migrations History

| Version | Description | Target Entities / Enhancements |
|---|---|---|
| `V1` | Initial setup & extensions | UUID generator extensions |
| `V2` | Core domain schema | patients, users, intake_sessions, consents, questions, answers, symptoms, triage_results, medications, allergies, medical_history |
| `V3` | Question bank seed | System questions and default options |
| `V4` | Triage rules schema & seed | Triage rule engine |
| `V5` | Ayurveda workflow schema & seed | Prakriti, Pariksha, and Ayurveda clinical history |
| `V6` | Document intelligence schema | Medical documents and OCR extractions |
| `V7` | Clinical summary AI schema | AI pre-consult summary generation & versioning |
| `V8` | Doctor review amendments | Physician review, amendments, and sign-off |
| `V9` | FHIR export schema | FHIR mappings, session linking, and validation state |
| `V10` | ABDM / ABHA integration foundation | `patients.abha_status`, `patients.abha_verified_at`, `consents.revoked_at`, `integration_records.idempotency_key`, `integration_records.response_payload` |
| `V11` | HIS integration schema | `idx_patients_his_id` indexing on `patients.his_id` for fast hospital Master Patient Index (MPI) lookups |
| `V12` | AWS HealthLake integration schema | `idx_patients_healthlake_ref` indexing on `patients.healthlake_ref` for fast cloud datastore matching |