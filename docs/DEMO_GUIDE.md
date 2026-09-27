# MediKiosk — Live Demonstration & Judge Presentation Guide

> **Prepared for:** Smart India Hackathon (SIH) Clinical Evaluation & Technical Demonstration  
> **System Status:** Phases 1–17 Complete & Verified (192 Backend Tests, 54 Frontend Tests Passing)  
> **Core Principle:** *AI assists &rarr; Information is structured &rarr; Doctor verifies &rarr; Doctor decides.*

---

## 1. Executive Summary & Pitch (60 Seconds)

- **The Problem:** Indian tertiary care and district hospitals manage hundreds of outpatients daily per doctor. A clinician typically spends 70% of consultation time on repetitive verbal history taking, manual data typing, and paper report deciphering, leaving mere minutes for actual clinical examination and empathetic patient care.
- **The Solution:** **MediKiosk** is an intelligent, multilingual pre-consultation clinical intake kiosk. While sitting in the waiting area, patients interact via touch or voice in their native language to register, give explicit consent, report symptoms, provide Ayurvedic lifestyle metrics, and upload paper lab reports.
- **The Intelligence:** A deterministic, non-diagnostic rule engine immediately flags red flags (e.g. acute chest pain &rarr; EMERGENCY). An AI structuring engine extracts entities from documents and structures symptoms into a draft pre-consultation note.
- **The Safety Guarantee:** MediKiosk **never diagnoses or prescribes**. All AI outputs are provisional drafts requiring explicit human clinician review, modification, and digital sign-off before being converted to FHIR R4 bundles and dispatched to ABDM/HIS/HealthLake.

---

## 2. Technical Architecture for Judges

```
┌───────────────────────────────────────────────────────────────────────────┐
│              Patient Kiosk UI & Doctor Review Workstation                 │
│              React 18 + TypeScript + Tailwind CSS + Lucide               │
└─────────────────────────────────────┬─────────────────────────────────────┘
                                      │ REST API / Bearer JWT
                                      ▼
┌───────────────────────────────────────────────────────────────────────────┐
│            Spring Boot 3.4.2 API Gateway & Security Filter Chain          │
│     - Stateless JWT RBAC               - Input Validation & Sanitization  │
│     - Secure HTTP Response Headers     - Immutable Audit Event Logging    │
└─────────────────────────────────────┬─────────────────────────────────────┘
                                      │
       ┌──────────────────────────────┴──────────────────────────────┐
       ▼                                                             ▼
┌──────────────────────────────────────────┐    ┌──────────────────────────────────────────┐
│             Clinical Services            │    │          Interoperability Layer          │
│ - Rule-Based Triage Engine (Red Flags)   │    │ - FHIR R4 Model & Bundle Mapper          │
│ - Ayurveda Intake Service (Agni/Prakriti)│    │ - ABDM Adapter (MOCK / Sandbox Boundary) │
│ - Document OCR & Entity Extraction       │    │ - HIS Adapter (MOCK / HL7 Boundary)      │
│ - AI Clinical Structuring Service        │    │ - AWS HealthLake Adapter (MOCK Boundary) │
│ - Doctor Verification & Audit Service    │    └──────────────────────────────────────────┘
└───────────────────┬──────────────────────┘
                    │
                    ▼
┌──────────────────────────────────────────┐
│      MySQL 8.x Relational Store         │
│ - 15 Normalized Tables (Flyway V1–V14)   │
└──────────────────────────────────────────┘
```

---

## 3. End-to-End 16-Step Live Demo Script

Follow this scripted flow during the presentation:

### Step 1: System Health & Baseline Verification
- Open terminal or browser to `http://localhost:8080/api/health`.
- Point out: `status: "UP"`, version `1.0.0`, active database connection, clean Spring Boot 3.4.2 architecture.
- Frontend status indicator in the top-right header shows `Backend: Online (200 OK)`.

### Step 2: Language & Accessibility Selection
- Navigate to Kiosk Mode (`http://localhost:5173`).
- Show the language selector: English, Hindi (हिंदी), Kannada (ಕನ್ನಡ), etc.
- Emphasize: Large high-contrast touch targets, bilingual interface, designed for rural and urban patients alike.

### Step 3: Patient Registration & Identity Capture
- Enter patient details: Name (e.g. *Rohan Verma*), Age (42), Gender (Male), Phone Number, and Patient Identifier (`PAT-2026-001`).
- Point out validation: Phone number format validation, mandatory field checks, sanitization.

### Step 4: Explicit 4-Tier Informed Consent
- Display the Consent Step. Show that MediKiosk enforces distinct consent types:
  1. General Intake & Triage
  2. Electronic Medical Record Storage
  3. AI-Assisted Clinical Structuring
  4. Anonymized Quality & Audit Research
- Point out: Each consent is individually timestamped with version number (`v1.0`) and stored immutably in MySQL 8.x.

### Step 5: Touch and Simulated Voice Input
- Switch between touch buttons and the voice intake interface.
- Show speech simulation / Web Speech API capability: speech transcription converts spoken complaints into structured text in real-time.

### Step 6: Chief Complaint & Symptom Intake
- Select primary complaint: *Chest Pain / Shortness of Breath*.
- Set onset duration: *2 hours ago*, severity slider: *8/10*, radiating to left arm.
- Note how the dynamic questioning module adapts and prompts for associated symptoms (diaphoresis, nausea).

### Step 7: Deterministic Rule-Based Triage (The Safety Net)
- Point out the instant triage alert generated: **EMERGENCY (Level 1)**.
- **Judge Highlight:** This is *not* an unpredictable LLM guess. It is computed by a hard-coded, clinical rule engine (`TriageEngineImpl`) matching red-flag clinical protocols. The session is flagged with an explicit clinical reason.

### Step 8: Ayurveda-Integrated Clinical Intake
- Navigate to the holistic/Ayurvedic intake section.
- Capture:
  - **Agni (Digestive Fire):** *Mandagni* (slow/sluggish)
  - **Koshtha (Bowel Habit):** *Krura* (constipated/hard)
  - **Prakriti Indicators:** Sleep disturbances, temperature preferences, body frame.
- **Judge Highlight:** Unique national integration. Traditional AYUSH parameters are captured systematically alongside allopathic history without conflict.

### Step 9: Medical Document Upload & OCR Parsing
- Upload a sample paper document (e.g. CBC lab report or ECG image).
- Trigger OCR processing.
- Demonstrate extracted structured entities: Hemoglobin (13.5 g/dL), Fasting Blood Glucose (142 mg/dL), Document Classification (`LAB_REPORT`).
- File uploads are strictly validated and constrained to 10MB to prevent denial of service.

### Step 10: Pre-Consultation AI Clinical Structuring
- Trigger the AI Clinical Structuring engine (`AiService`).
- Show generated output:
  - Subjective Narrative (HPI)
  - Symptom Matrix & Timeline
  - Objective Lab Findings from OCR
  - Provisional Clinical Summary
- Show **Confidence Score** and clear disclaimer: *"Draft Summary — Pending Clinician Verification"*.

### Step 11: Doctor Workstation Login (1-Click Clinician Access)
- Switch to the **Doctor View** in the top navigation.
- *(LOCAL DEMO ONLY — never use these credentials in production)*:
  When demo mode is active (`medikiosk.demo.enabled=true`), the system exposes the **"1-Click Demo Doctor Login"** button.
- Point out: Automatically logs in as `doctor@medikiosk.org` via a guarded backend demo endpoint, acquires a secure 24-hour JWT token, and switches the UI to clinician mode without exposing any passwords in frontend client code. In production (`medikiosk.demo.enabled=false`), this button is automatically hidden.

### Step 12: Doctor Dashboard & Intake Queue
- View the queue of waiting intake sessions.
- Sessions are color-coded by triage level: Red for EMERGENCY, Amber for PRIORITY, Green for ROUTINE.
- Select the demo session (*Rohan Verma*).

### Step 13: Clinician Review & In-Place Editing
- Open the **Clinical Review Workstation**.
- Highlight doctor control:
  - Doctor can review the AI-generated draft summary.
  - Doctor can directly edit or add clinical notes.
  - Doctor can override the triage category with a clinical justification.
- **Judge Highlight:** Zero automated diagnosis. The machine only organizes; the human doctor decides.

### Step 14: Doctor Verification & Digital Sign-off
- Click **"Verify & Sign Off"**.
- The system captures: Doctor ID, verification timestamp, signature status (`VERIFIED`), and any clinical amendments.
- The session transitions to `COMPLETED` and is locked against further edits.

### Step 15: Interoperability & FHIR R4 Bundle Generation
- Open the Interoperability tab.
- Click **"Generate FHIR R4 Bundle"**.
- View the generated JSON payload:
  - `Bundle` resource of type `document`
  - Encapsulates `Patient`, `Observation` (vital signs & triage), `Condition` (chief complaint), and `DiagnosticReport` (OCR lab findings).
  - Valid standard FHIR R4 schema ready for any modern health cloud.

### Step 16: External Integration Adapters (ABDM, HIS, HealthLake)
- Trigger external sync:
  1. **ABDM Adapter:** Dispatches mock ABHA registration and consent artifact.
  2. **HIS Adapter:** Dispatches mock HL7 encounter registration.
  3. **AWS HealthLake Adapter:** Simulates FHIR export to cloud data lake.
- **Judge Highlight:** Emphasize the clear architectural boundary. All three adapters operate in clean, deterministic offline mock mode so the entire kiosk functions reliably without live external network dependencies during hospital network drops.

---

## 4. Architectural Demarcation: Real vs Mock

Judges appreciate total honesty regarding external boundaries. Use this table:

| Subsystem / Capability | Implementation Type | Live vs Mock Behavior |
|---|---|---|
| **Patient Registration & Form Intake** | 100% Native Production Code | Real MySQL 8.x storage & validation |
| **Consent Management** | 100% Native Production Code | Real immutable database records & versioning |
| **Rule-Based Triage Engine** | 100% Native Production Code | Real deterministic algorithm with red-flag rules |
| **Ayurveda Assessment Service** | 100% Native Production Code | Real calculation of Agni, Koshtha, Prakriti |
| **Doctor Review Workstation** | 100% Native Production Code | Real clinician verification, editing & sign-off |
| **Spring Security & RBAC** | 100% Native Production Code | Real stateless JWT filter, password hashing, roles |
| **FHIR R4 Mapping** | 100% Native Production Code | Real standard FHIR R4 resource generation |
| **Speech-to-Text (Voice)** | Hybrid Native / Web Speech | Browser Web Speech API with fallback mock provider |
| **Document OCR Service** | Mock Provider / Sandbox | Deterministic medical entity parser simulating OCR |
| **AI Structuring Service** | Mock / Fallback Provider | Deterministic clinical structuring simulating LLM |
| **ABDM / ABHA Sandbox** | Architectural Adapter Mock | Simulates sandbox endpoints without live NIC servers |
| **Hospital Info System (HIS)** | Architectural Adapter Mock | Simulates HL7/FHIR ingest without hospital intranet |
| **AWS HealthLake Cloud** | Architectural Adapter Mock | Simulates cloud datastore ingest without cloud billing |

---

## 5. Frequently Asked Questions by Judges

### Q1: "Is this replacing the doctor?"
> **Answer:** Absolutely not. MediKiosk is strictly a pre-intake documentation and triage tool. It eliminates the 10–15 minutes a doctor spends typing demographic and history data. The system explicitly blocks unverified summaries from reaching the official medical record until a licensed physician reviews, edits, and digitally signs off.

### Q2: "What happens if an emergency patient arrives?"
> **Answer:** Our deterministic rule-based triage engine (`TriageEngineImpl`) executes within milliseconds. Red-flag symptoms (severe chest pain, difficulty breathing, altered consciousness) instantly trigger an **EMERGENCY (Level 1)** alert on the kiosk screen, directing the patient immediately to the casualty ward, and flagging the doctor's queue in bright red.

### Q3: "How do you protect patient privacy (DPDP / HIPAA)?"
> **Answer:**
> 1. Multi-tier explicit consent before data collection starts.
> 2. Passwords salted and hashed with BCrypt.
> 3. Stateless 24-hour JWT tokens with role-based access control.
> 4. All clinical and verification actions are logged in an immutable audit table (`audit_logs`) tracking timestamp, user ID, IP address, and payload.
> 5. Uploaded files are restricted in size (10MB) and type.

### Q4: "How does this integrate with the Indian Government's Ayushman Bharat (ABDM)?"
> **Answer:** MediKiosk features a dedicated `AbdmAdapter` interface with methods for ABHA creation, OTP verification, and consent management, fully mapped to FHIR R4 bundles. While demonstrated in deterministic sandbox mock mode to ensure zero offline demo failure, the interface is ready to connect to live ABDM Gateway APIs.

### Q5: "Why combine Allopathy and Ayurveda?"
> **Answer:** Under the National Ayush Mission and integrative health initiatives in India, patients frequently use both systems simultaneously. MediKiosk captures standard clinical metrics alongside Ayurvedic parameters (Agni, Koshtha, Prakriti) into a single structured record so clinicians have a holistic view of the patient's lifestyle and digestion.
