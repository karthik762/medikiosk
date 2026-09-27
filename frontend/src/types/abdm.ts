export type AbhaStatus = 'NOT_PROVIDED' | 'UNVERIFIED' | 'VERIFIED';

export type IntegrationStatus = 'PENDING' | 'SUCCESS' | 'FAILED';

export interface AbdmStatus {
  mode: 'MOCK' | 'SANDBOX' | 'PRODUCTION';
  isMock: boolean;
  enabled: boolean;
  gatewayBaseUrl: string;
  connected: boolean;
  message: string;
  timestamp: string;
}

export interface AbhaVerificationRequest {
  abhaId: string;
}

export interface AbhaVerificationResponse {
  verified: boolean;
  status: AbhaStatus;
  maskedAbha: string | null;
  verificationMethod: string;
  isMock: boolean;
  message: string;
  verifiedAt: string | null;
}

export interface AbdmConsentRequest {
  patientId?: string;
  consentType?: string;
  purpose?: string;
  validMonths?: number;
}

export interface AbdmConsentResponse {
  consentId: string;
  patientId: string;
  sessionId: string;
  consentStatus: 'ACCEPTED' | 'DECLINED' | 'REVOKED';
  consentType: string;
  externalArtefactId: string;
  consentedAt: string;
  revokedAt: string | null;
  isMock: boolean;
  message: string;
}

export interface AbdmExchangeRequest {
  notes?: string;
  idempotencyKey?: string;
}

export interface AbdmExchangeResponse {
  integrationRecordId: string;
  sessionId: string;
  patientId: string;
  status: IntegrationStatus;
  transactionId: string | null;
  bundleId: string;
  idempotencyKey: string;
  isMock: boolean;
  attemptedAt: string;
  completedAt: string | null;
  errorMessage: string | null;
  message: string;
}

export interface IntegrationRecordItem {
  id: string;
  patientId: string;
  sessionId: string;
  integrationType: 'ABDM' | 'HIS' | 'HEALTHLAKE';
  operation: string;
  externalReference: string | null;
  idempotencyKey: string | null;
  status: IntegrationStatus;
  errorMessage: string | null;
  attemptCount: number;
  attemptedAt: string;
  completedAt: string | null;
}
