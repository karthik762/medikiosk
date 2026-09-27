package com.medikiosk.service;

import com.medikiosk.dto.HealthResponse;

/**
 * Service interface for retrieving system health status.
 */
public interface HealthService {

    /**
     * Checks and returns the current system health status.
     *
     * @return HealthResponse containing status and service metadata.
     */
    HealthResponse getHealthStatus();
}
