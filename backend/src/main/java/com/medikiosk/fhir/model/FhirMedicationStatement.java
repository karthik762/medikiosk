package com.medikiosk.fhir.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirMedicationStatement extends FhirResource {
    private List<FhirIdentifier> identifier;
    private String status; // active | completed | entered-in-error | intended | stopped | on-hold | unknown | not-taken
    private FhirCodeableConcept medicationCodeableConcept;
    private FhirReference subject; // Patient
    private FhirReference context; // Encounter
    private String effectiveDateTime;
    private String dateAsserted;
    private FhirReference informationSource;
    private List<MedicationDosage> dosage;
    private List<MedicationNote> note;

    public FhirMedicationStatement() {
        super("MedicationStatement", null);
    }

    public FhirMedicationStatement(String id) {
        super("MedicationStatement", id);
    }

    public List<FhirIdentifier> getIdentifier() {
        return identifier;
    }

    public void setIdentifier(List<FhirIdentifier> identifier) {
        this.identifier = identifier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public FhirCodeableConcept getMedicationCodeableConcept() {
        return medicationCodeableConcept;
    }

    public void setMedicationCodeableConcept(FhirCodeableConcept medicationCodeableConcept) {
        this.medicationCodeableConcept = medicationCodeableConcept;
    }

    public FhirReference getSubject() {
        return subject;
    }

    public void setSubject(FhirReference subject) {
        this.subject = subject;
    }

    public FhirReference getContext() {
        return context;
    }

    public void setContext(FhirReference context) {
        this.context = context;
    }

    public String getEffectiveDateTime() {
        return effectiveDateTime;
    }

    public void setEffectiveDateTime(String effectiveDateTime) {
        this.effectiveDateTime = effectiveDateTime;
    }

    public String getDateAsserted() {
        return dateAsserted;
    }

    public void setDateAsserted(String dateAsserted) {
        this.dateAsserted = dateAsserted;
    }

    public FhirReference getInformationSource() {
        return informationSource;
    }

    public void setInformationSource(FhirReference informationSource) {
        this.informationSource = informationSource;
    }

    public List<MedicationDosage> getDosage() {
        return dosage;
    }

    public void setDosage(List<MedicationDosage> dosage) {
        this.dosage = dosage;
    }

    public void addDosage(String text) {
        if (this.dosage == null) {
            this.dosage = new ArrayList<>();
        }
        this.dosage.add(new MedicationDosage(text));
    }

    public List<MedicationNote> getNote() {
        return note;
    }

    public void setNote(List<MedicationNote> note) {
        this.note = note;
    }

    public void addNote(String text) {
        if (this.note == null) {
            this.note = new ArrayList<>();
        }
        this.note.add(new MedicationNote(text));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MedicationDosage {
        private String text;

        public MedicationDosage() {
        }

        public MedicationDosage(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MedicationNote {
        private String text;

        public MedicationNote() {
        }

        public MedicationNote(String text) {
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
