package com.medikiosk.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.ayurveda.AyurvedaIntakeService;
import com.medikiosk.dto.ayurveda.AyurvedaHistoryRequest;
import com.medikiosk.dto.ayurveda.AyurvedaHistoryResponse;
import com.medikiosk.dto.ayurveda.AyurvedaTerminologyDto;
import com.medikiosk.dto.ayurveda.NidanaDataDto;
import com.medikiosk.dto.ayurveda.ParikshaDataDto;
import com.medikiosk.dto.ayurveda.PrakritiDataDto;
import com.medikiosk.entity.AuditLog;
import com.medikiosk.entity.AyurvedaHistory;
import com.medikiosk.entity.Consent;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.AyurvedaHistoryStatus;
import com.medikiosk.entity.enums.ConsentStatus;
import com.medikiosk.entity.enums.ConsentType;
import com.medikiosk.entity.enums.IntakeSessionStatus;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.exception.ApiException;
import com.medikiosk.exception.ResourceNotFoundException;
import com.medikiosk.repository.AuditLogRepository;
import com.medikiosk.repository.AyurvedaHistoryRepository;
import com.medikiosk.repository.ConsentRepository;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.service.AyurvedaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AyurvedaServiceImpl implements AyurvedaService, AyurvedaIntakeService {

    private static final Logger log = LoggerFactory.getLogger(AyurvedaServiceImpl.class);

    private final AyurvedaHistoryRepository ayurvedaHistoryRepository;
    private final IntakeSessionRepository intakeSessionRepository;
    private final ConsentRepository consentRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AyurvedaServiceImpl(
            AyurvedaHistoryRepository ayurvedaHistoryRepository,
            IntakeSessionRepository intakeSessionRepository,
            ConsentRepository consentRepository,
            AuditLogRepository auditLogRepository,
            ObjectMapper objectMapper) {
        this.ayurvedaHistoryRepository = ayurvedaHistoryRepository;
        this.intakeSessionRepository = intakeSessionRepository;
        this.consentRepository = consentRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public AyurvedaHistoryResponse getAyurvedaHistory(UUID sessionId, User authenticatedUser) {
        IntakeSession session = getSessionOrThrow(sessionId);
        validateSessionAccess(session, authenticatedUser);

        AyurvedaHistory history = ayurvedaHistoryRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Ayurveda history not found for session: " + sessionId));

        return mapToResponse(history);
    }

    @Override
    @Transactional
    public AyurvedaHistoryResponse saveOrUpdateAyurvedaHistory(UUID sessionId, AyurvedaHistoryRequest request, User authenticatedUser) {
        IntakeSession session = getSessionOrThrow(sessionId);
        validateSessionAccess(session, authenticatedUser);
        verifyConsentGiven(sessionId);
        verifySessionModifiable(session);

        Optional<AyurvedaHistory> existingOpt = ayurvedaHistoryRepository.findBySessionId(sessionId);
        AyurvedaHistory history;

        if (existingOpt.isPresent()) {
            history = existingOpt.get();
            if (history.getStatus() == AyurvedaHistoryStatus.DOCTOR_REVIEWED) {
                throw new ApiException("Ayurveda history has already been reviewed by doctor and cannot be modified", HttpStatus.CONFLICT);
            }
            // If previously confirmed and now modified, revert to draft until confirmed again
            if (history.isPatientConfirmed()) {
                history.setStatus(AyurvedaHistoryStatus.DRAFT);
                history.setPatientConfirmed(false);
                history.setConfirmedAt(null);
            }
        } else {
            history = new AyurvedaHistory(session.getPatient(), session);
            history.setStatus(AyurvedaHistoryStatus.DRAFT);
            history.setPatientConfirmed(false);
        }

        try {
            if (request != null) {
                if (request.getPrakriti() != null) {
                    history.setPrakritiData(objectMapper.writeValueAsString(request.getPrakriti()));
                }
                if (request.getNidana() != null) {
                    history.setNidanaData(objectMapper.writeValueAsString(request.getNidana()));
                }
                if (request.getPariksha() != null) {
                    history.setParikshaData(objectMapper.writeValueAsString(request.getPariksha()));
                }
                if (request.getTerminology() != null) {
                    history.setTerminologyData(objectMapper.writeValueAsString(request.getTerminology()));
                } else if (history.getTerminologyData() == null) {
                    history.setTerminologyData(objectMapper.writeValueAsString(createDefaultTerminology()));
                }
                if (request.getNotes() != null) {
                    history.setNotes(request.getNotes());
                }
            }
        } catch (Exception e) {
            log.error("Failed to serialize Ayurveda history payload for session {}", sessionId, e);
            throw new ApiException("Failed to serialize Ayurveda clinical data", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        AyurvedaHistory saved = ayurvedaHistoryRepository.save(history);
        log.info("Saved Ayurveda history {} for session {}", saved.getId(), sessionId);

        // Audit log
        try {
            AuditLog auditLog = new AuditLog(
                    authenticatedUser != null ? authenticatedUser.getId() : null,
                    "AYURVEDA_RECORDED",
                    "AyurvedaHistory",
                    saved.getId(),
                    "{\"sessionId\":\"" + sessionId + "\",\"status\":\"" + saved.getStatus().name() + "\"}"
            );
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.warn("Failed to create audit log for Ayurveda record on session {}", sessionId, e);
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public AyurvedaHistoryResponse confirmAyurvedaHistory(UUID sessionId, User authenticatedUser) {
        IntakeSession session = getSessionOrThrow(sessionId);
        validateSessionAccess(session, authenticatedUser);
        verifyConsentGiven(sessionId);
        verifySessionModifiable(session);

        AyurvedaHistory history = ayurvedaHistoryRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Ayurveda history not found for session: " + sessionId));

        if (history.getStatus() == AyurvedaHistoryStatus.DOCTOR_REVIEWED) {
            throw new ApiException("Ayurveda history is already reviewed by doctor", HttpStatus.CONFLICT);
        }

        history.setStatus(AyurvedaHistoryStatus.PATIENT_CONFIRMED);
        history.setPatientConfirmed(true);
        history.setConfirmedAt(Instant.now());

        AyurvedaHistory saved = ayurvedaHistoryRepository.save(history);
        log.info("Patient confirmed Ayurveda history {} for session {}", saved.getId(), sessionId);

        // Audit log
        try {
            AuditLog auditLog = new AuditLog(
                    authenticatedUser != null ? authenticatedUser.getId() : null,
                    "AYURVEDA_CONFIRMED",
                    "AyurvedaHistory",
                    saved.getId(),
                    "{\"sessionId\":\"" + sessionId + "\",\"confirmedAt\":\"" + saved.getConfirmedAt() + "\"}"
            );
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.warn("Failed to create audit log for Ayurveda confirmation on session {}", sessionId, e);
        }

        return mapToResponse(saved);
    }

    // --- Implementation of architectural AyurvedaIntakeService interface ---

    @Override
    @Transactional
    public void saveAyurvedaIntake(UUID intakeSessionId, Map<String, Object> ayurvedaData) {
        IntakeSession session = getSessionOrThrow(intakeSessionId);
        Optional<AyurvedaHistory> existingOpt = ayurvedaHistoryRepository.findBySessionId(intakeSessionId);
        AyurvedaHistory history = existingOpt.orElseGet(() -> new AyurvedaHistory(session.getPatient(), session));

        try {
            if (ayurvedaData.containsKey("prakriti")) {
                history.setPrakritiData(objectMapper.writeValueAsString(ayurvedaData.get("prakriti")));
            }
            if (ayurvedaData.containsKey("nidana")) {
                history.setNidanaData(objectMapper.writeValueAsString(ayurvedaData.get("nidana")));
            }
            if (ayurvedaData.containsKey("pariksha")) {
                history.setParikshaData(objectMapper.writeValueAsString(ayurvedaData.get("pariksha")));
            }
            if (ayurvedaData.containsKey("notes")) {
                history.setNotes(String.valueOf(ayurvedaData.get("notes")));
            }
            if (history.getTerminologyData() == null) {
                history.setTerminologyData(objectMapper.writeValueAsString(createDefaultTerminology()));
            }
        } catch (Exception e) {
            log.error("Failed to serialize architectural Ayurveda data for session {}", intakeSessionId, e);
            throw new ApiException("Failed to save architectural Ayurveda data", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ayurvedaHistoryRepository.save(history);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAyurvedaIntake(UUID intakeSessionId) {
        AyurvedaHistory history = ayurvedaHistoryRepository.findBySessionId(intakeSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Ayurveda intake not found for session: " + intakeSessionId));

        Map<String, Object> result = new HashMap<>();
        result.put("id", history.getId());
        result.put("sessionId", intakeSessionId);
        result.put("status", history.getStatus().name());
        result.put("patientConfirmed", history.isPatientConfirmed());
        result.put("notes", history.getNotes());

        try {
            if (history.getPrakritiData() != null) {
                result.put("prakriti", objectMapper.readValue(history.getPrakritiData(), Map.class));
            }
            if (history.getNidanaData() != null) {
                result.put("nidana", objectMapper.readValue(history.getNidanaData(), Map.class));
            }
            if (history.getParikshaData() != null) {
                result.put("pariksha", objectMapper.readValue(history.getParikshaData(), Map.class));
            }
            if (history.getTerminologyData() != null) {
                result.put("terminology", objectMapper.readValue(history.getTerminologyData(), List.class));
            }
        } catch (Exception e) {
            log.error("Failed to deserialize architectural Ayurveda intake for session {}", intakeSessionId, e);
        }

        return result;
    }

    // --- Internal Helpers ---

    private IntakeSession getSessionOrThrow(UUID sessionId) {
        return intakeSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Intake session not found with id: " + sessionId));
    }

    private void validateSessionAccess(IntakeSession session, User user) {
        if (user == null) {
            throw new ApiException("Authentication required", HttpStatus.UNAUTHORIZED);
        }
        if (user.getRole() == UserRole.PATIENT) {
            Patient patient = session.getPatient();
            if (patient.getUserId() == null || !patient.getUserId().equals(user.getId())) {
                log.warn("Cross-patient access denied: User {} tried accessing session {}", user.getId(), session.getId());
                throw new ApiException("Access denied: You do not own this intake session", HttpStatus.FORBIDDEN);
            }
        }
    }

    private void verifySessionModifiable(IntakeSession session) {
        if (session.getStatus() == IntakeSessionStatus.COMPLETED) {
            throw new ApiException("Intake session is already completed and cannot be modified", HttpStatus.CONFLICT);
        }
        if (session.getStatus() == IntakeSessionStatus.EXPIRED) {
            throw new ApiException("Intake session is expired", HttpStatus.CONFLICT);
        }
    }

    private void verifyConsentGiven(UUID sessionId) {
        Optional<Consent> consentOpt = consentRepository
                .findFirstBySessionIdAndConsentTypeOrderByConsentedAtDesc(sessionId, ConsentType.GENERAL_INTAKE);
        if (consentOpt.isEmpty() || consentOpt.get().getConsentStatus() != ConsentStatus.ACCEPTED) {
            log.warn("Ayurveda intake operation blocked: Valid accepted GENERAL_INTAKE consent missing for session {}", sessionId);
            throw new ApiException("Clinical intake cannot proceed without valid consent", HttpStatus.BAD_REQUEST);
        }
    }

    private List<AyurvedaTerminologyDto> createDefaultTerminology() {
        List<AyurvedaTerminologyDto> terms = new ArrayList<>();
        terms.add(new AyurvedaTerminologyDto("Prakriti", "प्रकृति", "Constitutional body-mind typing"));
        terms.add(new AyurvedaTerminologyDto("Agni", "अग्नि", "Digestive and metabolic capacity"));
        terms.add(new AyurvedaTerminologyDto("Koshtha", "कोष्ठ", "Alimentary canal responsiveness and bowel motility"));
        terms.add(new AyurvedaTerminologyDto("Bala", "बल", "Physical stamina and immunity"));
        terms.add(new AyurvedaTerminologyDto("Jihva", "जिह्वा", "Tongue assessment indicating ama/metabolic state"));
        return terms;
    }

    private AyurvedaHistoryResponse mapToResponse(AyurvedaHistory history) {
        AyurvedaHistoryResponse response = new AyurvedaHistoryResponse();
        response.setId(history.getId());
        response.setPatientId(history.getPatient().getId());
        response.setSessionId(history.getSession().getId());
        response.setStatus(history.getStatus());
        response.setPatientConfirmed(history.isPatientConfirmed());
        response.setConfirmedAt(history.getConfirmedAt());
        response.setNotes(history.getNotes());
        response.setCreatedAt(history.getCreatedAt());
        response.setUpdatedAt(history.getUpdatedAt());

        try {
            if (history.getPrakritiData() != null) {
                response.setPrakriti(objectMapper.readValue(history.getPrakritiData(), PrakritiDataDto.class));
            }
            if (history.getNidanaData() != null) {
                response.setNidana(objectMapper.readValue(history.getNidanaData(), NidanaDataDto.class));
            }
            if (history.getParikshaData() != null) {
                response.setPariksha(objectMapper.readValue(history.getParikshaData(), ParikshaDataDto.class));
            }
            if (history.getTerminologyData() != null) {
                response.setTerminology(objectMapper.readValue(history.getTerminologyData(),
                        new TypeReference<List<AyurvedaTerminologyDto>>() {}));
            }
        } catch (Exception e) {
            log.warn("Failed to parse JSON fields for Ayurveda history {}", history.getId(), e);
        }

        return response;
    }
}
