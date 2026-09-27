# MediKiosk — API Specification

## 1. API Overview

MediKiosk uses a RESTful backend API built with Spring Boot.

Architecture:

Frontend
  ↓ (HTTPS / REST)
Spring Boot Backend
  ↓
MySQL 8.x

---

# 2. API Design Principles

- RESTful design.
- JSON request/response formats.
- Clear HTTP status codes.
- Strict input validation.
- Role-based authorization where required.
- Consistent error formatting.
- No direct database access from frontend.

The frontend must never communicate directly with MySQL 8.x or external healthcare systems.

---

# 2. API Base URL

Development:

    http://localhost:8080/api

Production:

    /api

All API responses should use JSON unless the endpoint explicitly returns a file or binary response.

---

# 3. Authentication & Authorization

Authentication uses:

- Spring Security
- Stateless JWT access tokens (HMAC-SHA256)
- Role-based access control (RBAC)

Roles:

- PATIENT
- DOCTOR
- ADMIN

Example header:

    Authorization: Bearer <access_token>

### Endpoints

#### POST /api/auth/login
Public endpoint for user authentication.

Request:
```json
{
  "email": "doctor@hospital.org",
  "password": "SecurePassword123!"
}
```

Response (200 OK):
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOi...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "email": "doctor@hospital.org",
    "firstName": "John",
    "lastName": "Doe",
    "role": "DOCTOR",
    "status": "ACTIVE"
  },
  "message": "Authentication successful"
}
```

Failure Responses:
- 400 Bad Request: Missing or invalid request fields
- 401 Unauthorized: Invalid credentials or inactive/locked account

#### GET /api/auth/me
Protected endpoint returning current authenticated user profile.

Headers:
```
Authorization: Bearer <access_token>
```

Response (200 OK):
```json
{
  "success": true,
  "data": {
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "email": "doctor@hospital.org",
    "firstName": "John",
    "lastName": "Doe",
    "role": "DOCTOR",
    "status": "ACTIVE"
  },
  "message": "Operation completed successfully"
}
```

Failure Responses:
- 401 Unauthorized: Missing, expired, or malformed JWT token
- 403 Forbidden: Insufficient role permissions for restricted resource

---

# 4. Standard Response Format

Successful response:

```json
{
  "success": true,
  "data": {},
  "message": "Operation completed successfully"
}
```

---

# 5. Patient Registration Endpoints

#### POST /api/patients
Registers a patient profile.
- Authentication: Protected (Bearer JWT required)
- Authorization: PATIENT (binds to current user), DOCTOR, ADMIN

Request:
```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "dateOfBirth": "1990-05-15",
  "gender": "FEMALE",
  "phone": "+919876543210",
  "email": "jane.doe@example.com"
}
```

Response (201 Created):
```json
{
  "success": true,
  "data": {
    "id": "c1f3c3a4-8b65-4d2c-9a1b-123456789abc",
    "patientIdentifier": "MK-PAT-AB12CD34",
    "firstName": "Jane",
    "lastName": "Doe",
    "dateOfBirth": "1990-05-15",
    "gender": "FEMALE",
    "phone": "+919876543210",
    "email": "jane.doe@example.com",
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
  },
  "message": "Patient registered successfully"
}
```

Failure Responses:
- 400 Bad Request: Missing required names
- 409 Conflict: Duplicate patient identifier

#### GET /api/patients/{id}
Retrieves patient profile by UUID.
- Authentication: Protected
- Authorization: Owner PATIENT, DOCTOR, ADMIN (Cross-patient access forbidden with 403)

---

# 6. Patient Intake Session Endpoints

All intake endpoints require authentication and enforce patient ownership / clinical role authorization.

#### POST /api/intake/sessions
Initializes an intake session for a patient (Status: CREATED).

#### GET /api/intake/sessions/{sessionId}
Retrieves current session details, status, and timestamps.

#### POST /api/intake/sessions/{sessionId}/consent
Records explicit patient consent. If `ACCEPTED`, advances status to `IN_PROGRESS`.
- If `DECLINED`, all subsequent clinical data collection endpoints return 400 Bad Request.

#### POST /api/intake/sessions/{sessionId}/complaint
Stores patient chief complaint as structured source answer. Requires valid consent.

#### POST /api/intake/sessions/{sessionId}/symptoms
Stores reported symptoms (name, location, duration, severity, frequency, onset). Requires valid consent.

#### POST /api/intake/sessions/{sessionId}/medical-history
Stores medical history items by category (PAST_HISTORY, SURGICAL_HISTORY, FAMILY_HISTORY, LIFESTYLE). Requires valid consent.

#### POST /api/intake/sessions/{sessionId}/medications
Stores current medications (name, dosage, frequency, duration). Requires valid consent.

#### POST /api/intake/sessions/{sessionId}/allergies
Stores patient allergies and reactions. Requires valid consent.

#### GET /api/intake/sessions/{sessionId}/questions
Retrieves approved adaptive intake questions evaluated against recorded answers and condition branching rules. Requires valid consent.

#### POST /api/intake/sessions/{sessionId}/answers
Records answer to an approved question. Requires valid consent.

#### POST /api/intake/sessions/{sessionId}/complete
Finalizes intake session:
- Validates active consent exists and is `ACCEPTED`.
- Validates intake data (complaint/answers/symptoms) exists.
- Sets status to `COMPLETED` and records `completedAt`.
- Subsequent attempts to modify or re-complete return 409 Conflict.

---

### 7. Rapid Triage APIs

Deterministic clinical screening and routing engine conforming to `docs/TRIAGE_RULES.md`. Evaluates approved rules without LLM or AI generation, producing routing classifications (`URGENT`, `PRIORITY`, `STABLE`).

#### POST /api/intake/sessions/{sessionId}/triage
Evaluates structured clinical intake responses against active triage rules.
- Supported alias: `POST /api/sessions/{sessionId}/triage`
- Requires authentication (`ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_ADMIN`).
- Enforces session ownership: patients cannot evaluate other patients' sessions (403 Forbidden).
- Preconditions: Requires active `ACCEPTED` consent; requires structured intake data; blocks modification on completed or cancelled sessions (409 Conflict).
- Idempotent: Subsequent evaluations update the existing `TriageResult` record rather than creating duplicates.
- Priority resolution: Highest priority wins (`URGENT` > `PRIORITY` > `STABLE`), while preserving all triggered rule IDs in the audit trace.

Request Body (Optional):
```json
{
  "parameters": {
    "testFlag": true
  }
}
```

Response (Clinician view - Doctor/Admin):
```json
{
  "success": true,
  "message": "Triage screening evaluated successfully",
  "data": {
    "sessionId": "11111111-1111-1111-1111-111111111100",
    "priority": "URGENT",
    "status": "COMPLETED",
    "triggeredRules": [
      {
        "ruleId": "TRIAGE-001",
        "version": "1.0",
        "name": "Severe Symptom Intensity Flag",
        "priority": "URGENT",
        "reason": "Configured red-flag condition detected: Severe symptom intensity (severity >= 8) reported"
      }
    ],
    "reasons": [
      "Configured red-flag condition detected: Severe symptom intensity (severity >= 8) reported"
    ],
    "explanation": "Potential red flag identified: Configured red-flag condition detected: Severe symptom intensity (severity >= 8) reported",
    "patientInstruction": "Your responses indicate that a healthcare professional should review your information promptly.",
    "evaluatedAt": "2026-09-11T20:45:00Z"
  }
}
```

Response (Patient view - Role Sanitized):
```json
{
  "success": true,
  "message": "Triage screening evaluated successfully",
  "data": {
    "sessionId": "11111111-1111-1111-1111-111111111100",
    "priority": "URGENT",
    "status": "COMPLETED",
    "triggeredRules": [],
    "reasons": [
      "Your responses indicate that a healthcare professional should review your information promptly."
    ],
    "explanation": "Potential red flag identified: Configured red-flag condition detected: Severe symptom intensity (severity >= 8) reported",
    "patientInstruction": "Your responses indicate that a healthcare professional should review your information promptly.",
    "evaluatedAt": "2026-09-11T20:45:00Z"
  }
}
```

#### GET /api/intake/sessions/{sessionId}/triage
Retrieves the stored triage screening result for an intake session.
- Supported alias: `GET /api/sessions/{sessionId}/triage`
- Requires authentication (`ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_ADMIN`).
- Enforces ownership (403 for unauthorized patient access).
- Returns 404 Not Found if triage has not yet been evaluated for the session.
- Sanitized for patient caller; complete audit trace returned for doctor/admin.

---

### 8. Voice and Speech APIs

Voice input is strictly an **INPUT METHOD**. Voice transcription and synthesis do **NOT** determine diagnoses, treatments, or clinical conclusions. Transcripts must be explicitly confirmed by the patient before being saved into the intake record.

#### POST /api/voice/transcribe
Submits an audio recording for speech-to-text transcription.
- Supported alias: `POST /api/voice/sessions/{sessionId}/transcribe`
- Requires authentication (`ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_ADMIN`).
- Enforces session ownership (403 for cross-patient access).
- Rejects completed sessions (400/409).
- Requires active accepted patient consent.
- Content-Type: `multipart/form-data`.
- Maximum audio size: 10MB.
- Supported audio formats: WAV, WebM, MP3, OGG, MP4/M4A, AAC (validated via magic bytes).
- Supported languages: `en`, `hi`, `te` (and BCP-47 variants `en-IN`, `hi-IN`, `te-IN`).

Request Parameters:
- `file`: Multipart audio file (required).
- `sessionId`: UUID of the active intake session (required).
- `language`: Target spoken language code (optional; defaults to session language or `en`).

Response (200 OK):
```json
{
  "success": true,
  "message": "Audio transcribed successfully. Please confirm transcript before saving.",
  "data": {
    "sessionId": "11111111-1111-1111-1111-111111111100",
    "transcript": "I have had a mild headache and fever for two days.",
    "confidence": 0.9500,
    "language": "en",
    "status": "SUCCESS",
    "isMock": true
  }
}
```

#### Saving Confirmed Voice Answer
After reviewing the transcription, the patient confirms or edits the text. The answer is submitted using the existing intake answers API:

`POST /api/intake/sessions/{sessionId}/answers`
```json
{
  "questionId": "11111111-1111-1111-1111-111111111101",
  "answerText": "I have had a mild headache and fever for two days.",
  "inputMethod": "VOICE",
  "originalTranscription": "I have had a mild headache and fever for two days.",
  "confidenceScore": 0.9500,
  "confirmedByPatient": true
}
```

- If `inputMethod == "VOICE"` and `confirmedByPatient != true`, the backend rejects with HTTP 400 Bad Request.
- If the patient rejects the transcript, no answer is saved, and the patient falls back to touch/text.

#### POST /api/voice/synthesize
Synthesizes approved UI or question text into spoken audio (Text-to-Speech).
- Requires authentication (`ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_ADMIN`).
- Content-Type: `application/json`.
- Maximum text length: 1000 characters.
- Generates standard PCM WAV audio stream (`audio/wav`).

Request:
```json
{
  "text": "What is the primary reason for your visit today?",
  "language": "en",
  "sessionId": "11111111-1111-1111-1111-111111111100"
}
```

Response:
- HTTP 200 OK
- Content-Type: `audio/wav`
- Content-Disposition: `inline; filename="speech_prompt.wav"`
- Binary audio data stream.

---

### 9. Structured Ayurveda Intake Workflow APIs

Conforming to `docs/AYURVEDA_SPEC.md`, `docs/PRODUCT_SPEC.md`, and `docs/USER_FLOWS.md`.

#### Record / Update Ayurveda History Draft
- `POST /api/intake/sessions/{sessionId}/ayurveda`
- Supported alias: `POST /api/ayurveda/sessions/{sessionId}`
- Requires authentication (`ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_ADMIN`).
- Validates session ownership (403 for cross-patient access).
- Rejects completed sessions (409 Conflict).
- Requires active accepted patient consent (400 Bad Request if missing).
- Status set to `DRAFT` upon save; if modifying an already confirmed draft, reverts to `DRAFT` and requires re-confirmation. Rejects if session history is already `DOCTOR_REVIEWED` (409 Conflict).

Request Body:
```json
{
  "prakriti": {
    "bodyFrame": "SLENDER",
    "weatherPreference": "DISLIKES_COLD",
    "digestionPattern": "IRREGULAR",
    "sleepPattern": "LIGHT_INTERRUPTED",
    "skinTexture": "DRY",
    "hairTexture": "ROUGH",
    "temperament": "ACTIVE_RESTLESS",
    "additionalPrakritiDetails": {}
  },
  "nidana": {
    "dietaryHabits": "IRREGULAR_MEALS",
    "activityPattern": "SEDENTARY",
    "stressFactors": "HIGH_MENTAL_STRESS",
    "sleepSchedule": "LATE_NIGHT",
    "seasonalAggravation": "WINTER",
    "additionalNidanaDetails": {}
  },
  "pariksha": {
    "koshtha": "KRURA",
    "bala": "MADHYAMA",
    "jihva": "WHITE_COATING",
    "agni": "VISHAMA",
    "mutra": "NORMAL",
    "additionalParikshaDetails": {}
  },
  "notes": "Patient reports constitutional dry skin and irregular appetite during travel.",
  "terminology": [
    {
      "conceptName": "Prakriti",
      "sanskritTerm": "प्रकृति",
      "namasteCode": null,
      "namasteDisplay": null,
      "icd11Tm2Code": null,
      "icd11Tm2Display": null,
      "mappingStatus": "UNRESOLVED",
      "description": "Constitutional body-mind typing"
    }
  ]
}
```

Response (200 OK):
```json
{
  "success": true,
  "message": "Ayurveda intake history saved successfully",
  "data": {
    "id": "55555555-5555-5555-5555-555555555520",
    "patientId": "11111111-1111-1111-1111-111111111101",
    "sessionId": "11111111-1111-1111-1111-111111111100",
    "status": "DRAFT",
    "patientConfirmed": false,
    "confirmedAt": null,
    "prakriti": { ... },
    "nidana": { ... },
    "pariksha": { ... },
    "terminology": [ ... ],
    "notes": "Patient reports constitutional dry skin and irregular appetite during travel.",
    "createdAt": "2026-09-11T12:00:00Z",
    "updatedAt": "2026-09-11T12:00:00Z"
  }
}
```

#### Get Ayurveda History
- `GET /api/intake/sessions/{sessionId}/ayurveda`
- Supported alias: `GET /api/ayurveda/sessions/{sessionId}`
- Requires authentication (`ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_ADMIN`).
- Validates session ownership (403 for cross-patient access).
- Returns 404 if no Ayurveda history has been recorded yet for this session.

Response (200 OK):
```json
{
  "success": true,
  "message": "Ayurveda intake history retrieved successfully",
  "data": {
    "id": "55555555-5555-5555-5555-555555555520",
    "patientId": "11111111-1111-1111-1111-111111111101",
    "sessionId": "11111111-1111-1111-1111-111111111100",
    "status": "DRAFT",
    "patientConfirmed": false,
    "confirmedAt": null,
    "prakriti": { ... },
    "nidana": { ... },
    "pariksha": { ... },
    "terminology": [ ... ],
    "notes": "...",
    "createdAt": "2026-09-11T12:00:00Z",
    "updatedAt": "2026-09-11T12:00:00Z"
  }
}
```

#### Patient Confirmation of Ayurveda Intake
- `POST /api/intake/sessions/{sessionId}/ayurveda/confirm`
- Supported alias: `POST /api/ayurveda/sessions/{sessionId}/confirm`
- Requires authentication (`ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_ADMIN`).
- Validates session ownership.
- Rejects completed sessions (409 Conflict).
- Requires active accepted patient consent.
- Transitions status to `PATIENT_CONFIRMED`, sets `patientConfirmed = true`, records `confirmedAt = now()`.
- Emits audit log event `AYURVEDA_CONFIRMED`.

Response (200 OK):
```json
{
  "success": true,
  "message": "Ayurveda intake history confirmed by patient successfully",
  "data": {
    "id": "55555555-5555-5555-5555-555555555520",
    "patientId": "11111111-1111-1111-1111-111111111101",
    "sessionId": "11111111-1111-1111-1111-111111111100",
    "status": "PATIENT_CONFIRMED",
    "patientConfirmed": true,
    "confirmedAt": "2026-09-11T12:05:00Z",
    "prakriti": { ... },
    "nidana": { ... },
    "pariksha": { ... },
    "terminology": [ ... ],
    "notes": "...",
    "createdAt": "2026-09-11T12:00:00Z",
    "updatedAt": "2026-09-11T12:05:00Z"
  }
}
```

---

### 10. Document Intelligence and OCR APIs

Conforming to `docs/PRODUCT_SPEC.md`, `docs/USER_FLOWS.md`, `docs/DATABASE.md`, `docs/AI_SPEC.md`, and `docs/SECURITY.md`.

#### Upload and Process Medical Document
- `POST /api/intake/sessions/{sessionId}/documents`
- Content-Type: `multipart/form-data`
- Requires authentication (`ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_ADMIN`).
- Validates session ownership (403 for cross-patient access).
- Rejects completed sessions (409 Conflict).
- Requires active accepted patient consent (400 Bad Request if missing/declined).
- Validates file presence, size (max 10MB), permitted MIME types (`application/pdf`, `image/jpeg`, `image/png`, `image/webp`), and byte-level magic signatures.
- Computes SHA-256 hash and persists file securely via `DocumentStorageProvider`.
- Synchronously invokes `OcrProvider` abstraction.
- Status set to `COMPLETED` (`SUCCESS`) if confidence >= 0.75, `REVIEW_REQUIRED` (`LOW_CONFIDENCE`) if confidence < 0.75, or `FAILED` if OCR engine fails. Source document binary is **always retained**.

Request Parameters (Multipart):
- `file`: Multipart binary document file (required).
- `documentType`: `PRESCRIPTION` | `LAB_REPORT` | `MEDICAL_RECORD` | `DISCHARGE_SUMMARY` | `OTHER` (optional, defaults to `OTHER`).

Response (201 Created):
```json
{
  "success": true,
  "message": "Document uploaded and processed successfully",
  "data": {
    "id": "66666666-6666-6666-6666-666666666601",
    "patientId": "11111111-1111-1111-1111-111111111101",
    "sessionId": "11111111-1111-1111-1111-111111111100",
    "documentType": "PRESCRIPTION",
    "fileName": "prescription_scan.png",
    "fileType": "image/png",
    "fileSize": 1048576,
    "fileHash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    "processingStatus": "COMPLETED",
    "uploadedAt": "2026-09-11T12:10:00Z",
    "processedAt": "2026-09-11T12:10:01Z",
    "extraction": {
      "id": "77777777-7777-7777-7777-777777777701",
      "documentId": "66666666-6666-6666-6666-666666666601",
      "extractedText": "Rx: City Health Care Clinic\nDate: 2026-09-01\n...",
      "confidenceScore": 0.9450,
      "processingEngine": "MOCK_OCR_PROVIDER",
      "status": "SUCCESS",
      "uncertaintyFlags": [],
      "extractionData": {
        "facilityName": "City Health Care Clinic",
        "documentDate": "2026-09-01",
        "medications": [
          {
            "name": "Metformin",
            "dosage": "500 mg",
            "frequency": "Twice daily with meals",
            "confidence": 0.96
          }
        ]
      },
      "createdAt": "2026-09-11T12:10:01Z"
    }
  }
}
```

#### List Session Documents
- `GET /api/intake/sessions/{sessionId}/documents`
- Requires authentication (`ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_ADMIN`).
- Validates session ownership.

Response (200 OK):
```json
{
  "success": true,
  "message": "Session documents retrieved successfully",
  "data": [
    {
      "id": "66666666-6666-6666-6666-666666666601",
      "patientId": "11111111-1111-1111-1111-111111111101",
      "sessionId": "11111111-1111-1111-1111-111111111100",
      "documentType": "PRESCRIPTION",
      "fileName": "prescription_scan.png",
      "fileType": "image/png",
      "fileSize": 1048576,
      "processingStatus": "COMPLETED",
      "uploadedAt": "2026-09-11T12:10:00Z",
      "processedAt": "2026-09-11T12:10:01Z",
      "extraction": { ... }
    }
  ]
}
```

#### Get Document Details and Extraction
- `GET /api/intake/sessions/{sessionId}/documents/{documentId}`
- Supported direct alias: `GET /api/documents/{documentId}`
- Requires authentication.
- Validates session and document ownership.

Response (200 OK):
Returns the complete `DocumentUploadResponse` including latest extraction data.

#### Secure Source Document Stream / Download
- `GET /api/intake/sessions/{sessionId}/documents/{documentId}/file`
- Supported direct alias: `GET /api/documents/{documentId}/file`
- Requires authentication.
- Validates ownership (403 for cross-patient attempts).
- Never exposes filesystem paths or raw storage references.
- Streams the source document binary.
- Header `Content-Type`: MIME type of document (e.g. `application/pdf`, `image/png`).
- Header `Content-Disposition`: `inline; filename="<sanitizedFileName>"`.
- Emits audit log event `DOCUMENT_ACCESSED`.

---

### 11. AI Clinical Structuring & Pre-Consult Summary APIs

Conforming to `docs/AI_SPEC.md`, `docs/PRODUCT_SPEC.md`, `docs/DATABASE.md`, and `docs/SECURITY.md`.

#### Generate / Regenerate Pre-Consult Clinical Summary
- `POST /api/intake/sessions/{sessionId}/clinical-summary/generate`
- Requires authentication (`ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_ADMIN`).
- Validates session ownership (403 for cross-patient access).
- Requires active, accepted patient consent (`400 BAD_REQUEST` if missing).
- Aggregates patient answers, symptoms, medical history, medications, allergies, Ayurveda history, OCR document findings, and rapid triage results into the structuring context.
- Invokes pluggable AI provider abstraction (deterministic `MockAiProvider` by default).
- Deterministic Post-Generation Safety Validation:
  - Asserts non-diagnostic framing (rejects diagnostic assertions, ICD/SNOMED diagnostic claims).
  - Asserts assistive role (rejects prescriptions and therapy recommendations).
  - Asserts triage fidelity (strictly preserves rule engine priority and reasons, rejecting AI overrides).
  - Asserts doctor authority (status initialized to `PENDING` doctor verification; rejects AI verification spoofing).
  - Asserts grounding (every finding traceable to patient input, OCR, or triage; rejects ungrounded hallucinations).
- Persists structured summary to MySQL 8.x `clinical_summaries` table.
- Emits audit log event `CLINICAL_SUMMARY_GENERATED`.

Response (201 Created):
```json
{
  "success": true,
  "message": "AI Clinical pre-consult summary generated successfully",
  "data": {
    "id": "77777777-7777-7777-7777-777777777701",
    "sessionId": "11111111-1111-1111-1111-111111111100",
    "patientId": "11111111-1111-1111-1111-111111111101",
    "chiefComplaint": "Fever and mild persistent cough",
    "symptoms": [
      {
        "name": "Fever",
        "severity": 6,
        "duration": "3 days",
        "onset": "Acute",
        "frequency": "Continuous",
        "location": "Whole body",
        "source": "PATIENT_RESPONSE"
      }
    ],
    "medicalHistory": [
      {
        "category": "CHRONIC_CONDITION",
        "description": "Type 2 Diabetes Mellitus",
        "source": "PATIENT_RESPONSE"
      }
    ],
    "medications": [
      {
        "medicationName": "Metformin",
        "dosage": "500 mg",
        "frequency": "Twice daily",
        "duration": "2 years",
        "source": "PATIENT_RESPONSE"
      }
    ],
    "allergies": [
      {
        "allergen": "Penicillin",
        "reaction": "Skin rash and hives",
        "severity": "MODERATE",
        "source": "PATIENT_RESPONSE"
      }
    ],
    "ayurvedaSummary": {
      "prakritiAssessment": "Vata-Pitta",
      "agniType": "Tikshnagni",
      "kosthaType": "Krura",
      "nidanaFactors": "Irregular spicy food",
      "source": "AYURVEDA_HISTORY"
    },
    "documentFindings": [
      {
        "documentId": "66666666-6666-6666-6666-666666666601",
        "documentType": "PRESCRIPTION",
        "fileName": "rx.png",
        "findings": "Rx: Metformin 500 mg daily",
        "confidence": 0.95,
        "source": "OCR_EXTRACTION"
      }
    ],
    "triage": {
      "priority": "PRIORITY",
      "summaryReason": "Screened for moderate priority review",
      "triggeredRules": ["TRIAGE-008"],
      "source": "RULE_ENGINE"
    },
    "missingInformation": [],
    "potentialConflicts": [],
    "doctorVerification": {
      "status": "PENDING"
    },
    "metadata": {
      "generationModel": "medikiosk-clinical-structuring-mock-v1",
      "generatedAt": "2026-09-11T12:00:00Z",
      "isMock": true,
      "version": 1
    },
    "status": "AI_GENERATED"
  }
}
```

#### Get Clinical Summary by Session
- `GET /api/intake/sessions/{sessionId}/clinical-summary`
- Requires authentication (`ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_ADMIN`).
- Validates session ownership (403 for unauthorized users).
- Emits audit log event `CLINICAL_SUMMARY_VIEWED`.
- Returns latest structured summary for the intake session.

#### Get Clinical Summary by ID
- `GET /api/intake/clinical-summary/{summaryId}`
- Requires authentication.
- Validates session ownership (403 for unauthorized users).
- Emits audit log event `CLINICAL_SUMMARY_VIEWED`.
- Returns structured summary by summary UUID.

---

# 12. Doctor Consultation Queue & Clinical Review APIs

Access to all `/api/doctor/**` endpoints strictly requires `ROLE_DOCTOR` or `ROLE_ADMIN`. Patient tokens receive 403 Forbidden.

### 12.1 Consultation Queue
- `GET /api/doctor/queue`
- Optional query parameters:
  - `priority`: Filter by triage priority (`EMERGENCY`, `URGENT`, `PRIORITY`, `ROUTINE`, `STABLE`)
  - `status`: Filter by summary status (`AI_GENERATED`, `DOCTOR_EDITED`, `VERIFIED`)
- Orders queue deterministically:
  1. Triage Priority: `EMERGENCY` > `URGENT` > `PRIORITY` > `ROUTINE` > `STABLE`
  2. Creation Time: Oldest session first (FIFO within same triage tier)
- Patient names, identifier, age, gender, masked phone, chief complaint, session status, triage priority, summary status, and review status.

### 12.2 Comprehensive Case Review Workstation
- `GET /api/doctor/sessions/{sessionId}/review`
- Aggregates full case information across all MediKiosk subsystems:
  - Patient demographics (masked phone, calculated age, DOB, patient identifier)
  - Deterministic Triage evaluation (priority, explanation, triggered rules, rule engine source tag)
  - Pre-consult clinical summary (AI generated or amended, non-diagnostic disclaimer, source tags, conflicts)
  - Original patient intake source data (immutable Q&A transcripts with input method, symptoms, conventional history, medications, allergies)
  - Ayurveda case-taking profile (Prakriti, Pariksha, Nidana, patient confirmation)
  - Uploaded medical documents with OCR extractions and confidence scores
  - Audit history of all previous clinician reviews and edits
- Resilient: Operates fully when AI pre-consult summary or document OCR is unavailable.
- Emits audit event `DOCTOR_REVIEW_OPENED`.

### 12.3 Doctor Summary Amendments (Optimistic Locking)
- `PUT /api/doctor/sessions/{sessionId}/summary/amend`
- Request:
```json
{
  "expectedVersion": 1,
  "amendedSummary": {
    "chiefComplaint": "Persistent dry cough and high fever",
    "symptoms": [...]
  },
  "clinicalNotes": "Updated severity following clinician examination",
  "conflictResolutions": {
    "medication": "Patient confirms taking generic Albuterol"
  }
}
```
- Preserves the original AI summary snapshot in `original_ai_data`.
- Increments version number (`version = expectedVersion + 1`).
- Returns 409 Conflict if `expectedVersion` does not match the current database summary version.
- Returns 409 Conflict if the summary is already `VERIFIED` (verified summaries cannot be silently amended).
- Transitions summary status to `DOCTOR_REVIEWED`.
- Creates a `DoctorReview` audit record (`action = EDITED`).
- Emits audit log event `CLINICAL_SUMMARY_AMENDED`.

### 12.4 Doctor Summary Formal Verification
- `POST /api/doctor/sessions/{sessionId}/summary/verify`
- Request:
```json
{
  "expectedVersion": 2,
  "clinicalNotes": "Intake data verified against source responses. Patient cleared for consultation.",
  "confirmUnderstood": true
}
```
- Requires explicit confirmation (`confirmUnderstood == true`).
- Enforces optimistic concurrency (`expectedVersion == currentVersion`, returns 409 on mismatch).
- Transitions summary status to `VERIFIED`.
- Records doctor identity (derived securely from JWT authentication context, preventing spoofing).
- Creates a `DoctorReview` audit record (`action = VERIFIED`, `verifiedAt = CURRENT_TIMESTAMP`).
- Emits audit log event `CLINICAL_SUMMARY_VERIFIED`.

---

# 13. FHIR R4 Interoperability & Export Endpoints

Conforming to `docs/FHIR_MAPPING.md` and Phase 11 requirements.

### 13.1 Get Clinical Document Bundle JSON
- `GET /api/fhir/sessions/{sessionId}/bundle`
- Header: `Accept: application/fhir+json, application/json`
- Role required: `ROLE_DOCTOR` or `ROLE_ADMIN`.
- Content-Type returned: `application/fhir+json;charset=UTF-8`.
- Returns a complete, valid FHIR R4 document bundle containing:
  - `Composition` (as first entry, `status = "final"` if verified, `"preliminary"` if unverified)
  - `Patient` (with identifiers, demographics, language)
  - `Encounter` (`class = AMB`, `status = finished|in-progress`)
  - `Consent` (privacy consent)
  - `Observation` (symptoms with severity, duration, onset; triage priority with `RULE_ENGINE` source; Ayurveda findings)
  - `Condition` (past medical history problem-list items)
  - `MedicationStatement` (current medications)
  - `AllergyIntolerance` (allergies with reactions and criticality)
  - `DocumentReference` (attached documents with authenticated REST download URLs, no local filesystem paths)
  - `Provenance` (provenance tracking patient, AI structuring engine, rule engine, and verifying doctor)

### 13.2 Get FHIR Export Summary
- `GET /api/fhir/sessions/{sessionId}/export`
- Role required: `ROLE_DOCTOR` or `ROLE_ADMIN`.
- Returns metadata summary of stored FHIR export or live preview:
  - `sessionId`, `patientId`, `bundleId`, `bundleType`
  - `totalResources`, `resourceTypeCounts`
  - `verificationStatus` (`VERIFIED` | `UNVERIFIED`)
  - `compositionStatus` (`final` | `preliminary`)
  - `validationStatus` (`VALID` | `WARNING` | `INVALID`)
  - `fhirBundleJson`

### 13.3 Export and Persist FHIR Bundle
- `POST /api/fhir/sessions/{sessionId}/export`
- Role required: `ROLE_DOCTOR` or `ROLE_ADMIN`.
- Actions performed:
  - Generates FHIR R4 bundle.
  - Validates profile invariants.
  - Persists mapped resources into `fhir_mappings` table with JSON payloads.
  - Creates tracking entry in `integration_records` table (`IntegrationType.HIS`, `status = PENDING`, `errorMessage = "Adapter not implemented in Phase 11: Future adapter required for live transmission"`).
  - Emits audit log event `FHIR_BUNDLE_EXPORTED`.
  - Returns `FhirExportSummaryDto`.

### 13.4 Validate Session Bundle
- `GET /api/fhir/sessions/{sessionId}/validate`
- Role required: `ROLE_DOCTOR` or `ROLE_ADMIN`.
- Performs validation on session FHIR bundle and returns:
  - `valid`: boolean
  - `errors`: list of invariant error descriptions
  - `warnings`: list of warning messages
  - `totalChecked`: number of resources evaluated

### 13.5 Get Individual FHIR Patient
- `GET /api/fhir/patients/{patientId}`
- Role required: `ROLE_DOCTOR` or `ROLE_ADMIN`.
- Content-Type returned: `application/fhir+json;charset=UTF-8`.
- Returns standalone FHIR R4 `Patient` resource.

### 13.6 Get Individual FHIR Encounter
- `GET /api/fhir/sessions/{sessionId}/encounter`
- Role required: `ROLE_DOCTOR` or `ROLE_ADMIN`.
- Content-Type returned: `application/fhir+json;charset=UTF-8`.
- Returns standalone FHIR R4 `Encounter` resource.

---

# 14. ABDM & ABHA Integration Endpoints (Phase 12)

### 14.1 Get ABDM Adapter Status
- `GET /api/abdm/status`
- Roles allowed: `ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_ADMIN`.
- Returns operating mode (`MOCK`, `SANDBOX`, `PRODUCTION`), `isMock` boolean, connectivity flag, gateway URL, and message.

### 14.2 Verify Patient ABHA
- `POST /api/abdm/patients/{patientId}/abha/verify`
- Roles allowed: Patient owner, `ROLE_DOCTOR`, `ROLE_ADMIN`.
- Request body:
  ```json
  {
    "abhaId": "12-3456-7890-1234"
  }
  ```
- Response returns verification status, masked ABHA (`XX-XXXX-XXXX-1234`), verification method, and timestamp. Updates patient record and creates `IntegrationRecord` + `AuditLog`.

### 14.3 Grant ABDM Data Sharing Consent
- `POST /api/abdm/sessions/{sessionId}/consent`
- Roles allowed: Session patient owner, `ROLE_DOCTOR`, `ROLE_ADMIN`.
- Request body:
  ```json
  {
    "patientId": "UUID",
    "consentType": "ABDM_DATA_SHARE",
    "validMonths": 12
  }
  ```
- Creates active consent with generated consent artefact ID and logs audit event `ABDM_CONSENT_GRANTED`.

### 14.4 Revoke ABDM Consent
- `POST /api/abdm/sessions/{sessionId}/consent/revoke`
- Roles allowed: Session patient owner, `ROLE_DOCTOR`, `ROLE_ADMIN`.
- Transitions consent to `REVOKED`, sets `revokedAt`, calls provider revocation, and logs `ABDM_CONSENT_REVOKED`.

### 14.5 Exchange Clinical Summary with ABDM
- `POST /api/abdm/sessions/{sessionId}/exchange`
- Roles allowed: `ROLE_DOCTOR`, `ROLE_ADMIN`.
- Request body:
  ```json
  {
    "notes": "Routine referral exchange",
    "idempotencyKey": "ABDM-EXCHANGE-UUID"
  }
  ```
- Gating prerequisites enforced server-side:
  - Doctor verification (`ClinicalSummaryStatus == VERIFIED`)
  - Active patient consent (`consentType == "ABDM_DATA_SHARE"`, `status == ACCEPTED`)
  - Valid FHIR R4 document bundle
  - Idempotency check against previous submissions
- Returns `AbdmExchangeResponseDto` with transaction ID and status (`SUCCESS` or `FAILED`).

### 14.6 Get Session ABDM Transaction History
- `GET /api/abdm/sessions/{sessionId}/status`
- Roles allowed: Session patient owner, `ROLE_DOCTOR`, `ROLE_ADMIN`.
- Returns list of `IntegrationRecordDto` items tracking all ABDM operations for the session.

---

# 15. Hospital Information System (HIS) Integration Endpoints (Phase 13)

### 15.1 Get HIS Adapter Status
- `GET /api/his/status`
- Roles allowed: `ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_ADMIN`.
- Returns operating mode (`MOCK` or `LIVE`), `isMock` boolean, connectivity flag (`connected: false`), endpoint URL, and status message.

### 15.2 Export Doctor-Verified Summary to HIS Intake Queue
- `POST /api/his/sessions/{sessionId}/export`
- Roles allowed: `ROLE_DOCTOR`, `ROLE_ADMIN`.
- Request body:
  ```json
  {
    "intakeQueue": "OPD_INTAKE",
    "hisId": "HIS-MRN-90210",
    "idempotencyKey": "HIS-EXPORT-UUID",
    "notes": "Cleared for Cardiology OPD queue"
  }
  ```
- Gating prerequisites enforced server-side:
  - Doctor verification (`ClinicalSummaryStatus == VERIFIED`). Rejects unverified intake with HTTP 412 (PRECONDITION_FAILED).
  - Valid FHIR R4 document bundle (`FhirMappingService.validateBundleForSession`). Rejects invalid bundles with HTTP 422 (UNPROCESSABLE_ENTITY).
  - Idempotency replay against previous transmissions.
- Returns `HisExportResponseDto` with encounter reference ID and status (`SUCCESS` or `FAILED`).

### 15.3 Get Session HIS Integration History
- `GET /api/his/sessions/{sessionId}/status`
- Roles allowed: Session patient owner, `ROLE_DOCTOR`, `ROLE_ADMIN`.
- Returns list of `IntegrationRecordDto` items tracking all HIS operations for the session.

### 15.4 Search Hospital Master Patient Index (MPI)
- `GET /api/his/patients/search?query={q}&searchType={type}`
- Roles allowed: `ROLE_DOCTOR`, `ROLE_ADMIN`.
- Query parameters:
  - `query`: search term (name, phone, or MRN)
  - `searchType`: `ALL` | `NAME` | `PHONE` | `MRN`
- Returns matching candidates from hospital patient index.

### 15.5 Link Patient to Hospital HIS MRN
- `POST /api/his/patients/{patientId}/link`
- Roles allowed: `ROLE_DOCTOR`, `ROLE_ADMIN`.
- Request body:
  ```json
  {
    "hisId": "HIS-MRN-90210",
    "notes": "Verified with hospital admissions desk"
  }
  ```
- Updates `patients.his_id`, persists `IntegrationRecord` (`HIS_PATIENT_LINK`), emits audit log, and returns `HisLinkPatientResponseDto`.

---

# 16. AWS HealthLake Integration Endpoints (Phase 14)

### 16.1 Get AWS HealthLake Adapter Status
- `GET /api/healthlake/status`
- Roles allowed: `ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_ADMIN`.
- Returns operating mode (`MOCK` or `LIVE`), `isMock` boolean, connectivity flag (`connected: false` in MOCK mode), datastore endpoint, datastore ID, region (`ap-south-1`), and status message.

### 16.2 Export Doctor-Verified Summary to AWS HealthLake
- `POST /api/healthlake/sessions/{sessionId}/export`
- Roles allowed: `ROLE_DOCTOR`, `ROLE_ADMIN`.
- Request body:
  ```json
  {
    "healthlakeRef": "HL-PAT-001",
    "datastoreId": "mock-healthlake-datastore",
    "idempotencyKey": "HL-EXPORT-UUID",
    "notes": "Verified clinical export to cloud datastore"
  }
  ```
- Gating prerequisites enforced server-side:
  - Doctor verification (`ClinicalSummaryStatus == VERIFIED`). Rejects unverified intake with HTTP 412 (PRECONDITION_FAILED).
  - Valid FHIR R4 document bundle (`FhirMappingService.validateBundleForSession`). Rejects invalid bundles with HTTP 422 (UNPROCESSABLE_ENTITY).
  - Idempotency replay against previous transmissions.
- Returns `HealthLakeExportResponseDto` with cloud bundle reference, patient healthlake reference, and status (`SUCCESS` or `FAILED`).

### 16.3 Get Session HealthLake Integration History
- `GET /api/healthlake/sessions/{sessionId}/status`
- Roles allowed: Session patient owner, `ROLE_DOCTOR`, `ROLE_ADMIN`.
- Returns list of `IntegrationRecordDto` items tracking all HealthLake operations for the session.


