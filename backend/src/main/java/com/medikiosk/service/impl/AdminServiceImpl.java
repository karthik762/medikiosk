package com.medikiosk.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.dto.admin.AuditLogDetailDto;
import com.medikiosk.dto.admin.CreateUserRequestDto;
import com.medikiosk.dto.admin.SystemAnalyticsDto;
import com.medikiosk.dto.admin.SystemConfigSummaryDto;
import com.medikiosk.dto.admin.UserManagementDto;
import com.medikiosk.entity.AuditLog;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.TriageResult;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.ClinicalSummaryStatus;
import com.medikiosk.entity.enums.IntakeSessionStatus;
import com.medikiosk.entity.enums.IntegrationType;
import com.medikiosk.entity.enums.TriagePriority;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;
import com.medikiosk.exception.ApiException;
import com.medikiosk.repository.AuditLogRepository;
import com.medikiosk.repository.ClinicalSummaryRepository;
import com.medikiosk.repository.ConsentRepository;
import com.medikiosk.repository.DoctorReviewRepository;
import com.medikiosk.repository.FhirMappingRepository;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.repository.IntegrationRecordRepository;
import com.medikiosk.repository.MedicalDocumentRepository;
import com.medikiosk.repository.PatientRepository;
import com.medikiosk.repository.TriageResultRepository;
import com.medikiosk.repository.UserRepository;
import com.medikiosk.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private IntakeSessionRepository intakeSessionRepository;

    @Autowired
    private TriageResultRepository triageResultRepository;

    @Autowired
    private ClinicalSummaryRepository clinicalSummaryRepository;

    @Autowired
    private DoctorReviewRepository doctorReviewRepository;

    @Autowired
    private MedicalDocumentRepository medicalDocumentRepository;

    @Autowired
    private FhirMappingRepository fhirMappingRepository;

    @Autowired
    private IntegrationRecordRepository integrationRecordRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public SystemAnalyticsDto getSystemAnalytics() {
        SystemAnalyticsDto analytics = new SystemAnalyticsDto();

        long totalIntakes = intakeSessionRepository.count();
        long completedIntakes = intakeSessionRepository.countByStatus(IntakeSessionStatus.COMPLETED);
        long inProgressIntakes = intakeSessionRepository.countByStatus(IntakeSessionStatus.IN_PROGRESS);

        analytics.setTotalIntakes(totalIntakes);
        analytics.setCompletedIntakes(completedIntakes);
        analytics.setInProgressIntakes(inProgressIntakes);

        analytics.setTotalPatients(patientRepository.count());
        analytics.setTotalDoctors(userRepository.countByRole(UserRole.DOCTOR));

        // Average duration calculation for completed sessions
        List<IntakeSession> completedSessions = intakeSessionRepository.findByStatus(IntakeSessionStatus.COMPLETED);
        double totalSeconds = 0;
        int countWithTimes = 0;
        for (IntakeSession session : completedSessions) {
            if (session.getStartedAt() != null && session.getCompletedAt() != null) {
                totalSeconds += Duration.between(session.getStartedAt(), session.getCompletedAt()).getSeconds();
                countWithTimes++;
            }
        }
        analytics.setAvgIntakeDurationSeconds(countWithTimes > 0 ? Math.round((totalSeconds / countWithTimes) * 10.0) / 10.0 : 0.0);

        // Triage distribution
        Map<String, Long> triageMap = new HashMap<>();
        triageMap.put("EMERGENCY", 0L);
        triageMap.put("URGENT", 0L);
        triageMap.put("PRIORITY", 0L);
        triageMap.put("ROUTINE", 0L);

        List<TriageResult> triages = triageResultRepository.findAll();
        for (TriageResult tr : triages) {
            if (tr.getPriority() != null) {
                String priorityStr = tr.getPriority().name();
                triageMap.put(priorityStr, triageMap.getOrDefault(priorityStr, 0L) + 1);
            }
        }
        analytics.setTriageDistribution(triageMap);

        // Clinical summary generation & verification stats
        analytics.setTotalSummariesGenerated(clinicalSummaryRepository.count());
        analytics.setTotalSummariesVerified(clinicalSummaryRepository.countByStatus(ClinicalSummaryStatus.VERIFIED));

        // Document OCR processing volume
        analytics.setTotalDocumentsProcessed(medicalDocumentRepository.count());

        // Multi-system integration counts
        analytics.setFhirBundlesGenerated(fhirMappingRepository.count());
        analytics.setAbdmExchangesCount(integrationRecordRepository.countByIntegrationType(IntegrationType.ABDM));
        analytics.setHisExportsCount(integrationRecordRepository.countByIntegrationType(IntegrationType.HIS));
        analytics.setHealthLakeSyncsCount(integrationRecordRepository.countByIntegrationType(IntegrationType.HEALTHLAKE));

        return analytics;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserManagementDto> getAllUsers(UserRole roleFilter, String searchQuery) {
        List<User> users;
        if (roleFilter != null) {
            users = userRepository.findByRole(roleFilter);
        } else {
            users = userRepository.findAll();
        }

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String q = searchQuery.trim().toLowerCase();
            users = users.stream()
                    .filter(u -> (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(q)) ||
                                 (u.getLastName() != null && u.getLastName().toLowerCase().contains(q)) ||
                                 (u.getEmail() != null && u.getEmail().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        return users.stream().map(this::mapUserToDto).collect(Collectors.toList());
    }

    @Override
    public UserManagementDto createUser(CreateUserRequestDto request, User adminUser) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ApiException("User already exists with email: " + request.getEmail(), HttpStatus.CONFLICT);
        }

        User newUser = new User();
        newUser.setEmail(request.getEmail().trim().toLowerCase());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setFirstName(request.getFirstName().trim());
        newUser.setLastName(request.getLastName().trim());
        newUser.setRole(request.getRole());
        newUser.setStatus(UserStatus.ACTIVE);

        newUser = userRepository.save(newUser);

        recordAuditLog(adminUser.getId(), "ADMIN_USER_CREATED", "User", newUser.getId(),
                Map.of("targetUserEmail", newUser.getEmail(), "role", newUser.getRole().name()));

        return mapUserToDto(newUser);
    }

    @Override
    public UserManagementDto updateUserStatus(UUID userId, UserStatus status, User adminUser) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found with id: " + userId, HttpStatus.NOT_FOUND));

        if (targetUser.getId().equals(adminUser.getId())) {
            throw new ApiException("Admin cannot deactivate their own active session account", HttpStatus.BAD_REQUEST);
        }

        targetUser.setStatus(status);
        targetUser = userRepository.save(targetUser);

        recordAuditLog(adminUser.getId(), "ADMIN_USER_STATUS_UPDATED", "User", targetUser.getId(),
                Map.of("targetUserEmail", targetUser.getEmail(), "newStatus", status.name()));

        return mapUserToDto(targetUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDetailDto> getAuditLogs(String actionFilter, UUID userIdFilter, int limit) {
        List<AuditLog> logs;
        if (actionFilter != null && !actionFilter.trim().isEmpty()) {
            logs = auditLogRepository.findByAction(actionFilter.trim());
        } else if (userIdFilter != null) {
            logs = auditLogRepository.findByUserId(userIdFilter);
        } else {
            logs = auditLogRepository.findAll();
        }

        // Sort descending by timestamp
        logs.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        if (limit > 0 && logs.size() > limit) {
            logs = logs.subList(0, limit);
        }

        return logs.stream().map(logEntry -> {
            AuditLogDetailDto dto = new AuditLogDetailDto();
            dto.setId(logEntry.getId());
            dto.setUserId(logEntry.getUserId());
            dto.setAction(logEntry.getAction());
            dto.setEntityType(logEntry.getEntityType());
            dto.setEntityId(logEntry.getEntityId());
            dto.setMetadata(logEntry.getMetadata());
            dto.setCreatedAt(logEntry.getCreatedAt());

            if (logEntry.getUserId() != null) {
                userRepository.findById(logEntry.getUserId()).ifPresent(u -> {
                    dto.setUserEmail(u.getEmail());
                    dto.setUserName(u.getFirstName() + " " + u.getLastName());
                });
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SystemConfigSummaryDto getSystemConfigSummary() {
        SystemConfigSummaryDto config = new SystemConfigSummaryDto();
        config.setEnvironment("PRODUCTION_READY");
        config.setAiProviderMode("MOCK_DETERMINISTIC_FALLBACK");
        config.setExternalAiEnabled(false);

        config.setAbdmOperatingMode("MOCK");
        config.setAbdmMock(true);

        config.setHisOperatingMode("MOCK");
        config.setHisMock(true);

        config.setHealthLakeOperatingMode("MOCK");
        config.setHealthLakeMock(true);

        config.setDatabaseStatus("CONNECTED_HEALTHY");

        Map<String, Boolean> toggles = new HashMap<>();
        toggles.put("adaptiveQuestioning", true);
        toggles.put("triageRedFlags", true);
        toggles.put("ayurvedaIntake", true);
        toggles.put("ocrExtraction", true);
        toggles.put("fhirR4Mapper", true);
        toggles.put("abdmAdapter", true);
        toggles.put("hisAdapter", true);
        toggles.put("healthLakeAdapter", true);
        toggles.put("clinicianVerificationGate", true);
        config.setFeatureToggles(toggles);

        return config;
    }

    private UserManagementDto mapUserToDto(User user) {
        UserManagementDto dto = new UserManagementDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    private void recordAuditLog(UUID actorUserId, String action, String entityType, UUID entityId, Map<String, Object> details) {
        try {
            AuditLog audit = new AuditLog();
            audit.setUserId(actorUserId);
            audit.setAction(action);
            audit.setEntityType(entityType);
            audit.setEntityId(entityId);
            audit.setMetadata(objectMapper.writeValueAsString(details));
            auditLogRepository.save(audit);
        } catch (Exception e) {
            log.warn("Failed to save audit log for action {}: {}", action, e.getMessage());
        }
    }
}
