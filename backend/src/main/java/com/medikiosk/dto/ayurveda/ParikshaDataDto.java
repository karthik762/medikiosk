package com.medikiosk.dto.ayurveda;

import java.util.Map;

public class ParikshaDataDto {
    private String koshtha;
    private String bala;
    private String jihva;
    private String agni;
    private String mutra;
    private Map<String, Object> additionalParikshaDetails;

    public ParikshaDataDto() {
    }

    public String getKoshtha() {
        return koshtha;
    }

    public void setKoshtha(String koshtha) {
        this.koshtha = koshtha;
    }

    public String getBala() {
        return bala;
    }

    public void setBala(String bala) {
        this.bala = bala;
    }

    public String getJihva() {
        return jihva;
    }

    public void setJihva(String jihva) {
        this.jihva = jihva;
    }

    public String getAgni() {
        return agni;
    }

    public void setAgni(String agni) {
        this.agni = agni;
    }

    public String getMutra() {
        return mutra;
    }

    public void setMutra(String mutra) {
        this.mutra = mutra;
    }

    public Map<String, Object> getAdditionalParikshaDetails() {
        return additionalParikshaDetails;
    }

    public void setAdditionalParikshaDetails(Map<String, Object> additionalParikshaDetails) {
        this.additionalParikshaDetails = additionalParikshaDetails;
    }
}
