package com.medikiosk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Maps MediKiosk domain entities to their canonical FHIR R4 resource identifiers.
 */
@Entity
@Table(name = "fhir_mappings")
public class FhirMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "entity_type", nullable = false, length = 64)
    private String entityType;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "fhir_resource_type", nullable = false, length = 64)
    private String fhirResourceType;

    @Column(name = "fhir_resource_id", nullable = false, length = 128)
    private String fhirResourceId;

    @Column(name = "fhir_version", length = 32)
    private String fhirVersion = "R4";

    @Column(name = "sync_status", nullable = false, length = 32)
    private String syncStatus = "PENDING";

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "resource_payload", columnDefinition = "TEXT")
    private String resourcePayload;

    @Column(name = "validation_status", length = 32)
    private String validationStatus = "VALID";

    @Column(name = "validation_errors", columnDefinition = "TEXT")
    private String validationErrors;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public FhirMapping() {
    }

    public FhirMapping(String entityType, UUID entityId, String fhirResourceType, String fhirResourceId) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.fhirResourceType = fhirResourceType;
        this.fhirResourceId = fhirResourceId;
        this.fhirVersion = "R4";
        this.syncStatus = "PENDING";
        this.validationStatus = "VALID";
    }

    public FhirMapping(UUID sessionId, String entityType, UUID entityId, String fhirResourceType, String fhirResourceId, String resourcePayload, String validationStatus) {
        this.sessionId = sessionId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.fhirResourceType = fhirResourceType;
        this.fhirResourceId = fhirResourceId;
        this.resourcePayload = resourcePayload;
        this.fhirVersion = "R4";
        this.syncStatus = "PENDING";
        this.validationStatus = validationStatus != null ? validationStatus : "VALID";
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public String getFhirResourceType() {
        return fhirResourceType;
    }

    public void setFhirResourceType(String fhirResourceType) {
        this.fhirResourceType = fhirResourceType;
    }

    public String getFhirResourceId() {
        return fhirResourceId;
    }

    public void setFhirResourceId(String fhirResourceId) {
        this.fhirResourceId = fhirResourceId;
    }

    public String getFhirVersion() {
        return fhirVersion;
    }

    public void setFhirVersion(String fhirVersion) {
        this.fhirVersion = fhirVersion;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public String getResourcePayload() {
        return resourcePayload;
    }

    public void setResourcePayload(String resourcePayload) {
        this.resourcePayload = resourcePayload;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    public String getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(String validationErrors) {
        this.validationErrors = validationErrors;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(Instant lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
