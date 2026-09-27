package com.medikiosk.abdm.provider;

/**
 * Result of transmitting a clinical bundle to an ABDM provider.
 */
public record AbdmExchangeResult(
        boolean success,
        String transactionId,
        String externalReference,
        String errorMessage,
        String responsePayload
) {
}
