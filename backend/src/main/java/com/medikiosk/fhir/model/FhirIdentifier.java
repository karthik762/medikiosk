package com.medikiosk.fhir.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirIdentifier {
    private String use;
    private FhirCodeableConcept type;
    private String system;
    private String value;

    public FhirIdentifier() {
    }

    public FhirIdentifier(String system, String value) {
        this.system = system;
        this.value = value;
    }

    public FhirIdentifier(String use, String system, String value) {
        this.use = use;
        this.system = system;
        this.value = value;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public FhirCodeableConcept getType() {
        return type;
    }

    public void setType(FhirCodeableConcept type) {
        this.type = type;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
