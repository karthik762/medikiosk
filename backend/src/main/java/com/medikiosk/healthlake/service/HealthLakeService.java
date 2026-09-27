package com.medikiosk.healthlake.service;

import com.medikiosk.abdm.dto.IntegrationRecordDto;
import com.medikiosk.healthlake.dto.HealthLakeExportRequestDto;
import com.medikiosk.healthlake.dto.HealthLakeExportResponseDto;
import com.medikiosk.healthlake.dto.HealthLakeStatusDto;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for AWS HealthLake FHIR integration workflows.
 * Conforms to docs/HEALTHLAKE_SPEC.md, docs/SECURITY.md, and Phase 14 requirements.
 */
public interface HealthLakeService {

    /**
     * Retrieves current AWS HealthLake adapter status, operating mode, and cloud connectivity.
     */
    HealthLakeStatusDto getStatus();

    /**
     * Validates and transmits a doctor-verified FHIR clinical document bundle to AWS HealthLake.
     * Enforces the doctor verification safety gate (ClinicalSummaryStatus.VERIFIED).
     */
    HealthLakeExportResponseDto exportClinicalData(UUID sessionId, HealthLakeExportRequestDto request, UUID authenticatedDoctorId);

    /**
     * Retrieves the history of AWS HealthLake integration operations for a given intake session.
     */
    List<IntegrationRecordDto> getSessionIntegrationHistory(UUID sessionId, UUID authenticatedUserId);
}
