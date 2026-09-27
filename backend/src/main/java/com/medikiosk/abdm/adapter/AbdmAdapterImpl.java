package com.medikiosk.abdm.adapter;

import com.medikiosk.abdm.config.AbdmProperties;
import com.medikiosk.abdm.dto.AbdmExchangeRequestDto;
import com.medikiosk.abdm.dto.AbdmExchangeResponseDto;
import com.medikiosk.abdm.service.AbdmService;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Architectural adapter implementation for Ayushman Bharat Digital Mission (ABDM).
 * Conforms to docs/ABDM_SPEC.md requirements and delegates to AbdmService.
 */
@Component
public class AbdmAdapterImpl implements AbdmAdapter {

    private final AbdmProperties properties;
    private final AbdmService abdmService;

    public AbdmAdapterImpl(AbdmProperties properties, AbdmService abdmService) {
        this.properties = properties;
        this.abdmService = abdmService;
    }

    @Override
    public boolean isAvailable() {
        return abdmService.getStatus().isEnabled();
    }

    @Override
    public String linkRecord(UUID doctorId, UUID intakeSessionId, String fhirBundleJson) {
        AbdmExchangeRequestDto request = new AbdmExchangeRequestDto();
        request.setIdempotencyKey("ABDM-ADAPTER-" + intakeSessionId);
        AbdmExchangeResponseDto response = abdmService.exchangeClinicalData(intakeSessionId, request, doctorId);
        return (response != null && response.getStatus() == com.medikiosk.entity.enums.IntegrationStatus.SUCCESS) ? response.getTransactionId() : null;
    }
}
