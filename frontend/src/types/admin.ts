export interface SystemAnalytics {
  totalIntakes: number;
  completedIntakes: number;
  inProgressIntakes: number;
  totalPatients: number;
  totalDoctors: number;
  avgIntakeDurationSeconds: number;
  triageDistribution: Record<string, number>;
  totalSummariesGenerated: number;
  totalSummariesVerified: number;
  totalDocumentsProcessed: number;
  fhirBundlesGenerated: number;
  abdmExchangesCount: number;
  hisExportsCount: number;
  healthLakeSyncsCount: number;
}

export interface AdminUser {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: 'PATIENT' | 'DOCTOR' | 'ADMIN';
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserRequest {
  email: string;
  password?: string;
  firstName: string;
  lastName: string;
  role: 'DOCTOR' | 'ADMIN';
}

export interface AuditLogDetail {
  id: string;
  userId?: string;
  userEmail?: string;
  userName?: string;
  action: string;
  entityType?: string;
  entityId?: string;
  metadata?: string;
  createdAt: string;
}

export interface SystemConfigSummary {
  environment: string;
  aiProviderMode: string;
  externalAiEnabled: boolean;
  abdmOperatingMode: string;
  abdmMock: boolean;
  hisOperatingMode: string;
  hisMock: boolean;
  healthLakeOperatingMode: string;
  healthLakeMock: boolean;
  databaseStatus: string;
  featureToggles: Record<string, boolean>;
}
