package com.medikiosk.repository;

import com.medikiosk.entity.Consent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsentRepository extends JpaRepository<Consent, UUID> {
    List<Consent> findByPatientId(UUID patientId);
    /**
     * Returns all consent records for a session (GENERAL_INTAKE, ABDM_DATA_SHARE, etc.).
     * Prefer typed lookups for gate checks so consent types are never conflated.
     */
    List<Consent> findAllBySessionId(UUID sessionId);
    Optional<Consent> findByPatientIdAndSessionIdAndConsentType(UUID patientId, UUID sessionId, String consentType);
    List<Consent> findByPatientIdAndConsentType(UUID patientId, String consentType);
    Optional<Consent> findFirstBySessionIdAndConsentTypeOrderByConsentedAtDesc(UUID sessionId, String consentType);
    Optional<Consent> findFirstByPatientIdAndConsentTypeOrderByConsentedAtDesc(UUID patientId, String consentType);
}
