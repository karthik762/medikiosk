package com.medikiosk.triage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Architectural interface for deterministic rule-based red-flag clinical triage screening.
 * Conforms to docs/TRIAGE_RULES.md.
 * Note: MediKiosk triage is strictly rule-based and NEVER delegated to non-deterministic LLM generation.
 */
public interface TriageEngine {

    /**
     * Evaluates structured intake context against configured deterministic triage rules.
     *
     * @param context Structured clinical intake context.
     * @return TriageDecision containing resolved priority, triggered rules trace, and safe routing explanations.
     */
    TriageDecision evaluate(TriageInputContext context);

    /**
     * Legacy evaluation method for symptoms and physiological parameters.
     *
     * @param intakeSessionId Intake session identifier.
     * @param symptoms        Reported symptoms.
     * @param parameters      Clinical screening inputs.
     * @return Triage evaluation result containing triage category and triggered flags.
     */
    Map<String, Object> evaluateTriage(UUID intakeSessionId, List<String> symptoms, Map<String, Object> parameters);
}
