import { IntegrationStatus } from './abdm';

export interface HealthLakeStatus {
  mode: string;
  isMock: boolean;
  enabled: boolean;
  endpoint: string | null;
  datastoreId: string | null;
  region: string | null;
  connected: boolean;
  message: string;
  timestamp: string;
}

export interface HealthLakeExportRequest {
  healthlakeRef?: string;
  datastoreId?: string;
  idempotencyKey?: string;
  notes?: string;
}

export interface HealthLakeExportResponse {
  integrationRecordId: string;
  sessionId: string;
  patientId: string;
  status: IntegrationStatus;
  healthlakeRef: string | null;
  bundleId: string;
  datastoreId: string | null;
  idempotencyKey: string;
  isMock: boolean;
  attemptedAt: string;
  completedAt: string | null;
  errorMessage: string | null;
  message: string;
}
