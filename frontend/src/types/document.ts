export type DocumentType =
  | 'PRESCRIPTION'
  | 'LAB_REPORT'
  | 'MEDICAL_RECORD'
  | 'DISCHARGE_SUMMARY'
  | 'OTHER';

export type DocumentProcessingStatus =
  | 'UPLOADED'
  | 'PROCESSING'
  | 'COMPLETED'
  | 'REVIEW_REQUIRED'
  | 'FAILED';

export type ExtractionStatus = 'SUCCESS' | 'LOW_CONFIDENCE' | 'FAILED';

export interface ExtractionResult {
  id: string;
  documentId: string;
  extractedText?: string;
  extractionData?: Record<string, unknown>;
  confidenceScore?: number;
  processingEngine?: string;
  status: ExtractionStatus;
  uncertaintyFlags?: string[];
  createdAt: string;
}

export interface DocumentUploadResponse {
  id: string;
  patientId: string;
  sessionId?: string;
  documentType: DocumentType;
  fileName: string;
  fileType?: string;
  fileSize?: number;
  fileHash?: string;
  processingStatus: DocumentProcessingStatus;
  uploadedAt: string;
  processedAt?: string;
  extraction?: ExtractionResult;
}
