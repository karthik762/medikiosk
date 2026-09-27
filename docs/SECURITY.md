# MediKiosk — Security Specification

## 1. Purpose

MediKiosk handles sensitive patient and clinical information.

Security must therefore be built into the application architecture rather than added after implementation.

The security architecture must protect:

- Patient information
- Clinical history
- Medical documents
- AI-generated summaries
- Doctor verification records
- Consent information
- Authentication credentials
- Integration credentials
- Audit information

---

# 2. Security Principles

MediKiosk follows these principles:

- Least privilege
- Defense in depth
- Secure by default
- Explicit authorization
- Data minimization
- Encryption in transit
- Encryption at rest
- Complete auditability
- Separation of environments
- Secure secret management
- Human verification for clinical decisions

Security controls must not prevent the core patient intake workflow from safely preserving collected information.

---

# 3. Security Architecture

Recommended architecture:

    Patient / Doctor
          ↓
    React Frontend
          ↓
    HTTPS
          ↓
    Spring Boot Backend
          ↓
    Authentication / Authorization
          ↓
    Clinical Services
          ↓
    MySQL
          ↓
    Secure External Integrations

External integrations:

    Spring Boot
       ├── ABDM Adapter
       ├── HIS Adapter
       └── HealthLake Adapter

The frontend must not directly communicate with protected external systems.

---

# 4. Authentication

MediKiosk must require authentication for protected doctor and administrator workflows.

Patient kiosk workflows may use a controlled temporary session rather than a permanent authenticated account.

Recommended authentication mechanism for the backend:

    Spring Security
          ↓
        JWT
          ↓
    Authenticated User

Authentication must be handled by the backend.

---

# 5. Role-Based Access Control

MediKiosk should use role-based access control.

Recommended roles:

```text
PATIENT
DOCTOR
ADMIN
```

---

# 6. ABDM & ABHA Security Controls (Phase 12)

### 6.1 ABHA Identifier Privacy & Masking
- ABHA numbers (`XX-XXXX-XXXX-1234`) and addresses (`na***@sbx`) are masked when displayed on clinical UI screens, patient queues, and audit logs.
- Full unmasked ABHA numbers are only used internally by the backend server when communicating with the ABDM adapter.
- The browser and client storage never store raw ABHA numbers.

### 6.2 Token and Credential Isolation
- ABDM client credentials (`ABDM_CLIENT_ID`, `ABDM_CLIENT_SECRET`) and gateway tokens remain exclusively server-side.
- The frontend never receives external ABDM tokens or calls ABDM endpoints directly.
- Credentials and access tokens are never logged or returned in error payloads.

### 6.3 Clinical Data Gating
- Only clinical summaries signed off and verified by licensed doctors (`ClinicalSummaryStatus == VERIFIED`) are eligible for external exchange.
- Transmission of unverified AI summaries is blocked server-side (HTTP 412).
- Explicit, active patient consent for data sharing (`consentType == "ABDM_DATA_SHARE"`) is mandatory. Revoked consent immediately blocks data exchange.
- `GENERAL_INTAKE` consent authorizes clinical intake only and must never be treated as ABDM data-sharing authorization. Consent types remain distinct and auditable.

### 6.4 Audit Trail
- All ABDM operations are audited:
  - `ABDM_REQUESTED`
  - `ABDM_SUCCEEDED`
  - `ABDM_FAILED`
  - `ABHA_VERIFICATION_REQUESTED`
  - `ABHA_VERIFICATION_SUCCEEDED`
  - `ABHA_VERIFICATION_FAILED`
  - `ABDM_CONSENT_GRANTED`
  - `ABDM_CONSENT_REVOKED`

---

# 7. Hospital Information System (HIS) Security Controls (Phase 13)

### 7.1 Doctor Verification Boundary Gate
- Unverified AI pre-consult summaries are strictly prohibited from transmission to hospital information systems.
- Attempting to export an unverified or draft summary returns HTTP 412 (PRECONDITION_FAILED) and generates no external requests or integration records.
- Only summaries in `ClinicalSummaryStatus.VERIFIED` signed by a licensed doctor can be exported.

### 7.2 Patient Identifier Separation
- MediKiosk internal UUIDs (`patient.id`) and patient identifiers (`patient_identifier`) remain authoritative and decoupled from external hospital MRNs (`patients.his_id`).
- Linking a hospital HIS MRN requires `ROLE_DOCTOR` or `ROLE_ADMIN` and does not overwrite or corrupt core MediKiosk patient records.

### 7.3 Role-Based Access Control (RBAC)
- Only `ROLE_DOCTOR` and `ROLE_ADMIN` can trigger clinical exports to hospital queues or perform patient lookups in the hospital Master Patient Index (MPI).
- `ROLE_PATIENT` attempts to initiate HIS exports, search MPI, or link hospital IDs are blocked with HTTP 403 (FORBIDDEN).

### 7.4 Credential & Network Isolation
- External HIS connection credentials (`HIS_CLIENT_ID`, `HIS_CLIENT_SECRET`, endpoint URLs) are managed exclusively through backend environment configuration.
- The frontend never communicates directly with hospital HIS endpoints.
- In `MOCK` mode, the system operates completely offline without network calls, explicitly declaring its status as `connected: false` and `MOCK MODE (NOT CONNECTED)`.

### 7.5 Immutable Audit Trail
- All HIS integration actions are logged into `audit_logs` and tracked in `integration_records`:
  - `HIS_REQUESTED`: When clinical transmission is initiated.
  - `HIS_SUCCEEDED`: When transmission successfully acknowledges external intake queue reception.
  - `HIS_FAILED`: When transmission fails or encounters gateway timeouts.
  - `HIS_PATIENT_SEARCH`: When clinical staff query hospital MPI records.
  - `HIS_PATIENT_LINK`: When a patient is associated with an external hospital MRN.

---

## 8. AWS HealthLake Security & Verification Boundary (Phase 14)

### 8.1 Verification Gate (Hard Precondition)
- Attempting to export an unverified summary to AWS HealthLake returns HTTP 412 (PRECONDITION_FAILED).
- Only summaries in `ClinicalSummaryStatus.VERIFIED` evaluated and certified by a licensed doctor can be transmitted to cloud FHIR datastores.

### 8.2 Credential & Cloud Isolation
- AWS credentials, datastore endpoints, and SigV4 configurations are managed exclusively through backend configuration (`HEALTHLAKE_ENDPOINT`, `HEALTHLAKE_DATASTORE_ID`, `HEALTHLAKE_REGION`).
- The frontend never communicates directly with AWS HealthLake endpoints.
- In default `MOCK` mode, no network calls are made to AWS, and the system explicitly reports `isMock: true` and `connected: false` ("AWS HealthLake connection: NOT CONNECTED").

### 8.3 Data Minimization & Document References
- Binary files, voice audio recordings, and raw attachments are never uploaded to HealthLake.
- Attached medical documents are referenced strictly via authenticated MediKiosk REST endpoints (`/api/intake/sessions/{id}/documents/{docId}/download`), preventing exposure of server filesystem paths.

### 8.4 Role-Based Access Control (RBAC)
- Only `ROLE_DOCTOR` and `ROLE_ADMIN` can trigger HealthLake exports.
- `ROLE_PATIENT` attempts to export to HealthLake are blocked with HTTP 403 (FORBIDDEN).

### 8.5 Audit Trail
- All HealthLake operations are logged with structured metadata into `audit_logs` and `integration_records`:
  - `HEALTHLAKE_REQUESTED`: When cloud export is initiated.
  - `HEALTHLAKE_SUCCEEDED`: When cloud export is confirmed.
  - `HEALTHLAKE_FAILED`: When cloud export fails or is rejected.

