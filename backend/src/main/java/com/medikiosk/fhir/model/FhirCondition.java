package com.medikiosk.fhir.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirCondition extends FhirResource {
    private List<FhirIdentifier> identifier;
    private FhirCodeableConcept clinicalStatus;
    private FhirCodeableConcept verificationStatus;
    private List<FhirCodeableConcept> category;
    private FhirCodeableConcept severity;
    private FhirCodeableConcept code;
    private FhirReference subject; // Patient
    private FhirReference encounter;
    private String onsetDateTime;
    private String onsetString;
    private String recordedDate;
    private List<ConditionNote> note;

    public FhirCondition() {
        super("Condition", null);
    }

    public FhirCondition(String id) {
        super("Condition", id);
    }

    public List<FhirIdentifier> getIdentifier() {
        return identifier;
    }

    public void setIdentifier(List<FhirIdentifier> identifier) {
        this.identifier = identifier;
    }

    public FhirCodeableConcept getClinicalStatus() {
        return clinicalStatus;
    }

    public void setClinicalStatus(FhirCodeableConcept clinicalStatus) {
        this.clinicalStatus = clinicalStatus;
    }

    public FhirCodeableConcept getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(FhirCodeableConcept verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public List<FhirCodeableConcept> getCategory() {
        return category;
    }

    public void setCategory(List<FhirCodeableConcept> category) {
        this.category = category;
    }

    public void addCategory(FhirCodeableConcept c) {
        if (this.category == null) {
            this.category = new ArrayList<>();
        }
        this.category.add(c);
    }

    public FhirCodeableConcept getSeverity() {
        return severity;
    }

    public void setSeverity(FhirCodeableConcept severity) {
        this.severity = severity;
    }

    public FhirCodeableConcept getCode() {
        return code;
    }

    public void setCode(FhirCodeableConcept code) {
        this.code = code;
    }

    public FhirReference getSubject() {
        return subject;
    }

    public void setSubject(FhirReference subject) {
        this.subject = subject;
    }

    public FhirReference getEncounter() {
        return encounter;
    }

    public void setEncounter(FhirReference encounter) {
        this.encounter = encounter;
    }

    public String getOnsetDateTime() {
        return onsetDateTime;
    }

    public void setOnsetDateTime(String onsetDateTime) {
        this.onsetDateTime = onsetDateTime;
    }

    public String getOnsetString() {
        return onsetString;
    }

    public void setOnsetString(String onsetString) {
        this.onsetString = onsetString;
    }

    public String getRecordedDate() {
        return recordedDate;
    }

    public void setRecordedDate(String recordedDate) {
        this.recordedDate = recordedDate;
    }

    public List<ConditionNote> getNote() {
        return note;
    }

    public void setNote(List<ConditionNote> note) {
        this.note = note;
    }

    public void addNote(String text) {
        if (this.note == null) {
            this.note = new ArrayList<>();
        }
        this.note.add(new ConditionNote(text));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ConditionNote {
        private String text;

        public ConditionNote() {
        }

        public ConditionNote(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
