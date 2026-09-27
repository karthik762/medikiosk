package com.medikiosk.dto.clinical;

import java.time.Instant;
import java.util.UUID;

/**
 * Doctor verification status block.
 * For Phase 9, AI summary verification status defaults to PENDING.
 */
public class DoctorVerificationDto {

    private String status = "PENDING";
    private UUID verifiedBy;
    private String verifiedByName;
    private Instant verifiedAt;
    private String comments;

    public DoctorVerificationDto() {
    }

    public DoctorVerificationDto(String status, UUID verifiedBy, String verifiedByName, Instant verifiedAt, String comments) {
        this.status = status != null ? status : "PENDING";
        this.verifiedBy = verifiedBy;
        this.verifiedByName = verifiedByName;
        this.verifiedAt = verifiedAt;
        this.comments = comments;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(UUID verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public String getVerifiedByName() {
        return verifiedByName;
    }

    public void setVerifiedByName(String verifiedByName) {
        this.verifiedByName = verifiedByName;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
