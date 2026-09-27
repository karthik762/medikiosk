export interface FhirValidationResponse {
  valid: boolean;
  errors: string[];
  warnings: string[];
  totalChecked: number;
}

export interface FhirExportSummary {
  sessionId: string;
  patientId: string;
  bundleId: string;
  bundleType: string;
  totalResources: number;
  resourceTypeCounts: Record<string, number>;
  verificationStatus: 'VERIFIED' | 'UNVERIFIED';
  compositionStatus: 'final' | 'preliminary';
  validationStatus: 'VALID' | 'WARNING' | 'INVALID';
  exportedAt: string;
  fhirBundleJson: string;
}
