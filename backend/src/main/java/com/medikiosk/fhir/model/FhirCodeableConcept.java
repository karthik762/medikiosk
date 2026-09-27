package com.medikiosk.fhir.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirCodeableConcept {
    private List<FhirCoding> coding;
    private String text;

    public FhirCodeableConcept() {
    }

    public FhirCodeableConcept(String text) {
        this.text = text;
        this.coding = new ArrayList<>();
    }

    public FhirCodeableConcept(String system, String code, String display) {
        this.coding = new ArrayList<>();
        this.coding.add(new FhirCoding(system, code, display));
        this.text = display;
    }

    public FhirCodeableConcept(String system, String code, String display, String text) {
        this.coding = new ArrayList<>();
        this.coding.add(new FhirCoding(system, code, display));
        this.text = text;
    }

    public List<FhirCoding> getCoding() {
        return coding;
    }

    public void setCoding(List<FhirCoding> coding) {
        this.coding = coding;
    }

    public void addCoding(FhirCoding c) {
        if (this.coding == null) {
            this.coding = new ArrayList<>();
        }
        this.coding.add(c);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
