package com.medikiosk.his.adapter;

import com.medikiosk.his.config.HisProperties;
import com.medikiosk.his.dto.HisExportRequestDto;
import com.medikiosk.his.dto.HisExportResponseDto;
import com.medikiosk.his.service.HisService;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Architectural adapter implementation for Hospital Information System (HIS).
 * Conforms to docs/HIS_SPEC.md requirements and delegates to HisService.
 */
@Component
public class HisAdapterImpl implements HisAdapter {

    private final HisProperties properties;
    private final HisService hisService;

    public HisAdapterImpl(HisProperties properties, HisService hisService) {
        this.properties = properties;
        this.hisService = hisService;
    }

    @Override
    public boolean isAvailable() {
        return hisService.getStatus().isEnabled();
    }

    @Override
    public String exportToIntakeQueue(UUID doctorId, UUID intakeSessionId, String clinicalSummary) {
        // High-level adapter facade method
        HisExportRequestDto request = HisExportRequestDto.builder()
                .intakeQueue("OPD_INTAKE")
                .idempotencyKey("HIS-ADAPTER-" + intakeSessionId)
                .build();
        // Uses patient/system context
        HisExportResponseDto response = hisService.exportClinicalData(intakeSessionId, request, doctorId);
        return (response != null && response.getStatus() == com.medikiosk.entity.enums.IntegrationStatus.SUCCESS) ? response.getEncounterReference() : null;
    }
}
