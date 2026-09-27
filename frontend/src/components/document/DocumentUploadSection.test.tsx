import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { DocumentUploadSection } from './DocumentUploadSection';
import { documentService } from '../../services/documentService';

vi.mock('../../services/documentService', () => ({
  documentService: {
    getSessionDocuments: vi.fn(),
    uploadDocument: vi.fn(),
    getDocumentDetails: vi.fn(),
  },
}));

describe('DocumentUploadSection Component', () => {
  const mockSessionId = '11111111-1111-1111-1111-111111111100';

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(documentService.getSessionDocuments).mockResolvedValue({
      success: true,
      data: [
        {
          id: 'doc-001',
          patientId: 'pat-001',
          sessionId: mockSessionId,
          documentType: 'PRESCRIPTION',
          fileName: 'rx_metformin.png',
          fileSize: 204800,
          processingStatus: 'COMPLETED',
          uploadedAt: '2026-09-11T12:00:00Z',
          extraction: {
            id: 'ext-001',
            documentId: 'doc-001',
            extractedText: 'Rx: Metformin 500mg\nTwice daily',
            confidenceScore: 0.95,
            processingEngine: 'MOCK_OCR_PROVIDER',
            status: 'SUCCESS',
            createdAt: '2026-09-11T12:00:01Z',
          },
        },
      ],
    });

    vi.mocked(documentService.uploadDocument).mockResolvedValue({
      success: true,
      message: 'Uploaded',
      data: {
        id: 'doc-002',
        patientId: 'pat-001',
        sessionId: mockSessionId,
        documentType: 'LAB_REPORT',
        fileName: 'glucose_test.pdf',
        fileSize: 512000,
        processingStatus: 'COMPLETED',
        uploadedAt: '2026-09-11T12:05:00Z',
        extraction: {
          id: 'ext-002',
          documentId: 'doc-002',
          extractedText: 'Fasting Blood Glucose: 110 mg/dL',
          confidenceScore: 0.96,
          processingEngine: 'MOCK_OCR_PROVIDER',
          status: 'SUCCESS',
          createdAt: '2026-09-11T12:05:01Z',
        },
      },
    });
  });

  it('renders header, non-diagnostic notice, document type selector, and existing documents', async () => {
    render(<DocumentUploadSection sessionId={mockSessionId} />);

    expect(screen.getByText(/Medical Document Intelligence & Scanning/i)).toBeInTheDocument();
    expect(screen.getByText(/MediKiosk is NOT an autonomous diagnostic system/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Document Category/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Choose Document/i)).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('rx_metformin.png')).toBeInTheDocument();
      expect(screen.getByText('COMPLETED')).toBeInTheDocument();
      expect(screen.getByText(/Rx: Metformin 500mg/i)).toBeInTheDocument();
    });
  });

  it('allows document type selection and file upload', async () => {
    const onUploadedMock = vi.fn();
    render(<DocumentUploadSection sessionId={mockSessionId} onDocumentUploaded={onUploadedMock} />);

    // Select LAB_REPORT
    const typeSelect = screen.getByLabelText(/Document Category/i);
    fireEvent.change(typeSelect, { target: { value: 'LAB_REPORT' } });
    expect(typeSelect).toHaveValue('LAB_REPORT');

    // Select file
    const file = new File(['%PDF-1.4 test'], 'glucose_test.pdf', { type: 'application/pdf' });
    const fileInput = screen.getByLabelText(/Choose Document/i);
    fireEvent.change(fileInput, { target: { files: [file] } });

    // Submit upload
    const uploadBtn = screen.getByRole('button', { name: /Upload & Process Document/i });
    fireEvent.click(uploadBtn);

    await waitFor(() => {
      expect(documentService.uploadDocument).toHaveBeenCalledWith(
        mockSessionId,
        file,
        'LAB_REPORT',
        undefined
      );
      expect(onUploadedMock).toHaveBeenCalledTimes(1);
    });

    expect(await screen.findByText(/Document uploaded and processed successfully/i)).toBeInTheDocument();
  });

  it('displays low-confidence warning for uncertain OCR extractions', async () => {
    vi.mocked(documentService.getSessionDocuments).mockResolvedValue({
      success: true,
      data: [
        {
          id: 'doc-low',
          patientId: 'pat-001',
          sessionId: mockSessionId,
          documentType: 'PRESCRIPTION',
          fileName: 'blurry_scan.jpg',
          fileSize: 102400,
          processingStatus: 'REVIEW_REQUIRED',
          uploadedAt: '2026-09-11T12:00:00Z',
          extraction: {
            id: 'ext-low',
            documentId: 'doc-low',
            extractedText: 'Illegible handwriting...',
            confidenceScore: 0.54,
            processingEngine: 'MOCK_OCR_PROVIDER',
            status: 'LOW_CONFIDENCE',
            createdAt: '2026-09-11T12:00:01Z',
          },
        },
      ],
    });

    render(<DocumentUploadSection sessionId={mockSessionId} />);

    await waitFor(() => {
      expect(screen.getByText('REVIEW_REQUIRED')).toBeInTheDocument();
      expect(screen.getByText(/Review Required:/i)).toBeInTheDocument();
      expect(screen.getByText(/OCR confidence is below threshold \(54%\)/i)).toBeInTheDocument();
    });
  });

  it('displays graceful fallback when OCR extraction fails while retaining source document', async () => {
    vi.mocked(documentService.getSessionDocuments).mockResolvedValue({
      success: true,
      data: [
        {
          id: 'doc-failed',
          patientId: 'pat-001',
          sessionId: mockSessionId,
          documentType: 'OTHER',
          fileName: 'corrupted_file.pdf',
          fileSize: 102400,
          processingStatus: 'FAILED',
          uploadedAt: '2026-09-11T12:00:00Z',
          extraction: {
            id: 'ext-failed',
            documentId: 'doc-failed',
            extractedText: '',
            confidenceScore: 0.0,
            processingEngine: 'MOCK_OCR_PROVIDER',
            status: 'FAILED',
            createdAt: '2026-09-11T12:00:01Z',
          },
        },
      ],
    });

    render(<DocumentUploadSection sessionId={mockSessionId} />);

    await waitFor(() => {
      expect(screen.getByText('FAILED')).toBeInTheDocument();
      expect(screen.getByText(/Text extraction unreadable:/i)).toBeInTheDocument();
      expect(screen.getByText(/Source document is safely stored and will be reviewed by the doctor/i)).toBeInTheDocument();
    });
  });
});
