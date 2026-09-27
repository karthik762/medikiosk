package com.medikiosk.dto.ayurveda;

import java.util.List;

public class AyurvedaHistoryRequest {
    private PrakritiDataDto prakriti;
    private NidanaDataDto nidana;
    private ParikshaDataDto pariksha;
    private String notes;
    private List<AyurvedaTerminologyDto> terminology;

    public AyurvedaHistoryRequest() {
    }

    public PrakritiDataDto getPrakriti() {
        return prakriti;
    }

    public void setPrakriti(PrakritiDataDto prakriti) {
        this.prakriti = prakriti;
    }

    public NidanaDataDto getNidana() {
        return nidana;
    }

    public void setNidana(NidanaDataDto nidana) {
        this.nidana = nidana;
    }

    public ParikshaDataDto getPariksha() {
        return pariksha;
    }

    public void setPariksha(ParikshaDataDto pariksha) {
        this.pariksha = pariksha;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<AyurvedaTerminologyDto> getTerminology() {
        return terminology;
    }

    public void setTerminology(List<AyurvedaTerminologyDto> terminology) {
        this.terminology = terminology;
    }
}
