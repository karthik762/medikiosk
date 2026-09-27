package com.medikiosk.his.provider;

/**
 * Result of transmitting a clinical bundle or summary to a Hospital Information System (HIS) intake queue.
 */
public record HisExportResult(
        boolean success,
        String transactionId,
        String externalReference,
        String errorMessage,
        String responsePayload
) {
}
