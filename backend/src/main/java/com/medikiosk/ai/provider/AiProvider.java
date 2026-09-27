package com.medikiosk.ai.provider;

/**
 * Pluggable AI provider abstraction for structuring clinical intake data into doctor-ready pre-consult summaries.
 * Conforms to docs/AI_SPEC.md (Section 41-42) and docs/PRODUCT_SPEC.md.
 */
public interface AiProvider {

    /**
     * Generates a structured clinical pre-consult summary from available intake context.
     *
     * @param context Complete intake, symptoms, history, Ayurveda, triage, and document extractions.
     * @return Raw structured result before server-side safety verification.
     */
    AiStructuringResult generateClinicalSummary(AiStructuringContext context);

    /**
     * Provider engine identifier name.
     */
    String getProviderName();

    /**
     * Model identifier.
     */
    String getModelIdentifier();

    /**
     * Whether this provider is a deterministic mock / test provider.
     */
    boolean isMock();
}
