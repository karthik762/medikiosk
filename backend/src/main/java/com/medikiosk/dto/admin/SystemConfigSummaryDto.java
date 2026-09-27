package com.medikiosk.dto.admin;

import java.util.Map;

public class SystemConfigSummaryDto {
    private String environment;
    private String aiProviderMode;
    private boolean externalAiEnabled;
    private String abdmOperatingMode;
    private boolean abdmMock;
    private String hisOperatingMode;
    private boolean hisMock;
    private String healthLakeOperatingMode;
    private boolean healthLakeMock;
    private String databaseStatus;
    private Map<String, Boolean> featureToggles;

    public SystemConfigSummaryDto() {
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getAiProviderMode() {
        return aiProviderMode;
    }

    public void setAiProviderMode(String aiProviderMode) {
        this.aiProviderMode = aiProviderMode;
    }

    public boolean isExternalAiEnabled() {
        return externalAiEnabled;
    }

    public void setExternalAiEnabled(boolean externalAiEnabled) {
        this.externalAiEnabled = externalAiEnabled;
    }

    public String getAbdmOperatingMode() {
        return abdmOperatingMode;
    }

    public void setAbdmOperatingMode(String abdmOperatingMode) {
        this.abdmOperatingMode = abdmOperatingMode;
    }

    public boolean isAbdmMock() {
        return abdmMock;
    }

    public void setAbdmMock(boolean abdmMock) {
        this.abdmMock = abdmMock;
    }

    public String getHisOperatingMode() {
        return hisOperatingMode;
    }

    public void setHisOperatingMode(String hisOperatingMode) {
        this.hisOperatingMode = hisOperatingMode;
    }

    public boolean isHisMock() {
        return hisMock;
    }

    public void setHisMock(boolean hisMock) {
        this.hisMock = hisMock;
    }

    public String getHealthLakeOperatingMode() {
        return healthLakeOperatingMode;
    }

    public void setHealthLakeOperatingMode(String healthLakeOperatingMode) {
        this.healthLakeOperatingMode = healthLakeOperatingMode;
    }

    public boolean isHealthLakeMock() {
        return healthLakeMock;
    }

    public void setHealthLakeMock(boolean healthLakeMock) {
        this.healthLakeMock = healthLakeMock;
    }

    public String getDatabaseStatus() {
        return databaseStatus;
    }

    public void setDatabaseStatus(String databaseStatus) {
        this.databaseStatus = databaseStatus;
    }

    public Map<String, Boolean> getFeatureToggles() {
        return featureToggles;
    }

    public void setFeatureToggles(Map<String, Boolean> featureToggles) {
        this.featureToggles = featureToggles;
    }
}
