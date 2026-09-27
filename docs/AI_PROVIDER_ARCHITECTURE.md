# MediKiosk — AI Provider Architecture & Gateway Specification

## 1. Overview
The MediKiosk AI subsystem decouples core clinical logic from specific AI models and service vendors via the `AiModelGateway` abstraction. This enables offline operation with zero external dependencies during SIH evaluations or disconnected hospital deployments, while providing seamless connectivity to OpenRouter / OpenAI endpoints when API credentials are provided.

---

## 2. Gateway Architecture

```
                    ┌────────────────────────┐
                    │   AIQuestionPlanner    │
                    │ ClinicalReasoningProv. │
                    └───────────┬────────────┘
                                │
                                ▼
                    ┌────────────────────────┐
                    │     AiModelGateway     │
                    │      (Interface)       │
                    └───────────┬────────────┘
                                │
               ┌────────────────┴────────────────┐
               ▼                                 ▼
┌─────────────────────────────┐   ┌─────────────────────────────┐
│    MockAiModelGateway       │   │OpenAiCompatibleAiModelGate. │
│ (Default / Offline / Safe)  │   │  (OpenRouter / OpenAI HTTP) │
└─────────────────────────────┘   └──────────────┬──────────────┘
                                                 │
                                                 ▼
                                  ┌─────────────────────────────┐
                                  │   https://openrouter.ai/    │
                                  │   /api/v1/chat/completions  │
                                  └─────────────────────────────┘
```

---

## 3. Tiered Model Routing

MediKiosk supports configurable tiered model routing:

| Tier | Config Key | Default Model | Primary Purpose |
|------|------------|---------------|-----------------|
| **Fast Model** | `medikiosk.ai.fast-model` | `google/gemini-2.0-flash-001` | Rapid question phrasing, conversational translation, symptom extraction |
| **Strong Model** | `medikiosk.ai.strong-model` | `anthropic/claude-3.5-sonnet` | Complex context synthesis, conflict resolution, clinical summary generation |

---

## 4. Automatic Fault Tolerance & Fallback Policy

If the external provider encounters:
- Network timeout (`> 5000ms`)
- HTTP 429 rate limiting
- HTTP 5xx upstream outage
- Malformed JSON output

**The gateway automatically and instantaneously falls back to `MockAiModelGateway` and the pre-approved clinical fallback templates**.
The patient never sees an error modal or broken interface. The kiosk maintains uninterrupted intake flow.

---

## 5. Security Guardrails

1. **No Frontend API Exposure**: All AI calls originate from the Spring Boot backend (`localhost:8080`). Frontend clients never receive or store the `AI_API_KEY`.
2. **Safe Default Configuration**: `AI_PROVIDER=mock` by default. No external API keys are required to build, test, or run MediKiosk.
3. **Opt-in External Integration Tests**: Real external API calls are disabled during normal Maven builds (`AI_EXTERNAL_TESTS=false`) to prevent accidental costs and build instability.
