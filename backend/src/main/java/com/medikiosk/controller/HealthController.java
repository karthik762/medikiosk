package com.medikiosk.controller;

import com.medikiosk.dto.HealthResponse;
import com.medikiosk.service.HealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health Controller exposing /api/health endpoint.
 * Public endpoint to verify backend service availability and connectivity.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> getHealth() {
        HealthResponse response = healthService.getHealthStatus();
        return ResponseEntity.ok(response);
    }
}
