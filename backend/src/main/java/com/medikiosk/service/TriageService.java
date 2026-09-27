package com.medikiosk.service;

import com.medikiosk.dto.triage.TriageEvaluateRequest;
import com.medikiosk.dto.triage.TriageResponse;
import com.medikiosk.entity.User;

import java.util.UUID;

/**
 * Service interface for deterministic triage screening and clinical routing.
 * Conforms to docs/TRIAGE_RULES.md.
 */
public interface TriageService {

    /**
     * Executes deterministic triage screening evaluation on an intake session and persists the result.
     *
     * @param sessionId   Intake session UUID.
     * @param request     Optional parameters for evaluation.
     * @param currentUser Currently authenticated user.
     * @return Role-sanitized TriageResponse.
     */
    TriageResponse evaluateSessionTriage(UUID sessionId, TriageEvaluateRequest request, User currentUser);

    /**
     * Retrieves the stored triage screening result for an intake session.
     *
     * @param sessionId   Intake session UUID.
     * @param currentUser Currently authenticated user.
     * @return Role-sanitized TriageResponse.
     */
    TriageResponse getSessionTriage(UUID sessionId, User currentUser);
}
