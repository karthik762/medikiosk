# MediKiosk — FHIR Mapping Specification

## 1. Purpose

MediKiosk uses a FHIR-compatible interoperability layer to represent clinical information in a standardized healthcare data format.

FHIR is an interoperability layer, not the primary application database.

Architecture:

    MediKiosk MySQL 8.x
        ↓
    FHIR Mapping Engine
        ↓
    Canonical FHIR R4 Bundle
        ↓
    External Systems (ABDM / HIS / AWS HealthLake)

---

# 2. Integration Principles

The internal MySQL 8.x schema remains the application source of truth.

FHIR mapping should be implemented as an export and transformation layer rather than replacing the application domain model.

FHIR mappings should be implemented in a dedicated service layer so that the application can evolve without redesigning the MySQL 8.x schema.

---

# 3. Layered Mapping Flow

    React Frontend
        ↓
    Spring Boot REST Controller
        ↓
    Clinical Domain / Persistence (MySQL 8.x)

- Represent relevant MediKiosk clinical information using appropriate FHIR resources.
- Preserve source information.
- Preserve identifiers.
- Preserve verification status where applicable.
- Avoid inventing clinical information.
- Support future interoperability.
- Keep external-system logic separate from the core database.
- Allow FHIR resources to be generated on demand or synchronized through adapters.

---

# 4. Internal Database vs FHIR

The relationship is:

    MySQL 8.x
    Application Data
          ↓
    Mapping Layer
          ↓
    FHIR Representation

FHIR resources should not replace the internal relational model.

Example:

    patients table
          ↓
    FHIR Patient

    intake_sessions table
          ↓
    FHIR Encounter

    allergies table
          ↓
    FHIR AllergyIntolerance

---

# 5. Primary Resource Mapping

Initial mapping:

| MediKiosk Entity | FHIR Resource |
|---|---|
| Patient | Patient |
| Intake Session | Encounter |
| Symptoms / Conditions | Condition |
| Clinical measurements/findings | Observation |
| Medications | Medication / MedicationRequest where appropriate |
| Allergies | AllergyIntolerance |
| Medical Documents | DocumentReference |
| Consent | Consent |
| Clinical Summary | Composition |
| Patient identifiers | Identifier |
| Doctor | Practitioner |
| Doctor relationship to patient/session | PractitionerRole / Encounter references where appropriate |

The exact resource used should depend on the semantics of the information being represented.

---

# 6. Patient → FHIR Patient

MediKiosk:

    patients

Maps to:

    Patient

Example:

```json
{
  "resourceType": "Patient",
  "id": "patient-id",
  "identifier": [
    {
      "system": "https://medikiosk.local/fhir/patient-id",
      "value": "PAT-000001"
    }
  ],
  "name": [
    {
      "use": "official",
      "text": "John Doe",
      "family": "Doe",
      "given": ["John"]
    }
  ],
  "gender": "male",
  "birthDate": "1998-01-01"
}
```

---

# 7. Phase 11 Implementation Architecture

### 7.1 Verification Boundary Rule
- **Verified Clinical Summaries (`status = VERIFIED`)**: Produce `Composition.status = "final"` with author set to the verifying physician (`Practitioner/{doctorId}`).
- **Unverified Summaries (`status = AI_GENERATED | DOCTOR_REVIEWED`)**: Produce `Composition.status = "preliminary"` with author set to `Device/medikiosk-ai-engine`.

### 7.2 Patient Symptoms vs Diagnoses Invariant
- Acute patient symptoms are mapped strictly as **`Observation`** resources (with severity, onset, duration, location).
- They are **NEVER** converted into `Condition` diagnoses without clinical sign-off.
- Past medical history records are mapped to **`Condition`** (`category: problem-list-item`).

### 7.3 Deterministic Triage Rule Engine Preservation
- Triage priority is preserved as an **`Observation`** with:
  - `code: triage-category`
  - `valueString`: `URGENT` | `PRIORITY` | `STABLE`
  - Extension `https://medikiosk.local/fhir/StructureDefinition/triage-source`: `"RULE_ENGINE"`
  - Extension `https://medikiosk.local/fhir/StructureDefinition/triggered-rules`: triggered rules array

### 7.4 Document Attachment Security
- `DocumentReference` attachments use authenticated REST endpoints:
  `/api/intake/sessions/{sessionId}/documents/{documentId}/download`
- Local filesystem paths, OS drive letters, or temporary paths are strictly forbidden in exported resources.

### 7.5 External Integrations Boundary
- ABDM, HIS, and AWS HealthLake live integrations require external adapters in future phases.
- In Phase 11, exports create tracking entries in `integration_records` with status `PENDING` and error message `"Adapter not implemented in Phase 11: Future adapter required for live transmission"`.