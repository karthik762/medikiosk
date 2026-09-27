package com.medikiosk.abdm.dto;

/**
 * Request payload for ABDM clinical data exchange.
 */
public class AbdmExchangeRequestDto {

    private String notes;
    private String idempotencyKey;

    public AbdmExchangeRequestDto() {
    }

    public AbdmExchangeRequestDto(String notes, String idempotencyKey) {
        this.notes = notes;
        this.idempotencyKey = idempotencyKey;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
