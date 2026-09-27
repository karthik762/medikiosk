package com.medikiosk.dto.clinical;

import java.util.UUID;

/**
 * Patient demographic and encounter context for the structured clinical summary.
 */
public class PatientInformationDto {

    private UUID patientId;
    private String patientIdentifier;
    private String firstName;
    private String lastName;
    private Integer age;
    private String gender;
    private String phone;
    private String email;
    private UUID sessionId;

    public PatientInformationDto() {
    }

    public PatientInformationDto(UUID patientId, String patientIdentifier, String firstName, String lastName, Integer age, String gender, String phone, String email, UUID sessionId) {
        this.patientId = patientId;
        this.patientIdentifier = patientIdentifier;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
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

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }
}
