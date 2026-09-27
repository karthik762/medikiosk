# MediKiosk — AWS HealthLake Integration Specification

## 1. Purpose

MediKiosk should be designed to support integration with AWS HealthLake as an optional cloud-based health data layer.

AWS HealthLake can provide a FHIR-based environment for storing and exchanging structured healthcare information.

Architecture:

    MediKiosk
        ↓
    FHIR Layer
        ↓
    HealthLake Adapter
        ↓
    AWS HealthLake

HealthLake must remain an external integration and must not become a hard dependency of the core MediKiosk application.

---

# 2. Integration Principle

AWS HealthLake is an external cloud service.

Therefore:

- MySQL 8.x remains the primary application database during development and prototype deployment.
- FHIR remains the interoperability representation.
- HealthLake-specific logic belongs in an adapter/service layer.
- HealthLake availability must not determine whether patient intake can continue.
- External failures must not cause patient data loss.
- HealthLake credentials must never be stored in source code.
- The application must not claim HealthLake connectivity unless an actual configured connection has been established and verified.

---

# 3. Architecture

Recommended architecture:

    React Frontend
          ↓
    Spring Boot Backend
          ↓
    Clinical Services
          ↓
    MySQL 8.x
        ↓
    FhirMappingService
        ↓
    HealthLakeAdapter
        ↓
    AWS HealthLake (MOCK Mode / S3 Boundary)

The frontend must never communicate directly with AWS HealthLake.

---

# 4. Role of MySQL 8.x

MySQL 8.x remains the primary application database.

All:

- Patient records
- Intake sessions
- Clinical summaries
- Doctor verification states
- Audit logs

are stored first in MySQL 8.x.

HealthLake receives derivative copies formatted as FHIR resources.

HealthLake should not replace MySQL 8.x during the initial MediKiosk implementation.

---

# 5. Role of HealthLake

HealthLake may be used as an external FHIR-based health data environment.

Potential responsibilities:

- Store standardized healthcare data.
- Support FHIR-based interoperability.
- Provide a cloud-based health data layer.
- Support future integration with healthcare systems.
- Provide a scalable external health-data infrastructure.

The exact production usage depends on the deployment architecture and AWS configuration.

---

# 6. HealthLake Adapter

Recommended package:

    com.medikiosk.healthlake

Structure:

```text
healthlake/
├── adapter/
│   ├── HealthLakeAdapter.java
│   └── HealthLakeAdapterImpl.java
├── config/
│   └── HealthLakeProperties.java
├── controller/
│   └── HealthLakeController.java
├── dto/
│   ├── HealthLakeExportRequestDto.java
│   ├── HealthLakeExportResponseDto.java
│   └── HealthLakeStatusDto.java
├── provider/
│   ├── AwsHealthLakeProvider.java
│   ├── HealthLakeExportResult.java
│   ├── HealthLakeIntegrationProvider.java
│   └── MockHealthLakeProvider.java
└── service/
    ├── HealthLakeService.java
    └── impl/
        └── HealthLakeServiceImpl.java
```

---

# 7. Implemented Endpoints & Safety Invariants (Phase 14)

### 7.1 REST Endpoints
- `GET /api/healthlake/status`: Returns current adapter operating mode, datastore endpoint, and cloud connectivity (`isMock = true`, `connected = false` by default in MOCK mode).
- `POST /api/healthlake/sessions/{sessionId}/export`: Transmits doctor-verified FHIR R4 clinical bundle to AWS HealthLake datastore. Restricted to `DOCTOR` and `ADMIN` roles.
- `GET /api/healthlake/sessions/{sessionId}/status`: Retrieves AWS HealthLake integration transaction history for the session.

### 7.2 Safety Invariants & Verification Boundary
- **No Fabricated Live Connection**: Explicitly declare `isMock = true` and `connected = false` unless live credentials and endpoints are actively verified. Status displays `AWS HealthLake connection: NOT CONNECTED (MOCK ONLY)`.
- **Doctor Verification Gate**: Cloud export strictly requires `ClinicalSummaryStatus.VERIFIED`. Unverified summaries (`AI_GENERATED`, `DOCTOR_REVIEWED`) are rejected with HTTP 412 Precondition Failed.
- **FHIR Profile Validation**: FHIR Document Bundles are validated prior to transmission. Invalid bundles fail with HTTP 422 Unprocessable Entity.
- **Identifier Separation**: MediKiosk internal UUID (`patient.id`) remains decoupled from cloud datastore references (`patient.healthlake_ref`).
- **Data Minimization**: Voice recordings and raw binary files are never transmitted to HealthLake. Medical documents are referenced via authenticated REST endpoints.
- **Audit Logging**: Every request, success, and failure is recorded in `audit_logs` and `integration_records` (`IntegrationType.HEALTHLAKE`).