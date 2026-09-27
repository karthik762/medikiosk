package com.medikiosk.dto.doctor;

import com.medikiosk.dto.clinical.StructuredAllergyDto;
import com.medikiosk.dto.clinical.StructuredHistoryDto;
import com.medikiosk.dto.clinical.StructuredMedicationDto;
import com.medikiosk.dto.clinical.StructuredSymptomDto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Encapsulates the immutable, raw intake information provided directly by the patient.
 * Read-only in the doctor workstation to prevent accidental modification of source records.
 */
public class OriginalPatientDataDto {

    private String chiefComplaint;
    private List<PatientAnswerRecordDto> intakeAnswers = new ArrayList<>();
    private List<StructuredSymptomDto> symptoms = new ArrayList<>();
    private List<StructuredHistoryDto> medicalHistory = new ArrayList<>();
    private List<StructuredMedicationDto> medications = new ArrayList<>();
    private List<StructuredAllergyDto> allergies = new ArrayList<>();

    public OriginalPatientDataDto() {
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public List<PatientAnswerRecordDto> getIntakeAnswers() {
        return intakeAnswers;
    }

    public void setIntakeAnswers(List<PatientAnswerRecordDto> intakeAnswers) {
        this.intakeAnswers = intakeAnswers;
    }

    public List<StructuredSymptomDto> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<StructuredSymptomDto> symptoms) {
        this.symptoms = symptoms;
    }

    public List<StructuredHistoryDto> getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(List<StructuredHistoryDto> medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public List<StructuredMedicationDto> getMedications() {
        return medications;
    }

    public void setMedications(List<StructuredMedicationDto> medications) {
        this.medications = medications;
    }

    public List<StructuredAllergyDto> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<StructuredAllergyDto> allergies) {
        this.allergies = allergies;
    }

    public static class PatientAnswerRecordDto {
        private UUID questionId;
        private String questionCode;
        private String questionText;
        private String answerText;
        private String inputMethod;
        private String originalTranscription;
        private Instant answeredAt;

        public PatientAnswerRecordDto() {
        }

        public PatientAnswerRecordDto(UUID questionId, String questionCode, String questionText,
                                      String answerText, String inputMethod, String originalTranscription,
                                      Instant answeredAt) {
            this.questionId = questionId;
            this.questionCode = questionCode;
            this.questionText = questionText;
            this.answerText = answerText;
            this.inputMethod = inputMethod;
            this.originalTranscription = originalTranscription;
            this.answeredAt = answeredAt;
        }

        public UUID getQuestionId() {
            return questionId;
        }

        public void setQuestionId(UUID questionId) {
            this.questionId = questionId;
        }

        public String getQuestionCode() {
            return questionCode;
        }

        public void setQuestionCode(String questionCode) {
            this.questionCode = questionCode;
        }

        public String getQuestionText() {
            return questionText;
        }

        public void setQuestionText(String questionText) {
            this.questionText = questionText;
        }

        public String getAnswerText() {
            return answerText;
        }

        public void setAnswerText(String answerText) {
            this.answerText = answerText;
        }

        public String getInputMethod() {
            return inputMethod;
        }

        public void setInputMethod(String inputMethod) {
            this.inputMethod = inputMethod;
        }

        public String getOriginalTranscription() {
            return originalTranscription;
        }

        public void setOriginalTranscription(String originalTranscription) {
            this.originalTranscription = originalTranscription;
        }

        public Instant getAnsweredAt() {
            return answeredAt;
        }

        public void setAnsweredAt(Instant answeredAt) {
            this.answeredAt = answeredAt;
        }
    }
}
