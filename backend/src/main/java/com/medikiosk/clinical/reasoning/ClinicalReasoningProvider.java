package com.medikiosk.clinical.reasoning;

/**
 * Pluggable reasoning provider abstraction for understanding natural language patient statements,
 * deriving clinical context, and identifying question exploration intents.
 */
public interface ClinicalReasoningProvider {

    /**
     * Analyzes patient statements against conversation history and clinical state.
     *
     * @param request Contains the patient's latest statement, conversation history, and existing facts.
     * @return Structured result containing known facts, uncertainties, contradictions, and suggested intents.
     */
    ClinicalContextResult analyzeContext(ClinicalContextRequest request);

    /**
     * Provider implementation identifier.
     */
    String getProviderName();

    /**
     * Returns true if running as a deterministic mock / offline intelligence engine.
     */
    boolean isMock();
}
