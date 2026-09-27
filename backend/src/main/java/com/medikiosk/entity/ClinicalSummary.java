package com.medikiosk.entity;

import com.medikiosk.entity.enums.ClinicalSummaryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Versioned clinical summary (AI generated, doctor reviewed, clinician verified).
 */
@Entity
@Table(name = "clinical_summaries")
public class ClinicalSummary extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private IntakeSession session;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "summary_data")
    private String summaryData;

    @Column(name = "summary_text", columnDefinition = "TEXT")
    private String summaryText;

    @Column(name = "generated_by", length = 32)
    private String generatedBy = "AI";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ClinicalSummaryStatus status = ClinicalSummaryStatus.AI_GENERATED;

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @Column(name = "model_identifier", length = 128)
    private String modelIdentifier;

    @Column(name = "is_mock")
    private Boolean isMock = true;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "original_ai_data")
    private String originalAiData;

    public ClinicalSummary() {
    }

    public ClinicalSummary(Patient patient, IntakeSession session, String summaryText, ClinicalSummaryStatus status) {
        this.patient = patient;
        this.session = session;
        this.summaryText = summaryText;
        this.status = status != null ? status : ClinicalSummaryStatus.AI_GENERATED;
        this.version = 1;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public IntakeSession getSession() {
        return session;
    }

    public void setSession(IntakeSession session) {
        this.session = session;
    }

    public String getSummaryData() {
        return summaryData;
    }

    public void setSummaryData(String summaryData) {
        this.summaryData = summaryData;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
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

    public String getModelIdentifier() {
        return modelIdentifier;
    }

    public void setModelIdentifier(String modelIdentifier) {
        this.modelIdentifier = modelIdentifier;
    }

    public Boolean getIsMock() {
        return isMock;
    }

    public void setIsMock(Boolean isMock) {
        this.isMock = isMock;
    }

    public String getOriginalAiData() {
        return originalAiData;
    }

    public void setOriginalAiData(String originalAiData) {
        this.originalAiData = originalAiData;
    }
}
