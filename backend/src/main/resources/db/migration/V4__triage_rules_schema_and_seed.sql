-- ==============================================================================
-- MediKiosk Database Migration V4: Triage Rules Schema & Approved Rule Bank
-- Conforming to docs/TRIAGE_RULES.md (Sections 6, 8, 33, 35, 37)
-- ==============================================================================

CREATE TABLE IF NOT EXISTS triage_rules (
    id VARCHAR(36) PRIMARY KEY,
    rule_id VARCHAR(64) NOT NULL UNIQUE,
    version VARCHAR(32) NOT NULL DEFAULT '1.0',
    name VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(32) NOT NULL,
    conditions LONGTEXT NOT NULL,
    enabled BOOLEAN DEFAULT true,
    effective_from TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_triage_rules_rule_id ON triage_rules(rule_id);
CREATE INDEX idx_triage_rules_enabled ON triage_rules(enabled);
CREATE INDEX idx_triage_rules_priority ON triage_rules(priority);

-- Insert baseline rules explicitly defined in docs/TRIAGE_RULES.md
INSERT INTO triage_rules (id, rule_id, version, name, description, priority, conditions, enabled, effective_from, created_at, updated_at)
VALUES
    (
        '33333333-3333-3333-3333-333333333301',
        'TRIAGE-001',
        '1.0',
        'Severe Symptom Intensity Flag',
        'Clinical screening rule triggering urgent routing when severe symptom intensity threshold (severity >= 8) is reported.',
        'URGENT',
        '[{"field": "symptom.severity", "operator": "GREATER_THAN_OR_EQUAL", "value": 8, "reason": "Configured red-flag condition detected: Severe symptom intensity (severity >= 8) reported"}]',
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        '33333333-3333-3333-3333-333333333302',
        'TRIAGE-002',
        '1.0',
        'Moderate Symptom Intensity Flag',
        'Clinical screening rule triggering priority clinical review when moderate symptom intensity threshold (severity >= 5) is reported.',
        'PRIORITY',
        '[{"field": "symptom.severity", "operator": "GREATER_THAN_OR_EQUAL", "value": 5, "reason": "Configured symptom threshold met: Moderate symptom intensity (severity >= 5) reported"}]',
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        '33333333-3333-3333-3333-333333333303',
        'TEST-001',
        '1.0',
        'Architecture Verification Flag',
        'Non-clinical test rule from docs/TRIAGE_RULES.md Section 35 verifying condition evaluation architecture.',
        'PRIORITY',
        '[{"field": "testFlag", "operator": "EQUALS", "value": true, "reason": "Test flag condition matched"}]',
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );
