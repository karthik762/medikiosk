# MediKiosk — ABDM Integration Specification

## 1. Purpose

MediKiosk supports integration with India's Ayushman Bharat Digital Mission (ABDM).

The ABDM layer provides a secure, standards-compliant interoperability boundary between MediKiosk and the broader digital health ecosystem.

Architecture:

    MediKiosk
        ↓
    Doctor-Verified Clinical Summary
        ↓
    FHIR R4 Layer
        ↓
    ABDM Adapter
        ↓
    ABDM Ecosystem (Mock / Sandbox / Production)

The ABDM integration remains strictly isolated from core application intake logic.

---

# 2. Integration Principles

ABDM is an external integration.

Therefore:

- MySQL 8.x remains the primary application database and source of truth.
- FHIR R4 remains the interoperability data representation.
- ABDM-specific logic belongs strictly in the `com.medikiosk.abdm` adapter layer.
- ABDM availability does not determine whether patient intake or doctor consultations continue.
- External gateway failures never cause local patient clinical data loss.
- **No Fake Live Connection**: Live ABDM gateway connection is **NOT CONNECTED** in environments without authentic production credentials. Operating in deterministic MOCK mode is clearly declared in all APIs (`isMock = true`).
- **No Synthetic Identity Claims**: Synthetic test ABHA numbers used in mock mode are strictly flagged as test verifications and never masqueraded as real-world identities.

---

# 3. ABDM Architecture

```
    React Frontend (AbdmStatusPanel)
          ↓ (HTTPS / REST)
    Spring Boot Backend (AbdmController)
          ↓
    Clinical Domain (Doctor Verification & Consent Gating)
          ↓
    MySQL 8.x (Patients, Consents, IntegrationRecords, AuditLogs)
          ↓
    FHIR Service (FhirMappingService: Bundle generation & validation)
          ↓
    com.medikiosk.abdm.service.AbdmService
          ↓
    com.medikiosk.abdm.provider.AbdmIntegrationProvider
       ┌───────────────────────┴───────────────────────┐
       ▼                                               ▼
MockAbdmProvider (Deterministic Local/Test)     SandboxAbdmProvider (ABDM Gateway)
```

The frontend never communicates directly with ABDM APIs or holds confidential gateway credentials.

---

# 4. Package Structure

Package root: `com.medikiosk.abdm`

```text
abdm/
├── config/
│   └── AbdmProperties.java              # @ConfigurationProperties(prefix = "app.abdm")
├── controller/
│   └── AbdmController.java              # REST endpoints for status, verification, consent, exchange
├── dto/
│   ├── AbdmStatusDto.java               # Health, mode, and connectivity state
│   ├── AbhaVerificationRequestDto.java  # Verification input
│   ├── AbhaVerificationResponseDto.java # Verification result and masked ID
│   ├── AbdmConsentRequestDto.java       # Consent grant input
│   ├── AbdmConsentResponseDto.java      # Consent artefact status
│   ├── AbdmExchangeRequestDto.java      # Clinical transmission request
│   ├── AbdmExchangeResponseDto.java     # Exchange transaction result
│   └── IntegrationRecordDto.java        # Clean DTO for integration history
├── provider/
│   ├── AbdmIntegrationProvider.java     # Gateway abstraction interface
│   ├── AbdmExchangeResult.java          # Transmission result record
│   ├── MockAbdmProvider.java            # Deterministic mock provider (isMock=true)
│   └── SandboxAbdmProvider.java         # Gateway provider skeleton (requires credentials)
└── service/
    ├── AbdmService.java                 # Business domain contract
    └── impl/
        └── AbdmServiceImpl.java         # Verification, consent, and gating orchestrator
```

---

# 5. Operating Modes

| Mode | Environment | External Calls | Credentials Required | Flag |
|---|---|---|---|---|
| `MOCK` | Local dev, automated test suites | None (in-memory deterministic simulation) | None | `isMock: true`, `connected: false` |
| `SANDBOX` | Staging / ABDM Sandbox integration | HTTPS to ABDM Sandbox Gateway | `ABDM_CLIENT_ID`, `ABDM_CLIENT_SECRET` | `isMock: false`, `connected: true/false` |
| `PRODUCTION` | Live certified hospital deployment | HTTPS to ABDM Production Gateway | Production OAuth client credentials | `isMock: false`, `connected: true` |

---

# 6. ABHA Identity Lifecycle & Boundary

The national health ID (ABHA) is an external identifier and never replaces the internal MediKiosk UUID or `patient_identifier`.

### ABHA States (`AbhaStatus` enum):
1. `NOT_PROVIDED`: Patient has not entered an ABHA identifier.
2. `UNVERIFIED`: ABHA identifier or address entered by patient but not yet verified against national registry.
3. `VERIFIED`: ABHA confirmed via authorized OTP or demographic verification through ABDM gateway.

### Masking & Privacy:
ABHA numbers (`XX-XXXX-XXXX-1234`) and addresses (`na***@sbx`) are masked in general responses, UI displays, and log files per `docs/SECURITY.md`.

---

# 7. Consent Management Lifecycle

ABDM health data sharing requires a dedicated consent workflow distinct from initial kiosk intake consent:

- **Consent Type**: `ABDM_DATA_SHARE`
- **Statuses**:
  - `ACCEPTED`: Active patient consent for sharing clinical summary.
  - `REVOKED`: Patient has explicitly withdrawn consent; subsequent clinical exchange is strictly blocked.
- **Audit**: Every consent grant and revocation is recorded in `consents`, `integration_records`, and `audit_logs`.

---

# 8. Clinical Data Exchange & Gating Rules

Before any clinical data is transmitted to ABDM:

1. **Doctor Verification Check**: The clinical summary must have status `VERIFIED` signed off by a licensed doctor. Unverified AI pre-consult summaries are strictly rejected (HTTP 412).
2. **Consent Check**: Active, unrevoked consent with type `ABDM_DATA_SHARE` must exist for the intake session (HTTP 412).
3. **FHIR Validation Check**: The session data must pass all FHIR R4 profile and invariant validations (HTTP 422).
4. **Idempotency Check**: Requests with duplicate idempotency keys return existing successful records rather than creating duplicate transmissions.
5. **IntegrationRecord Persistence**: Status is tracked across `PENDING`, `SUCCESS`, and `FAILED` states.
6. **Audit Logging**: Operations emit `ABDM_REQUESTED`, `ABDM_SUCCEEDED`, or `ABDM_FAILED` events.

---

# 9. Current Integration Status

- **ABDM Live Connection**: NOT CONNECTED (Deterministic Mock Provider active).
- **HIS Integration**: NOT IMPLEMENTED IN PHASE 12.
- **AWS HealthLake Integration**: NOT IMPLEMENTED IN PHASE 12.