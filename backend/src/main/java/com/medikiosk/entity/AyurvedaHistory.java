package com.medikiosk.entity;

import com.medikiosk.entity.enums.AyurvedaHistoryStatus;
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

import java.time.Instant;

/**
 * Structured Ayurveda clinical history (Prakriti, Nidana, Pariksha, and Terminology).
 */
@Entity
@Table(name = "ayurveda_histories")
public class AyurvedaHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private IntakeSession session;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private AyurvedaHistoryStatus status = AyurvedaHistoryStatus.DRAFT;

    @Column(name = "patient_confirmed", nullable = false)
    private boolean patientConfirmed = false;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "prakriti_data")
    private String prakritiData;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "nidana_data")
    private String nidanaData;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "pariksha_data")
    private String parikshaData;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "terminology_data")
    private String terminologyData;

    public AyurvedaHistory() {
    }

    public AyurvedaHistory(Patient patient, IntakeSession session) {
        this.patient = patient;
        this.session = session;
        this.status = AyurvedaHistoryStatus.DRAFT;
        this.patientConfirmed = false;
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

    public AyurvedaHistoryStatus getStatus() {
        return status;
    }

    public void setStatus(AyurvedaHistoryStatus status) {
        this.status = status;
    }

    public boolean isPatientConfirmed() {
        return patientConfirmed;
    }

    public void setPatientConfirmed(boolean patientConfirmed) {
        this.patientConfirmed = patientConfirmed;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPrakritiData() {
        return prakritiData;
    }

    public void setPrakritiData(String prakritiData) {
        this.prakritiData = prakritiData;
    }

    public String getNidanaData() {
        return nidanaData;
    }

    public void setNidanaData(String nidanaData) {
        this.nidanaData = nidanaData;
    }

    public String getParikshaData() {
        return parikshaData;
    }

    public void setParikshaData(String parikshaData) {
        this.parikshaData = parikshaData;
    }

    public String getTerminologyData() {
        return terminologyData;
    }

    public void setTerminologyData(String terminologyData) {
        this.terminologyData = terminologyData;
    }
}
