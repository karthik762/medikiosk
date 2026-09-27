package com.medikiosk.fhir.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirEncounter extends FhirResource {
    private List<FhirIdentifier> identifier;
    private String status; // planned | arrived | triaged | in-progress | onleave | finished | cancelled +
    @JsonProperty("class")
    private FhirCoding class_;
    private List<FhirCodeableConcept> type;
    private FhirReference subject; // Patient
    private FhirPeriod period;
    private List<FhirCodeableConcept> reasonCode;

    public FhirEncounter() {
        super("Encounter", null);
    }

    public FhirEncounter(String id) {
        super("Encounter", id);
    }

    public List<FhirIdentifier> getIdentifier() {
        return identifier;
    }

    public void setIdentifier(List<FhirIdentifier> identifier) {
        this.identifier = identifier;
    }

    public void addIdentifier(FhirIdentifier id) {
        if (this.identifier == null) {
            this.identifier = new ArrayList<>();
        }
        this.identifier.add(id);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("class")
    public FhirCoding getClass_() {
        return class_;
    }

    @JsonProperty("class")
    public void setClass_(FhirCoding class_) {
        this.class_ = class_;
    }

    // Jackson getter/setter naming support for class
    public FhirCoding getClazz() {
        return class_;
    }

    public void setClazz(FhirCoding class_) {
        this.class_ = class_;
    }

    public List<FhirCodeableConcept> getType() {
        return type;
    }

    public void setType(List<FhirCodeableConcept> type) {
        this.type = type;
    }

    public FhirReference getSubject() {
        return subject;
    }

    public void setSubject(FhirReference subject) {
        this.subject = subject;
    }

    public FhirPeriod getPeriod() {
        return period;
    }

    public void setPeriod(FhirPeriod period) {
        this.period = period;
    }

    public List<FhirCodeableConcept> getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(List<FhirCodeableConcept> reasonCode) {
        this.reasonCode = reasonCode;
    }

    public void addReasonCode(FhirCodeableConcept code) {
        if (this.reasonCode == null) {
            this.reasonCode = new ArrayList<>();
        }
        this.reasonCode.add(code);
    }
}
