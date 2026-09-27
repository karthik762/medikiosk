# MediKiosk — Multilingual Voice & Speech Architecture

## 1. Overview
MediKiosk supports voice-first patient intake across **English (`en`)**, **Hindi (`hi`)**, and **Telugu (`te`)**. The voice pipeline is fully integrated into the unified `ClinicalState`, ensuring that spoken language inputs update standard, language-independent clinical concept codes.

## 2. Speech Pipeline

```
Patient Audio (EN / HI / TE)
          ↓
Speech-to-Text (STT) Ephemeral Processing
          ↓
  Transcript Display & Kiosk Confirmation (Confirm / Edit / Retry)
          ↓
Multilingual Clinical Reasoner (Natural Language Extraction)
          ↓
   Language-Independent ClinicalState (Concept Codes)
          ↓
  AI Question Planner (Context-Aware Intent Selection)
          ↓
Validated Patient Question (English / Hindi / Telugu)
          ↓
   Text-to-Speech (TTS) Audio Playback
```

## 3. Language Concept Mapping Examples

| Patient Input Language | Spoken Statement | Extracted Clinical Concept Code | Fact Assertion |
| :--- | :--- | :--- | :--- |
| **English** | "I've had stomach pain since yesterday." | `ABDOMINAL_PAIN` | `PRESENT` (AFFIRMED) |
| **Hindi** | "Mujhe pet me dard hai aur bukhar nahi hai." | `ABDOMINAL_PAIN`<br>`FEVER` | `PRESENT` (AFFIRMED)<br>`ABSENT` (NEGATED) |
| **Telugu** | "Kaduppu noppi undi, jwaram ledu." | `ABDOMINAL_PAIN`<br>`FEVER` | `PRESENT` (AFFIRMED)<br>`ABSENT` (NEGATED) |
| **Code-Switched** | "I have bad pet me dard since kal se." | `ABDOMINAL_PAIN`<br>`DURATION` | `PRESENT` (AFFIRMED)<br>`1 DAY` (AFFIRMED) |

## 4. Safety & Fallback Principles
1. **Mandatory Patient Confirmation**: Spoken transcripts require explicit patient confirmation before clinical state updates.
2. **Graceful Kiosk Fallback**: If STT or TTS encounters network latency or failure, the kiosk seamlessly falls back to text and touch controls without halting intake.
3. **Ephemeral Audio Storage**: Audio files are processed in-memory/temp files and immediately deleted to comply with security guidelines.
