package com.medikiosk.entity;

import com.medikiosk.entity.enums.TriagePriority;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Approved clinical/screening triage rule conforming to docs/TRIAGE_RULES.md.
 */
@Entity
@Table(name = "triage_rules")
public class TriageRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "rule_id", nullable = false, unique = true, length = 64)
    private String ruleId;

    @Column(name = "version", nullable = false, length = 32)
    private String version = "1.0";

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 32)
    private TriagePriority priority;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "conditions", nullable = false)
    private String conditions;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.effectiveFrom == null) {
            this.effectiveFrom = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public TriageRule() {
    }

    public TriageRule(String ruleId, String version, String name, String description,
                      TriagePriority priority, String conditions, Boolean enabled) {
        this.ruleId = ruleId;
        this.version = version;
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.conditions = conditions;
        this.enabled = enabled;
        this.effectiveFrom = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TriagePriority getPriority() {
        return priority;
    }

    public void setPriority(TriagePriority priority) {
        this.priority = priority;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(Instant effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
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
