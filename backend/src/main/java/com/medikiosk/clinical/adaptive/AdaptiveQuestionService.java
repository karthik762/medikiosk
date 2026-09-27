package com.medikiosk.clinical.adaptive;

import com.medikiosk.clinical.state.ClinicalState;
import com.medikiosk.entity.User;

import java.util.UUID;

/**
 * Service orchestrating adaptive questioning and clinical state compilation for intake sessions.
 */
public interface AdaptiveQuestionService {

    /**
     * Determines and returns the next best clinical question for the session.
     */
    NextQuestionResponse getNextQuestion(UUID sessionId, User authenticatedUser);

    /**
     * Compiles and returns the comprehensive clinical state for the session.
     */
    ClinicalState getClinicalState(UUID sessionId, User authenticatedUser);
}
