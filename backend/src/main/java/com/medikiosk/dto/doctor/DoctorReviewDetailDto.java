package com.medikiosk.dto.doctor;

import com.medikiosk.dto.ayurveda.AyurvedaHistoryResponse;
import com.medikiosk.dto.clinical.ClinicalSummaryDto;
import com.medikiosk.dto.clinical.TriageSummaryDto;
import com.medikiosk.dto.document.DocumentUploadResponse;
import com.medikiosk.entity.enums.ClinicalSummaryStatus;
import com.medikiosk.entity.enums.IntakeSessionStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Comprehensive DTO for the Doctor Clinical Review Workstation.
 * Assembles triage, AI summary, original patient data, Ayurveda profile, documents, and audit history.
 */
public class DoctorReviewDetailDto {

    private UUID sessionId;
    private UUID patientId;
    private String patientIdentifier;
    private String patientName;
    private LocalDate dateOfBirth;
    private Integer age;
    private String gender;
    private String phone;

    private IntakeSessionStatus sessionStatus;
    private Instant sessionCreatedAt;
    private Instant sessionUpdatedAt;

    private TriageSummaryDto triage;
    private ClinicalSummaryDto clinicalSummary;
    private OriginalPatientDataDto originalPatientData;
    private AyurvedaHistoryResponse ayurvedaHistory;
    private List<DocumentUploadResponse> documents = new ArrayList<>();
    private List<DoctorReviewHistoryDto> reviewHistory = new ArrayList<>();

    private Integer currentVersion;
    private ClinicalSummaryStatus currentStatus;
    private boolean verified;
    private boolean aiSummaryAvailable;

    public DoctorReviewDetailDto() {
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

    public String getPatientIdentifier() {
        return patientIdentifier;
    }

    public void setPatientIdentifier(String patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public IntakeSessionStatus getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(IntakeSessionStatus sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    public Instant getSessionCreatedAt() {
        return sessionCreatedAt;
    }

    public void setSessionCreatedAt(Instant sessionCreatedAt) {
        this.sessionCreatedAt = sessionCreatedAt;
    }

    public Instant getSessionUpdatedAt() {
        return sessionUpdatedAt;
    }

    public void setSessionUpdatedAt(Instant sessionUpdatedAt) {
        this.sessionUpdatedAt = sessionUpdatedAt;
    }

    public TriageSummaryDto getTriage() {
        return triage;
    }

    public void setTriage(TriageSummaryDto triage) {
        this.triage = triage;
    }

    public ClinicalSummaryDto getClinicalSummary() {
        return clinicalSummary;
    }

    public void setClinicalSummary(ClinicalSummaryDto clinicalSummary) {
        this.clinicalSummary = clinicalSummary;
    }

    public OriginalPatientDataDto getOriginalPatientData() {
        return originalPatientData;
    }

    public void setOriginalPatientData(OriginalPatientDataDto originalPatientData) {
        this.originalPatientData = originalPatientData;
    }

    public AyurvedaHistoryResponse getAyurvedaHistory() {
        return ayurvedaHistory;
    }

    public void setAyurvedaHistory(AyurvedaHistoryResponse ayurvedaHistory) {
        this.ayurvedaHistory = ayurvedaHistory;
    }

    public List<DocumentUploadResponse> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentUploadResponse> documents) {
        this.documents = documents;
    }

    public List<DoctorReviewHistoryDto> getReviewHistory() {
        return reviewHistory;
    }

    public void setReviewHistory(List<DoctorReviewHistoryDto> reviewHistory) {
        this.reviewHistory = reviewHistory;
    }

    public Integer getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(Integer currentVersion) {
        this.currentVersion = currentVersion;
    }

    public ClinicalSummaryStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(ClinicalSummaryStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isAiSummaryAvailable() {
        return aiSummaryAvailable;
    }

    public void setAiSummaryAvailable(boolean aiSummaryAvailable) {
        this.aiSummaryAvailable = aiSummaryAvailable;
    }
}
