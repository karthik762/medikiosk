package com.medikiosk.dto.ayurveda;

import java.util.Map;

public class NidanaDataDto {
    private String dietaryHabits;
    private String activityPattern;
    private String stressFactors;
    private String sleepSchedule;
    private String seasonalAggravation;
    private Map<String, Object> additionalNidanaDetails;

    public NidanaDataDto() {
    }

    public String getDietaryHabits() {
        return dietaryHabits;
    }

    public void setDietaryHabits(String dietaryHabits) {
        this.dietaryHabits = dietaryHabits;
    }

    public String getActivityPattern() {
        return activityPattern;
    }

    public void setActivityPattern(String activityPattern) {
        this.activityPattern = activityPattern;
    }

    public String getStressFactors() {
        return stressFactors;
    }

    public void setStressFactors(String stressFactors) {
        this.stressFactors = stressFactors;
    }

    public String getSleepSchedule() {
        return sleepSchedule;
    }

    public void setSleepSchedule(String sleepSchedule) {
        this.sleepSchedule = sleepSchedule;
    }

    public String getSeasonalAggravation() {
        return seasonalAggravation;
    }

    public void setSeasonalAggravation(String seasonalAggravation) {
        this.seasonalAggravation = seasonalAggravation;
    }

    public Map<String, Object> getAdditionalNidanaDetails() {
        return additionalNidanaDetails;
    }

    public void setAdditionalNidanaDetails(Map<String, Object> additionalNidanaDetails) {
        this.additionalNidanaDetails = additionalNidanaDetails;
    }
}
