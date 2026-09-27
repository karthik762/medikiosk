# MediKiosk — Phases 24–27 Implementation & Verification Report
## Multimodal AI Clinical Interview & Intelligent Completion Policy

### 1. What Was Already Present (Phases 20–23 Baseline)
Prior to starting Phases 24–27, the codebase contained:
- `AIQuestionPlanner` & `QuestionSafetyValidator` for single-turn intent planning and safety guardrail enforcement.
- `ClinicalState` & `ConversationContext` for tracking dialogue state and sliding conversation history.
- `ClinicalReasoningProvider` & `MockClinicalReasoningProvider` for offline concept extraction and negation detection.
- `OpenAiCompatibleAiModelGateway` for external LLM API connectivity (OpenAI/OpenRouter).
- Basic STT / TTS voice abstraction interfaces and mock document OCR extractions.
- Doctor Review Workstation with provenance badge rendering.
- Baseline test suite (220 backend tests, 60 frontend tests passing).

---

### 2. What Was Upgraded
- **Multimodal Pipeline Convergence**: Extended `ClinicalState` to act as the single source of truth for TEXT, VOICE, DOCUMENT/OCR, CAMERA, and TOUCH inputs. Removed separate logic paths.
- **Voice Pipeline Extension (Phase 24.1 & 24.2)**: Audio → STT → Transcript → Context Extraction → `ClinicalState` → `AIQuestionPlanner` → `QuestionSafetyValidator` → Question → TTS. Voice answers are marked with `PATIENT_VOICE` provenance.
- **Natural Language Severity & Exertional Extraction (Phase 24.2)**: Upgraded reasoning provider to extract numeric severity from natural phrases (e.g., "around seven" → `SEVERITY = 7`) and compound exertional factors ("especially when walking" → `EXERTIONAL = PRESENT`).
- **TTS Question Isolation (Phase 24.3)**: Ensured TTS speaks ONLY validated patient-facing questions, strictly excluding internal reasoning, metadata, prompts, or confidence scores.
- **Document & Image Context Integration (Phase 25)**:
  - *Confidence Gating (>0.85)*: High-confidence OCR extractions contribute directly to `ClinicalState` facts (`DOCUMENT_OCR` provenance), preventing duplicate medication questions.
  - *Low Confidence Gating (<0.85)*: Triggers neutral confirmation questions ("We found a medication name in your document. Can you confirm whether you currently take it?") without exposing internal confidence floats to the patient.
  - *Neutral Conflict Resolution (Phase 25.2)*: Discrepancies between patient report and document OCR (e.g. "No known allergies" vs "Penicillin allergy") generate neutral clarification questions without silently overwriting either source.
- **Intelligent Completion Policy (Phase 26)**:
  - **Removed the hard 6-turn completion limit**.
  - Replaced with dynamic clinical criteria: Chief complaint understood, major symptoms characterized, safety-critical screening complete, conflicts addressed, and no high-priority intents remaining.
  - Added configurable `MAX_AI_QUESTIONS` (default: 20) purely as a **runaway safety ceiling** (distinct from clinical completion criteria).
  - Implemented **Patient Termination Intent** detection ("That's all", "Nothing else", "I want to finish"), allowing patients to conclude intake when safety items are satisfied.
- **AI Question Quality & Multi-Turn Verification (Phase 27)**: Added `MultiTurnEvaluationTest.java` verifying 5 multi-turn clinical scenarios (Chest Pain, Abdominal Pain, Negation, Document+Conversation, Conflict Resolution) and natural language severity extraction. Verified `OpenAiCompatibleAiModelGateway` integration when `AI_PROVIDER=external/openrouter`.

---

### 3. Multimodal Architecture

```
┌───────┐   ┌───────┐   ┌──────────┐   ┌────────┐   ┌───────┐
│ TEXT  │   │ VOICE │   │ DOCUMENT │   │ CAMERA │   │ TOUCH │
└───┬───┘   └───┬───┘   └────┬─────┘   └───┬────┘   └───┬───┘
    │           │            │             │            │
    └───────────┴─────┬──────┴─────────────┴────────────┘
                      ▼
            Input Normalization
                      │
                      ▼
              Clinical Context
                      │
                      ▼
               Clinical State
                      │
                      ▼
              AI Question Planner
                      │
                      ▼
           Question Safety Validator
                      │
                      ▼
           Patient-Facing Question (UI & TTS)
```

---

### 4. Voice Pipeline
- Audio file uploaded via multipart form → ephemerally transcribed via STT provider → transcript passed to `ClinicalReasoningProvider` → facts stored with `PATIENT_VOICE` provenance.
- Conversational answers supported ("Honestly, it's around seven, especially when walking").
- Front-end explicit Voice UI state machine: `READY` | `LISTENING` | `UNDERSTANDING` | `QUESTION_READY` | `SPEAKING` | `WAITING` | `RETRY`.

---

### 5. Document Pipeline
- Document uploaded → validated → OCR processed.
- Confidence >= 0.85: `ClinicalFact` created with `DOCUMENT_OCR` provenance. Duplicate questions avoided.
- Confidence < 0.85: `LOW_CONFIDENCE_OCR_MEDICATION` fact created; neutral confirmation question generated.
- Conflicts: Detected and stored in `ClinicalState.conflicts`. Both sources preserved for physician review.

---

### 6. AI Question Generation & Routing
- `AIQuestionPlanner` selects candidate intent from `ClinicalContextResult` or priority conflict/OCR queues.
- If `AI_PROVIDER=external/openrouter`, invokes `OpenAiCompatibleAiModelGateway.generateQuestion(...)` to fetch natural phrasing from external LLM.
- Candidate question evaluated by `QuestionSafetyValidator`.
- If valid, displayed to patient with `generatedBy` provider metadata.
- If invalid or timeout, falls back to approved clinical template.

---

### 7. Completion Policy
- **No Fixed 6-Turn Cutoff**: Intake completes when clinical characterization and safety criteria are satisfied.
- **Runaway Protection**: Configurable ceiling (`medikiosk.adaptive.max-questions:20`) prevents infinite loops.
- **Patient Intent**: Patient saying "That's all" triggers completion if safety checks pass.
- **Safety Disclaimers**: AI never states "Patient is medically cleared", "Nothing else is wrong", or makes a diagnosis.

---

### 8. Safety & Guardrails
- `QuestionSafetyValidator` blocks diagnostic statements, treatment advice, false reassurance, and duplicate questions.
- Fail-safe fallback to approved clinical templates.
- Patient authorization enforced on all session endpoints; API keys kept strictly server-side.

---

### 9. External Provider Status & OpenRouter Configuration
- **Mock Mode (Default)**: `AI_PROVIDER=mock`, operates 100% offline with zero external network dependencies.
- **External Mode**: Configurable via environment variables:
  ```properties
  medikiosk.ai.provider=openrouter
  medikiosk.ai.base-url=https://openrouter.ai/api/v1
  medikiosk.ai.api-key=${OPENROUTER_API_KEY}
  medikiosk.ai.model=google/gemini-2.0-flash-001
  ```

---

### 10. Fallback Mechanisms
- External API Timeout (>5s) or HTTP 429/5xx → Instantaneous fallback to `MockAiModelGateway` and approved templates.
- STT Failure → Text fallback prompt.
- OCR Low Confidence → Neutral patient confirmation question.

---

### 11. Testing & Verification Summary
- **Backend Tests**: 226+ tests passing (`mvn clean test`). Includes `MultiTurnEvaluationTest.java` (5 multi-turn scenarios).
- **Frontend Tests**: 60/60 tests passing (`npm test -- --run`).
- **Build Verification**: `npm run build` completes cleanly with zero errors.

---

### 12. Mock vs Real AI Verification
- **Mock Mode Verification**: Tested locally during standard Maven and Vitest runs. All 5 multi-turn evaluation scenarios pass in mock mode.
- **Real External AI Mode**: Opt-in capability via `medikiosk.ai.provider=external` and valid API key. In test environments without external API keys, fallback to Mock Gateway is verified to preserve system availability.

---

### 13. Remaining Limitations
- Speech-to-Text provider in mock environment uses synthesized transcript fallbacks when binary audio input is synthetic. Real hardware microphone integration requires browser WebAudio API access in production deployment.
- External LLM provider latency depends on network connectivity to OpenRouter endpoints.
