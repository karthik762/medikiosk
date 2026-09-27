# MediKiosk — Feature Specification

## 1. Feature Priority

### MVP
Core features required for the working SIH prototype.

### SHOULD HAVE
Important features to add after the core workflow is stable.

### INTEGRATION
Features dependent on external systems, credentials, or sandbox access.

---

# 2. Patient Kiosk

## F-001 — Patient Registration

Priority: MVP

Purpose:
Create a patient profile before starting clinical intake.

Features:
- Patient ID generation or existing patient lookup
- Name
- Age / Date of Birth
- Gender
- Contact information
- Basic demographic information

Output:
Patient profile linked to an intake session.

---

## F-002 — Patient Consent

Priority: MVP

Purpose:
Obtain patient consent before collecting and processing information.

Features:
- Consent screen
- Privacy information
- Accept / Decline
- Consent timestamp
- Consent status

Output:
Consent record linked to the patient session.

---

## F-003 — Touch-Based Input

Priority: MVP

Purpose:
Allow patients to provide information without voice.

Features:
- Large touch-friendly buttons
- Multiple-choice options
- Yes / No options
- Sliders / scales
- Text input
- Symptom selection

Fallback:
Touch input must remain available if voice input fails.

---

## F-004 — Multilingual Interface

Priority: SHOULD HAVE

Purpose:
Allow patients to interact with MediKiosk in supported languages.

Features:
- Language selection
- Translated interface text
- Localized questions
- Language persistence during session

---

# 3. Voice and Language

## F-005 — Voice-to-Text

Priority: MVP

Purpose:
Allow patients to answer questions using speech.

Flow:

Patient speaks
→ Speech Recognition
→ Transcribed text
→ Confirmation
→ Save response

Requirements:
- Microphone permission handling
- Recording state
- Transcription state
- Error handling
- Retry
- Touch/text fallback

---

## F-006 — Text-to-Speech

Priority: MVP

Purpose:
Read questions aloud to patients.

Flow:

Question
→ Text-to-Speech
→ Audio playback

Features:
- Play
- Pause / stop
- Replay
- Language-specific speech where supported

---

## F-007 — Voice Confirmation

Priority: MVP

Purpose:
Prevent incorrect voice transcriptions from being silently stored.

Flow:

Speech
→ Transcription
→ Patient confirmation
→ Save

Patient must be able to:
- Accept
- Edit
- Retry

---

# 4. Clinical Intake

## F-008 — Chief Complaint

Priority: MVP

Capture the primary reason for the patient's visit.

Input:
- Voice
- Text
- Touch where applicable

Output:
Structured chief complaint.

---

## F-009 — Symptom Collection

Priority: MVP

Capture relevant symptoms.

Fields may include:
- Symptom
- Location
- Duration
- Severity
- Frequency
- Onset
- Associated symptoms

---

## F-010 — Medical History

Priority: MVP

Capture:
- Previous conditions
- Previous surgeries
- Hospitalizations
- Current medications
- Allergies
- Family history
- Lifestyle information

---

## F-011 — Adaptive Question Engine

Priority: MVP

Purpose:
Ask relevant questions based on previous responses.

Flow:

Complaint
→ Identify relevant question set
→ Ask question
→ Process answer
→ Select next question
→ Complete history

Requirements:
- Conditional questions
- Question dependencies
- Skip irrelevant questions
- Required / optional questions
- Progress tracking
- Resume session

The question engine must follow the approved questionnaire specification.

---

# 5. Rapid Triage

## F-012 — Red-Flag Screening

Priority: MVP

Purpose:
Identify predefined potential red-flag symptoms during intake.

Features:
- Rule-based evaluation
- Approved clinical rules
- Trigger conditions
- Priority classification

Important:
The triage engine is a screening and routing mechanism, not a diagnostic system.

---

## F-013 — Triage Priority

Priority: MVP

Categories:

- URGENT
- PRIORITY
- STABLE

Output:
Triage result linked to the intake session.

---

## F-014 — Urgent Escalation

Priority: MVP

When an approved urgent rule is triggered:

- Display appropriate patient-facing instruction
- Alert / flag the clinical staff workflow
- Mark patient as urgent
- Show status on doctor dashboard

The system must not provide an autonomous diagnosis.

---

# 6. Ayurveda Module

## F-015 — Ayurveda History

Priority: MVP

Capture structured Ayurvedic information.

Main sections:
- Prakriti
- Nidana
- Pariksha
- Relevant Ayurveda history

---

## F-016 — Prakriti Assessment

Priority: MVP

Purpose:
Capture approved Prakriti-related responses.

Requirements:
- Structured questions
- Approved answer options
- Store individual responses
- Store assessment result where applicable

---

## F-017 — Nidana Information

Priority: MVP

Capture relevant causative factors and patient-provided information according to the approved Ayurveda questionnaire.

---

## F-018 — Pariksha Information

Priority: MVP

Capture approved examination/history-related information for structured Ayurvedic documentation.

---

## F-019 — Ayurveda Terminology

Priority: SHOULD HAVE

Support:
- NAMASTE terminology
- ICD-11 TM2 terminology/mapping where applicable

---

# 7. Document Intelligence

## F-020 — Document Upload

Priority: MVP

Supported inputs:
- Images
- PDFs
- Camera captures

Document types:
- Prescriptions
- Laboratory reports
- Previous medical records
- Discharge documents

---

## F-021 — Document Scanner

Priority: MVP

Allow the kiosk/device camera to capture medical documents.

Features:
- Camera access
- Capture
- Preview
- Retake
- Confirm
- Upload

---

## F-022 — OCR

Priority: MVP

Flow:

Document
→ OCR
→ Extracted text
→ Confidence information
→ Structured extraction

Requirements:
- Printed text recognition
- Handwriting handling where supported
- OCR confidence
- Error handling

---

## F-023 — Medical Entity Extraction

Priority: MVP

Extract relevant information such as:

- Medications
- Dosages
- Tests
- Results
- Dates
- Conditions
- Symptoms
- Allergies

Extracted information must remain traceable to the source document.

---

## F-024 — Source Document Verification

Priority: MVP

Doctor must be able to:

- View original document
- View extracted information
- Compare extracted information with source
- Correct incorrect extraction

---

## F-025 — Medical Timeline

Priority: SHOULD HAVE

Organize extracted and recorded information chronologically.

Example:

Date
→ Consultation
→ Prescription
→ Laboratory report
→ Follow-up

---

# 8. AI Intelligence

## F-026 — AI Information Structuring

Priority: MVP

Convert patient responses and extracted information into structured clinical fields.

Input:
- Patient responses
- Medical history
- Documents
- Triage result
- Ayurveda information

Output:
Structured clinical data.

---

## F-027 — AI Clinical Summary

Priority: MVP

Generate a concise pre-consultation summary.

Possible sections:
- Chief complaint
- Symptoms
- Duration
- Relevant history
- Medications
- Allergies
- Documents
- Triage status
- Ayurveda information

---

## F-028 — Missing Information Detection

Priority: SHOULD HAVE

Identify important fields that remain unanswered or uncertain.

Output:
List of missing or uncertain information for review.

---

## F-029 — AI Uncertainty Handling

Priority: MVP

AI must:
- Preserve uncertainty
- Never invent information
- Identify low-confidence extraction
- Distinguish patient-reported information from AI-generated structure
- Allow doctor correction

---

## F-030 — AI Clinical Safety

Priority: MVP

AI must NOT:
- Independently diagnose
- Prescribe medication
- Make final treatment decisions
- Override doctors
- Mark information as verified without doctor confirmation

---

# 9. Doctor Dashboard

## F-031 — Doctor Authentication

Priority: MVP

Features:
- Login
- Secure authentication
- Role verification
- Session management

---

## F-032 — Patient Queue

Priority: MVP

Display:
- Waiting patients
- Active patients
- Completed patients
- Priority / urgent status

---

## F-033 — Patient Overview

Priority: MVP

Display:
- Patient information
- Intake status
- Triage status
- Chief complaint
- Key clinical information

---

## F-034 — Clinical History View

Priority: MVP

Doctor can view:
- Symptoms
- Medical history
- Medications
- Allergies
- Family history
- Lifestyle information

---

## F-035 — Ayurveda History View

Priority: MVP

Doctor can view:
- Prakriti
- Nidana
- Pariksha
- Other collected Ayurveda information

---

## F-036 — Document Viewer

Priority: MVP

Doctor can:
- Open uploaded documents
- View original source
- View OCR output
- Review extracted entities

---

## F-037 — AI Summary View

Priority: MVP

Display:
- Structured clinical summary
- Triage status
- Extracted information
- Uncertainty / confidence indicators
- Source references where applicable

---

## F-038 — Doctor Editing

Priority: MVP

Doctor can:
- Edit AI-generated information
- Correct OCR extraction
- Add notes
- Modify structured fields

---

## F-039 — Doctor Verification

Priority: MVP

Doctor can explicitly verify the final summary.

Verification must record:
- Doctor
- Timestamp
- Summary version
- Verification status

---

# 10. Administration

## F-040 — Admin Authentication

Priority: SHOULD HAVE

Secure administrator login with role-based permissions.

---

## F-041 — Patient Management

Priority: SHOULD HAVE

Admin can:
- Search patients
- View basic patient records
- Manage patient records according to permissions

---

## F-042 — Doctor Management

Priority: SHOULD HAVE

Admin can:
- Add doctors
- Manage doctor accounts
- Assign roles
- Activate/deactivate accounts

---

## F-043 — System Analytics

Priority: SHOULD HAVE

Possible metrics:
- Number of intakes
- Completed intakes
- Average intake duration
- Triage distribution
- Document processing volume
- AI summary generation volume

---

# 11. Security

## F-044 — Role-Based Access Control

Priority: MVP

Roles:
- Patient
- Doctor
- Admin

Each role must have restricted permissions.

---

## F-045 — Audit Logging

Priority: MVP

Record important actions including:

- Login
- Patient record access
- Data creation
- Data modification
- Document access
- AI summary generation
- Doctor verification
- Integration activity

---

## F-046 — Session Management

Priority: MVP

Features:
- Session timeout
- Logout
- Expired session handling
- Kiosk session reset

---

## F-047 — Consent Management

Priority: MVP

Store:
- Consent status
- Consent timestamp
- Consent version
- Related patient/session

---

# 12. FHIR

## F-048 — FHIR-Compatible Data Model

Priority: INTEGRATION

MediKiosk data should be mapped to appropriate FHIR resources.

Expected resources include:

- Patient
- Encounter
- Condition
- Observation
- Medication-related resources
- AllergyIntolerance
- DocumentReference
- Consent
- Composition

---

## F-049 — FHIR Data Exchange

Priority: INTEGRATION

Support structured FHIR JSON representation for interoperability.

FHIR must remain separated from the core application through a dedicated FHIR service/layer.

---

# 13. ABDM

## F-050 — ABDM Integration Layer

Priority: INTEGRATION

Create a dedicated ABDM integration service.

Responsibilities:
- ABDM authentication/integration
- Consent-related workflows
- Health-record exchange
- FHIR-based communication

Actual production integration depends on required ABDM access and credentials.

---

## F-051 — ABHA Integration

Priority: INTEGRATION

Where supported and authorized:

- ABHA-linked patient identification
- Patient lookup/linking
- Consent-based health information exchange

---

# 14. HIS Integration

## F-052 — HIS Integration Layer

Priority: INTEGRATION

MediKiosk should support communication with a Hospital Information System.

Potential HIS → MediKiosk data:
- Patient information
- Existing history
- Previous encounters
- Appointments
- Medical records

Potential MediKiosk → HIS data:
- Patient registration
- Encounter
- Clinical intake
- Triage
- Documents
- Structured summary

---

## F-053 — Mock HIS

Priority: MVP / DEVELOPMENT

Create a mock HIS environment for development and demonstration when a real hospital HIS is unavailable.

The mock system must clearly be identified as a simulation.

---

# 15. AWS HealthLake

## F-054 — HealthLake Integration Layer

Priority: INTEGRATION

Create a dedicated HealthLake adapter.

Architecture:

MediKiosk
→ Spring Boot
→ FHIR Layer
→ HealthLake

Potential responsibilities:
- FHIR data preparation
- FHIR resource transmission
- Record retrieval
- Integration error handling

Actual AWS deployment requires appropriate AWS account configuration and credentials.

---

# 16. Error Handling and Fallbacks

## F-055 — Voice Failure

If voice recognition fails:

Voice
→ Retry
→ Text / Touch fallback

---

## F-056 — OCR Failure

If OCR fails or confidence is low:

OCR
→ Flag uncertainty
→ Preserve original document
→ Doctor verification

---

## F-057 — AI Failure

If AI service is unavailable:

- Preserve collected patient information
- Continue manual workflow where possible
- Notify appropriate system component
- Never block access to already collected information

---

## F-058 — Integration Failure

If ABDM, HIS, or HealthLake is unavailable:

- Preserve local structured data
- Record integration failure
- Retry where appropriate
- Do not lose patient information
- Clearly indicate synchronization status

---

# 17. Core End-to-End Workflow

The complete MVP workflow is:

Patient
→ Registration
→ Consent
→ Chief Complaint
→ Voice / Text / Touch
→ Adaptive Questions
→ Red-Flag Triage
→ Conventional History
→ Ayurveda History
→ Document Upload / Scan
→ OCR
→ AI Structuring
→ Clinical Summary
→ Doctor Dashboard
→ Doctor Review
→ Doctor Edit
→ Doctor Verification
→ Consultation

FHIR, ABDM, HIS, and AWS HealthLake operate through the interoperability layer and must not break the core intake workflow if unavailable.

# 18. Feature Priority Summary

## MVP

F-001 to F-014
F-015 to F-018
F-020 to F-024
F-026 to F-030
F-031 to F-039
F-044 to F-049
F-053
F-055 to F-058

## SHOULD HAVE

F-004
F-019
F-025
F-028
F-040 to F-043

## INTEGRATION

F-050
F-051
F-052
F-054

# 19. Product Completion Criteria

The MVP is considered functional when:

1. A patient can register and provide consent.
2. A patient can complete an intake using voice, text, or touch.
3. Questions can adapt based on previous responses.
4. Red-flag screening can classify predefined cases.
5. Conventional history can be collected.
6. Ayurveda history can be collected.
7. Documents can be uploaded or scanned.
8. OCR can extract document information.
9. AI can structure patient information.
10. AI can generate a clinical summary.
11. A doctor can view the complete patient intake.
12. A doctor can view source documents.
13. A doctor can edit AI/OCR results.
14. A doctor can verify the final summary.
15. Data is stored in MySQL 8.x.
16. Clinical data can be represented using FHIR-compatible structures.
17. Security and audit controls are implemented.
18. External integrations can be connected through dedicated integration layers.