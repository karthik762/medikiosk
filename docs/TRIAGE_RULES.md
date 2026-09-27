# MediKiosk — Triage Rules Specification

## 1. Purpose

The MediKiosk triage engine performs rapid screening and routing of patients based on approved clinical rules.

Its purpose is to identify cases that may require faster clinical attention.

The triage engine is NOT a diagnostic system.

Core principle:

> Triage routes. It does not diagnose.

---

# 2. Triage Architecture

The triage engine is rule-based.

Architecture:

    Patient Responses
          ↓
    Structured Intake Data
          ↓
    Approved Triage Rules
          ↓
    Rule Evaluation
          ↓
    Triggered Conditions
          ↓
    Priority Classification
          ↓
    Doctor Dashboard

AI is not responsible for determining the final triage priority.

---

# 3. Priority Levels

MediKiosk uses three primary priority levels:

## URGENT

Potentially concerning information has triggered an approved urgent-routing rule.

Action:

- Clearly flag the patient.
- Surface the case prominently on the doctor dashboard.
- Follow the configured escalation workflow.
- Avoid presenting the result as a diagnosis.

---

## PRIORITY

The collected information meets an approved condition requiring increased attention but does not meet the configured urgent criteria.

Action:

- Mark the patient as priority.
- Surface the case appropriately on the doctor dashboard.
- Allow clinical staff to review the information.

---

## STABLE

No configured urgent or priority triage rule has been triggered based on the information collected.

Action:

- Continue the normal intake workflow.
- Make the triage status visible to the authorized doctor.

Important:

    STABLE does not mean "healthy".

It only means that no configured triage rule was triggered using the available information.

---

# 4. Safety Boundary

The triage engine must never state:

    "Patient has disease X."

Instead, it should state:

    "An approved triage rule was triggered."

Example:

```json
{
  "priority": "URGENT",
  "reason": "Configured red-flag rule triggered",
  "status": "ROUTING_RECOMMENDATION"
}
```

---

# 5. Rule Ownership

Clinical triage rules must be:

- Explicitly defined.
- Versioned.
- Reviewable.
- Testable.
- Approved by the responsible clinical/product team before deployment.

AI must not create safety-critical rules dynamically.

---

# 6. Rule Structure

Each rule should have a unique identifier.

Example:

```json
{
  "ruleId": "TRIAGE-001",
  "version": "1.0",
  "name": "Example Red Flag",
  "priority": "URGENT",
  "conditions": [],
  "enabled": true
}
```

---

# 7. Rule Components

Each rule contains:

- Rule ID
- Rule version
- Rule name
- Description
- Priority
- Conditions
- Logical operator
- Enabled/disabled state
- Effective date
- Review metadata

Recommended structure:

```text
Rule
├── ID
├── Version
├── Name
├── Description
├── Priority
├── Conditions
├── Logical Operator
├── Enabled
└── Review Metadata
```

---

# 8. Condition Structure

Conditions should operate on structured intake information.

Examples of input fields:

- Symptom presence
- Symptom severity
- Duration
- Patient-reported warning signs
- Relevant structured responses

Example:

```json
{
  "field": "symptom.example",
  "operator": "EQUALS",
  "value": true
}
```

Supported operators may include:

- EQUALS
- NOT_EQUALS
- GREATER_THAN
- LESS_THAN
- GREATER_THAN_OR_EQUAL
- LESS_THAN_OR_EQUAL
- CONTAINS
- EXISTS

Only approved operators should be supported.

---

# 9. Logical Operators

Rules may combine conditions using:

- AND
- OR

Example:

```text
Condition A AND Condition B
```

or:

```text
Condition A OR Condition B
```

Complex nested logic should be implemented carefully and covered by tests.

---

# 10. Rule Evaluation

Basic process:

    Load active rules
          ↓
    Load structured patient data
          ↓
    Evaluate conditions
          ↓
    Identify triggered rules
          ↓
    Determine highest applicable priority
          ↓
    Store result
          ↓
    Display to authorized clinical user

---

# 11. Priority Resolution

If multiple rules are triggered, the highest configured priority should take precedence.

Example:

    URGENT
       ↑
    PRIORITY
       ↑
    STABLE

If:

    Rule A → STABLE
    Rule B → PRIORITY
    Rule C → URGENT

Final triage:

    URGENT

All triggered rules should remain traceable.

---

# 12. Triggered Rule Storage

The system should preserve the rules that caused the result.

Example:

```json
{
  "priority": "URGENT",
  "triggeredRules": [
    {
      "ruleId": "TRIAGE-001",
      "version": "1.0",
      "reason": "Configured condition matched"
    }
  ]
}
```

This supports auditability and doctor review.

---

# 13. Triage Result Lifecycle

Recommended lifecycle:

    NOT_EVALUATED
          ↓
    EVALUATING
          ↓
    COMPLETED
          ↓
    DOCTOR_REVIEWED

Failure:

    EVALUATING
          ↓
    FAILED
          ↓
    RETRY / MANUAL REVIEW

---

# 14. Triage Timing

Triage may be evaluated:

1. After sufficient initial complaint information is collected.
2. After relevant adaptive questions.
3. Again when important new information is added.

The system should avoid unnecessary repeated evaluations.

---

# 15. Early Red-Flag Screening

MediKiosk should support an early screening stage.

Flow:

    Patient Complaint
         ↓
    Initial Red-Flag Screening
         ↓
    Potential Urgent Case?
       /           \
     YES            NO
      ↓              ↓
    Escalate      Continue Intake

The exact clinical conditions used for red-flag screening must come from the approved clinical rule set.

---

# 16. Adaptive Intake Interaction

Triage and adaptive questioning may work together.

Example:

    Patient Answer
         ↓
    Rule Evaluation
         ↓
    Relevant Information Missing?
       /             \
     YES              NO
      ↓                ↓
    Ask approved      Continue
    question
         ↓
    Re-evaluate

The adaptive engine must use approved questions and approved branching logic.

---

# 17. Triage Must Not Block Data Preservation

Even if an urgent rule is triggered:

- Patient information must be preserved.
- The session must remain traceable.
- Relevant answers must remain available.
- The doctor must be able to review the collected information.

Urgent routing must not cause data loss.

---

# 18. Doctor Dashboard Behaviour

Triage results should be visible in the doctor dashboard.

Example:

```text
Patient
──────────────
Name: Patient Name
Priority: URGENT

Reason:
Configured triage rule triggered

Status:
Awaiting clinical review
```

The dashboard should not display an AI-generated diagnosis in place of the triage status.

---

# 19. Patient-Facing Behaviour

Patient-facing messaging must be carefully designed.

Avoid:

    "You have a serious condition."

Prefer:

    "Your responses indicate that a healthcare professional should review your information promptly."

The patient-facing system should not create unnecessary alarm.

---

# 20. Triage and AI Separation

Architecture:

    Patient Data
         ↓
    Structured Data
       ↙        ↘
    Triage       AI
    Engine       Layer
      ↓            ↓
    Priority     Summary
       ↘         ↙
        Doctor
        Dashboard

The AI summary must not modify the triage result.

---

# 21. AI Interaction With Triage

AI may:

- Display the existing triage status.
- Include the status in the pre-consult summary.
- Explain which configured rule was triggered in a neutral way.

AI may NOT:

- Create new red-flag rules.
- Change rule thresholds.
- Upgrade a case independently.
- Downgrade a case independently.
- Override the rule engine.
- Mark the patient as clinically safe.

---

# 22. Missing Information

If a required triage input is missing:

The system should not assume the answer.

Example:

```text
Severity:
UNKNOWN
```

not:

```text
Severity:
MODERATE
```

unless the patient actually provided that information.

---

# 23. Conflicting Information

If structured inputs conflict:

Example:

```text
Patient response:
No allergy reported.

Document:
Possible allergy listed.
```

The system should preserve the conflict.

Recommended status:

    REVIEW_REQUIRED

The triage engine must not silently invent a resolution.

---

# 24. Rule Versioning

Rules must be versioned.

Example:

```text
TRIAGE-001
Version 1.0
```

Later:

```text
TRIAGE-001
Version 1.1
```

Previously generated triage results should retain the rule version used.

This allows historical results to remain explainable.

---

# 25. Rule Activation

Rules should have an enabled/disabled state.

Example:

```json
{
  "ruleId": "TRIAGE-001",
  "enabled": true
}
```

Disabled rules must not participate in new evaluations.

---

# 26. Rule Changes

Clinical rules must not be silently modified in production.

A rule change should record:

- Previous version
- New version
- Change description
- Effective date
- Reviewer/approver
- Timestamp

---

# 27. Triage Audit Trail

The system should record:

- Session ID
- Rule IDs triggered
- Rule versions
- Result
- Evaluation timestamp
- Engine version
- Relevant input references

Example:

```json
{
  "sessionId": "uuid",
  "engineVersion": "1.0",
  "priority": "PRIORITY",
  "triggeredRules": [
    "TRIAGE-002"
  ],
  "evaluatedAt": "timestamp"
}
```

---

# 28. Triage API Integration

The backend exposes:

    POST /api/sessions/{sessionId}/triage

The endpoint should:

1. Validate session.
2. Verify required data.
3. Load active rules.
4. Evaluate rules.
5. Determine priority.
6. Store result.
7. Create audit record.
8. Return result.

---

# 29. Example API Response

```json
{
  "success": true,
  "data": {
    "sessionId": "uuid",
    "priority": "PRIORITY",
    "status": "COMPLETED",
    "triggeredRules": [
      {
        "ruleId": "TRIAGE-002",
        "version": "1.0",
        "reason": "Configured condition matched"
      }
    ]
  }
}
```

---

# 30. Triage Failure

If evaluation fails:

```json
{
  "success": false,
  "error": {
    "code": "TRIAGE_PROCESSING_FAILED",
    "message": "Triage could not be completed."
  }
}
```

The application must:

- Preserve patient data.
- Log the failure.
- Avoid generating a misleading priority.
- Allow retry.
- Provide an appropriate manual workflow.

---

# 31. Rule Testing

Every active clinical rule must have automated tests.

Each rule should test:

- Positive case
- Negative case
- Missing data
- Boundary values
- Conflicting data where applicable
- Multiple-rule scenarios

Example:

```text
Input:
Condition A = true

Expected:
Rule TRIAGE-001 triggered
Priority = URGENT
```

---

# 32. Regression Testing

Whenever a triage rule changes:

1. Run all existing triage tests.
2. Run tests for the modified rule.
3. Test interactions with other rules.
4. Verify priority resolution.
5. Verify audit information.
6. Verify dashboard output.

No rule update should silently change unrelated triage behaviour.

---

# 33. Prototype Rule Configuration

For the SIH prototype, rules should be stored in a configurable format rather than hard-coded throughout the application.

Possible implementation:

    MySQL
        ↓
    TriageRuleRepository
        ↓
    TriageRuleService
        ↓
    RuleEvaluator
        ↓
    TriageResult

This allows rules to be changed without rewriting the entire application.

---

# 34. Clinical Rule Content

The exact red-flag conditions and thresholds must be supplied and approved by the responsible clinical/domain team.

The software specification intentionally does not invent medical thresholds.

The implementation must treat the approved rule set as configuration/data.

---

# 35. Example Non-Clinical Rule

For software testing only:

```json
{
  "ruleId": "TEST-001",
  "version": "1.0",
  "priority": "PRIORITY",
  "conditions": [
    {
      "field": "testFlag",
      "operator": "EQUALS",
      "value": true
    }
  ],
  "enabled": true
}
```

This example is for testing the engine architecture only.

It is not a medical triage rule.

---

# 36. Security

Triage rules must not be editable by ordinary patients.

Access should be restricted to authorized administrative/clinical configuration roles.

Triage results should be protected as clinical information.

All rule configuration changes must be audited.

---

# 37. Data Model

Triage requires at minimum:

```text
triage_rules
├── id
├── rule_id
├── version
├── name
├── description
├── priority
├── conditions
├── enabled
├── effective_from
├── created_at
└── updated_at
```

And:

```text
triage_results
├── id
├── session_id
├── priority
├── triggered_rules
├── explanation
├── status
└── created_at
```

---

# 38. Definition of Done

The triage module is complete when:

1. Rules can be stored.
2. Rules can be versioned.
3. Rules can be enabled/disabled.
4. Conditions can be evaluated.
5. Multiple conditions can be combined.
6. Multiple triggered rules can be recorded.
7. Highest priority is correctly selected.
8. Missing information is not guessed.
9. Conflicting information is preserved.
10. Results are stored against the intake session.
11. Rule versions are retained.
12. Triage results appear on the doctor dashboard.
13. AI cannot override triage.
14. Triage does not diagnose.
15. Triage failures do not cause data loss.
16. Rule changes are auditable.
17. Automated tests cover the rule engine.
18. The exact clinical rules come from an approved clinical/domain source.

---

# 39. Core Principle

MediKiosk follows:

    PATIENT DATA
          ↓
    APPROVED RULES
          ↓
    TRIAGE SCREENING
          ↓
    ROUTING PRIORITY
          ↓
    DOCTOR REVIEW
          ↓
    CLINICAL DECISION

Not:

    PATIENT DATA
          ↓
        AI
          ↓
      DIAGNOSIS