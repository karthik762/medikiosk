export interface ClinicalCoding {
  system: string;
  code: string;
  display?: string;
  confidence?: number;
  source?: string;
}

export interface DualCodedConcept {
  concept: string;
  clinicalConceptCode?: string;
  coding: ClinicalCoding[];
  status?: string;
  confidence?: number;
  source?: string;
  verified?: boolean;
}

export interface PatientInformation {
  patientId: string;
  patientIdentifier?: string;
  firstName?: string;
  lastName?: string;
  age?: number;
  gender?: string;
  phone?: string;
  email?: string;
  sessionId?: string;
}

export interface StructuredSymptom {
  name: string;
  severity: number;
  duration?: string;
  onset?: string;
  frequency?: string;
  location?: string;
  source: string;
  clinicalConceptCode?: string;
  coding?: ClinicalCoding[];
  confidence?: number;
  status?: string;
  verified?: boolean;
}

export interface StructuredHistory {
  category: string;
  description: string;
  source: string;
  verified?: boolean;
  clinicalConceptCode?: string;
  coding?: ClinicalCoding[];
  confidence?: number;
  status?: string;
}

export interface StructuredMedication {
  medicationName: string;
  dosage?: string;
  frequency?: string;
  duration?: string;
  source: string;
  conflictFlag?: boolean;
  verificationNotes?: string;
  clinicalConceptCode?: string;
  coding?: ClinicalCoding[];
  confidence?: number;
  status?: string;
  verified?: boolean;
}

export interface StructuredAllergy {
  allergen: string;
  reaction?: string;
  severity?: string;
  source: string;
  clinicalConceptCode?: string;
  coding?: ClinicalCoding[];
  confidence?: number;
  status?: string;
  verified?: boolean;
}

export interface StructuredAyurveda {
  prakriti?: string;
  vikriti?: string;
  agni?: string;
  symptoms?: string[];
  documentedObservations?: string[];
  source?: string;
  verified?: boolean;
  confidence?: number;

  prakritiAssessment?: string;
  agniType?: string;
  kosthaType?: string;
  nidanaFactors?: string;
  prakritiResponses?: Record<string, unknown>;
  nidanaResponses?: Record<string, unknown>;
  parikshaResponses?: Record<string, unknown>;
}

export interface DocumentFinding {
  documentId: string;
  documentType: string;
  fileName: string;
  findings?: string;
  confidence?: number;
  uncertaintyWarning?: string;
  uncertaintyFlags?: string[];
  extractionData?: Record<string, unknown>;
  extractedTextPreview?: string;
  source?: string;
}

export interface TriageSummary {
  priority: 'ROUTINE' | 'PRIORITY' | 'URGENT' | 'EMERGENCY' | 'STABLE';
  summaryReason?: string;
  explanation?: string;
  triggeredRules?: any[];
  source?: string;
}

export interface MissingInformation {
  category?: string;
  field?: string;
  description?: string;
  reason?: string;
  clinicalRelevance?: string;
}

export interface ClinicalConflict {
  field?: string;
  patientStated?: string;
  documentStated?: string;
  conflictDescription?: string;
  resolutionStatus?: 'PENDING_REVIEW' | 'RESOLVED' | 'DISMISSED';
}

export interface DoctorVerification {
  status: 'PENDING' | 'VERIFIED' | 'AMENDED' | 'REJECTED';
  verifiedBy?: string;
  verifiedByName?: string;
  verifiedAt?: string;
  amendments?: string;
  comments?: string;
  clinicalNotes?: string;
}

export interface SummaryMetadata {
  generationModel?: string;
  providerName?: string;
  modelIdentifier?: string;
  generatedAt?: string;
  isMock?: boolean;
  version?: number;
  disclaimer?: string;
}

export interface ClinicalSummary {
  id: string;
  sessionId: string;
  patientId: string;
  patientInfo?: PatientInformation;
  chiefComplaint: string;
  symptoms: StructuredSymptom[];
  medicalHistory: StructuredHistory[];
  medications: StructuredMedication[];
  allergies: StructuredAllergy[];
  ayurveda?: StructuredAyurveda;
  ayurvedaSummary?: StructuredAyurveda;
  documentFindings: DocumentFinding[];
  triage?: TriageSummary;
  dualCodedConcepts?: DualCodedConcept[];
  missingInformation: MissingInformation[];
  conflicts?: ClinicalConflict[];
  potentialConflicts?: ClinicalConflict[];
  doctorVerification: DoctorVerification;
  metadata: SummaryMetadata;
  status: 'AI_GENERATED' | 'DOCTOR_REVIEWED' | 'VERIFIED' | 'DOCTOR_VERIFIED' | 'REJECTED';
  humanReadableSummary?: string;
}
