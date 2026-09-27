package com.medikiosk.dto.admin;

import java.util.Map;

public class SystemAnalyticsDto {
    private long totalIntakes;
    private long completedIntakes;
    private long inProgressIntakes;
    private long totalPatients;
    private long totalDoctors;
    private double avgIntakeDurationSeconds;
    private Map<String, Long> triageDistribution;
    private long totalSummariesGenerated;
    private long totalSummariesVerified;
    private long totalDocumentsProcessed;
    private long fhirBundlesGenerated;
    private long abdmExchangesCount;
    private long hisExportsCount;
    private long healthLakeSyncsCount;

    public SystemAnalyticsDto() {
    }

    public long getTotalIntakes() {
        return totalIntakes;
    }

    public void setTotalIntakes(long totalIntakes) {
        this.totalIntakes = totalIntakes;
    }

    public long getCompletedIntakes() {
        return completedIntakes;
    }

    public void setCompletedIntakes(long completedIntakes) {
        this.completedIntakes = completedIntakes;
    }

    public long getInProgressIntakes() {
        return inProgressIntakes;
    }

    public void setInProgressIntakes(long inProgressIntakes) {
        this.inProgressIntakes = inProgressIntakes;
    }

    public long getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(long totalPatients) {
        this.totalPatients = totalPatients;
    }

    public long getTotalDoctors() {
        return totalDoctors;
    }

    public void setTotalDoctors(long totalDoctors) {
        this.totalDoctors = totalDoctors;
    }

    public double getAvgIntakeDurationSeconds() {
        return avgIntakeDurationSeconds;
    }

    public void setAvgIntakeDurationSeconds(double avgIntakeDurationSeconds) {
        this.avgIntakeDurationSeconds = avgIntakeDurationSeconds;
    }

    public Map<String, Long> getTriageDistribution() {
        return triageDistribution;
    }

    public void setTriageDistribution(Map<String, Long> triageDistribution) {
        this.triageDistribution = triageDistribution;
    }

    public long getTotalSummariesGenerated() {
        return totalSummariesGenerated;
    }

    public void setTotalSummariesGenerated(long totalSummariesGenerated) {
        this.totalSummariesGenerated = totalSummariesGenerated;
    }

    public long getTotalSummariesVerified() {
        return totalSummariesVerified;
    }

    public void setTotalSummariesVerified(long totalSummariesVerified) {
        this.totalSummariesVerified = totalSummariesVerified;
    }

    public long getTotalDocumentsProcessed() {
        return totalDocumentsProcessed;
    }

    public void setTotalDocumentsProcessed(long totalDocumentsProcessed) {
        this.totalDocumentsProcessed = totalDocumentsProcessed;
    }

    public long getFhirBundlesGenerated() {
        return fhirBundlesGenerated;
    }

    public void setFhirBundlesGenerated(long fhirBundlesGenerated) {
        this.fhirBundlesGenerated = fhirBundlesGenerated;
    }

    public long getAbdmExchangesCount() {
        return abdmExchangesCount;
    }

    public void setAbdmExchangesCount(long abdmExchangesCount) {
        this.abdmExchangesCount = abdmExchangesCount;
    }

    public long getHisExportsCount() {
        return hisExportsCount;
    }

    public void setHisExportsCount(long hisExportsCount) {
        this.hisExportsCount = hisExportsCount;
    }

    public long getHealthLakeSyncsCount() {
        return healthLakeSyncsCount;
    }

    public void setHealthLakeSyncsCount(long healthLakeSyncsCount) {
        this.healthLakeSyncsCount = healthLakeSyncsCount;
    }
}
