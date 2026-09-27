package com.medikiosk.his.service;

import com.medikiosk.abdm.dto.IntegrationRecordDto;
import com.medikiosk.his.dto.HisExportRequestDto;
import com.medikiosk.his.dto.HisExportResponseDto;
import com.medikiosk.his.dto.HisLinkPatientRequestDto;
import com.medikiosk.his.dto.HisLinkPatientResponseDto;
import com.medikiosk.his.dto.HisPatientSearchResponseDto;
import com.medikiosk.his.dto.HisStatusDto;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Hospital Information System (HIS) integration workflows.
 * Conforms to docs/HIS_SPEC.md, docs/SECURITY.md, and Phase 13 requirements.
 */
public interface HisService {

    /**
     * Retrieves current HIS adapter status, configured operating mode, and hospital connectivity.
     */
    HisStatusDto getStatus();

    /**
     * Validates and transmits a doctor-verified FHIR clinical document bundle to the hospital HIS intake queue.
     * Enforces the doctor verification safety gate (ClinicalSummaryStatus.VERIFIED).
     */
    HisExportResponseDto exportClinicalData(UUID sessionId, HisExportRequestDto request, UUID authenticatedDoctorId);

    /**
     * Searches hospital Master Patient Index (MPI) for existing patient records.
     */
    HisPatientSearchResponseDto searchHospitalPatients(String query, String searchType, UUID authenticatedUserId);

    /**
     * Associates a hospital HIS MRN / identifier with an existing MediKiosk patient record.
     */
    HisLinkPatientResponseDto linkPatientHisId(UUID patientId, HisLinkPatientRequestDto request, UUID authenticatedUserId);

    /**
     * Retrieves the history of HIS integration operations for a given intake session.
     */
    List<IntegrationRecordDto> getSessionIntegrationHistory(UUID sessionId, UUID authenticatedUserId);
}
