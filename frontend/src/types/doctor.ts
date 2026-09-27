import { ClinicalSummary } from './clinicalSummary';
import { AyurvedaHistoryResponse } from './ayurveda';
import { DocumentUploadResponse } from './document';

export type TriagePriority = 'EMERGENCY' | 'URGENT' | 'PRIORITY' | 'ROUTINE' | 'STABLE';

export type ClinicalSummaryStatus = 'AI_GENERATED' | 'DOCTOR_REVIEWED' | 'VERIFIED';

export type IntakeSessionStatus =
  | 'CREATED'
  | 'IN_PROGRESS'
  | 'PROCESSING'
  | 'COMPLETED'
  | 'EXPIRED'
  | 'ABANDONED';

export type DoctorReviewAction = 'REVIEWED' | 'EDITED' | 'VERIFIED';

export interface DoctorQueueItem {
  sessionId: string;
  patientId: string;
  patientIdentifier: string;
  patientName: string;
  patientAge?: number;
  patientGender?: string;
  chiefComplaint?: string;
  sessionStatus: IntakeSessionStatus;
  triagePriority?: TriagePriority;
  triageReason?: string;
  summaryStatus?: ClinicalSummaryStatus;
  reviewStatus: 'PENDING_INTAKE' | 'READY_FOR_REVIEW' | 'IN_REVIEW' | 'VERIFIED';
  sessionCreatedAt: string;
  sessionUpdatedAt?: string;
  urgent: boolean;
}

export interface PatientAnswerRecord {
  questionId?: string;
  questionCode: string;
  questionText: string;
  answerText: string;
  inputMethod: string;
  originalTranscription?: string;
  answeredAt: string;
}

export interface OriginalPatientData {
  chiefComplaint: string;
  intakeAnswers: PatientAnswerRecord[];
  symptoms: Array<{
    name: string;
    severity: number;
    duration?: string;
    onset?: string;
    location?: string;
    frequency?: string;
    source: string;
  }>;
  medicalHistory: Array<{
    category: string;
    description: string;
    source: string;
  }>;
  medications: Array<{
    medicationName: string;
    dosage?: string;
    frequency?: string;
    duration?: string;
    source: string;
  }>;
  allergies: Array<{
    allergen: string;
    reaction?: string;
    severity?: string;
    source: string;
  }>;
}

export interface DoctorReviewHistory {
  id: string;
  action: DoctorReviewAction;
  doctorName?: string;
  doctorEmail?: string;
  summaryVersion: number;
  comments?: string;
  amendments?: string;
  createdAt: string;
  verifiedAt?: string;
}

export interface DoctorReviewDetail {
  sessionId: string;
  patientId: string;
  patientIdentifier: string;
  patientName: string;
  dateOfBirth?: string;
  age?: number;
  gender?: string;
  phone?: string;
  sessionStatus: IntakeSessionStatus;
  sessionCreatedAt: string;
  sessionUpdatedAt?: string;
  triage?: {
    priority: TriagePriority;
    explanation: string;
    triggeredRules?: any[];
    source: string;
  };
  clinicalSummary?: ClinicalSummary;
  originalPatientData: OriginalPatientData;
  ayurvedaHistory?: AyurvedaHistoryResponse;
  documents: DocumentUploadResponse[];
  reviewHistory: DoctorReviewHistory[];
  currentVersion: number;
  currentStatus?: ClinicalSummaryStatus;
  verified: boolean;
  aiSummaryAvailable: boolean;
}

export interface DoctorAmendmentRequest {
  expectedVersion: number;
  amendedSummary: Partial<ClinicalSummary>;
  clinicalNotes?: string;
  conflictResolutions?: Record<string, string>;
}

export interface DoctorVerificationRequest {
  expectedVersion: number;
  clinicalNotes?: string;
  confirmUnderstood: boolean;
}

export interface DoctorInteroperabilityStatus {
  sessionId: string;
  patientId: string;
  sessionLocked: boolean;
  clinicalSummaryStatus: string;
  fhirStatus: string;
  fhirBundleId?: string;
  abdmStatus: string;
  abdmTransactionId?: string;
  hisStatus: string;
  hisEncounterRef?: string;
  healthLakeStatus: string;
  healthLakeResourceRef?: string;
  lastSyncedAt?: string;
  adapterStatuses?: Record<string, string>;
}

