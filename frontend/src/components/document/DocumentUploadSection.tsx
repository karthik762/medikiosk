import React, { useState, useEffect } from 'react';
import { DocumentType, DocumentUploadResponse } from '../../types/document';
import { documentService } from '../../services/documentService';

interface DocumentUploadSectionProps {
  sessionId: string;
  authToken?: string;
  onDocumentUploaded?: (document: DocumentUploadResponse) => void;
}

export const DocumentUploadSection: React.FC<DocumentUploadSectionProps> = ({
  sessionId,
  authToken,
  onDocumentUploaded,
}) => {
  const [documents, setDocuments] = useState<DocumentUploadResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [selectedType, setSelectedType] = useState<DocumentType>('PRESCRIPTION');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Load existing session documents
  useEffect(() => {
    let isMounted = true;
    async function loadDocuments() {
      setLoading(true);
      try {
        const res = await documentService.getSessionDocuments(sessionId, authToken);
        if (isMounted && res.data) {
          setDocuments(res.data);
        }
      } catch (err: unknown) {
        console.debug('No existing documents or unable to load:', err);
      } finally {
        if (isMounted) setLoading(false);
      }
    }

    if (sessionId) {
      loadDocuments();
    }
    return () => {
      isMounted = false;
    };
  }, [sessionId, authToken]);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setErrorMessage(null);
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      if (file.size > 10 * 1024 * 1024) {
        setErrorMessage('File exceeds maximum allowed upload size (10MB).');
        setSelectedFile(null);
        return;
      }
      setSelectedFile(file);
    }
  };

  const handleUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedFile) {
      setErrorMessage('Please select a file to upload.');
      return;
    }

    setUploading(true);
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      const res = await documentService.uploadDocument(sessionId, selectedFile, selectedType, authToken);
      if (res.data) {
        const newDoc = res.data;
        setDocuments((prev) => [newDoc, ...prev]);
        setSuccessMessage('Document uploaded and processed successfully.');
        setSelectedFile(null);
        // Reset file input
        const input = document.getElementById('document-file-input') as HTMLInputElement;
        if (input) input.value = '';

        if (onDocumentUploaded) {
          onDocumentUploaded(res.data);
        }
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to upload document.';
      setErrorMessage(msg);
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="bg-white rounded-xl shadow-md border border-slate-200 overflow-hidden my-6">
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-slate-800 to-indigo-900 text-white p-5">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <div className="flex items-center gap-2">
              <span className="text-2xl" role="img" aria-label="Document Icon">📄</span>
              <h2 className="text-xl font-bold tracking-tight">Medical Document Intelligence & Scanning</h2>
            </div>
            <p className="text-xs text-slate-200 mt-1">
              Upload prescriptions, laboratory reports, or discharge documents for OCR extraction and doctor review.
            </p>
          </div>
          <span className="px-3 py-1 text-xs font-semibold rounded-full bg-indigo-700 text-indigo-100 border border-indigo-500">
            Phase 8 • OCR Active
          </span>
        </div>
      </div>

      {/* Non-diagnostic clinical notice */}
      <div className="bg-slate-50 border-b border-slate-200 p-3 px-5 text-xs text-slate-800 flex items-start gap-2">
        <span className="font-bold text-slate-900 uppercase tracking-wide">Clinical Notice:</span>
        <span>
          MediKiosk is NOT an autonomous diagnostic system. Document text and extracted values are recorded for the
          consulting doctor. The doctor remains the final clinical decision-maker.
        </span>
      </div>

      {/* Messages */}
      {errorMessage && (
        <div className="bg-rose-50 border-l-4 border-rose-500 p-3 mx-5 mt-4 text-rose-800 text-sm" role="alert">
          {errorMessage}
        </div>
      )}
      {successMessage && (
        <div className="bg-emerald-50 border-l-4 border-emerald-500 p-3 mx-5 mt-4 text-emerald-800 text-sm" role="status">
          {successMessage}
        </div>
      )}

      {/* Upload Form */}
      <div className="p-6 border-b border-slate-100">
        <form onSubmit={handleUpload} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label htmlFor="document-type-select" className="block text-xs font-semibold text-gray-700 mb-1">
                Document Category
              </label>
              <select
                id="document-type-select"
                value={selectedType}
                onChange={(e) => setSelectedType(e.target.value as DocumentType)}
                className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-indigo-500 focus:border-indigo-500"
              >
                <option value="PRESCRIPTION">Prescription (Rx)</option>
                <option value="LAB_REPORT">Laboratory / Diagnostic Report</option>
                <option value="MEDICAL_RECORD">Previous Medical Record</option>
                <option value="DISCHARGE_SUMMARY">Hospital Discharge Summary</option>
                <option value="OTHER">Other Clinical Document</option>
              </select>
            </div>

            <div>
              <label htmlFor="document-file-input" className="block text-xs font-semibold text-gray-700 mb-1">
                Choose Document (PDF, JPEG, PNG, WebP — max 10MB)
              </label>
              <input
                id="document-file-input"
                type="file"
                accept=".pdf,.png,.jpg,.jpeg,.webp,application/pdf,image/png,image/jpeg,image/webp"
                onChange={handleFileChange}
                className="w-full border border-gray-300 rounded-lg p-1.5 text-sm file:mr-3 file:py-1 file:px-3 file:rounded-md file:border-0 file:text-xs file:font-semibold file:bg-indigo-50 file:text-indigo-700 hover:file:bg-indigo-100"
              />
            </div>
          </div>

          <div className="flex justify-end pt-2">
            <button
              type="submit"
              id="btn-upload-document"
              disabled={uploading || !selectedFile}
              className="px-6 py-2 bg-indigo-700 text-white rounded-lg text-sm font-semibold hover:bg-indigo-800 transition shadow-sm disabled:opacity-50 flex items-center gap-2"
            >
              {uploading ? (
                <>
                  <span className="animate-spin text-xs">⏳</span>
                  Processing OCR...
                </>
              ) : (
                'Upload & Process Document'
              )}
            </button>
          </div>
        </form>
      </div>

      {/* Uploaded Documents List */}
      <div className="p-6">
        <h3 className="text-sm font-bold text-gray-800 mb-3">
          Session Uploaded Documents ({documents.length})
        </h3>

        {loading ? (
          <div className="text-center py-6 text-gray-500 text-sm">Loading documents...</div>
        ) : documents.length === 0 ? (
          <div className="text-center py-8 border-2 border-dashed border-gray-200 rounded-lg text-gray-400 text-xs">
            No medical documents uploaded for this session yet.
          </div>
        ) : (
          <div className="space-y-4">
            {documents.map((doc) => (
              <div
                key={doc.id}
                className="border border-gray-200 rounded-lg p-4 bg-gray-50 hover:bg-white transition"
              >
                <div className="flex flex-wrap items-center justify-between gap-2 border-b border-gray-200 pb-2 mb-3">
                  <div>
                    <span className="font-semibold text-sm text-gray-800">{doc.fileName}</span>
                    <span className="ml-2 text-xs text-gray-500">
                      ({doc.documentType} • {doc.fileSize ? `${Math.round(doc.fileSize / 1024)} KB` : 'Unknown size'})
                    </span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span
                      className={`px-2.5 py-0.5 text-xs font-semibold rounded-full border ${
                        doc.processingStatus === 'COMPLETED'
                          ? 'bg-emerald-100 text-emerald-800 border-emerald-300'
                          : doc.processingStatus === 'REVIEW_REQUIRED'
                          ? 'bg-amber-100 text-amber-800 border-amber-300'
                          : doc.processingStatus === 'FAILED'
                          ? 'bg-rose-100 text-rose-800 border-rose-300'
                          : 'bg-blue-100 text-blue-800 border-blue-300'
                      }`}
                    >
                      {doc.processingStatus}
                    </span>
                  </div>
                </div>

                {/* Extraction View */}
                {doc.extraction ? (
                  <div className="space-y-2">
                    {doc.extraction.status === 'LOW_CONFIDENCE' && (
                      <div className="bg-amber-50 border-l-4 border-amber-400 p-2 text-xs text-amber-800">
                        <strong>Review Required:</strong> OCR confidence is below threshold ({Math.round((doc.extraction.confidenceScore || 0) * 100)}%). Your doctor will inspect the original source document directly.
                      </div>
                    )}

                    {doc.extraction.status === 'FAILED' && (
                      <div className="bg-rose-50 border-l-4 border-rose-400 p-2 text-xs text-rose-800">
                        <strong>Text extraction unreadable:</strong> Source document is safely stored and will be reviewed by the doctor during consultation.
                      </div>
                    )}

                    {doc.extraction.extractedText && (
                      <div className="bg-white p-3 rounded border border-gray-200 text-xs font-mono text-gray-700 whitespace-pre-line max-h-36 overflow-y-auto">
                        {doc.extraction.extractedText}
                      </div>
                    )}

                    <div className="flex flex-wrap items-center justify-between text-xs text-gray-500 pt-1">
                      <span>Engine: {doc.extraction.processingEngine || 'OCR'}</span>
                      {doc.extraction.confidenceScore !== undefined && (
                        <span>Confidence: {Math.round(doc.extraction.confidenceScore * 100)}%</span>
                      )}
                    </div>
                  </div>
                ) : (
                  <div className="text-xs text-gray-500 italic">Processing document extraction...</div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
