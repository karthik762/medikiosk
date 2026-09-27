package com.medikiosk.dto.clinical;

/**
 * Dual terminology coding representation for clinical concepts (e.g. ICD-10, NAMASTE).
 * Conforms to docs/AI_SPEC.md and Stage 5B specifications.
 */
public class ClinicalCodingDto {

    private String system;
    private String code;
    private String display;
    private Double confidence;
    private String source;

    public ClinicalCodingDto() {
    }

    public ClinicalCodingDto(String system, String code, String display, Double confidence, String source) {
        this.system = system != null ? system : "UNMAPPED";
        this.code = code != null ? code : "UNMAPPED";
        this.display = display;
        this.confidence = confidence != null ? confidence : 0.0;
        this.source = source != null ? source : "CONCEPT_REGISTRY";
    }

    public static ClinicalCodingDto unmapped(String originalDisplay) {
        return new ClinicalCodingDto("UNMAPPED", "UNMAPPED", originalDisplay, 0.0, "UNMAPPED");
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

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
