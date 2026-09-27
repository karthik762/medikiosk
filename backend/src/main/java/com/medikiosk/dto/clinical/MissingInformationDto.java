package com.medikiosk.dto.clinical;

/**
 * Explicitly identified missing or unrecorded information.
 */
public class MissingInformationDto {

    private String field;
    private String reason;

    public MissingInformationDto() {
    }

    public MissingInformationDto(String field, String reason) {
        this.field = field;
        this.reason = reason;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
