# MediKiosk — Adaptive Question Engine & Conversation Context

## 1. Core Responsibilities
The Adaptive Question Engine coordinates the patient-facing intake questionnaire. It ensures that:
- Every question addresses an uncollected clinical concept or clarifying intent.
- No redundant or repetitive questions are asked.
- Symptoms explicitly negated by the patient are never probed unnecessarily.
- Safety-critical red flags (radiating chest pain, dyspnea at rest, syncope, neuro red flags) are prioritized when relevant.
- Questioning automatically terminates when core data has been collected or upon reaching the maximum turn limit.

---

## 2. Information Intent Taxonomy
The system categorizes clinical questions under controlled intents:

| Clinical Concept | Question Intent | Purpose | Type | Safety Critical |
|------------------|-----------------|---------|------|-----------------|
| `CHEST_PAIN` | `CHEST_PAIN_CHARACTER` | Differentiate ischemic pressure from pleuritic pain | `CHOICE` | False |
| `CHEST_PAIN` | `CHEST_PAIN_AGGRAVATING` | Assess exertional aggravation (walking, activity) | `TEXT` | False |
| `CHEST_PAIN` | `CHEST_PAIN_RADIATION` | Screen for radiation to left arm, jaw, neck, back | `YES_NO` | **True** |
| `CHEST_PAIN` | `CHEST_PAIN_SEVERITY` | Standard 0–10 numerical pain rating | `SCALE_0_10` | **True** |
| `SHORTNESS_OF_BREATH` | `BREATHING_DIFFICULTY` | Screen for co-occurring dyspnea | `YES_NO` | **True** |
| `SHORTNESS_OF_BREATH` | `BREATHING_DIFFICULTY_REST` | Screen for orthopnea or dyspnea at rest | `YES_NO` | **True** |
| `FEVER` | `FEVER_DURATION` | Length of febrile illness | `TEXT` | False |
| `FEVER` | `FEVER_CHILLS` | Screen for shaking rigors / bacteremia risk | `YES_NO` | False |
| `HEADACHE` | `HEADACHE_RED_FLAGS` | Screen for meningismus, photophobia, vision changes | `YES_NO` | **True** |
| `HEADACHE` | `HEADACHE_SEVERITY` | Rate headache intensity 0–10 | `SCALE_0_10` | False |
| `ABDOMINAL_PAIN` | `ABDOMINAL_LOCATION` | Localize pain quadrant | `TEXT` | False |
| `ABDOMINAL_PAIN` | `ABDOMINAL_VOMITING` | Screen for acute abdomen or obstruction | `YES_NO` | **True** |

---

## 3. Intelligent Completion Policy & Stopping Boundaries
The questionnaire does **NOT** rely on a fixed 6-turn limit. Instead, it evaluates dynamic clinical completion criteria:

### Clinical Completion Criteria
Intake is complete when:
1. **Chief Complaint Characterized**: Core symptom features (duration, character, severity, location) are gathered.
2. **Safety-Critical Screening Complete**: All high-priority red flags (radiating chest pain, dyspnea at rest, syncope, neuro red flags, acute abdomen) are addressed.
3. **Conflicts Clarified**: Discrepancies between patient report and document OCR are resolved or flagged for physician inspection.
4. **No High-Priority Intent Remaining**: All relevant clinical intents for active symptoms are satisfied.
5. **Patient Intent**: Patient expresses completion intent ("That's all", "Nothing else", "I want to finish").

### Runaway Protection Ceiling
- A configurable safety ceiling (`medikiosk.adaptive.max-questions:20`) acts purely as a **runaway protection ceiling** against infinite AI conversational loops.
- **IMPORTANT**: The runaway ceiling is NOT the clinical completion criterion.

### Non-Diagnostic Safety Guarantee
- The AI never states "Patient is medically cleared", "Nothing else is wrong", or provides a diagnosis.

