package com.medikiosk.entity;

import com.medikiosk.entity.enums.AbhaStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Patient demographic and identity entity.
 */
@Entity
@Table(name = "patients")
public class Patient extends BaseEntity {

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "patient_identifier", nullable = false, unique = true, length = 64)
    private String patientIdentifier;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 32)
    private String gender;

    @Column(name = "phone", length = 32)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "abha_id", length = 64)
    private String abhaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "abha_status", nullable = false, length = 32)
    private AbhaStatus abhaStatus = AbhaStatus.NOT_PROVIDED;

    @Column(name = "abha_verified_at")
    private Instant abhaVerifiedAt;

    @Column(name = "his_id", length = 64)
    private String hisId;

    @Column(name = "healthlake_ref")
    private String healthlakeRef;

    public Patient() {
    }

    public Patient(String patientIdentifier, String firstName, String lastName) {
        this.patientIdentifier = patientIdentifier;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getPatientIdentifier() {
        return patientIdentifier;
    }

    public void setPatientIdentifier(String patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAbhaId() {
        return abhaId;
    }

    public void setAbhaId(String abhaId) {
        this.abhaId = abhaId;
    }

    public String getHisId() {
        return hisId;
    }

    public void setHisId(String hisId) {
        this.hisId = hisId;
    }

    public String getHealthlakeRef() {
        return healthlakeRef;
    }

    public void setHealthlakeRef(String healthlakeRef) {
        this.healthlakeRef = healthlakeRef;
    }

    public AbhaStatus getAbhaStatus() {
        return abhaStatus;
    }

    public void setAbhaStatus(AbhaStatus abhaStatus) {
        this.abhaStatus = abhaStatus;
    }

    public Instant getAbhaVerifiedAt() {
        return abhaVerifiedAt;
    }

    public void setAbhaVerifiedAt(Instant abhaVerifiedAt) {
        this.abhaVerifiedAt = abhaVerifiedAt;
    }

    /**
     * Returns masked ABHA identifier for safe display/logging conforming to docs/SECURITY.md.
     * Example: 12-3456-7890-1234 -> XX-XXXX-XXXX-1234
     */
    public String getMaskedAbha() {
        if (abhaId == null || abhaId.isBlank()) {
            return null;
        }
        String clean = abhaId.trim();
        if (clean.length() <= 4) {
            return "****";
        }
        if (clean.contains("@")) {
            int atIndex = clean.indexOf('@');
            return clean.substring(0, Math.min(2, atIndex)) + "***" + clean.substring(atIndex);
        }
        return "XX-XXXX-XXXX-" + clean.substring(clean.length() - 4);
    }
}
