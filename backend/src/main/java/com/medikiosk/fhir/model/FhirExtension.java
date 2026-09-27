package com.medikiosk.fhir.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirExtension {
    private String url;
    private String valueString;
    private Boolean valueBoolean;
    private Integer valueInteger;
    private FhirCodeableConcept valueCodeableConcept;
    private FhirReference valueReference;

    public FhirExtension() {
    }

    public FhirExtension(String url, String valueString) {
        this.url = url;
        this.valueString = valueString;
    }

    public FhirExtension(String url, Boolean valueBoolean) {
        this.url = url;
        this.valueBoolean = valueBoolean;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    public Boolean getValueBoolean() {
        return valueBoolean;
    }

    public void setValueBoolean(Boolean valueBoolean) {
        this.valueBoolean = valueBoolean;
    }

    public Integer getValueInteger() {
        return valueInteger;
    }

    public void setValueInteger(Integer valueInteger) {
        this.valueInteger = valueInteger;
    }

    public FhirCodeableConcept getValueCodeableConcept() {
        return valueCodeableConcept;
    }

    public void setValueCodeableConcept(FhirCodeableConcept valueCodeableConcept) {
        this.valueCodeableConcept = valueCodeableConcept;
    }

    public FhirReference getValueReference() {
        return valueReference;
    }

    public void setValueReference(FhirReference valueReference) {
        this.valueReference = valueReference;
    }
}
