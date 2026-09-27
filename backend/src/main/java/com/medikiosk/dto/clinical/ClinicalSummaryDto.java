package com.medikiosk.dto.clinical;

import com.medikiosk.entity.enums.ClinicalSummaryStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Pre-consultation clinical summary DTO organized for doctor review.
 * Conforms to docs/AI_SPEC.md (Section 20-21) and docs/PRODUCT_SPEC.md.
 */
public class ClinicalSummaryDto {

    private UUID id;
    private UUID patientId;
    private UUID sessionId;
    private ClinicalSummaryStatus status;
    private Integer version;

    private PatientInformationDto patientInfo;
    private String chiefComplaint;
    private List<StructuredSymptomDto> symptoms;
    private List<StructuredHistoryDto> medicalHistory;
    private List<StructuredMedicationDto> medications;
    private List<StructuredAllergyDto> allergies;
    private StructuredAyurvedaDto ayurveda;
    private List<DocumentFindingDto> documentFindings;
    private TriageSummaryDto triage;
    private List<DualCodedConceptDto> dualCodedConcepts;
    private List<MissingInformationDto> missingInformation;
    private List<ClinicalConflictDto> conflicts;
    private DoctorVerificationDto doctorVerification;
    private SummaryMetadataDto metadata;

    private String humanReadableSummary;
    private Instant createdAt;
    private Instant updatedAt;

    public ClinicalSummaryDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public ClinicalSummaryStatus getStatus() {
        return status;
    }

    public void setStatus(ClinicalSummaryStatus status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public PatientInformationDto getPatientInfo() {
        return patientInfo;
    }

    public void setPatientInfo(PatientInformationDto patientInfo) {
        this.patientInfo = patientInfo;
    }

    public List<DualCodedConceptDto> getDualCodedConcepts() {
        return dualCodedConcepts;
    }

    public void setDualCodedConcepts(List<DualCodedConceptDto> dualCodedConcepts) {
        this.dualCodedConcepts = dualCodedConcepts;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public List<StructuredSymptomDto> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<StructuredSymptomDto> symptoms) {
        this.symptoms = symptoms;
    }

    public List<StructuredHistoryDto> getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(List<StructuredHistoryDto> medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public List<StructuredMedicationDto> getMedications() {
        return medications;
    }

    public void setMedications(List<StructuredMedicationDto> medications) {
        this.medications = medications;
    }

    public List<StructuredAllergyDto> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<StructuredAllergyDto> allergies) {
        this.allergies = allergies;
    }

    public StructuredAyurvedaDto getAyurveda() {
        return ayurveda;
    }

    public void setAyurveda(StructuredAyurvedaDto ayurveda) {
        this.ayurveda = ayurveda;
    }

    public List<DocumentFindingDto> getDocumentFindings() {
        return documentFindings;
    }

    public void setDocumentFindings(List<DocumentFindingDto> documentFindings) {
        this.documentFindings = documentFindings;
    }

    public TriageSummaryDto getTriage() {
        return triage;
    }

    public void setTriage(TriageSummaryDto triage) {
        this.triage = triage;
    }

    public List<MissingInformationDto> getMissingInformation() {
        return missingInformation;
    }

    public void setMissingInformation(List<MissingInformationDto> missingInformation) {
        this.missingInformation = missingInformation;
    }

    public List<ClinicalConflictDto> getConflicts() {
        return conflicts;
    }

    public void setConflicts(List<ClinicalConflictDto> conflicts) {
        this.conflicts = conflicts;
    }

    public DoctorVerificationDto getDoctorVerification() {
        return doctorVerification;
    }

    public void setDoctorVerification(DoctorVerificationDto doctorVerification) {
        this.doctorVerification = doctorVerification;
    }

    public SummaryMetadataDto getMetadata() {
        return metadata;
    }

    public void setMetadata(SummaryMetadataDto metadata) {
        this.metadata = metadata;
    }

    public String getHumanReadableSummary() {
        return humanReadableSummary;
    }

    public void setHumanReadableSummary(String humanReadableSummary) {
        this.humanReadableSummary = humanReadableSummary;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
