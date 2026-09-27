package com.medikiosk.ai;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Architectural interface for AI clinical intake structuring and summarization.
 * Conforms to docs/AI_SPEC.md.
 * Note: MediKiosk is NOT an autonomous diagnostic system; AI only assists clinical intake documentation.
 */
public interface AiService {

    /**
     * Structures raw patient intake transcript and responses into clinical history categories.
     *
     * @param intakeSessionId Unique intake session identifier.
     * @param rawResponses    Map of questions to patient answers.
     * @return Structured clinical findings map.
     */
    Map<String, Object> structureIntakeResponses(UUID intakeSessionId, Map<String, String> rawResponses);

    /**
     * Generates a pre-consultation clinical draft summary for doctor review and verification.
     *
     * @param intakeSessionId Unique intake session identifier.
     * @return Formatted pre-consultation clinical summary draft.
     */
    String generateDraftSummary(UUID intakeSessionId);

    /**
     * Determines relevant follow-up questions adaptively based on reported symptoms.
     *
     * @param chiefComplaint Patient reported chief complaint.
     * @param symptoms       Reported symptoms list.
     * @return Suggested relevant clarifying questions.
     */
    List<String> getAdaptiveQuestions(String chiefComplaint, List<String> symptoms);
}
