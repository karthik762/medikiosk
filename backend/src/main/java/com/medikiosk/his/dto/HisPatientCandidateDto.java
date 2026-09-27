package com.medikiosk.his.dto;

import java.time.LocalDate;

/**
 * Summary details of a patient candidate discovered in Hospital Information System (HIS) lookup.
 */
public class HisPatientCandidateDto {

    private String hisId;
    private String displayName;
    private String phone;
    private String gender;
    private LocalDate dateOfBirth;
    private String department;
    private String roomOrWard;

    public HisPatientCandidateDto() {
    }

    public HisPatientCandidateDto(String hisId, String displayName, String phone, String gender,
                                  LocalDate dateOfBirth, String department, String roomOrWard) {
        this.hisId = hisId;
        this.displayName = displayName;
        this.phone = phone;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.department = department;
        this.roomOrWard = roomOrWard;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String hisId;
        private String displayName;
        private String phone;
        private String gender;
        private LocalDate dateOfBirth;
        private String department;
        private String roomOrWard;

        public Builder hisId(String hisId) {
            this.hisId = hisId;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder gender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder dateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder department(String department) {
            this.department = department;
            return this;
        }

        public Builder roomOrWard(String roomOrWard) {
            this.roomOrWard = roomOrWard;
            return this;
        }

        public HisPatientCandidateDto build() {
            return new HisPatientCandidateDto(hisId, displayName, phone, gender, dateOfBirth, department, roomOrWard);
        }
    }

    public String getHisId() {
        return hisId;
    }

    public void setHisId(String hisId) {
        this.hisId = hisId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRoomOrWard() {
        return roomOrWard;
    }

    public void setRoomOrWard(String roomOrWard) {
        this.roomOrWard = roomOrWard;
    }
}
