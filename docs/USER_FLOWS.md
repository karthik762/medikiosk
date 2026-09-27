# MediKiosk — User Flows

## 1. Overview

MediKiosk has three primary user roles:

- Patient
- Doctor
- Hospital Administrator

The core workflow is:

Patient Intake
→ Clinical Processing
→ AI Structuring
→ Doctor Review
→ Doctor Verification
→ Consultation

AI-generated information must never be treated as clinically verified until reviewed and verified by a doctor.

---

# 2. Patient Flow

## P-001 — Start Session

Welcome Screen
→ Select Language
→ Start Intake

System:
- Creates a temporary intake session.
- Assigns a session identifier.
- Initializes the patient workflow.

---

## P-002 — Patient Registration

Start Intake
→ New Patient / Existing Patient
→ Enter Basic Details
→ Continue

Collect:

- Name
- Date of Birth / Age
- Gender
- Contact information
- Patient identifier where available

Output:

Patient profile
+
Active intake session

---

## P-003 — Consent

Patient Details
→ Consent Screen
→ Read Privacy Information
→ Accept / Decline

If Accepted:

Consent recorded
→ Continue intake

If Declined:

Do not proceed with information collection.
→ End session

Record:

- Consent status
- Timestamp
- Consent version
- Patient/session reference

---

# 3. Chief Complaint Flow

Consent
→ Chief Complaint

Patient can choose:

- Voice
- Text
- Touch

Example:

Patient:
"I have stomach pain for three days."

Voice:

Speech
→ Speech-to-Text
→ Transcription
→ Patient Confirmation
→ Save

Text:

Typed response
→ Confirmation
→ Save

Touch:

Symptom selection
→ Additional details
→ Save

---

# 4. Voice Flow

Question
→ Patient selects Voice
→ Microphone Permission
→ Recording
→ Speech-to-Text
→ Display Transcription

Patient chooses:

Confirm
→ Save answer

OR

Edit
→ Modify text
→ Save

OR

Retry
→ Record again

If voice fails:

Voice Error
→ Retry
→ Text / Touch fallback

Voice must never be the only input method.

---

# 5. Text-to-Speech Flow

Question displayed
→ Patient selects / system triggers Read Aloud
→ Text-to-Speech
→ Audio Playback

Controls:

- Play
- Pause / Stop
- Replay

If TTS fails:

Display question normally
→ Allow text/touch interaction

---

# 6. Adaptive Clinical Question Flow

Chief Complaint
→ Identify relevant question category
→ Load approved question set
→ Ask question
→ Record answer
→ Evaluate answer
→ Select next relevant question

Example:

Complaint:
Chest pain

→ Question A

Answer:
Yes

→ Question B becomes relevant

Answer:
No

→ Skip Question C

→ Continue relevant history

The question engine must:

- Follow approved question definitions.
- Support conditional questions.
- Skip irrelevant questions.
- Track completed questions.
- Track unanswered questions.
- Support required and optional questions.
- Preserve all patient responses.

The AI must not independently invent clinical questions or clinical rules.

---

# 7. Rapid Triage Flow

Patient answers
→ Triage Rule Engine
→ Evaluate approved red-flag rules
→ Assign priority

Possible results:

URGENT
PRIORITY
STABLE

If URGENT:

→ Display appropriate patient-facing instruction
→ Flag patient for clinical staff
→ Display urgent status on doctor dashboard
→ Continue according to approved workflow

If PRIORITY:

→ Mark patient as priority
→ Continue intake where appropriate
→ Display priority status to doctor

If STABLE:

→ Continue normal intake

Triage is screening and routing only.

It must not produce an autonomous diagnosis.

---

# 8. Conventional History Flow

Triage
→ Conventional Medical History

Collect approved sections such as:

- Past medical history
- Previous surgeries
- Hospitalizations
- Current medications
- Allergies
- Family history
- Lifestyle information

Each answer is stored against the active intake session.

---

# 9. Ayurveda Flow

Conventional History
→ Ayurveda History

Sections:

- Prakriti
- Nidana
- Pariksha
- Other approved Ayurveda history

Patient completes the approved Ayurveda questionnaire.

Responses are stored separately but remain linked to the same patient and intake session.

The Ayurveda workflow must not replace conventional clinical history unless explicitly configured for a specific workflow.

---

# 10. Document Flow

Patient History
→ Documents

Options:

- Scan Document
- Upload Image
- Upload PDF
- Skip

Supported examples:

- Prescription
- Laboratory report
- Previous medical record
- Discharge document

---

# 11. Document Scanner Flow

Scan Document
→ Camera Permission
→ Capture
→ Preview

Patient chooses:

Confirm
→ Process document

OR

Retake
→ Capture again

OR

Cancel
→ Return to documents

---

# 12. OCR Flow

Document
→ OCR
→ Extracted Text
→ Confidence Evaluation
→ Medical Entity Extraction

Possible extracted entities:

- Medication
- Dosage
- Test
- Result
- Date
- Condition
- Symptom
- Allergy

Store:

- Original document
- OCR text
- Extracted information
- Confidence information
- Processing status

---

# 13. Low OCR Confidence Flow

OCR
→ Low Confidence

System:

- Preserves original document.
- Flags uncertain information.
- Does not silently treat uncertain information as verified.
- Makes the source document available for doctor review.

Doctor:

View source
→ Compare extraction
→ Correct
→ Verify

---

# 14. AI Structuring Flow

Collected Information
+
Patient Responses
+
Medical History
+
Ayurveda History
+
Document Extraction
+
Triage Result

↓

AI Structuring Service

↓

Structured Clinical Data

The AI must:

- Structure provided information.
- Extract relevant entities.
- Preserve uncertainty.
- Identify missing information.
- Generate a pre-consultation summary.

The AI must not:

- Diagnose.
- Prescribe.
- Invent information.
- Override the triage engine.
- Mark information as doctor-verified.

---

# 15. AI Summary Flow

Structured Patient Data
→ AI Summary Generator
→ Clinical Pre-Consultation Summary
→ Store as AI-generated
→ Display to Doctor

Summary may include:

- Chief complaint
- Symptoms
- Duration
- Relevant history
- Medications
- Allergies
- Documents
- Triage status
- Ayurveda information
- Missing / uncertain information

---

# 16. Patient Review Flow

Before submission:

Collected Information
→ Review Screen

Patient can:

- Review information.
- Edit allowed fields.
- Return to previous sections.
- Confirm submission.

Submit
→ Lock patient intake for doctor review
→ Generate final processing status
→ Send patient to completion screen

---

# 17. Patient Completion Flow

Submission
→ Processing

System processes:

- Answers
- Documents
- OCR
- AI structuring
- Clinical summary

Then:

Processing Complete
→ Intake Completed
→ Patient receives completion message

The patient does not receive an autonomous diagnosis or treatment recommendation.

---

# 18. Doctor Login Flow

Doctor
→ Login
→ Authentication
→ Role Verification
→ Doctor Dashboard

If authentication fails:

Invalid Credentials
→ Display error
→ Allow retry

---

# 19. Doctor Dashboard Flow

Doctor Dashboard contains:

- Patient Queue
- Urgent Patients
- Priority Patients
- Stable Patients
- Recently Completed Intakes

Doctor selects patient:

Patient Queue
→ Patient Overview

---

# 20. Doctor Patient Overview

Patient Overview displays:

- Patient details
- Intake status
- Triage status
- Chief complaint
- Key information

Doctor can navigate to:

- Clinical History
- Ayurveda History
- Documents
- AI Summary
- Verification

---

# 21. Doctor Clinical History Flow

Patient
→ Clinical History

Display:

- Chief complaint
- Symptoms
- Duration
- Severity
- Medical history
- Medications
- Allergies
- Family history
- Lifestyle information

Information should remain traceable to patient responses where applicable.

---

# 22. Doctor Document Review Flow

Patient
→ Documents
→ Select Document

Display:

- Original document
- OCR text
- Extracted entities
- Confidence information

Doctor can:

- Confirm extraction
- Correct extraction
- Add information
- Reject incorrect extraction

Corrections must be recorded.

---

# 23. Doctor AI Summary Review

Patient
→ AI Summary

Display:

- Structured summary
- AI-generated sections
- Triage status
- Uncertain information
- Missing information
- Source information where available

Doctor can:

- Edit
- Correct
- Add notes
- Review supporting information

---

# 24. Doctor Verification Flow

AI Summary
→ Review
→ Edit if required
→ Verify

On verification:

Store:

- Doctor ID
- Timestamp
- Verified summary version
- Verification status

Status:

AI Generated
→ Doctor Reviewed
→ Doctor Verified

Only the doctor can mark the information as verified.

---

# 25. Doctor Consultation Flow

Doctor Verification
→ Consultation

The doctor can continue with normal clinical consultation using the structured intake as a starting point.

MediKiosk does not replace the consultation.

---

# 26. Admin Flow

Admin
→ Login
→ Authentication
→ Admin Dashboard

Admin sections:

- Patients
- Doctors
- Analytics
- Audit Logs
- System Configuration

Access must be controlled through role-based permissions.

---

# 27. FHIR Flow

MediKiosk Data
→ FHIR Mapping Layer
→ FHIR-Compatible Resources

Potential resources:

- Patient
- Encounter
- Condition
- Observation
- Medication-related resources
- AllergyIntolerance
- DocumentReference
- Consent
- Composition

FHIR processing must remain separated from the core intake workflow.

---

# 28. ABDM Flow

MediKiosk
→ FHIR Layer
→ ABDM Integration Layer
→ ABDM

Potential workflows:

- Patient identification
- ABHA linking where supported
- Consent-based exchange
- Health-record exchange

If ABDM is unavailable:

→ Preserve local data
→ Record integration status
→ Retry when appropriate

---

# 29. HIS Integration Flow

Hospital HIS
↔ HIS Integration Layer
↔ FHIR Layer
↔ MediKiosk

HIS → MediKiosk may provide:

- Patient information
- Previous encounters
- Existing records
- Appointments

MediKiosk → HIS may provide:

- Patient registration
- Encounter
- Intake information
- Triage
- Documents
- Structured summary

A mock HIS must be available for development/demo when a real HIS is unavailable.

---

# 30. AWS HealthLake Flow

MediKiosk
→ Spring Boot
→ FHIR Layer
→ HealthLake Adapter
→ AWS HealthLake

HealthLake integration must not be required for the application to operate locally.

If unavailable:

→ Continue local workflow
→ Store data locally
→ Record synchronization status
→ Retry when appropriate

---

# 31. Integration Failure Handling

## ABDM unavailable

Continue core workflow.
Store integration status.
Retry later.

## HIS unavailable

Continue core workflow.
Queue synchronization where appropriate.

## HealthLake unavailable

Continue core workflow.
Keep local data.
Retry synchronization.

External integration failure must never cause patient intake data loss.

---

# 32. AI Failure Handling

If AI service is unavailable:

- Preserve all collected information.
- Do not lose patient responses.
- Allow manual doctor review.
- Display AI processing status.
- Retry AI processing when available.

The core patient-intake workflow should remain usable without AI where possible.

---

# 33. Session Timeout Flow

Inactive Patient Session
→ Timeout Warning
→ Session Expiration

On expiration:

- Clear temporary kiosk state.
- Securely end session.
- Return to Welcome Screen.
- Do not expose previous patient's information.

---

# 34. General Data Flow

Patient Input
→ Intake Session
→ Structured Data
→ MySQL
→ FHIR Mapping
→ External Integrations where enabled
→ Doctor Dashboard
→ Doctor Review
→ Doctor Verification

---

# 35. Core Product Principle

Every major workflow follows:

Capture
→ Structure
→ Validate
→ Store
→ Review
→ Verify
→ Exchange where required

AI assists the workflow.

The doctor remains the final clinical decision-maker.