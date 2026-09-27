package com.medikiosk.service.impl;

import com.medikiosk.dto.HealthResponse;
import com.medikiosk.repository.SystemMetadataRepository;
import com.medikiosk.service.HealthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Implementation of HealthService providing system health diagnostics.
 */
@Service
public class HealthServiceImpl implements HealthService {

    private static final Logger log = LoggerFactory.getLogger(HealthServiceImpl.class);
    private static final String SERVICE_NAME = "MediKiosk Backend";
    private static final String VERSION = "1.0.0";

    private final SystemMetadataRepository systemMetadataRepository;

    public HealthServiceImpl(SystemMetadataRepository systemMetadataRepository) {
        this.systemMetadataRepository = systemMetadataRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public HealthResponse getHealthStatus() {
        String status = "UP";
        try {
            // Verify database connectivity by checking initialized metadata
            systemMetadataRepository.count();
        } catch (Exception e) {
            log.warn("Database connectivity check reported an issue: {}", e.getMessage());
            status = "DEGRADED";
        }

        return new HealthResponse(status, SERVICE_NAME, Instant.now(), VERSION);
    }
}
