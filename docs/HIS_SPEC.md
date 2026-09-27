# MediKiosk — HIS Integration Specification

## 1. Purpose

MediKiosk supports integration with a Hospital Information System (HIS).

The HIS integration provides an architectural boundary between MediKiosk and a hospital's existing clinical, outpatient, and administrative systems.

Architecture:

    MediKiosk Intake & Review
              ↓
    Doctor Verification Gate (ClinicalSummaryStatus.VERIFIED)
              ↓
    FHIR Layer (HAPI FHIR R4 Document Bundle)
              ↓
    HIS Adapter Boundary (com.medikiosk.his)
              ↓
    Hospital Information System (Outpatient / Triage / Emergency Queues)

The HIS integration remains isolated from core intake and application logic.

---

## 2. Integration Principles & Invariants

1. **MySQL 8.x as Primary MediKiosk Database**: All intake sessions, clinical summaries, and patient data remain authoritative in MediKiosk.
2. **FHIR R4 as Interoperability Currency**: MediKiosk reuses the Phase 11 `FhirMappingService` for bundle generation and validation. The HIS adapter does not duplicate clinical models.
3. **Doctor Verification Gate**: Unverified AI draft summaries are strictly blocked from external transmission (HTTP 412 PRECONDITION_FAILED). Only doctor-verified cases (`ClinicalSummaryStatus.VERIFIED`) may be exported.
4. **Identifier Separation**: MediKiosk internal UUIDs (`patient.id`) and patient identifiers (`patient_identifier`) remain distinct from hospital identifiers (`patients.his_id`). Linking does not overwrite patient records.
5. **Deterministic Mock Mode Invariant**: In local development, demo, and test environments, the system operates in `MOCK` mode (`isMock = true`, `connected = false`). It explicitly identifies itself as NOT CONNECTED to any live hospital network.
6. **Resilience & Fault Tolerance**: Failure or timeout of the external HIS adapter never blocks patient intake at the kiosk. Operations are recorded in `integration_records` with full error diagnostic logs.
7. **Deterministic Idempotency**: Repeated export attempts using the same idempotency key return the existing record without duplicate transmission.

---

## 3. HIS Architecture & Provider Pattern

Package: `com.medikiosk.his`

```text
com.medikiosk.his/
├── adapter/
│   ├── HisAdapter.java
│   └── HisAdapterImpl.java
├── config/
│   └── HisProperties.java
├── controller/
│   └── HisController.java
├── dto/
│   ├── HisExportRequestDto.java
│   ├── HisExportResponseDto.java
│   ├── HisLinkPatientRequestDto.java
│   ├── HisLinkPatientResponseDto.java
│   ├── HisPatientCandidateDto.java
│   ├── HisPatientSearchResponseDto.java
│   └── HisStatusDto.java
├── provider/
│   ├── HisExportResult.java
│   ├── HisIntegrationProvider.java
│   ├── MockHisProvider.java
│   └── HospitalHisProvider.java
└── service/
    ├── HisService.java
    └── impl/
        └── HisServiceImpl.java
```

### 3.1 Provider Implementations

1. **MockHisProvider (`mockHisProvider`)**:
   - Used in local, development, and testing environments.
   - Explicitly returns `isMock = true` and `connected = false`.
   - Provides deterministic patient candidates for hospital Master Patient Index (MPI) search.
   - Deterministically simulates timeout triggers and 500 internal server error responses for robustness testing.
2. **HospitalHisProvider (`hospitalHisProvider`)**:
   - Boundary provider for real or staging hospital integration.
   - Evaluates client credentials and endpoint URLs from application configuration.
   - Gracefully reports unconfigured and disabled when real credentials are not supplied.

---

## 4. API Endpoints

### 4.1 Check Adapter Status
- **GET** `/api/his/status`
- **Access**: Authenticated users
- **Response**: `HisStatusDto` (mode, isMock, enabled, baseUrl, connected, message)

### 4.2 Export Clinical Summary & FHIR Bundle
- **POST** `/api/his/sessions/{sessionId}/export`
- **Access**: DOCTOR, ADMIN
- **Precondition**: `summary.status == VERIFIED` (HTTP 412 if unverified)
- **Validation**: FHIR R4 Bundle validation (HTTP 422 if invalid)
- **Request**: `HisExportRequestDto` (`intakeQueue`, `hisId`, `idempotencyKey`, `notes`)
- **Response**: `HisExportResponseDto` (integrationRecordId, encounterReference, status, queue, isMock)

### 4.3 Get Session Integration History
- **GET** `/api/his/sessions/{sessionId}/status`
- **Access**: DOCTOR, ADMIN (or patient owning the session)
- **Response**: List of `IntegrationRecordDto` for `IntegrationType.HIS`

### 4.4 Search Hospital MPI
- **GET** `/api/his/patients/search?query={q}&searchType={type}`
- **Access**: DOCTOR, ADMIN
- **Response**: `HisPatientSearchResponseDto` (candidates matching name, phone, or MRN)

### 4.5 Link Hospital Patient MRN
- **POST** `/api/his/patients/{patientId}/link`
- **Access**: DOCTOR, ADMIN
- **Request**: `HisLinkPatientRequestDto` (`hisId`, `notes`)
- **Response**: `HisLinkPatientResponseDto` (patientId, patientIdentifier, hisId, linkedAt)

---

## 5. Audit Logging & Tracking

All operations record persistent audit logs (`audit_logs`) and integration tracking entries (`integration_records`):

| Operation | Integration Record Operation | Audit Log Action |
| :--- | :--- | :--- |
| Clinical Export Requested | `HIS_CLINICAL_EXPORT` (PENDING) | `HIS_REQUESTED` |
| Clinical Export Succeeded | `HIS_CLINICAL_EXPORT` (SUCCESS) | `HIS_SUCCEEDED` |
| Clinical Export Failed | `HIS_CLINICAL_EXPORT` (FAILED) | `HIS_FAILED` |
| Hospital Patient Search | N/A | `HIS_PATIENT_SEARCH` |
| Patient ID Linked | `HIS_PATIENT_LINK` (SUCCESS) | `HIS_PATIENT_LINK` |