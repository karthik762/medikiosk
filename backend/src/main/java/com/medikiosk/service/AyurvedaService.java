package com.medikiosk.service;

import com.medikiosk.dto.ayurveda.AyurvedaHistoryRequest;
import com.medikiosk.dto.ayurveda.AyurvedaHistoryResponse;
import com.medikiosk.entity.User;

import java.util.UUID;

/**
 * Service contract for managing structured Ayurveda intake histories.
 * Conforms to docs/AYURVEDA_SPEC.md and docs/SECURITY.md.
 */
public interface AyurvedaService {

    AyurvedaHistoryResponse getAyurvedaHistory(UUID sessionId, User authenticatedUser);

    AyurvedaHistoryResponse saveOrUpdateAyurvedaHistory(UUID sessionId, AyurvedaHistoryRequest request, User authenticatedUser);

    AyurvedaHistoryResponse confirmAyurvedaHistory(UUID sessionId, User authenticatedUser);
}
