package com.medikiosk.dto.ayurveda;

import com.medikiosk.entity.enums.AyurvedaHistoryStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AyurvedaHistoryResponse {
    private UUID id;
    private UUID patientId;
    private UUID sessionId;
    private AyurvedaHistoryStatus status;
    private boolean patientConfirmed;
    private Instant confirmedAt;
    private PrakritiDataDto prakriti;
    private NidanaDataDto nidana;
    private ParikshaDataDto pariksha;
    private List<AyurvedaTerminologyDto> terminology;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    public AyurvedaHistoryResponse() {
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

    public PrakritiDataDto getPrakriti() {
        return prakriti;
    }

    public void setPrakriti(PrakritiDataDto prakriti) {
        this.prakriti = prakriti;
    }

    public NidanaDataDto getNidana() {
        return nidana;
    }

    public void setNidana(NidanaDataDto nidana) {
        this.nidana = nidana;
    }

    public ParikshaDataDto getPariksha() {
        return pariksha;
    }

    public void setPariksha(ParikshaDataDto pariksha) {
        this.pariksha = pariksha;
    }

    public List<AyurvedaTerminologyDto> getTerminology() {
        return terminology;
    }

    public void setTerminology(List<AyurvedaTerminologyDto> terminology) {
        this.terminology = terminology;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
