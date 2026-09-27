package com.medikiosk.dto.ayurveda;

public class AyurvedaTerminologyDto {
    private String conceptName;
    private String sanskritTerm;
    private String namasteCode;
    private String namasteDisplay;
    private String icd11Tm2Code;
    private String icd11Tm2Display;
    private String mappingStatus = "UNRESOLVED";
    private String description;

    public AyurvedaTerminologyDto() {
    }

    public AyurvedaTerminologyDto(String conceptName, String sanskritTerm, String description) {
        this.conceptName = conceptName;
        this.sanskritTerm = sanskritTerm;
        this.description = description;
        this.mappingStatus = "UNRESOLVED";
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    public String getSanskritTerm() {
        return sanskritTerm;
    }

    public void setSanskritTerm(String sanskritTerm) {
        this.sanskritTerm = sanskritTerm;
    }

    public String getNamasteCode() {
        return namasteCode;
    }

    public void setNamasteCode(String namasteCode) {
        this.namasteCode = namasteCode;
    }

    public String getNamasteDisplay() {
        return namasteDisplay;
    }

    public void setNamasteDisplay(String namasteDisplay) {
        this.namasteDisplay = namasteDisplay;
    }

    public String getIcd11Tm2Code() {
        return icd11Tm2Code;
    }

    public void setIcd11Tm2Code(String icd11Tm2Code) {
        this.icd11Tm2Code = icd11Tm2Code;
    }

    public String getIcd11Tm2Display() {
        return icd11Tm2Display;
    }

    public void setIcd11Tm2Display(String icd11Tm2Display) {
        this.icd11Tm2Display = icd11Tm2Display;
    }

    public String getMappingStatus() {
        return mappingStatus;
    }

    public void setMappingStatus(String mappingStatus) {
        this.mappingStatus = mappingStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
