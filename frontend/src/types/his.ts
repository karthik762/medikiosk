import { IntegrationStatus } from './abdm';

export interface HisStatus {
  mode: string;
  isMock: boolean;
  enabled: boolean;
  baseUrl: string;
  connected: boolean;
  message: string;
  timestamp: string;
}

export interface HisPatientCandidate {
  hisId: string;
  displayName: string;
  phone: string;
  gender: string;
  dateOfBirth: string;
  department: string;
  roomOrWard: string;
}

export interface HisPatientSearchResponse {
  candidates: HisPatientCandidate[];
  totalFound: number;
  query: string;
  searchType: string;
  isMock: boolean;
  message: string;
}

export interface HisExportRequest {
  intakeQueue?: string;
  hisId?: string;
  idempotencyKey?: string;
  notes?: string;
}

export interface HisExportResponse {
  integrationRecordId: string;
  sessionId: string;
  patientId: string;
  status: IntegrationStatus;
  encounterReference: string | null;
  bundleId: string;
  hisId: string | null;
  intakeQueue: string;
  idempotencyKey: string;
  isMock: boolean;
  attemptedAt: string;
  completedAt: string | null;
  errorMessage: string | null;
  message: string;
}

export interface HisLinkPatientRequest {
  hisId: string;
  notes?: string;
}

export interface HisLinkPatientResponse {
  patientId: string;
  patientIdentifier: string;
  hisId: string;
  linkedAt: string;
  message: string;
}
