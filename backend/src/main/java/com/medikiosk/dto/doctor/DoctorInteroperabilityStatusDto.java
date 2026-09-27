package com.medikiosk.dto.doctor;

import com.medikiosk.entity.enums.IntegrationStatus;
import com.medikiosk.entity.enums.IntegrationType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * DTO summarizing the multi-system interoperability synchronization status
 * across FHIR R4, ABDM Adapter, HIS Adapter, and AWS HealthLake Datastore Adapter.
 */
public class DoctorInteroperabilityStatusDto {

    private UUID sessionId;
    private UUID patientId;
    private boolean sessionLocked;
    private String clinicalSummaryStatus;
    
    private String fhirStatus;
    private String fhirBundleId;
    
    private String abdmStatus;
    private String abdmTransactionId;
    
    private String hisStatus;
    private String hisEncounterRef;
    
    private String healthLakeStatus;
    private String healthLakeResourceRef;
    
    private Instant lastSyncedAt;
    private Map<String, String> adapterStatuses;

    public DoctorInteroperabilityStatusDto() {
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public boolean isSessionLocked() {
        return sessionLocked;
    }

    public void setSessionLocked(boolean sessionLocked) {
        this.sessionLocked = sessionLocked;
    }

    public String getClinicalSummaryStatus() {
        return clinicalSummaryStatus;
    }

    public void setClinicalSummaryStatus(String clinicalSummaryStatus) {
        this.clinicalSummaryStatus = clinicalSummaryStatus;
    }

    public String getFhirStatus() {
        return fhirStatus;
    }

    public void setFhirStatus(String fhirStatus) {
        this.fhirStatus = fhirStatus;
    }

    public String getFhirBundleId() {
        return fhirBundleId;
    }

    public void setFhirBundleId(String fhirBundleId) {
        this.fhirBundleId = fhirBundleId;
    }

    public String getAbdmStatus() {
        return abdmStatus;
    }

    public void setAbdmStatus(String abdmStatus) {
        this.abdmStatus = abdmStatus;
    }

    public String getAbdmTransactionId() {
        return abdmTransactionId;
    }

    public void setAbdmTransactionId(String abdmTransactionId) {
        this.abdmTransactionId = abdmTransactionId;
    }

    public String getHisStatus() {
        return hisStatus;
    }

    public void setHisStatus(String hisStatus) {
        this.hisStatus = hisStatus;
    }

    public String getHisEncounterRef() {
        return hisEncounterRef;
    }

    public void setHisEncounterRef(String hisEncounterRef) {
        this.hisEncounterRef = hisEncounterRef;
    }

    public String getHealthLakeStatus() {
        return healthLakeStatus;
    }

    public void setHealthLakeStatus(String healthLakeStatus) {
        this.healthLakeStatus = healthLakeStatus;
    }

    public String getHealthLakeResourceRef() {
        return healthLakeResourceRef;
    }

    public void setHealthLakeResourceRef(String healthLakeResourceRef) {
        this.healthLakeResourceRef = healthLakeResourceRef;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(Instant lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public Map<String, String> getAdapterStatuses() {
        return adapterStatuses;
    }

    public void setAdapterStatuses(Map<String, String> adapterStatuses) {
        this.adapterStatuses = adapterStatuses;
    }
}
