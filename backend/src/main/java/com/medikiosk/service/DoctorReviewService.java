package com.medikiosk.service;

import com.medikiosk.dto.clinical.ClinicalSummaryDto;
import com.medikiosk.dto.doctor.DoctorAmendmentRequest;
import com.medikiosk.dto.doctor.DoctorQueueItemDto;
import com.medikiosk.dto.doctor.DoctorReviewDetailDto;
import com.medikiosk.dto.doctor.DoctorVerificationRequest;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.ClinicalSummaryStatus;
import com.medikiosk.entity.enums.TriagePriority;

import java.util.List;
import java.util.UUID;

/**
 * Service orchestrating the Doctor Consultation Queue and Clinical Review Workstation.
 * Enforces role authorization (ROLE_DOCTOR or ROLE_ADMIN), optimistic locking,
 * source immutability, and clinical audit logging.
 */
public interface DoctorReviewService {

    /**
     * Retrieves the consultation queue ordered by triage severity (EMERGENCY -> URGENT -> PRIORITY -> ROUTINE)
     * and session creation time.
     */
    List<DoctorQueueItemDto> getConsultationQueue(TriagePriority priorityFilter, ClinicalSummaryStatus statusFilter, User doctor);

    /**
     * Retrieves the full case review details for a patient intake session.
     * Guaranteed to return complete original intake data and documents even if AI summary is unavailable.
     */
    DoctorReviewDetailDto getCaseReviewDetails(UUID sessionId, User doctor);

    /**
     * Records physician amendments to the pre-consult summary.
     * Increments the summary version, records an amendment audit entry, and protects original AI data.
     * Enforces optimistic concurrency control via request.getExpectedVersion().
     */
    ClinicalSummaryDto amendClinicalSummary(UUID sessionId, DoctorAmendmentRequest request, User doctor);

    /**
     * Formally marks a clinical summary as doctor verified.
     * Enforces deliberate clinician confirmation, optimistic locking, and audit log generation.
     */
    ClinicalSummaryDto verifyClinicalSummary(UUID sessionId, DoctorVerificationRequest request, User doctor);

    /**
     * Triggers automated multi-system interoperability synchronization across FHIR R4 Bundle, ABDM Adapter, HIS Adapter,
     * and AWS HealthLake Adapter for a clinician-verified session.
     */
    com.medikiosk.dto.doctor.DoctorInteroperabilityStatusDto syncInteroperability(UUID sessionId, User doctor);

    /**
     * Retrieves current multi-system interoperability sync status for an intake session.
     */
    com.medikiosk.dto.doctor.DoctorInteroperabilityStatusDto getInteroperabilityStatus(UUID sessionId, User doctor);
}
