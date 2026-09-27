# MediKiosk — AI Coding Agent Instructions

## 1. Project Overview

MediKiosk is an AI-powered clinical intake kiosk designed to collect and structure patient information before doctor consultation.

The system supports:

- Patient registration
- Consent
- Voice and touch-based interaction
- Multilingual input
- Clinical history collection
- Adaptive questioning
- Rapid red-flag triage
- Conventional medical history
- Ayurveda-integrated history
- Medical document scanning
- OCR and information extraction
- AI-assisted clinical structuring
- Doctor-ready clinical summaries
- Doctor review and verification
- FHIR-compatible data representation
- ABDM integration boundary
- HIS integration boundary
- AWS HealthLake integration boundary
- Secure patient data management
- Audit logging

The system is intended to assist healthcare workflows.

MediKiosk is NOT an autonomous diagnostic system.

The doctor remains the final clinical decision-maker.

---

# 2. Source of Truth

Before implementing functionality, read the relevant specification files inside:

    /docs

The specification files are the primary project requirements.

Available specifications:

    docs/PRODUCT_SPEC.md
    docs/FEATURES.md
    docs/USER_FLOWS.md
    docs/DATABASE.md
    docs/API_SPEC.md
    docs/AI_SPEC.md
    docs/TRIAGE_RULES.md
    docs/AYURVEDA_SPEC.md
    docs/FHIR_MAPPING.md
    docs/ABDM_SPEC.md
    docs/HIS_SPEC.md
    docs/HEALTHLAKE_SPEC.md
    docs/SECURITY.md

Do not invent conflicting requirements when the specifications already define the behaviour.

If a requirement is unclear:

1. Check the relevant specification.
2. Check related specifications.
3. Preserve existing architecture.
4. Do not silently invent a major product decision.
5. If implementation requires a product or clinical decision that is genuinely undefined, stop and clearly identify the decision that needs to be made.

---

# 3. Core Technology Stack

Use the following architecture unless a specification explicitly requires otherwise.

## Frontend

    React
    TypeScript
    Tailwind CSS

## Backend

    Java
    Spring Boot
    Spring Security
    REST APIs

## Database

    MySQL 8.x

## Interoperability

    FHIR-compatible data model

## External Integrations

    ABDM Adapter
    HIS Adapter
    AWS HealthLake Adapter

## AI / Intelligence

    AI/LLM service
    Speech-to-Text
    Text-to-Speech
    OCR
    Rule-based triage engine

---

# 4. High-Level Architecture

Follow this architecture:

    ┌─────────────────────────────┐
    │       Patient / Doctor      │
    └──────────────┬──────────────┘
                   ↓
    ┌─────────────────────────────┐
    │       React Frontend         │
    └──────────────┬──────────────┘
                   ↓
              HTTPS / REST
                   ↓
    ┌─────────────────────────────┐
    │      Spring Boot API        │
    └──────────────┬──────────────┘
                   ↓
    ┌─────────────────────────────┐
    │      Clinical Services      │
    └──────┬──────────┬───────────┘
           │          │
           ↓          ↓
    ┌──────────┐   ┌──────────────┐
    │  MySQL   │   │ AI / OCR /   │
    │   8.x    │   │ STT Services │
    └──────────┘   └──────────────┘
           │
           ↓
    ┌─────────────────────────────┐
    │       FHIR Service          │
    └──────┬────────┬─────────────┘
           │        │
           ↓        ↓
       External Integration Adapters
           │        │
      ┌────┴───┐ ┌──┴────┐ ┌────────────┐
      │  ABDM  │ │  HIS  │ │ HealthLake │
      └────────┘ └───────┘ └────────────┘ 