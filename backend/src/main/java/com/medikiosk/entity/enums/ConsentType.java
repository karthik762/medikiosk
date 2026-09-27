package com.medikiosk.entity.enums;

/**
 * Canonical consent type identifiers kept as string constants for JPA string columns.
 * GENERAL_INTAKE and ABDM_DATA_SHARE must remain distinct and never be conflated.
 */
public final class ConsentType {

    public static final String GENERAL_INTAKE = "GENERAL_INTAKE";
    public static final String ABDM_DATA_SHARE = "ABDM_DATA_SHARE";

    private ConsentType() {
    }
}
