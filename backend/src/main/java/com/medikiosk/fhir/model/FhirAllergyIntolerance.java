package com.medikiosk.fhir.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirAllergyIntolerance extends FhirResource {
    private List<FhirIdentifier> identifier;
    private FhirCodeableConcept clinicalStatus;
    private FhirCodeableConcept verificationStatus;
    private String type; // allergy | intolerance
    private List<String> category; // food | medication | environment | biologic
    private String criticality; // low | high | unable-to-assess
    private FhirCodeableConcept code;
    private FhirReference patient;
    private FhirReference encounter;
    private String recordedDate;
    private List<AllergyReaction> reaction;
    private List<AllergyNote> note;

    public FhirAllergyIntolerance() {
        super("AllergyIntolerance", null);
    }

    public FhirAllergyIntolerance(String id) {
        super("AllergyIntolerance", id);
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getCategory() {
        return category;
    }

    public void setCategory(List<String> category) {
        this.category = category;
    }

    public void addCategory(String cat) {
        if (this.category == null) {
            this.category = new ArrayList<>();
        }
        this.category.add(cat);
    }

    public String getCriticality() {
        return criticality;
    }

    public void setCriticality(String criticality) {
        this.criticality = criticality;
    }

    public FhirCodeableConcept getCode() {
        return code;
    }

    public void setCode(FhirCodeableConcept code) {
        this.code = code;
    }

    public FhirReference getPatient() {
        return patient;
    }

    public void setPatient(FhirReference patient) {
        this.patient = patient;
    }

    public FhirReference getEncounter() {
        return encounter;
    }

    public void setEncounter(FhirReference encounter) {
        this.encounter = encounter;
    }

    public String getRecordedDate() {
        return recordedDate;
    }

    public void setRecordedDate(String recordedDate) {
        this.recordedDate = recordedDate;
    }

    public List<AllergyReaction> getReaction() {
        return reaction;
    }

    public void setReaction(List<AllergyReaction> reaction) {
        this.reaction = reaction;
    }

    public void addReaction(String manifestationText, String severity) {
        if (this.reaction == null) {
            this.reaction = new ArrayList<>();
        }
        AllergyReaction r = new AllergyReaction();
        if (manifestationText != null) {
            List<FhirCodeableConcept> manifestations = new ArrayList<>();
            manifestations.add(new FhirCodeableConcept(manifestationText));
            r.setManifestation(manifestations);
        }
        r.setSeverity(severity);
        this.reaction.add(r);
    }

    public List<AllergyNote> getNote() {
        return note;
    }

    public void setNote(List<AllergyNote> note) {
        this.note = note;
    }

    public void addNote(String text) {
        if (this.note == null) {
            this.note = new ArrayList<>();
        }
        this.note.add(new AllergyNote(text));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AllergyReaction {
        private List<FhirCodeableConcept> manifestation;
        private String severity; // mild | moderate | severe

        public AllergyReaction() {
        }

        public List<FhirCodeableConcept> getManifestation() {
            return manifestation;
        }

        public void setManifestation(List<FhirCodeableConcept> manifestation) {
            this.manifestation = manifestation;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AllergyNote {
        private String text;

        public AllergyNote() {
        }

        public AllergyNote(String text) {
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
