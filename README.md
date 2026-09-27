# MediKiosk — AI-Powered Clinical Intake Kiosk & Doctor Review Platform

> **Status: PHASES 1–17 COMPLETE & PRODUCTION READY**  
> Complete end-to-end clinical intake pipeline, deterministic rule-based triage, Ayurveda-integrated intake, OCR document processing, AI structuring, doctor verification workstation, FHIR R4 interoperability, mock ABDM/HIS/HealthLake adapters, security hardening, and audit logging.

---

## 1. Project Overview & Clinical Mission

MediKiosk is an intelligent, bilingual clinical intake kiosk designed to collect, organize, and clinically structure patient intake data before doctor consultation.

### Core Clinical Principle
> **AI assists &rarr; Information is structured &rarr; Doctor verifies &rarr; Doctor decides.**  
> MediKiosk is **NOT** an autonomous diagnostic system. The human doctor remains the sole clinical decision-maker.

### Key Capabilities
- **Patient Registration & Consent:** Quick identity capture with explicit, timestamped digital consent (General Intake, Data Storage, AI Processing, Research).
- **Multimodal Interaction:** Touch-first interface and simulated voice intake with multilingual support (English, Hindi, Kannada, etc.).
- **Deterministic Rule-Based Triage:** Real-time red-flag evaluation (EMERGENCY / PRIORITY / ROUTINE) based on clinical protocol rules with mandatory explanation audit trails.
- **Comprehensive History Collection:** Chief complaints, onset/duration, severity scoring, past medical history, surgical history, and current medications.
- **Ayurveda-Integrated Clinical Intake:** Captures systemic and lifestyle indicators (Agni, Koshtha, Prakriti indicators, sleep, digestion) alongside conventional clinical data.
- **Medical Document Scanning & OCR:** Upload and simulated OCR extraction of lab reports, discharge summaries, and prescriptions into structured lab/vital records.
- **AI-Assisted Clinical Structuring:** Pre-consultation draft summaries generated from patient responses with clear confidence flags and doctor-editable fields.
- **Clinician Review Workstation:** Complete dashboard for doctors to inspect intake, edit AI drafts, adjust triage categories, and sign off with cryptographic audit records.
- **FHIR R4 Interoperability:** Automatic transformation of intake sessions into FHIR R4 Bundles (Patient, Observation, Condition, QuestionnaireResponse, DiagnosticReport).
- **External Integration Boundaries:** Clean architecture adapters for **ABDM** (Ayushman Bharat Digital Mission), **HIS** (Hospital Information Systems), and **AWS HealthLake**.

---

## 2. System Architecture

```
┌───────────────────────────────────────────────────────────────────────────┐
│                    Patient Kiosk / Clinician Workstation                  │
│                     (React 18 + TypeScript + Tailwind CSS)               │
└─────────────────────────────────────┬─────────────────────────────────────┘
                                      │ HTTPS / REST (/api/*)
                                      ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                 Spring Boot 3.4.2 API Gateway & Security                  │
│     - Stateless JWT Filter Chain       - Strict CORS & Security Headers   │
│     - Global Exception Sanitization    - Granular Role-Based Access       │
└─────────────────────────────────────┬─────────────────────────────────────┘
                                      │
       ┌──────────────────────────────┴──────────────────────────────┐
       ▼                                                             ▼
┌──────────────────────────────────────────┐    ┌──────────────────────────────────────────┐
│             Clinical Services            │    │          Interoperability Layer          │
│ - Rule-Based Triage Engine (Red Flags)   │    │ - FHIR R4 Model & Bundle Mapper          │
│ - Ayurveda Intake Service (Agni/Prakriti)│    │ - ABDM Adapter (MOCK / Sandbox Boundary) │
│ - Document OCR & Entity Extraction       │    │ - HIS Adapter (MOCK / HL7 Boundary)      │
│ - AI Clinical Structuring Service        │    │ - AWS HealthLake Adapter (MOCK Boundary) │
│ - Audit Logging & Clinical Verification  │    └──────────────────────────────────────────┘
└───────────────────┬──────────────────────┘
                    │
                    ▼
┌──────────────────────────────────────────┐
│             MySQL 8.x Database           │
│ - 15 Normalized Clinical Tables          │
│ - Flyway Schema Migrations               │
│ - Immutable Audit Trail                  │
└──────────────────────────────────────────┘
```

---

## 3. Technology Stack

### Frontend
- **Framework:** React 18
- **Language:** TypeScript 5.6
- **Build Tool:** Vite 5.4
- **Styling:** Tailwind CSS 3.4
- **Icons:** Lucide React
- **Testing:** Vitest + React Testing Library + JSDOM (54 tests passing)

### Backend
- **Framework:** Spring Boot 3.4.2
- **Language:** Java 21 (targeting Java 21 LTS)
- **Security:** Spring Security 6 with stateless JWT authorization
- **Persistence:** Spring Data JPA / Hibernate 6.6
- **Validation:** Hibernate Validator (Bean Validation 3.0)
- **Database Migrations:** Flyway 10.20
- **Testing:** JUnit 5 + Spring Boot Test + MockMvc (192 tests passing)

### Database
- **Production / Dev:** MySQL 8.x
- **In-Memory Testing / Quick Demo:** H2 Database (with `local` and `test` profiles in MySQL compatibility mode)

---

## 4. Repository Structure

```
medikiosk/
├── backend/                       # Spring Boot Java REST API
│   ├── pom.xml                    # Maven dependencies & build configuration
│   └── src/
│       ├── main/java/com/medikiosk/
│       │   ├── abdm/              # ABDM MOCK adapter & DTOs
│       │   ├── ai/                # AI structuring & clinical summarization
│       │   ├── audit/             # Audit logging & event tracking
│       │   ├── ayurveda/          # Ayurvedic clinical intake services
│       │   ├── config/            # DemoDataInitializer, CORS, Web config
│       │   ├── controller/        # REST endpoints (Auth, Intake, Review, etc.)
│       │   ├── document/          # Document upload & storage services
│       │   ├── dto/               # API Request/Response contracts
│       │   ├── entity/            # 15 JPA clinical entities (BaseEntity)
│       │   ├── exception/         # Global REST exception handler & error codes
│       │   ├── fhir/              # FHIR R4 mapping services
│       │   ├── healthlake/        # AWS HealthLake MOCK adapter
│       │   ├── his/               # HIS integration MOCK adapter
│       │   ├── ocr/               # OCR document parsing & entity extraction
│       │   ├── patient/           # Patient management service
│       │   ├── repository/        # Spring Data JPA repositories
│       │   ├── security/          # JWT filter, UserDetailsService, SecurityConfig
│       │   ├── service/           # Intake orchestration & clinical services
│       │   └── triage/            # Deterministic rule-based triage engine
│       └── resources/
│           ├── application.yml    # Default configuration (MySQL 8.x)
│           ├── application-local.yml # H2 in-memory local dev profile
│           └── db/migration/      # Flyway SQL schema migrations (V1–V13)
├── frontend/                      # React 18 + TypeScript + Vite UI
│   ├── src/
│   │   ├── components/            # UI components (Header, Dashboard, etc.)
│   │   ├── constants/             # App phases, languages, API constants
│   │   ├── hooks/                 # useBackendHealth, useSpeechRecognition
│   │   ├── pages/                 # Patient Kiosk, Doctor Workstation, Foundation
│   │   ├── services/              # Axios/Fetch API client wrappers
│   │   └── types/                 # TypeScript interfaces mirroring backend DTOs
│   ├── package.json
│   └── vite.config.ts             # Vite server & /api proxy to 8080
├── docs/                          # Source of Truth Specifications
│   ├── DEMO_GUIDE.md              # 16-step SIH demo script & walkthrough
│   ├── PRODUCT_SPEC.md            # Product vision, scope & clinical rules
│   ├── FEATURES.md                # Feature inventory & completion status
│   ├── USER_FLOWS.md              # Detailed Kiosk and Workstation journeys
│   ├── DATABASE.md                # Relational database schema documentation
│   ├── API_SPEC.md                # Complete REST API specifications
│   ├── AI_SPEC.md                 # Pre-consultation AI structuring protocol
│   ├── TRIAGE_RULES.md            # Emergency/Priority clinical red flags
│   ├── AYURVEDA_SPEC.md           # Agni, Koshtha, and Prakriti definitions
│   ├── FHIR_MAPPING.md            # FHIR R4 bundle & resource specification
│   ├── ABDM_SPEC.md               # Ayushman Bharat boundary definitions
│   ├── HIS_SPEC.md                # Hospital Information System boundary
│   ├── HEALTHLAKE_SPEC.md         # AWS HealthLake boundary definitions
│   └── SECURITY.md                # Security, RBAC & audit guidelines
└── docker-compose.yml             # Local MySQL 8.x container definition
```

---

## 5. Prerequisites

| Tool | Minimum Version | Recommended Version |
|---|---|---|
| **Java JDK** | 21 LTS | OpenJDK 21 or Oracle JDK 21+ |
| **Apache Maven** | 3.8+ | 3.9+ |
| **Node.js** | 20 LTS | 20+ or 22+ |
| **npm** | 10+ | 10+ |
| **MySQL** | 8.0+ | 8.4 LTS (Optional if using `-Dspring-boot.run.profiles=local`) |

---

## 6. How to Run Locally

### Option A: Quick Standalone Demo Mode (No MySQL required)
Uses an embedded H2 database with automatic schema generation and demo data pre-seeding.

#### Step 1: Start Backend (Terminal 1)
```bash
cd backend
mvn clean compile spring-boot:run -Dspring-boot.run.profiles=local
```
The backend will initialize and listen on `http://localhost:8080`.  
Verify health:
```bash
curl http://localhost:8080/api/health
```

#### Step 2: Start Frontend (Terminal 2)
```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:5173` in your browser.

---

### Option B: Full Production Mode with MySQL

#### Step 1: Start MySQL via Docker Compose
```bash
docker compose up -d mysql
```
*(Or ensure your native MySQL instance is running on port 3306 with database `medikiosk`, user `medikiosk`, password `medikiosk`).*

#### Step 2: Run Backend
```bash
cd backend
mvn clean compile spring-boot:run
```
Flyway will execute migrations `V1__initial_setup.sql` through `V14`, creating all 15 clinical tables.

#### Step 3: Run Frontend
```bash
cd frontend
npm run dev
```

---

## 7. Pre-Seeded Demo Accounts (Local Demo Only)

> **⚠️ LOCAL DEMO ONLY — never use these credentials in production.**  
> Demo initialization is explicitly opt-in (`medikiosk.demo.enabled=true` or `-Dspring-boot.run.profiles=local`).  
> In default production configurations (`medikiosk.demo.enabled=false`), demo data initialization and 1-click demo logins are strictly disabled.

| Role | Email / Username | Password | Purpose |
|---|---|---|---|
| **Doctor / Clinician** | `doctor@medikiosk.org` | `Doctor@12345` | *(LOCAL DEMO ONLY)* Doctor Review Workstation & Verification |
| **Administrator** | `admin@medikiosk.org` | `Admin@12345` | *(LOCAL DEMO ONLY)* Full administrative & audit access |
| **Patient** | `patient@medikiosk.org` | `Patient@12345` | *(LOCAL DEMO ONLY)* Patient kiosk portal access |

> **Pro-Tip for Demos:** When running in demo mode, the frontend features a secure **"1-Click Demo Doctor Login"** button that acquires a session token via `/api/demo/doctor-login` without exposing credentials in client source code. In production mode, this button is automatically hidden.

---

## 8. Running Automated Tests

### Backend Unit & Integration Tests (192 Tests)
Validates all controllers, security filters, triage rules, Ayurveda calculations, FHIR mappings, OCR parsers, and mock adapters:
```bash
cd backend
mvn test
```

### Frontend Unit Tests (54 Tests)
Validates UI rendering, component logic, state transitions, and mocked API responses:
```bash
cd frontend
npm test -- --run
```

### Frontend Production Build Validation
Ensures clean TypeScript compilation without warnings or unused symbols:
```bash
cd frontend
npm run build
```

---

## 9. Mock External Integrations Demarcation

MediKiosk features enterprise-grade integration boundaries designed to connect to external systems while running in **safe, deterministic offline mock mode** during hackathons and local evaluation:

| Adapter | Status | Mode | Boundary Description |
|---|---|---|---|
| **ABDM Adapter** | Implemented | `MOCK MODE / SANDBOX` | Simulates Ayushman Bharat Health Account (ABHA) creation, OTP verification, and consent artifact dispatch without live government servers. |
| **HIS Adapter** | Implemented | `MOCK MODE / SANDBOX` | Simulates patient demographic lookup, encounter registration, and clinical summary ingest into Hospital Information Systems. |
| **AWS HealthLake Adapter** | Implemented | `MOCK MODE / SANDBOX` | Simulates FHIR R4 Export to cloud data stores with mock storage IDs and S3 export paths without incurring AWS charges. |

> All mock adapters log simulated payloads clearly and return structured success/failure responses.

---

## 10. Security & Hardening Features

- **Stateless JWT Authentication:** Tokens expire after 24 hours; passwords hashed with BCrypt.
- **Role-Based Access Control (RBAC):** Distinct roles (`ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_ADMIN`).
- **Security Response Headers:** `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Cache-Control: no-cache, no-store`.
- **Global Error Sanitization:** No stack traces leaked to clients; sanitized JSON error responses for malformed JSON, unhandled verbs, and size limit violations.
- **Strict File Upload Limits:** Enforces 10MB maximum file size for document and audio uploads.
- **Immutable Audit Logging:** Every clinical verification, consent record, and triage calculation is recorded with user, action, IP, and timestamp.

---

## 11. SIH Judge Demo Walkthrough (Quick Summary)

For judges at the Smart India Hackathon:
1. **The Problem:** OPD queues in Indian hospitals are overwhelmed. Doctors spend 70% of consultation time typing basic history instead of examining patients.
2. **The Solution:** MediKiosk collects structured history, flags emergencies, takes Ayurvedic lifestyle input, and prepares an AI-assisted summary before the patient walks in.
3. **The Guarantee:** The AI summary is strictly editable; the doctor must review and sign off before data is pushed to FHIR/HIS/ABDM.

👉 See [DEMO_GUIDE.md](docs/DEMO_GUIDE.md) for the complete, scripted 16-step presentation walkthrough.
