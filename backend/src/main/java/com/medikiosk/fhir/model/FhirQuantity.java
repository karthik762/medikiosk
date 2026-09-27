package com.medikiosk.fhir.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirQuantity {
    private BigDecimal value;
    private String unit;
    private String system;
    private String code;

    public FhirQuantity() {
    }

    public FhirQuantity(BigDecimal value, String unit) {
        this.value = value;
        this.unit = unit;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
