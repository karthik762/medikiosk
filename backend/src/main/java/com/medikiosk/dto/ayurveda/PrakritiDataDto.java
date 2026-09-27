package com.medikiosk.dto.ayurveda;

import java.util.Map;

public class PrakritiDataDto {
    private String bodyFrame;
    private String weatherPreference;
    private String digestionPattern;
    private String sleepPattern;
    private String skinTexture;
    private String hairTexture;
    private String temperament;
    private Map<String, Object> additionalPrakritiDetails;

    public PrakritiDataDto() {
    }

    public String getBodyFrame() {
        return bodyFrame;
    }

    public void setBodyFrame(String bodyFrame) {
        this.bodyFrame = bodyFrame;
    }

    public String getWeatherPreference() {
        return weatherPreference;
    }

    public void setWeatherPreference(String weatherPreference) {
        this.weatherPreference = weatherPreference;
    }

    public String getDigestionPattern() {
        return digestionPattern;
    }

    public void setDigestionPattern(String digestionPattern) {
        this.digestionPattern = digestionPattern;
    }

    public String getSleepPattern() {
        return sleepPattern;
    }

    public void setSleepPattern(String sleepPattern) {
        this.sleepPattern = sleepPattern;
    }

    public String getSkinTexture() {
        return skinTexture;
    }

    public void setSkinTexture(String skinTexture) {
        this.skinTexture = skinTexture;
    }

    public String getHairTexture() {
        return hairTexture;
    }

    public void setHairTexture(String hairTexture) {
        this.hairTexture = hairTexture;
    }

    public String getTemperament() {
        return temperament;
    }

    public void setTemperament(String temperament) {
        this.temperament = temperament;
    }

    public Map<String, Object> getAdditionalPrakritiDetails() {
        return additionalPrakritiDetails;
    }

    public void setAdditionalPrakritiDetails(Map<String, Object> additionalPrakritiDetails) {
        this.additionalPrakritiDetails = additionalPrakritiDetails;
    }
}
