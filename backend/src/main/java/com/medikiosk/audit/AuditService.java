package com.medikiosk.audit;

import java.util.UUID;

/**
 * Architectural interface for system, security, and clinical audit logging.
 * Conforms to docs/SECURITY.md and docs/DATABASE.md requirements.
 */
public interface AuditService {

    /**
     * Records an audit event for tracking and compliance.
     *
     * @param userId      ID of the user performing the action (if authenticated).
     * @param action      Standardized action code.
     * @param entityType  Target entity type.
     * @param entityId    Target entity identifier.
     * @param detailsJson Additional event details in JSON format.
     */
    void logEvent(UUID userId, String action, String entityType, String entityId, String detailsJson);
}
