package com.medikiosk.dto.doctor;

import com.medikiosk.entity.enums.ClinicalSummaryStatus;
import com.medikiosk.entity.enums.IntakeSessionStatus;
import com.medikiosk.entity.enums.TriagePriority;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO representing an item in the doctor's consultation queue.
 * Conforms to clinical privacy rules: minimal identifying PHI with clear clinical indicators.
 */
public class DoctorQueueItemDto {

    private UUID sessionId;
    private UUID patientId;
    private String patientIdentifier;
    private String patientName;
    private Integer patientAge;
    private String patientGender;
    private String chiefComplaint;
    private IntakeSessionStatus sessionStatus;
    private TriagePriority triagePriority;
    private String triageReason;
    private ClinicalSummaryStatus summaryStatus;
    private String reviewStatus;
    private Instant sessionCreatedAt;
    private Instant sessionUpdatedAt;
    private boolean urgent;

    public DoctorQueueItemDto() {
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

    public Integer getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(Integer patientAge) {
        this.patientAge = patientAge;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public IntakeSessionStatus getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(IntakeSessionStatus sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    public TriagePriority getTriagePriority() {
        return triagePriority;
    }

    public void setTriagePriority(TriagePriority triagePriority) {
        this.triagePriority = triagePriority;
    }

    public String getTriageReason() {
        return triageReason;
    }

    public void setTriageReason(String triageReason) {
        this.triageReason = triageReason;
    }

    public ClinicalSummaryStatus getSummaryStatus() {
        return summaryStatus;
    }

    public void setSummaryStatus(ClinicalSummaryStatus summaryStatus) {
        this.summaryStatus = summaryStatus;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
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

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }
}
