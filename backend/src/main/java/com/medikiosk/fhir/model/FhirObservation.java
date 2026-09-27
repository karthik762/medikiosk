package com.medikiosk.fhir.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirObservation extends FhirResource {
    private List<FhirIdentifier> identifier;
    private String status; // registered | preliminary | final | amended
    private List<FhirCodeableConcept> category;
    private FhirCodeableConcept code;
    private FhirReference subject;
    private FhirReference encounter;
    private String effectiveDateTime;
    private String valueString;
    private FhirCodeableConcept valueCodeableConcept;
    private FhirQuantity valueQuantity;
    private List<ObservationComponent> component;
    private List<ObservationNote> note;

    public FhirObservation() {
        super("Observation", null);
    }

    public FhirObservation(String id) {
        super("Observation", id);
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

    public String getEffectiveDateTime() {
        return effectiveDateTime;
    }

    public void setEffectiveDateTime(String effectiveDateTime) {
        this.effectiveDateTime = effectiveDateTime;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    public FhirCodeableConcept getValueCodeableConcept() {
        return valueCodeableConcept;
    }

    public void setValueCodeableConcept(FhirCodeableConcept valueCodeableConcept) {
        this.valueCodeableConcept = valueCodeableConcept;
    }

    public FhirQuantity getValueQuantity() {
        return valueQuantity;
    }

    public void setValueQuantity(FhirQuantity valueQuantity) {
        this.valueQuantity = valueQuantity;
    }

    public List<ObservationComponent> getComponent() {
        return component;
    }

    public void setComponent(List<ObservationComponent> component) {
        this.component = component;
    }

    public void addComponent(FhirCodeableConcept code, String valueString) {
        if (this.component == null) {
            this.component = new ArrayList<>();
        }
        this.component.add(new ObservationComponent(code, valueString));
    }

    public List<ObservationNote> getNote() {
        return note;
    }

    public void setNote(List<ObservationNote> note) {
        this.note = note;
    }

    public void addNote(String text) {
        if (this.note == null) {
            this.note = new ArrayList<>();
        }
        this.note.add(new ObservationNote(text));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ObservationComponent {
        private FhirCodeableConcept code;
        private String valueString;
        private FhirCodeableConcept valueCodeableConcept;

        public ObservationComponent() {
        }

        public ObservationComponent(FhirCodeableConcept code, String valueString) {
            this.code = code;
            this.valueString = valueString;
        }

        public FhirCodeableConcept getCode() {
            return code;
        }

        public void setCode(FhirCodeableConcept code) {
            this.code = code;
        }

        public String getValueString() {
            return valueString;
        }

        public void setValueString(String valueString) {
            this.valueString = valueString;
        }

        public FhirCodeableConcept getValueCodeableConcept() {
            return valueCodeableConcept;
        }

        public void setValueCodeableConcept(FhirCodeableConcept valueCodeableConcept) {
            this.valueCodeableConcept = valueCodeableConcept;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ObservationNote {
        private String text;

        public ObservationNote() {
        }

        public ObservationNote(String text) {
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
