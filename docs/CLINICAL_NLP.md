# MediKiosk — Clinical NLP & Context Extraction

## 1. Overview

The MediKiosk Clinical NLP subsystem extracts structured clinical entities, detects contextual negations, characterizes symptoms, and detects conflicts across patient reports and OCR documents.

---

## 2. Capabilities

### Contextual Negation Detection
The NLP engine distinguishes between affirmed symptoms and negated complaints:
- *"I have chest pain since yesterday"* -> `CHEST_PAIN`: `AFFIRMED`, `DURATION`: `1 DAY`
- *"Mild fatigue, no chest pain"* -> `CHEST_PAIN`: `NEGATED`, `FATIGUE`: `AFFIRMED`
- Negative phrases recognized: *"no"*, *"not"*, *"denies"*, *"without"*, *"no sign of"*, *"never had"*

### Exertional & Radiation Modifiers
- Exertional markers (*"when walking"*, *"on climbing stairs"*, *"worse with activity"*) -> flagged as `EXERTIONAL`
- Radiation markers (*"radiating to left arm"*, *"spreads to jaw"*) -> flagged as `CHEST_PAIN_RADIATION`

### Cross-Source Conflict Detection
If a patient claims *"no allergies"* during intake, but an uploaded medical document OCR contains *"Penicillin 500mg - ALLERGIC"*, the engine generates a `ClinicalConflict`:
- **Concept**: `ALLERGY`
- **Primary Source**: `PATIENT_REPORT` (`No known allergies`)
- **Conflicting Source**: `DOCUMENT_OCR` (`Mention of Penicillin in record`)
- Highlighted in red in the Clinician Review Workstation for mandatory doctor clarification.

---

## 3. Provenance Tracking

All clinical elements carry a strict provenance classification:
1. `PATIENT_SELECTED`: Touch button selection
2. `PATIENT_TEXT`: Patient typed input
3. `VOICE_TRANSCRIPT`: Audio STT transcription
4. `AI_EXTRACTED`: NLP entity extraction from free-text
5. `DOCUMENT_OCR`: Extracted from uploaded medical records
6. `DOCTOR_EDITED`: Amended during clinician review
