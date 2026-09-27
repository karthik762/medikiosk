# MediKiosk — Product Specification

## 1. Product Overview

MediKiosk is an AI-powered clinical intake platform designed to collect, structure, and organize patient information before a clinical consultation.

The system allows patients to provide information using voice, touch, text, and medical documents. It performs structured clinical intake, preliminary red-flag screening, conventional and Ayurvedic history-taking, document processing, and AI-assisted clinical summarization.

The final information is presented to a doctor for review, correction, and verification.

## 2. Problem

Patient information is often collected manually and repeatedly during consultations. Medical information may also be fragmented across verbal responses, prescriptions, laboratory reports, and previous records.

This can increase repetitive workload for clinicians and make it difficult to organize patient information before consultation.

## 3. Proposed Solution

MediKiosk provides a guided digital intake process that:

- Collects patient information through voice, touch, text, and documents.
- Uses adaptive questioning to collect relevant clinical history.
- Performs preliminary red-flag screening.
- Supports both conventional and Ayurvedic case-taking.
- Uses OCR to process medical documents.
- Uses AI to structure and summarize patient-provided information.
- Provides a structured pre-consultation summary to the doctor.
- Allows the doctor to review, correct, and verify the information.
- Uses FHIR-compatible data structures for healthcare interoperability.
- Provides an integration architecture for ABDM, HIS, and AWS HealthLake.

## 4. Core Principle

AI assists → Information is structured → Doctor verifies → Doctor decides.

MediKiosk is an intake and clinical documentation support system. It must not independently diagnose patients, prescribe treatment, or replace clinical decision-making.

## 5. Target Users

### Patient

Uses the MediKiosk interface to:

- Provide personal information.
- Give complaints and symptoms.
- Answer clinical questions.
- Provide information through voice, text, or touch.
- Upload or scan medical documents.
- Complete conventional and Ayurvedic history.

### Doctor

Uses the doctor dashboard to:

- View patient intake information.
- Review triage status.
- Review clinical history.
- View uploaded documents.
- Review AI-generated summaries.
- Correct extracted or generated information.
- Verify the final structured summary.
- Continue with clinical consultation.

### Hospital Administrator

Uses the administrative interface to:

- Manage patients and doctors.
- Monitor system activity.
- View operational analytics.
- Manage selected system configurations.
- Review audit logs.

## 6. Core Modules

### 6.1 Patient Kiosk

- Patient registration
- Consent
- Basic demographic information
- Touch-friendly interface
- Multilingual interface
- Accessibility support
- Session management

### 6.2 Voice and Language

- Voice-to-text
- Text-to-speech
- Multilingual interaction
- Voice replay
- Touch/text fallback
- Confirmation before saving important responses

### 6.3 Clinical Intake

- Chief complaint
- Symptoms
- Duration
- Severity
- Associated symptoms
- Past medical history
- Medications
- Allergies
- Family history
- Lifestyle information
- Adaptive questioning

### 6.4 Rapid Triage

- Red-flag screening
- Rule-based priority classification
- Urgent / Priority / Stable status
- Escalation to clinical staff
- Doctor-visible triage status

Triage is intended for preliminary screening and routing, not diagnosis.

### 6.5 Ayurveda Module

- Prakriti
- Nidana
- Pariksha
- Ayurveda-specific history
- Structured Ayurvedic information
- NAMASTE terminology
- ICD-11 TM2 support

### 6.6 Document Intelligence

- Camera/document scanning
- Image upload
- PDF upload
- Prescription processing
- Laboratory report processing
- Medical record processing
- OCR
- Medical entity extraction
- Medication extraction
- Test/result extraction
- Document date extraction
- Source document verification
- Medical timeline

### 6.7 AI Structuring

- Patient response structuring
- Medical entity extraction
- Terminology normalization
- Document summarization
- Missing information identification
- Clinical pre-consultation summary generation

AI-generated information must remain identifiable as AI-generated until reviewed and verified by a doctor.

### 6.8 Doctor Dashboard

- Doctor authentication
- Patient queue
- Patient profile
- Triage status
- Clinical history
- Ayurveda history
- Medications
- Allergies
- Documents
- AI-generated summary
- Source information
- Editing
- Verification
- Consultation workflow

### 6.9 Interoperability

MediKiosk will use a FHIR-compatible data layer to support future and planned integration with:

- FHIR
- ABDM
- Hospital Information Systems (HIS)
- AWS HealthLake

External integrations must be isolated behind integration services/adapters.

### 6.10 Security

- Authentication
- Authorization
- Role-based access control
- Patient consent
- Encryption
- Secure document handling
- Session management
- Audit logging

## 7. Technology Stack

### Frontend

- React
- TypeScript
- Tailwind CSS

### Backend

- Java
- Spring Boot
- Spring Security
- REST APIs

### Database

- MySQL 8.x

### Clinical Data

- FHIR-compatible data structures

### AI and Document Processing

- Speech-to-text
- Text-to-speech
- OCR
- Large Language Model API
- Structured JSON outputs

### Integrations

- FHIR
- ABDM
- HIS
- AWS HealthLake

## 8. High-Level Data Flow

Patient
→ MediKiosk Interface
→ Voice / Touch / Text / Documents
→ Speech-to-Text / OCR
→ Clinical Intake
→ Adaptive Questions
→ Rapid Triage
→ Conventional / Ayurveda History
→ AI Structuring
→ Clinical Summary
→ FHIR Data Layer
→ ABDM / HIS / AWS HealthLake
→ Doctor Dashboard
→ Doctor Review
→ Doctor Verification
→ Consultation

## 9. AI Safety Requirements

The AI system must:

- Use only information provided by the patient or extracted from approved sources.
- Avoid inventing patient information.
- Preserve uncertainty.
- Clearly distinguish patient-reported information from extracted or AI-generated information.
- Return structured output where required.
- Allow doctor review and correction.

The AI system must not:

- Independently diagnose a patient.
- Prescribe medication.
- Recommend treatment as a final clinical decision.
- Override a doctor.
- Automatically mark AI-generated information as doctor-verified.
- Hide uncertainty or missing information.

## 10. Interoperability Strategy

The application will maintain a separation between:

1. Internal application data
2. FHIR-compatible clinical data
3. External integration services

Architecture:

MediKiosk
→ Spring Boot Backend
→ FHIR Layer
→ Integration Layer
→ ABDM / HIS / AWS HealthLake

External systems must not be tightly coupled to the core patient-intake workflow.

## 11. Prototype Strategy

The initial prototype should prioritize the complete end-to-end clinical intake workflow.

Core demonstration:

Patient registration
→ Consent
→ Complaint
→ Voice / Touch input
→ Adaptive questions
→ Triage
→ Medical history
→ Ayurveda history
→ Document upload
→ OCR
→ AI structuring
→ Clinical summary
→ Doctor dashboard
→ Doctor editing
→ Doctor verification

FHIR, ABDM, HIS, and AWS HealthLake integration should be designed into the architecture from the beginning while clearly distinguishing implemented integrations from sandbox, mocked, or future integrations.

## 12. Success Criteria

The MediKiosk prototype should demonstrate that:

- A patient can complete a guided clinical intake.
- Voice and touch can be used as alternative input methods.
- Relevant questions can adapt based on patient responses.
- Potential red flags can be identified through controlled rules.
- Conventional and Ayurvedic history can be captured.
- Medical documents can be processed using OCR.
- Patient information can be converted into structured data.
- An AI-assisted clinical summary can be generated.
- A doctor can review and correct the summary.
- A doctor can verify the final information.
- Clinical data can be represented in a FHIR-compatible structure.
- The architecture supports ABDM, HIS, and AWS HealthLake integration.
- Patient data is protected through appropriate authentication, authorization, consent, and auditing.