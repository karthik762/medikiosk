package com.medikiosk.controller;

import com.medikiosk.dto.ApiResponse;
import com.medikiosk.dto.ayurveda.AyurvedaHistoryRequest;
import com.medikiosk.dto.ayurveda.AyurvedaHistoryResponse;
import com.medikiosk.entity.User;
import com.medikiosk.service.AyurvedaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller exposing structured Ayurveda clinical intake and confirmation APIs.
 * Conforms to docs/AYURVEDA_SPEC.md, docs/USER_FLOWS.md, and docs/API_SPEC.md.
 */
@RestController
public class AyurvedaController {

    private final AyurvedaService ayurvedaService;

    public AyurvedaController(AyurvedaService ayurvedaService) {
        this.ayurvedaService = ayurvedaService;
    }

    @GetMapping({"/api/intake/sessions/{sessionId}/ayurveda", "/api/ayurveda/sessions/{sessionId}"})
    public ResponseEntity<ApiResponse<AyurvedaHistoryResponse>> getAyurvedaHistory(
            @PathVariable UUID sessionId,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        AyurvedaHistoryResponse response = ayurvedaService.getAyurvedaHistory(sessionId, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Ayurveda intake history retrieved successfully"));
    }

    @PostMapping({"/api/intake/sessions/{sessionId}/ayurveda", "/api/ayurveda/sessions/{sessionId}"})
    public ResponseEntity<ApiResponse<AyurvedaHistoryResponse>> saveOrUpdateAyurvedaHistory(
            @PathVariable UUID sessionId,
            @RequestBody(required = false) AyurvedaHistoryRequest request,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        AyurvedaHistoryResponse response = ayurvedaService.saveOrUpdateAyurvedaHistory(sessionId, request, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Ayurveda intake history saved successfully"));
    }

    @PostMapping({"/api/intake/sessions/{sessionId}/ayurveda/confirm", "/api/ayurveda/sessions/{sessionId}/confirm"})
    public ResponseEntity<ApiResponse<AyurvedaHistoryResponse>> confirmAyurvedaHistory(
            @PathVariable UUID sessionId,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        AyurvedaHistoryResponse response = ayurvedaService.confirmAyurvedaHistory(sessionId, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Ayurveda intake history confirmed by patient successfully"));
    }

    private User getUserFromAuth(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }
}
