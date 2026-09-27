# MediKiosk — AI Conversational Clinical Intake Specification

## 1. Executive Summary
MediKiosk has been upgraded from a deterministic, rule-based question decision tree to an **AI-assisted, context-aware clinical intake conversation engine**. The system collects and structures patient intake responses across multiple conversational turns, extracting clinical concepts, evaluating negation, dynamically formulating context-dependent questions, and validating all phrasing against clinical safety guardrails before displaying them to the patient.

---

## 2. Core Architectural Philosophy
1. **Clinical Safety Boundary**: The AI/LLM is **NEVER** an autonomous diagnostic system. It assists in clinical intake, symptom characterization, and doctor summary generation. The physician remains the sole decision-maker.
2. **Controlled Information Intents as Guardrails**: The LLM does not invent open-ended or speculative medical concepts. It plans questions by selecting from a controlled taxonomy of `QuestionIntent` (e.g., `CHEST_PAIN_CHARACTER`, `CHEST_PAIN_AGGRAVATING`, `CHEST_PAIN_RADIATION`, `BREATHING_DIFFICULTY`, `FEVER_CHILLS`, `HEADACHE_RED_FLAGS`).
3. **Conversational Wording Synthesis**: The natural phrasing of the question adapts to accumulated conversational facts (e.g., acknowledging previously mentioned symptoms or characterizations like "pressure").
4. **Pre-Transmission Safety Validator**: Every candidate question passes through `QuestionSafetyValidator` before presentation to the patient. If diagnostic assertions, treatment advice, or repetition are detected, the system safely falls back to a pre-approved clinical template.

---

## 3. Multi-Turn Conversational Progression Example

### Turn 1: Chief Complaint
- **Patient**: `"I've had chest pain since yesterday."`
- **Clinical Reasoning**:
  - `CHEST_PAIN`: AFFIRMED
  - `DURATION`: 1 DAY (AFFIRMED)
  - `SEVERITY`: UNKNOWN
  - `CHARACTER`: UNKNOWN
  - `ASSOCIATED_SYMPTOMS`: UNKNOWN
- **Selected Intent**: `CHEST_PAIN_CHARACTER` (Priority 100)
- **Generated Question**: `"Can you describe what the chest pain feels like?"`
- **Options Rendered**: `["Pressure / Squeezing", "Sharp / Stabbing", "Burning", "Dull Ache", "Other"]` + Free-text input.

### Turn 2: Characterization
- **Patient**: `"It's more like pressure."`
- **Clinical Reasoning**:
  - `CHEST_PAIN_CHARACTER`: PRESSURE_HEAVINESS (AFFIRMED)
- **Selected Intent**: `CHEST_PAIN_AGGRAVATING` (Priority 95)
- **Contextual Synthesis**: Recognizes previous context ("pressure").
- **Generated Question**: `"Does anything make the pressure better or worse?"`

### Turn 3: Aggravating Factor / Exertion
- **Patient**: `"It gets worse when I walk."`
- **Clinical Reasoning**:
  - `EXERTIONAL`: PRESENT (AFFIRMED)
- **Selected Intent**: `CHEST_PAIN_RADIATION` (Priority 90, Safety-Critical)
- **Generated Question**: `"Does the pressure spread or radiate anywhere, such as to your left arm, jaw, neck, or back?"`

### Turn 4: Negation Handling
- **Patient**: `"No, and I don't have fever."`
- **Clinical Reasoning**:
  - `CHEST_PAIN_RADIATION`: NEGATED
  - `FEVER`: NEGATED
- **Behavior**: Because `FEVER` is marked `NEGATED`, the system **NEVER** asks follow-up fever questions (e.g. `FEVER_DURATION` or `FEVER_CHILLS`).

---

## 4. Safety Guardrails & Validation Rules
The `QuestionSafetyValidator` enforces non-negotiable healthcare rules:
- **No Diagnostic Claims**: Rejects phrasing like `"Do you think you are having a heart attack?"` or `"Could this be angina?"`
- **No Treatment or Prescribing Advice**: Rejects phrasing like `"Should you take an aspirin?"`
- **No False Reassurance**: Rejects phrasing like `"Don't worry, you are completely safe."`
- **No Duplicate Inquiries**: Compares candidate question against `QuestionHistory` and prevents re-asking already explored intents or questions.
- **Fail-Safe Fallback**: If rejected, immediately substitutes `selectedIntent.getDefaultApprovedTemplate()`.

---

## 5. Provenance Tracking for Clinician Review
Every clinical fact displayed in the Clinician Review Workstation displays a verifiable provenance tag:
- ✍️ `PATIENT_TEXT` / `PATIENT_PROVIDED`: Directly typed or selected by the patient.
- 🎙️ `PATIENT_VOICE` / `VOICE_TRANSCRIPT`: Captured via Speech-to-Text with confidence rating.
- 📄 `DOCUMENT_OCR`: Extracted from uploaded medical documents/prescriptions.
- 🤖 `AI_EXTRACTED`: Inferred or structured by the AI reasoning engine.
- 🩺 `DOCTOR_EDITED`: Edited and signed by the examining physician.
