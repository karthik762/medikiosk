package com.medikiosk.fhir.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirReference {
    private String reference;
    private String type;
    private FhirIdentifier identifier;
    private String display;

    public FhirReference() {
    }

    public FhirReference(String reference) {
        this.reference = reference;
    }

    public FhirReference(String reference, String display) {
        this.reference = reference;
        this.display = display;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FhirIdentifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(FhirIdentifier identifier) {
        this.identifier = identifier;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }
}
