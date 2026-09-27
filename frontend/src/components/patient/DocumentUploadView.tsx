import React, { useState, useRef, useEffect } from 'react';
import { KioskButton } from './KioskButton';
import { getTranslation } from '../../i18n/translations';
import { DocumentType, DocumentUploadResponse } from '../../types/document';
import { documentService } from '../../services/documentService';

export interface UploadedDoc {
  id?: string;
  name: string;
  type: string;
  status: 'UPLOADED' | 'PROCESSING' | 'COMPLETED' | 'REVIEW_REQUIRED' | 'FAILED';
  extractedSummary?: string;
  confidence: number;
  fileSize?: number;
  error?: string;
}

interface DocumentUploadViewProps {
  onSubmit: (docs: UploadedDoc[]) => Promise<void>;
  onBack: () => void;
  loading?: boolean;
  language?: 'en' | 'hi' | 'te';
  sessionId?: string;
  authToken?: string;
}

function dataURLtoFile(dataurl: string, filename: string): File {
  const arr = dataurl.split(',');
  const mimeMatch = arr[0].match(/:(.*?);/);
  const mime = mimeMatch ? mimeMatch[1] : 'image/jpeg';
  const bstr = atob(arr[1]);
  let n = bstr.length;
  const u8arr = new Uint8Array(n);
  while (n--) {
    u8arr[n] = bstr.charCodeAt(n);
  }
  return new File([u8arr], filename, { type: mime });
}

export const DocumentUploadView: React.FC<DocumentUploadViewProps> = ({
  onSubmit,
  onBack,
  loading = false,
  language = 'en',
  sessionId,
  authToken,
}) => {
  const [documents, setDocuments] = useState<UploadedDoc[]>([]);
  const [isUploading, setIsUploading] = useState(false);
  const [fetchingDocs, setFetchingDocs] = useState(false);
  const [docType, setDocType] = useState<DocumentType>('PRESCRIPTION');
  const [uploadError, setUploadError] = useState<string | null>(null);

  // Live Camera State
  const [isCameraOpen, setIsCameraOpen] = useState(false);
  const [cameraStream, setCameraStream] = useState<MediaStream | null>(null);
  const [capturedDataUrl, setCapturedDataUrl] = useState<string | null>(null);
  const [cameraError, setCameraError] = useState<string | null>(null);

  const videoRef = useRef<HTMLVideoElement | null>(null);
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const t = (key: Parameters<typeof getTranslation>[1]) => getTranslation(language, key);

  // Load existing session documents from backend on mount
  useEffect(() => {
    let isMounted = true;
    async function loadExistingDocuments() {
      if (!sessionId) return;
      setFetchingDocs(true);
      try {
        const res = await documentService.getSessionDocuments(sessionId, authToken);
        if (isMounted && res.data) {
          const mappedDocs: UploadedDoc[] = res.data.map((doc: DocumentUploadResponse) => ({
            id: doc.id,
            name: doc.fileName,
            type: doc.documentType,
            status: doc.processingStatus,
            extractedSummary: doc.extraction?.extractedText || '',
            confidence: doc.extraction?.confidenceScore ?? (doc.processingStatus === 'COMPLETED' ? 0.95 : 0.5),
            fileSize: doc.fileSize,
          }));
          setDocuments(mappedDocs);
        }
      } catch (err) {
        console.debug('No existing session documents loaded:', err);
      } finally {
        if (isMounted) setFetchingDocs(false);
      }
    }
    loadExistingDocuments();
    return () => {
      isMounted = false;
    };
  }, [sessionId, authToken]);

  // Attach camera stream to video element when modal opens
  useEffect(() => {
    if (isCameraOpen && cameraStream && videoRef.current) {
      videoRef.current.srcObject = cameraStream;
    }
  }, [isCameraOpen, cameraStream]);

  // Clean up camera stream on unmount
  useEffect(() => {
    return () => {
      stopCameraStream();
    };
  }, []);

  const stopCameraStream = () => {
    if (cameraStream) {
      cameraStream.getTracks().forEach((track) => track.stop());
      setCameraStream(null);
    }
  };

  const handleOpenCamera = async () => {
    setCameraError(null);
    setCapturedDataUrl(null);
    setUploadError(null);
    try {
      if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        throw new Error('Camera API not supported in this browser environment.');
      }
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment', width: { ideal: 1280 }, height: { ideal: 720 } },
        audio: false,
      });
      setCameraStream(stream);
      setIsCameraOpen(true);
    } catch (err: any) {
      setCameraError(t('cameraUnavailable'));
    }
  };

  const handleCaptureFrame = () => {
    if (videoRef.current && canvasRef.current) {
      const video = videoRef.current;
      const canvas = canvasRef.current;
      canvas.width = video.videoWidth || 640;
      canvas.height = video.videoHeight || 480;
      const ctx = canvas.getContext('2d');
      if (ctx) {
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
        const dataUrl = canvas.toDataURL('image/jpeg', 0.85);
        setCapturedDataUrl(dataUrl);
        stopCameraStream();
      }
    }
  };

  const handleRetakePhoto = async () => {
    setCapturedDataUrl(null);
    await handleOpenCamera();
  };

  const processFile = async (file: File) => {
    setUploadError(null);
    // 1. Validate file size (max 10MB)
    if (file.size > 10 * 1024 * 1024) {
      setUploadError('File exceeds maximum allowed upload size (10MB).');
      return;
    }

    // 2. Validate file type
    const allowedMIMEs = [
      'image/jpeg',
      'image/png',
      'image/webp',
      'application/pdf',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    ];
    const ext = file.name.split('.').pop()?.toLowerCase();
    const allowedExts = ['pdf', 'png', 'jpg', 'jpeg', 'webp', 'doc', 'docx'];

    if (!allowedMIMEs.includes(file.type) && (!ext || !allowedExts.includes(ext))) {
      setUploadError('Unsupported document format. Please select PDF, PNG, JPG, or DOC.');
      return;
    }

    setIsUploading(true);
    try {
      if (sessionId) {
        const response = await documentService.uploadDocument(sessionId, file, docType, authToken);
        if (response.data) {
          const doc = response.data;
          const newDoc: UploadedDoc = {
            id: doc.id,
            name: doc.fileName,
            type: doc.documentType,
            status: doc.processingStatus,
            extractedSummary: doc.extraction?.extractedText || '',
            confidence: doc.extraction?.confidenceScore ?? (doc.processingStatus === 'COMPLETED' ? 0.95 : 0.5),
            fileSize: doc.fileSize,
          };
          setDocuments((prev) => [newDoc, ...prev]);
        }
      } else {
        // Local mode fallback if no session ID provided
        const newDoc: UploadedDoc = {
          name: file.name,
          type: docType,
          status: 'COMPLETED',
          confidence: 0.92,
          extractedSummary: `Document received: ${file.name}`,
          fileSize: file.size,
        };
        setDocuments((prev) => [newDoc, ...prev]);
      }
    } catch (err: any) {
      const message = err?.message || 'Failed to upload document. Please check connection and try again.';
      setUploadError(message);
    } finally {
      setIsUploading(false);
    }
  };

  const handleUsePhoto = async () => {
    if (capturedDataUrl) {
      const scanName = `camera_scan_${Date.now().toString().slice(-4)}.jpg`;
      const file = dataURLtoFile(capturedDataUrl, scanName);
      setIsCameraOpen(false);
      setCapturedDataUrl(null);
      await processFile(file);
    }
  };

  const handleCloseCamera = () => {
    stopCameraStream();
    setIsCameraOpen(false);
    setCapturedDataUrl(null);
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      const files = Array.from(e.target.files);
      e.target.value = '';
      files.forEach((file) => processFile(file));
    }
  };

  const removeDoc = (index: number) => {
    setDocuments((prev) => prev.filter((_, i) => i !== index));
  };

  return (
    <div className="max-w-2xl mx-auto py-8 px-6 animate-fadeIn">
      {/* Hidden File Input for Device Upload ONLY */}
      <input
        type="file"
        ref={fileInputRef}
        accept="image/*,.pdf,.doc,.docx"
        className="hidden"
        onChange={handleFileChange}
      />
      <canvas ref={canvasRef} className="hidden" />

      {/* Live Camera Modal Overlay */}
      {isCameraOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/90 p-4 animate-fadeIn">
          <div className="bg-slate-900 border border-slate-700 rounded-3xl p-6 max-w-lg w-full text-center space-y-4 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h3 className="text-lg font-bold text-white flex items-center gap-2">
                <span>📷</span>
                <span>{t('scanWithCamera')}</span>
              </h3>
              <button
                type="button"
                onClick={handleCloseCamera}
                className="text-slate-400 hover:text-white font-bold text-xl px-2"
              >
                ✕
              </button>
            </div>

            {/* Video View or Captured Canvas Preview */}
            <div className="relative rounded-2xl overflow-hidden bg-slate-950 border-2 border-[#AEA989] aspect-video flex items-center justify-center">
              {!capturedDataUrl ? (
                <video
                  ref={videoRef}
                  autoPlay
                  playsInline
                  muted
                  className="w-full h-full object-cover"
                />
              ) : (
                <img
                  src={capturedDataUrl}
                  alt="Captured document preview"
                  className="w-full h-full object-contain"
                />
              )}
            </div>

            {/* Camera Controls */}
            <div className="flex justify-center gap-3 pt-2">
              {!capturedDataUrl ? (
                <>
                  <button
                    type="button"
                    onClick={handleCloseCamera}
                    className="px-5 py-2.5 rounded-xl border border-slate-700 text-slate-300 text-xs font-bold hover:bg-slate-800"
                  >
                    Cancel
                  </button>
                  <button
                    type="button"
                    onClick={handleCaptureFrame}
                    className="px-6 py-2.5 rounded-xl bg-emerald-500 hover:bg-emerald-600 text-slate-950 text-xs font-extrabold shadow-md flex items-center gap-2"
                  >
                    {t('capturePhoto')}
                  </button>
                </>
              ) : (
                <>
                  <button
                    type="button"
                    onClick={handleRetakePhoto}
                    className="px-5 py-2.5 rounded-xl border border-slate-700 text-slate-200 text-xs font-bold hover:bg-slate-800 flex items-center gap-2"
                  >
                    {t('retakePhoto')}
                  </button>
                  <button
                    type="button"
                    onClick={handleUsePhoto}
                    className="px-6 py-2.5 rounded-xl bg-[#AEA989] text-[#131936] text-xs font-extrabold hover:bg-[#9d987a] shadow-md flex items-center gap-2"
                  >
                    {t('usePhoto')}
                  </button>
                </>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Main Header */}
      <div className="mb-6 text-center">
        <div className="inline-flex items-center justify-center w-12 h-12 rounded-2xl bg-[#AEA989]/20 text-[#AEA989] text-2xl mb-2 shadow-xs">
          📄
        </div>
        <h2 className="text-3xl font-extrabold text-white mb-2">{t('documentsTitle')}</h2>
        <p className="text-slate-300 text-sm">{t('documentsSubtitle')}</p>
      </div>

      <div className="bg-[#131936] rounded-3xl p-8 border border-[#AEA989]/40 shadow-xl space-y-6">
        {/* Upload Error / Validation Alert */}
        {uploadError && (
          <div className="p-4 rounded-2xl bg-rose-950/90 border border-rose-500/60 text-rose-200 text-xs font-bold flex items-center justify-between shadow-md">
            <div className="flex items-center gap-2">
              <span>⚠️</span>
              <span>{uploadError}</span>
            </div>
            <button
              type="button"
              onClick={() => setUploadError(null)}
              className="text-rose-400 hover:text-rose-200 text-sm font-extrabold px-2"
            >
              ✕
            </button>
          </div>
        )}

        {/* Camera Access Error Alert */}
        {cameraError && (
          <div className="p-4 rounded-2xl bg-amber-950/80 border border-amber-500/50 text-amber-200 text-xs font-bold space-y-2">
            <div className="flex items-center gap-2">
              <span>⚠️</span>
              <span>{cameraError}</span>
            </div>
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              className="px-4 py-2 rounded-xl bg-[#AEA989] text-[#131936] font-bold hover:bg-[#b8b394]"
            >
              📁 {t('uploadFromDevice')}
            </button>
          </div>
        )}

        {/* Upload Box */}
        <div className="p-6 rounded-2xl border-2 border-dashed border-[#AEA989]/40 bg-[#0b0e1e] flex flex-col items-center text-center">
          <div className="w-14 h-14 rounded-full bg-[#131936] border border-[#AEA989]/30 shadow-md flex items-center justify-center text-2xl mb-3 text-[#AEA989]">
            📷
          </div>

          <div className="mb-4">
            <span className="text-sm font-bold text-[#AEA989] block">Select Document Type:</span>
            <div className="flex flex-wrap justify-center gap-1.5 mt-2">
              {[
                { id: 'PRESCRIPTION' as DocumentType, label: 'Prescription' },
                { id: 'LAB_REPORT' as DocumentType, label: 'Lab Report' },
                { id: 'MEDICAL_RECORD' as DocumentType, label: 'Discharge Summary' },
                { id: 'OTHER' as DocumentType, label: 'Other Document' },
              ].map((type) => (
                <button
                  type="button"
                  key={type.id}
                  onClick={() => setDocType(type.id)}
                  className={`text-xs font-bold px-3.5 py-2 rounded-xl border transition-all ${
                    docType === type.id
                      ? 'bg-[#AEA989] text-[#131936] border-[#AEA989] font-extrabold shadow-sm'
                      : 'bg-[#131936] text-slate-200 border-[#AEA989]/30 hover:border-[#AEA989]'
                  }`}
                >
                  {type.label}
                </button>
              ))}
            </div>
          </div>

          {/* Dual Action Buttons */}
          <div className="flex flex-wrap gap-3 justify-center">
            {/* Primary: Scan with Camera -> Opens Live Camera Modal */}
            <button
              type="button"
              disabled={isUploading || loading}
              onClick={handleOpenCamera}
              className="px-5 py-3 rounded-xl bg-[#AEA989] hover:bg-[#b8b394] active:scale-95 text-[#131936] text-xs font-extrabold transition-all shadow-md flex items-center gap-2 disabled:opacity-50"
            >
              <span>📷</span>
              <span>{isUploading ? t('processingDocument') : t('scanWithCamera')}</span>
            </button>

            {/* Secondary: Upload from Device -> Opens File Input */}
            <button
              type="button"
              disabled={isUploading || loading}
              onClick={() => fileInputRef.current?.click()}
              className="px-5 py-3 rounded-xl bg-[#131936] border border-[#AEA989]/40 hover:bg-[#1a2147] active:scale-95 text-[#AEA989] text-xs font-bold transition-all shadow-xs flex items-center gap-2 disabled:opacity-50"
            >
              <span>📁</span>
              <span>{isUploading ? t('processingDocument') : t('uploadFromDevice')}</span>
            </button>
          </div>
          <p className="text-2xs text-slate-400 mt-3">Accepted formats: JPG, PNG, PDF, DOC (Up to 10MB)</p>
        </div>

        {/* Uploading Status Indicator */}
        {isUploading && (
          <div className="p-4 rounded-2xl bg-indigo-950/60 border border-indigo-500/40 text-indigo-200 text-xs font-bold flex items-center justify-center gap-2 animate-pulse">
            <span>⏳</span>
            <span>Uploading binary document to backend and running OCR extraction...</span>
          </div>
        )}

        {/* Loading existing session docs indicator */}
        {fetchingDocs && (
          <div className="text-center text-xs text-slate-400 italic">
            Retrieving session medical documents...
          </div>
        )}

        {/* Uploaded Documents List */}
        {documents.length > 0 && (
          <div className="space-y-3">
            <h4 className="text-xs font-bold text-[#AEA989] uppercase tracking-wider">
              Scanned Records ({documents.length})
            </h4>
            {documents.map((doc, idx) => (
              <div
                key={doc.id || idx}
                className="p-4 rounded-2xl border border-[#AEA989]/30 bg-[#0b0e1e] space-y-2"
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span className="text-lg">📑</span>
                    <span className="font-bold text-white text-sm">{doc.name}</span>
                    <span className="text-xs px-2 py-0.5 rounded-full bg-[#131936] text-[#AEA989] border border-[#AEA989]/30 font-semibold">
                      {doc.type}
                    </span>
                  </div>
                  <button
                    type="button"
                    onClick={() => removeDoc(idx)}
                    className="text-xs text-rose-400 hover:text-rose-200 font-bold"
                  >
                    Delete
                  </button>
                </div>

                {/* OCR Summary & Confidence */}
                <div className="p-3 rounded-xl bg-[#131936] border border-[#AEA989]/30 text-xs space-y-1">
                  <div className="flex items-center justify-between font-semibold">
                    <span className="text-slate-300">{t('extractionSummary')}:</span>
                    <span
                      className={`px-2 py-0.5 rounded-full text-2xs font-bold ${
                        doc.confidence >= 0.75
                          ? 'bg-[#AEA989] text-[#131936]'
                          : doc.status === 'FAILED'
                          ? 'bg-rose-950 text-rose-200 border border-rose-500/50'
                          : 'bg-amber-950 text-amber-200 border border-amber-500/50'
                      }`}
                    >
                      {doc.status === 'FAILED'
                        ? 'Extraction Failed'
                        : `${t('confidenceScore')}: ${Math.round(doc.confidence * 100)}%`}
                    </span>
                  </div>
                  {doc.extractedSummary ? (
                    <p className="text-white font-medium whitespace-pre-line">{doc.extractedSummary}</p>
                  ) : doc.status === 'FAILED' ? (
                    <p className="text-rose-300 font-medium">Text extraction unreadable. Document saved for physician review.</p>
                  ) : (
                    <p className="text-slate-400 italic">Processing text extraction...</p>
                  )}
                </div>

                {doc.status === 'REVIEW_REQUIRED' && (
                  <p className="text-2xs text-amber-200 bg-amber-950/80 p-2 rounded-lg font-semibold flex items-center gap-1 border border-amber-500/40">
                    <span>⚠️</span>
                    <span>Low confidence reading. Doctor will review original document.</span>
                  </p>
                )}

                {doc.status === 'FAILED' && (
                  <div className="flex items-center justify-between bg-rose-950/60 p-2 rounded-lg border border-rose-500/40">
                    <span className="text-2xs text-rose-200 font-semibold">
                      ⚠️ Source document preserved. Doctor can inspect directly.
                    </span>
                    <button
                      type="button"
                      onClick={() => fileInputRef.current?.click()}
                      className="text-2xs px-2 py-1 bg-rose-800 hover:bg-rose-700 text-white font-bold rounded"
                    >
                      Retry Upload
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}

        {/* Action Buttons */}
        <div className="pt-4 flex items-center justify-between gap-4 border-t border-slate-700/60">
          <KioskButton type="button" variant="outline" size="lg" onClick={onBack}>
            {t('back')}
          </KioskButton>
          <KioskButton
            type="button"
            size="lg"
            loading={loading || isUploading}
            onClick={() => onSubmit(documents)}
            className="px-8"
          >
            {documents.length === 0 ? t('skip') : t('confirmContinue')}
          </KioskButton>
        </div>
      </div>
    </div>
  );
};



