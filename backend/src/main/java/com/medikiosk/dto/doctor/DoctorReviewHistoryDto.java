package com.medikiosk.dto.doctor;

import com.medikiosk.entity.enums.DoctorReviewAction;

import java.time.Instant;
import java.util.UUID;

/**
 * Historical record of doctor review, amendment, and verification actions.
 */
public class DoctorReviewHistoryDto {

    private UUID id;
    private DoctorReviewAction action;
    private String doctorName;
    private String doctorEmail;
    private Integer summaryVersion;
    private String comments;
    private String amendments;
    private Instant createdAt;
    private Instant verifiedAt;

    public DoctorReviewHistoryDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public DoctorReviewAction getAction() {
        return action;
    }

    public void setAction(DoctorReviewAction action) {
        this.action = action;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorEmail() {
        return doctorEmail;
    }

    public void setDoctorEmail(String doctorEmail) {
        this.doctorEmail = doctorEmail;
    }

    public Integer getSummaryVersion() {
        return summaryVersion;
    }

    public void setSummaryVersion(Integer summaryVersion) {
        this.summaryVersion = summaryVersion;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getAmendments() {
        return amendments;
    }

    public void setAmendments(String amendments) {
        this.amendments = amendments;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
}
