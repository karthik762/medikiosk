# MediKiosk — Clinical Knowledge & Intent Catalog

## 1. Overview
MediKiosk utilizes a controlled, clinician-curated taxonomy of **information-gathering question intents** to drive adaptive patient intake. The knowledge catalog defines standard intents, clinical categories, safety metadata, and provenance references.

## 2. Intent Taxonomy

| Intent Code | Category | Provenance | Safety Critical | Description |
| :--- | :--- | :--- | :--- | :--- |
| `SYMPTOM_CHARACTER` | HPI | CLINICIAN_CURATED | No | Qualitative description (sharp, pressure, burning) |
| `SYMPTOM_ONSET` | HPI | CLINICIAN_CURATED | No | Initial timing or trigger of symptoms |
| `SYMPTOM_DURATION` | HPI | CLINICIAN_CURATED | No | Overall duration of ongoing symptoms |
| `SYMPTOM_SEVERITY` | HPI | CLINICIAN_CURATED | **Yes** | Standard pain scale (0–10) |
| `SYMPTOM_LOCATION` | HPI | CLINICIAN_CURATED | No | Anatomical location & radiation pattern |
| `ASSOCIATED_SYMPTOMS` | HPI | RESEARCH_REFERENCE | **Yes** | Co-occurring secondary symptoms |
| `AGGRAVATING_FACTORS` | HPI | CLINICIAN_CURATED | No | Exacerbating activities or movements |
| `RELIEVING_FACTORS` | HPI | CLINICIAN_CURATED | No | Position or medication relief |
| `MEDICATION_STATUS` | PMH | SYNTHETIC_EVALUATION | No | Regular prescription/OTC medications |
| `ALLERGY_STATUS` | SAFETY | CLINICIAN_CURATED | **Yes** | Medication/environmental hypersensitivities |
| `PAST_HISTORY` | PMH | RESEARCH_REFERENCE | No | Prior chronic conditions or surgeries |
| `FAMILY_HISTORY` | FH | SYNTHETIC_EVALUATION | No | Familial health history patterns |
| `SOCIAL_HISTORY` | SH | RESEARCH_REFERENCE | No | Tobacco, alcohol, or lifestyle context |
| `LIFESTYLE` | LIFESTYLE | TERMINOLOGY_REFERENCE | No | Habitual dietary and exercise patterns |
| `DOCUMENT_CONFIRMATION` | SAFETY | CLINICIAN_CURATED | **Yes** | Verification of OCR/scanned findings |

## 3. Provenance Classifications
- **`CLINICIAN_CURATED`**: Standard emergency/ambulatory intake questions verified by medical experts.
- **`SYNTHETIC_EVALUATION`**: Simulated patient profile scenarios (Synthea-aligned).
- **`RESEARCH_REFERENCE`**: Conversational dialogue structures (MTS-Dialog aligned).
- **`TERMINOLOGY_REFERENCE`**: Standard clinical QA terminology (MedQuAD reference).

> [!IMPORTANT]
> Question intents represent **information-gathering steps**, NOT disease-diagnosis rules. MediKiosk never diagnoses patients or overrides doctor judgment.
