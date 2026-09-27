package com.medikiosk.entity;

import com.medikiosk.entity.enums.DoctorReviewAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Audit and verification record for doctor reviews, edits, and final clinical verifications.
 */
@Entity
@Table(name = "doctor_reviews")
public class DoctorReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "summary_id", nullable = false)
    private ClinicalSummary summary;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 32)
    private DoctorReviewAction action;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "summary_version", nullable = false)
    private Integer summaryVersion;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "amendments")
    private String amendments;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public DoctorReview() {
    }

    public DoctorReview(ClinicalSummary summary, User doctor, DoctorReviewAction action, Integer summaryVersion, String comments) {
        this.summary = summary;
        this.doctor = doctor;
        this.action = action;
        this.summaryVersion = summaryVersion;
        this.comments = comments;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ClinicalSummary getSummary() {
        return summary;
    }

    public void setSummary(ClinicalSummary summary) {
        this.summary = summary;
    }

    public User getDoctor() {
        return doctor;
    }

    public void setDoctor(User doctor) {
        this.doctor = doctor;
    }

    public DoctorReviewAction getAction() {
        return action;
    }

    public void setAction(DoctorReviewAction action) {
        this.action = action;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Integer getSummaryVersion() {
        return summaryVersion;
    }

    public void setSummaryVersion(Integer summaryVersion) {
        this.summaryVersion = summaryVersion;
    }

    public String getAmendments() {
        return amendments;
    }

    public void setAmendments(String amendments) {
        this.amendments = amendments;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
