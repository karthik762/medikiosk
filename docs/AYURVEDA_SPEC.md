# MediKiosk — Ayurveda Specification

## 1. Purpose

MediKiosk supports Ayurveda-oriented clinical case-taking alongside conventional medical history.

The Ayurveda workflow is designed to capture structured information that can be reviewed by an authorized clinician.

The system should support:

- Prakriti
- Nidana
- Pariksha
- Structured Ayurveda history
- Standardized terminology
- NAMASTE terminology/coding support
- ICD-11 TM2-related mapping support

The Ayurveda module is an information-capture and structuring component.

It must not independently prescribe treatment or make autonomous clinical decisions.

---

# 2. Core Principle

MediKiosk supports both:

    CONVENTIONAL CLINICAL HISTORY

and:

    AYURVEDA CASE HISTORY

within the same patient intake workflow.

Architecture:

    Patient
       ↓
    MediKiosk Intake
       ↓
    ┌──────────────────────┐
    │ Conventional History │
    └──────────────────────┘
              +
    ┌──────────────────────┐
    │   Ayurveda History   │
    └──────────────────────┘
              ↓
    Structured Patient Record
              ↓
    Doctor Review

---

# 3. Ayurveda Workflow

Patient Intake
    ↓
Basic Patient Information
    ↓
Chief Complaint
    ↓
Initial Triage
    ↓
Conventional Clinical History
    ↓
Ayurveda History
    ↓
Prakriti
    ↓
Nidana
    ↓
Pariksha
    ↓
Document Processing
    ↓
Structured Summary
    ↓
Doctor Review

The exact order may be adjusted by the configured clinical workflow.

---

# 4. Ayurveda Module Components

The module contains:

1. Ayurveda intake selection
2. Prakriti information
3. Nidana information
4. Pariksha information
5. Ayurveda terminology
6. Structured storage
7. AI-assisted organization
8. Doctor review
9. Terminology/coding support

---

# 5. Ayurveda Intake Selection

The patient workflow may ask whether an Ayurveda-oriented case history is required.

Example:

```text
How would you like to continue?

[ Conventional History ]

[ Ayurveda + Conventional History ]