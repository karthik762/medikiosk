package com.medikiosk.controller;

import com.medikiosk.dto.ApiResponse;
import com.medikiosk.dto.triage.TriageEvaluateRequest;
import com.medikiosk.dto.triage.TriageResponse;
import com.medikiosk.entity.User;
import com.medikiosk.service.TriageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller exposing deterministic triage screening and clinical routing APIs.
 * Conforms to docs/TRIAGE_RULES.md (Section 28) and docs/API_SPEC.md.
 */
@RestController
public class TriageController {

    private final TriageService triageService;

    public TriageController(TriageService triageService) {
        this.triageService = triageService;
    }

    @PostMapping({"/api/intake/sessions/{sessionId}/triage", "/api/sessions/{sessionId}/triage"})
    public ResponseEntity<ApiResponse<TriageResponse>> evaluateTriage(
            @PathVariable UUID sessionId,
            @RequestBody(required = false) TriageEvaluateRequest request,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        TriageResponse response = triageService.evaluateSessionTriage(sessionId, request, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Triage screening evaluated successfully"));
    }

    @GetMapping({"/api/intake/sessions/{sessionId}/triage", "/api/sessions/{sessionId}/triage"})
    public ResponseEntity<ApiResponse<TriageResponse>> getTriage(
            @PathVariable UUID sessionId,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        TriageResponse response = triageService.getSessionTriage(sessionId, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Triage screening result retrieved successfully"));
    }

    private User getUserFromAuth(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }
}
