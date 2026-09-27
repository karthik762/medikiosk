import React, { useState, useEffect, useCallback } from 'react';
import {
  DoctorReviewDetail,
  DoctorAmendmentRequest,
  DoctorVerificationRequest,
  DoctorInteroperabilityStatus,
  TriagePriority,
} from '../../types/doctor';
import { ClinicalSummary } from '../../types/clinicalSummary';
import { doctorService } from '../../services/doctorService';
import { FhirExportViewer } from '../fhir/FhirExportViewer';
import { AbdmStatusPanel } from '../abdm/AbdmStatusPanel';
import { HisStatusPanel } from '../his/HisStatusPanel';
import { HealthLakeStatusPanel } from '../healthlake/HealthLakeStatusPanel';

interface ClinicalReviewWorkstationProps {
  sessionId: string;
  authToken?: string;
  onBackToQueue: () => void;
}

export const ClinicalReviewWorkstation: React.FC<ClinicalReviewWorkstationProps> = ({
  sessionId,
  authToken,
  onBackToQueue,
}) => {
  const [caseDetail, setCaseDetail] = useState<DoctorReviewDetail | null>(null);
  const [interopStatus, setInteropStatus] = useState<DoctorInteroperabilityStatus | null>(null);
  const [syncingInterop, setSyncingInterop] = useState(false);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [conflictError, setConflictError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // FHIR Export Modal state
  const [isFhirModalOpen, setIsFhirModalOpen] = useState(false);

  // Edit Mode state
  const [isEditing, setIsEditing] = useState(false);
  const [editedSummary, setEditedSummary] = useState<Partial<ClinicalSummary>>({});
  const [editNotes, setEditNotes] = useState('');
  const [savingEdit, setSavingEdit] = useState(false);

  // Verification Modal state
  const [isVerifyModalOpen, setIsVerifyModalOpen] = useState(false);
  const [verificationNotes, setVerificationNotes] = useState('');
  const [confirmUnderstood, setConfirmUnderstood] = useState(false);
  const [verifying, setVerifying] = useState(false);

  // Right column tab state
  const [activeSourceTab, setActiveSourceTab] = useState<'TRANSCRIPTS' | 'SYMPTOMS' | 'AYURVEDA' | 'DOCUMENTS'>('TRANSCRIPTS');

  const loadInteropStatus = useCallback(async () => {
    try {
      const res = await doctorService.getInteroperabilityStatus(sessionId, authToken);
      if (res.data) {
        setInteropStatus(res.data);
      }
    } catch (err) {
      console.warn('Failed to load interoperability status:', err);
    }
  }, [sessionId, authToken]);

  const handleSyncInteroperability = async () => {
    setSyncingInterop(true);
    setSuccessMessage(null);
    setErrorMessage(null);
    try {
      const res = await doctorService.syncInteroperability(sessionId, authToken);
      if (res.data) {
        setInteropStatus(res.data);
        setSuccessMessage('Multi-system healthcare interoperability synchronization executed successfully!');
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Interoperability sync failed';
      setErrorMessage(msg);
    } finally {
      setSyncingInterop(false);
    }
  };

  const renderProvenanceBadge = (source?: string, isEdited?: boolean) => {
    if (isEdited) {
      return (
        <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-purple-100 text-purple-800 border border-purple-200">
          Doctor-Edited
        </span>
      );
    }
    const s = (source || '').toUpperCase();
    if (s === 'VOICE' || s === 'VOICE_TRANSCRIPT' || s === 'VOICE_INPUT') {
      return (
        <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-sky-100 text-sky-800 border border-sky-200">
          🎙️ Voice Transcript
        </span>
      );
    }
    if (s.includes('AI') || s === 'NLP' || s === 'CLINICAL_NLP') {
      return (
        <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-violet-100 text-violet-800 border border-violet-200">
          🤖 AI-Extracted
        </span>
      );
    }
    if (s.includes('DOC') || s === 'OCR' || s === 'DOCUMENT_OCR') {
      return (
        <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-100 text-amber-800 border border-amber-200">
          📄 Document-Extracted
        </span>
      );
    }
    return (
      <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-100 text-emerald-800 border border-emerald-200">
        👤 Patient-Provided
      </span>
    );
  };

  const loadCaseDetail = useCallback(async () => {
    setLoading(true);
    setErrorMessage(null);
    setConflictError(null);
    try {
      const res = await doctorService.getCaseReview(sessionId, authToken);
      if (res.data) {
        setCaseDetail(res.data);
        if (res.data.clinicalSummary) {
          setEditedSummary(res.data.clinicalSummary);
        }
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to load case review details.';
      setErrorMessage(msg);
    } finally {
      setLoading(false);
    }
  }, [sessionId, authToken]);

  useEffect(() => {
    loadCaseDetail();
    loadInteropStatus();
  }, [loadCaseDetail, loadInteropStatus]);

  const handleStartEditing = () => {
    if (caseDetail?.clinicalSummary) {
      setEditedSummary({ ...caseDetail.clinicalSummary });
    }
    setEditNotes('');
    setConflictError(null);
    setIsEditing(true);
  };

  const handleCancelEditing = () => {
    if (caseDetail?.clinicalSummary) {
      setEditedSummary({ ...caseDetail.clinicalSummary });
    }
    setIsEditing(false);
    setConflictError(null);
  };

  const handleSaveAmendments = async () => {
    if (!caseDetail) return;
    setSavingEdit(true);
    setConflictError(null);
    setErrorMessage(null);

    try {
      const req: DoctorAmendmentRequest = {
        expectedVersion: caseDetail.currentVersion,
        amendedSummary: editedSummary,
        clinicalNotes: editNotes,
      };

      await doctorService.amendSummary(sessionId, req, authToken);
      setSuccessMessage('Physician amendments saved successfully.');
      setIsEditing(false);
      await loadCaseDetail();
    } catch (err: any) {
      if (err.status === 409 || err.message?.includes('Conflict')) {
        setConflictError(
          'Concurrency Conflict: Another clinician modified this case. Please refresh to inspect the latest amendments before making further updates.'
        );
      } else {
        setErrorMessage(err.message || 'Failed to save clinical amendments.');
      }
    } finally {
      setSavingEdit(false);
    }
  };

  const handleConfirmVerification = async () => {
    if (!caseDetail || !confirmUnderstood) return;
    setVerifying(true);
    setErrorMessage(null);

    try {
      const req: DoctorVerificationRequest = {
        expectedVersion: caseDetail.currentVersion,
        clinicalNotes: verificationNotes,
        confirmUnderstood: true,
      };

      await doctorService.verifySummary(sessionId, req, authToken);
      setSuccessMessage('Case verified and signed off successfully.');
      setIsVerifyModalOpen(false);
      await loadCaseDetail();
    } catch (err: any) {
      if (err.status === 409 || err.message?.includes('Conflict')) {
        setConflictError('Concurrency Conflict: The summary version changed. Please refresh the case.');
        setIsVerifyModalOpen(false);
      } else {
        setErrorMessage(err.message || 'Failed to verify summary.');
      }
    } finally {
      setVerifying(false);
    }
  };

  const getPriorityBadge = (priority?: TriagePriority) => {
    switch (priority) {
      case 'EMERGENCY':
        return 'bg-red-700 text-white border-red-800 animate-pulse';
      case 'URGENT':
        return 'bg-red-100 text-red-800 border-red-300';
      case 'PRIORITY':
        return 'bg-amber-100 text-amber-800 border-amber-300';
      case 'ROUTINE':
        return 'bg-blue-100 text-blue-800 border-blue-300';
      case 'STABLE':
        return 'bg-emerald-100 text-emerald-800 border-emerald-300';
      default:
        return 'bg-slate-100 text-slate-700 border-slate-300';
    }
  };

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-16 text-center">
        <div className="inline-block animate-spin rounded-full h-10 w-10 border-4 border-indigo-600 border-t-transparent mb-4"></div>
        <p className="text-slate-600 font-medium">Loading clinical case workstation...</p>
      </div>
    );
  }

  if (errorMessage && !caseDetail) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-12">
        <div className="bg-red-50 border border-red-200 rounded-xl p-6 text-center">
          <p className="text-red-700 font-semibold mb-4">{errorMessage}</p>
          <button
            onClick={onBackToQueue}
            className="px-4 py-2 bg-slate-800 text-white rounded-lg hover:bg-slate-900 transition-colors"
          >
            Back to Consultation Queue
          </button>
        </div>
      </div>
    );
  }

  if (!caseDetail) return null;

  return (
    <div className="max-w-7xl mx-auto px-4 py-6">
      {/* Navigation & Top Bar */}
      <div className="flex flex-wrap items-center justify-between gap-4 mb-6 pb-4 border-b border-slate-200">
        <div className="flex items-center space-x-3">
          <button
            onClick={onBackToQueue}
            className="flex items-center space-x-1.5 px-3 py-1.5 text-sm font-medium text-slate-600 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors shadow-sm"
          >
            <span>←</span>
            <span>Back to Queue</span>
          </button>
          <span className="text-slate-400">|</span>
          <h1 className="text-xl font-bold text-slate-900">
            Case Review: {caseDetail.patientName}
          </h1>
          <span className="px-2 py-0.5 text-xs font-mono bg-slate-100 text-slate-700 rounded border border-slate-300">
            {caseDetail.patientIdentifier}
          </span>
        </div>

        <div className="flex items-center space-x-3">
          {/* Visual Workflow Progression Indicator */}
          <div className="hidden md:flex items-center gap-1 bg-slate-100 p-1 rounded-lg text-[10px] font-bold">
            <span className="px-2 py-0.5 rounded bg-violet-100 text-violet-800 border border-violet-200">
              1. AI GENERATED
            </span>
            <span className="text-slate-400">→</span>
            <span className={`px-2 py-0.5 rounded ${isEditing ? 'bg-indigo-600 text-white font-bold animate-pulse' : 'bg-indigo-100 text-indigo-800 border border-indigo-200'}`}>
              2. DOCTOR REVIEW
            </span>
            <span className="text-slate-400">→</span>
            <span className={`px-2 py-0.5 rounded ${caseDetail.verified ? 'bg-emerald-600 text-white font-bold' : 'bg-slate-200 text-slate-500'}`}>
              3. CLINICIAN VERIFIED
            </span>
          </div>

          {caseDetail.verified ? (
            <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-emerald-100 text-emerald-800 border border-emerald-300">
              <span className="w-2 h-2 rounded-full bg-emerald-500 mr-1.5"></span>
              CLINICIAN VERIFIED (v{caseDetail.currentVersion})
            </span>
          ) : (
            <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-amber-100 text-amber-800 border border-amber-300">
              <span className="w-2 h-2 rounded-full bg-amber-500 mr-1.5"></span>
              {caseDetail.currentStatus || 'DOCTOR REVIEW'} (v{caseDetail.currentVersion})
            </span>
          )}

          <button
            onClick={() => setIsFhirModalOpen(true)}
            className="px-3 py-1.5 text-xs font-semibold text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors shadow-sm flex items-center space-x-1.5"
            title="View and Export FHIR R4 Bundle"
          >
            <span>📦</span>
            <span>FHIR R4 Export</span>
          </button>

          {!caseDetail.verified && !isEditing && (
            <>
              <button
                onClick={handleStartEditing}
                className="px-3.5 py-1.5 text-sm font-semibold text-indigo-700 bg-indigo-50 border border-indigo-200 rounded-lg hover:bg-indigo-100 transition-colors"
              >
                ✏️ Edit Summary
              </button>
              <button
                onClick={() => setIsVerifyModalOpen(true)}
                className="px-4 py-1.5 text-sm font-semibold text-white bg-emerald-600 border border-emerald-700 rounded-lg hover:bg-emerald-700 transition-colors shadow-sm"
              >
                ✓ Verify Case
              </button>
            </>
          )}

          {isEditing && (
            <>
              <button
                onClick={handleCancelEditing}
                disabled={savingEdit}
                className="px-3 py-1.5 text-sm font-medium text-slate-600 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleSaveAmendments}
                disabled={savingEdit}
                className="px-4 py-1.5 text-sm font-semibold text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 transition-colors shadow-sm disabled:opacity-50"
              >
                {savingEdit ? 'Saving...' : 'Save Amendments'}
              </button>
            </>
          )}
        </div>
      </div>

      {/* Notifications / Alerts */}
      {successMessage && (
        <div className="mb-6 p-4 bg-emerald-50 border border-emerald-200 rounded-xl text-emerald-800 text-sm flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <span>✓</span>
            <span>{successMessage}</span>
          </div>
          <button onClick={() => setSuccessMessage(null)} className="text-emerald-600 hover:text-emerald-800">
            ✕
          </button>
        </div>
      )}

      {conflictError && (
        <div className="mb-6 p-4 bg-amber-50 border border-amber-300 rounded-xl text-amber-900 text-sm flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <span>⚠️</span>
            <span>{conflictError}</span>
          </div>
          <button
            onClick={loadCaseDetail}
            className="ml-4 px-3 py-1 bg-amber-600 text-white rounded text-xs font-semibold hover:bg-amber-700"
          >
            Reload Latest Version
          </button>
        </div>
      )}

      {/* Patient Header & Deterministic Triage Banner */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
        {/* Patient Demographics Card */}
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-xs font-bold uppercase tracking-wider text-slate-400">
              Patient Information
            </h2>
            <span className="px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-amber-100 text-amber-800 border border-amber-300">
              Synthetic Demo Patient
            </span>
          </div>
          <div className="grid grid-cols-2 gap-y-2 text-sm">
            <div>
              <span className="text-slate-500 text-xs block">Age / Gender</span>
              <span className="font-semibold text-slate-800">
                {caseDetail.age !== undefined ? `${caseDetail.age} yrs` : 'N/A'}, {caseDetail.gender || 'N/A'}
              </span>
            </div>
            <div>
              <span className="text-slate-500 text-xs block">Date of Birth</span>
              <span className="font-semibold text-slate-800">{caseDetail.dateOfBirth || 'N/A'}</span>
            </div>
            <div>
              <span className="text-slate-500 text-xs block">Phone (Masked)</span>
              <span className="font-mono text-slate-800">{caseDetail.phone || 'N/A'}</span>
            </div>
            <div>
              <span className="text-slate-500 text-xs block">Intake Session</span>
              <span className="text-xs text-slate-700">{caseDetail.sessionStatus}</span>
            </div>
          </div>
        </div>

        {/* Deterministic Triage Card */}
        <div className="lg:col-span-2 bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-2">
              <h2 className="text-xs font-bold uppercase tracking-wider text-slate-400">
                Deterministic Triage Evaluation
              </h2>
              <span className="text-xs font-mono bg-slate-100 text-slate-600 px-2 py-0.5 rounded border border-slate-200">
                Source: {caseDetail.triage?.source || 'RULE_ENGINE'}
              </span>
            </div>

            <div className="flex items-center space-x-3 mb-2">
              <span
                className={`px-3 py-1 rounded-full text-xs font-extrabold border ${getPriorityBadge(
                  caseDetail.triage?.priority
                )}`}
              >
                {caseDetail.triage?.priority || 'UNASSIGNED'}
              </span>
              <p className="text-sm font-medium text-slate-800">
                {caseDetail.triage?.explanation || 'No triage explanation recorded.'}
              </p>
            </div>
          </div>

          {caseDetail.triage?.triggeredRules && caseDetail.triage.triggeredRules.length > 0 && (
            <div className="mt-3 pt-3 border-t border-slate-100">
              <span className="text-xs font-semibold text-slate-600 block mb-1">
                Triggered Deterministic Rules:
              </span>
              <div className="flex flex-wrap gap-1.5">
                {caseDetail.triage.triggeredRules.map((rule: any, idx: number) => (
                  <span
                    key={idx}
                    className="px-2 py-0.5 text-xs bg-slate-100 text-slate-700 rounded border border-slate-200 font-mono"
                  >
                    {rule.ruleCode || rule.ruleName || JSON.stringify(rule)}
                  </span>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Stage 5C: Multi-System Healthcare Interoperability & Cloud Sync Status Banner */}
      <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm mb-6">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-4 pb-3 border-b border-slate-100">
          <div>
            <div className="flex items-center space-x-2">
              <span className="text-lg">🌐</span>
              <h2 className="text-sm font-bold text-slate-900 uppercase tracking-wider">
                Multi-System Interoperability & Cloud Sync Status
              </h2>
              {caseDetail.verified && (
                <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-100 text-emerald-800 border border-emerald-300">
                  🔒 Locked & Verified Session
                </span>
              )}
            </div>
            <p className="text-xs text-slate-500 mt-0.5">
              Automated integration dispatch boundary for FHIR R4 Bundle, ABDM ABHA Sandbox, HIS OPD Queue, and AWS HealthLake Datastore.
            </p>
          </div>
          <div className="flex items-center space-x-2">
            <button
              onClick={() => setIsFhirModalOpen(true)}
              className="px-3 py-1.5 text-xs font-semibold text-slate-700 bg-slate-100 border border-slate-300 rounded-lg hover:bg-slate-200 transition-colors flex items-center space-x-1.5"
            >
              <span>📦</span>
              <span>View FHIR R4 Bundle</span>
            </button>
            <button
              onClick={handleSyncInteroperability}
              disabled={syncingInterop}
              className="px-4 py-1.5 text-xs font-bold text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 transition-colors shadow-sm disabled:opacity-50 flex items-center space-x-1.5"
            >
              <span>{syncingInterop ? '🔄 Syncing...' : '🔄 Re-Sync Adapters'}</span>
            </button>
          </div>
        </div>

        {/* System Badges Grid */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
            <div className="flex items-center justify-between mb-1">
              <span className="text-xs font-semibold text-slate-600">FHIR R4 Bundle</span>
              <span
                className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                  interopStatus?.fhirStatus === 'SUCCESS'
                    ? 'bg-emerald-100 text-emerald-800 border border-emerald-300'
                    : 'bg-amber-100 text-amber-800 border border-amber-300'
                }`}
              >
                {interopStatus?.fhirStatus || 'PENDING'}
              </span>
            </div>
            <span className="text-[10px] font-mono text-slate-500 block truncate">
              ID: {interopStatus?.fhirBundleId || 'Generated on verification'}
            </span>
          </div>

          <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
            <div className="flex items-center justify-between mb-1">
              <span className="text-xs font-semibold text-slate-600">ABDM ABHA Link</span>
              <span
                className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                  interopStatus?.abdmStatus === 'SUCCESS'
                    ? 'bg-emerald-100 text-emerald-800 border border-emerald-300'
                    : interopStatus?.abdmStatus === 'SKIPPED'
                    ? 'bg-blue-100 text-blue-800 border border-blue-300'
                    : 'bg-slate-100 text-slate-700 border border-slate-300'
                }`}
              >
                {interopStatus?.abdmStatus || 'NOT_SYNCED'}
              </span>
            </div>
            <span className="text-[10px] font-mono text-slate-500 block truncate">
              TxID: {interopStatus?.abdmTransactionId || 'ABDM-MOCK-SANDBOX'}
            </span>
          </div>

          <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
            <div className="flex items-center justify-between mb-1">
              <span className="text-xs font-semibold text-slate-600">HIS OPD Queue</span>
              <span
                className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                  interopStatus?.hisStatus === 'SUCCESS'
                    ? 'bg-emerald-100 text-emerald-800 border border-emerald-300'
                    : interopStatus?.hisStatus === 'SKIPPED'
                    ? 'bg-blue-100 text-blue-800 border border-blue-300'
                    : 'bg-slate-100 text-slate-700 border border-slate-300'
                }`}
              >
                {interopStatus?.hisStatus || 'NOT_SYNCED'}
              </span>
            </div>
            <span className="text-[10px] font-mono text-slate-500 block truncate">
              Ref: {interopStatus?.hisEncounterRef || 'HIS-OPD-QUEUE-MOCK'}
            </span>
          </div>

          <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
            <div className="flex items-center justify-between mb-1">
              <span className="text-xs font-semibold text-slate-600">AWS HealthLake</span>
              <span
                className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                  interopStatus?.healthLakeStatus === 'SUCCESS'
                    ? 'bg-emerald-100 text-emerald-800 border border-emerald-300'
                    : interopStatus?.healthLakeStatus === 'SKIPPED'
                    ? 'bg-blue-100 text-blue-800 border border-blue-300'
                    : 'bg-slate-100 text-slate-700 border border-slate-300'
                }`}
              >
                {interopStatus?.healthLakeStatus || 'NOT_SYNCED'}
              </span>
            </div>
            <span className="text-[10px] font-mono text-slate-500 block truncate">
              Datastore: {interopStatus?.healthLakeResourceRef || 'HL-MOCK-DATASTORE'}
            </span>
          </div>
        </div>
      </div>

      {/* Resiliency Warning when AI summary is absent */}
      {!caseDetail.aiSummaryAvailable && (
        <div className="mb-6 p-4 bg-amber-50 border border-amber-200 rounded-xl text-amber-800 text-sm flex items-start space-x-3">
          <span className="text-lg">ℹ️</span>
          <div>
            <h3 className="font-semibold text-amber-900">AI Pre-Consult Summary Unavailable</h3>
            <p className="text-xs mt-0.5 text-amber-800">
              The AI structuring pipeline did not produce a pre-consult summary for this session. The full original patient intake data, structured symptoms, Ayurveda case, and medical documents are fully available below for your direct clinical review.
            </p>
          </div>
        </div>
      )}

      {/* Two-Column Clinical Workstation */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* LEFT COLUMN: Pre-Consult Summary (AI / Doctor Amended) */}
        <div className="space-y-6">
          <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
            <div className="flex items-center justify-between mb-4 pb-3 border-b border-slate-100">
              <div className="flex items-center space-x-2">
                <span className="text-lg">📋</span>
                <h2 className="text-lg font-bold text-slate-900">Pre-Consult Clinical Summary</h2>
              </div>
              <span className="text-xs font-semibold text-slate-500">
                {isEditing ? 'Editing Mode (Unsaved)' : `Version ${caseDetail.currentVersion}`}
              </span>
            </div>

            {/* Non-diagnostic disclaimer */}
            <div className="p-3 bg-slate-50 border border-slate-200 rounded-lg text-xs text-slate-600 mb-6">
              ⚠️ <strong>Clinical Notice:</strong> This summary is generated from patient intake responses and OCR data to assist clinical workflow. It is NOT an autonomous diagnosis. Final clinical judgment and treatment decisions rest solely with the licensed physician.
            </div>

            {/* Chief Complaint */}
            <div className="mb-6">
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-500 mb-1.5">
                Chief Complaint
              </label>
              {isEditing ? (
                <input
                  type="text"
                  value={editedSummary.chiefComplaint || ''}
                  onChange={(e) => setEditedSummary({ ...editedSummary, chiefComplaint: e.target.value })}
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  placeholder="Enter chief complaint..."
                />
              ) : (
                <div className="p-3 bg-slate-50 rounded-lg border border-slate-100 text-sm font-semibold text-slate-900">
                  {caseDetail.clinicalSummary?.chiefComplaint || caseDetail.originalPatientData.chiefComplaint || 'None specified'}
                </div>
              )}
            </div>

            {/* Structured Symptoms */}
            <div className="mb-6">
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-500 mb-2">
                Clinical Symptoms & Severity
              </label>
              <div className="space-y-2">
                {caseDetail.clinicalSummary?.symptoms && caseDetail.clinicalSummary.symptoms.length > 0 ? (
                  caseDetail.clinicalSummary.symptoms.map((s, idx) => (
                    <div
                      key={idx}
                      className="flex items-center justify-between p-3 bg-slate-50 rounded-lg border border-slate-200 text-sm"
                    >
                      <div className="flex items-center space-x-2">
                        <span className="font-semibold text-slate-800">{s.name}</span>
                        {s.duration && <span className="text-xs text-slate-500">({s.duration})</span>}
                      </div>
                      <div className="flex items-center space-x-3">
                        <span className="text-xs px-2 py-0.5 bg-indigo-50 text-indigo-700 rounded border border-indigo-200 font-medium">
                          Severity: {s.severity}/10
                        </span>
                        <span className="text-xs font-mono text-slate-400 bg-white px-1.5 py-0.5 rounded border border-slate-200">
                          {s.source || 'PATIENT'}
                        </span>
                      </div>
                    </div>
                  ))
                ) : (
                  <p className="text-xs text-slate-400 italic">No structured symptoms in summary.</p>
                )}
              </div>
            </div>

            {/* Medical History */}
            <div className="mb-6">
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-500 mb-2">
                Past Medical History
              </label>
              <div className="space-y-1.5">
                {caseDetail.clinicalSummary?.medicalHistory && caseDetail.clinicalSummary.medicalHistory.length > 0 ? (
                  caseDetail.clinicalSummary.medicalHistory.map((h, idx) => (
                    <div
                      key={idx}
                      className="p-2.5 bg-slate-50 rounded-lg border border-slate-100 text-sm flex items-center justify-between"
                    >
                      <div>
                        <span className="font-semibold text-slate-700 text-xs mr-2">[{h.category}]</span>
                        <span className="text-slate-800">{h.description}</span>
                      </div>
                      <span className="text-xs font-mono text-slate-400">{h.source}</span>
                    </div>
                  ))
                ) : (
                  <p className="text-xs text-slate-400 italic">No medical history recorded in summary.</p>
                )}
              </div>
            </div>

            {/* Medications & Allergies */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-slate-500 mb-2">
                  Active Medications
                </label>
                <div className="space-y-1.5">
                  {caseDetail.clinicalSummary?.medications && caseDetail.clinicalSummary.medications.length > 0 ? (
                    caseDetail.clinicalSummary.medications.map((m, idx) => (
                      <div key={idx} className="p-2 bg-slate-50 rounded border border-slate-100 text-xs">
                        <span className="font-semibold text-slate-800">{m.medicationName}</span>
                        {m.dosage && <span className="text-slate-500 block">{m.dosage} {m.frequency}</span>}
                      </div>
                    ))
                  ) : (
                    <p className="text-xs text-slate-400 italic">No active medications.</p>
                  )}
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-slate-500 mb-2">
                  Known Allergies
                </label>
                <div className="space-y-1.5">
                  {caseDetail.clinicalSummary?.allergies && caseDetail.clinicalSummary.allergies.length > 0 ? (
                    caseDetail.clinicalSummary.allergies.map((a, idx) => (
                      <div key={idx} className="p-2 bg-red-50 rounded border border-red-100 text-xs">
                        <span className="font-semibold text-red-800">{a.allergen}</span>
                        {a.reaction && <span className="text-red-600 block">{a.reaction}</span>}
                      </div>
                    ))
                  ) : (
                    <p className="text-xs text-slate-400 italic">No known allergies.</p>
                  )}
                </div>
              </div>
            </div>

            {/* Conflicts & Uncertainties Highlighting */}
            {caseDetail.clinicalSummary?.potentialConflicts && caseDetail.clinicalSummary.potentialConflicts.length > 0 && (
              <div className="mb-6 p-4 bg-amber-50 border border-amber-200 rounded-xl">
                <div className="flex items-center space-x-2 text-amber-800 font-bold text-xs uppercase tracking-wider mb-2">
                  <span>⚠️</span>
                  <span>Uncertainties & Clinical Conflicts Detected</span>
                </div>
                <div className="space-y-2">
                  {caseDetail.clinicalSummary.potentialConflicts.map((conf, idx) => (
                    <div key={idx} className="p-2.5 bg-white rounded border border-amber-200 text-xs">
                      <span className="font-semibold text-slate-800 block mb-1">
                        Field: {conf.field}
                      </span>
                      <p className="text-slate-600">{conf.conflictDescription}</p>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Doctor Amendments Note input in Edit Mode */}
            {isEditing && (
              <div className="mt-4 pt-4 border-t border-slate-200">
                <label className="block text-xs font-bold uppercase tracking-wider text-indigo-700 mb-1.5">
                  Clinician Amendment Comments (Audit Logged)
                </label>
                <textarea
                  rows={3}
                  value={editNotes}
                  onChange={(e) => setEditNotes(e.target.value)}
                  className="w-full p-2.5 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  placeholder="Record justification for modifying symptoms, medication details, or conflict resolutions..."
                />
              </div>
            )}
          </div>

          {/* Review & Amendment Audit History */}
          {caseDetail.reviewHistory && caseDetail.reviewHistory.length > 0 && (
            <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3">
                Audit Trail & Review History
              </h3>
              <div className="space-y-2.5">
                {caseDetail.reviewHistory.map((rev) => (
                  <div
                    key={rev.id}
                    className="p-3 bg-slate-50 rounded-lg border border-slate-200 text-xs flex flex-col space-y-1"
                  >
                    <div className="flex items-center justify-between">
                      <span className="font-bold text-slate-800">
                        {rev.action} — Version {rev.summaryVersion}
                      </span>
                      <span className="text-slate-400">
                        {new Date(rev.createdAt).toLocaleString()}
                      </span>
                    </div>
                    <span className="text-slate-600">
                      Doctor: {rev.doctorName || rev.doctorEmail || 'Attending Physician'}
                    </span>
                    {rev.comments && (
                      <p className="text-slate-700 bg-white p-2 rounded border border-slate-200 mt-1 italic">
                        "{rev.comments}"
                      </p>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* RIGHT COLUMN: Original Patient Source Data */}
        <div className="space-y-6">
          <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
            <div className="flex items-center justify-between mb-4 pb-3 border-b border-slate-100">
              <div className="flex items-center space-x-2">
                <span className="text-lg">🔍</span>
                <h2 className="text-lg font-bold text-slate-900">Original Patient Intake Source</h2>
              </div>
              <span className="text-xs font-medium text-slate-400">Immutable Records</span>
            </div>

            {/* Tab navigation */}
            <div className="flex border-b border-slate-200 mb-4">
              <button
                onClick={() => setActiveSourceTab('TRANSCRIPTS')}
                className={`pb-2 px-3 text-xs font-semibold border-b-2 transition-colors ${
                  activeSourceTab === 'TRANSCRIPTS'
                    ? 'border-indigo-600 text-indigo-600'
                    : 'border-transparent text-slate-500 hover:text-slate-700'
                }`}
              >
                Q&A Transcripts ({caseDetail.originalPatientData.intakeAnswers.length})
              </button>
              <button
                onClick={() => setActiveSourceTab('SYMPTOMS')}
                className={`pb-2 px-3 text-xs font-semibold border-b-2 transition-colors ${
                  activeSourceTab === 'SYMPTOMS'
                    ? 'border-indigo-600 text-indigo-600'
                    : 'border-transparent text-slate-500 hover:text-slate-700'
                }`}
              >
                Reported Intake ({caseDetail.originalPatientData.symptoms.length})
              </button>
              <button
                onClick={() => setActiveSourceTab('AYURVEDA')}
                className={`pb-2 px-3 text-xs font-semibold border-b-2 transition-colors ${
                  activeSourceTab === 'AYURVEDA'
                    ? 'border-indigo-600 text-indigo-600'
                    : 'border-transparent text-slate-500 hover:text-slate-700'
                }`}
              >
                Ayurveda Profile
              </button>
              <button
                onClick={() => setActiveSourceTab('DOCUMENTS')}
                className={`pb-2 px-3 text-xs font-semibold border-b-2 transition-colors ${
                  activeSourceTab === 'DOCUMENTS'
                    ? 'border-indigo-600 text-indigo-600'
                    : 'border-transparent text-slate-500 hover:text-slate-700'
                }`}
              >
                Documents ({caseDetail.documents.length})
              </button>
            </div>

            {/* TAB 1: Transcripts */}
            {activeSourceTab === 'TRANSCRIPTS' && (
              <div className="space-y-3">
                {caseDetail.originalPatientData.intakeAnswers.length > 0 ? (
                  caseDetail.originalPatientData.intakeAnswers.map((ans, idx) => (
                    <div key={idx} className="p-3 bg-slate-50 rounded-lg border border-slate-200 text-sm">
                      <div className="flex items-center justify-between mb-1.5">
                        <span className="font-semibold text-slate-800 text-xs">{ans.questionText}</span>
                        <div className="flex items-center gap-1.5">
                          {renderProvenanceBadge(ans.inputMethod)}
                          <span className="text-xs px-2 py-0.5 bg-slate-200 text-slate-700 rounded font-mono">
                            {ans.inputMethod}
                          </span>
                        </div>
                      </div>
                      <p className="text-slate-900 font-medium">{ans.answerText}</p>
                      {ans.originalTranscription && ans.inputMethod === 'VOICE' && (
                        <p className="text-xs text-slate-500 italic mt-1">
                          STT Raw: "{ans.originalTranscription}"
                        </p>
                      )}
                    </div>
                  ))
                ) : (
                  <p className="text-xs text-slate-400 italic">No intake answers recorded.</p>
                )}
              </div>
            )}

            {/* TAB 2: Reported Intake Symptoms & History */}
            {activeSourceTab === 'SYMPTOMS' && (
              <div className="space-y-4">
                <div>
                  <h4 className="text-xs font-bold uppercase text-slate-500 mb-2">Patient Reported Symptoms</h4>
                  <div className="space-y-2">
                    {caseDetail.originalPatientData.symptoms.map((s, idx) => (
                      <div key={idx} className="p-2.5 bg-slate-50 rounded border border-slate-200 text-xs flex items-center justify-between">
                        <div>
                          <span className="font-bold text-slate-800">{s.name}</span> — Severity: {s.severity}/10
                          {s.duration && <span className="text-slate-500 block">Duration: {s.duration}</span>}
                        </div>
                        {renderProvenanceBadge('PATIENT_KIOSK')}
                      </div>
                    ))}
                  </div>
                </div>

                <div>
                  <h4 className="text-xs font-bold uppercase text-slate-500 mb-2">Conventional History</h4>
                  <div className="space-y-2">
                    {caseDetail.originalPatientData.medicalHistory.map((h, idx) => (
                      <div key={idx} className="p-2 bg-slate-50 rounded border border-slate-200 text-xs flex items-center justify-between">
                        <span><span className="font-semibold text-slate-700">[{h.category}]</span> {h.description}</span>
                        {renderProvenanceBadge('PATIENT_KIOSK')}
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}

            {/* TAB 3: Ayurveda Case Profile */}
            {activeSourceTab === 'AYURVEDA' && (
              <div className="space-y-3">
                {caseDetail.ayurvedaHistory ? (
                  <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-lg text-xs space-y-2">
                    <div className="font-bold text-emerald-900">Ayurveda Case-Taking History</div>
                    {caseDetail.ayurvedaHistory.prakriti && (
                      <div>
                        <span className="font-semibold text-emerald-800">Prakriti Constitution:</span>{' '}
                        {caseDetail.ayurvedaHistory.prakriti.bodyFrame || 'Recorded'}
                      </div>
                    )}
                    {caseDetail.ayurvedaHistory.pariksha && (
                      <div>
                        <span className="font-semibold text-emerald-800">Pariksha Observations:</span>{' '}
                        {JSON.stringify(caseDetail.ayurvedaHistory.pariksha)}
                      </div>
                    )}
                    <div className="text-emerald-700">
                      Confirmed by Patient: {caseDetail.ayurvedaHistory.patientConfirmed ? 'Yes' : 'No'}
                    </div>
                  </div>
                ) : (
                  <p className="text-xs text-slate-400 italic">No Ayurveda profile documented for this session.</p>
                )}
              </div>
            )}

            {/* TAB 4: Uploaded Documents & OCR */}
            {activeSourceTab === 'DOCUMENTS' && (
              <div className="space-y-3">
                {caseDetail.documents.length > 0 ? (
                  caseDetail.documents.map((doc) => (
                    <div key={doc.id} className="p-3 bg-slate-50 rounded-lg border border-slate-200 text-xs">
                      <div className="flex items-center justify-between mb-1.5">
                        <span className="font-bold text-slate-800">{doc.fileName}</span>
                        <span className="px-2 py-0.5 bg-blue-100 text-blue-800 rounded font-semibold text-[10px]">
                          {doc.documentType}
                        </span>
                      </div>
                      <div className="text-slate-500 mb-2">
                        Status: <span className="font-medium text-slate-700">{doc.processingStatus}</span>
                      </div>
                      {doc.extraction && (
                        <div className="p-2 bg-white rounded border border-slate-200">
                          <div className="flex items-center justify-between text-[11px] text-slate-500 mb-1">
                            <span>OCR Extracted Text</span>
                            <span
                              className={`font-semibold ${
                                (doc.extraction.confidenceScore || 0) >= 0.75
                                  ? 'text-emerald-700'
                                  : (doc.extraction.confidenceScore || 0) >= 0.5
                                    ? 'text-amber-700'
                                    : 'text-rose-700'
                              }`}
                            >
                              Confidence: {Math.round((doc.extraction.confidenceScore || 0) * 100)}%
                              {(doc.extraction.confidenceScore || 0) < 0.75 ? ' (Review Required)' : ''}
                            </span>
                          </div>
                          <p className="font-mono text-slate-700 whitespace-pre-wrap">
                            {doc.extraction.extractedText || 'No text extracted'}
                          </p>
                        </div>
                      )}
                    </div>
                  ))
                ) : (
                  <p className="text-xs text-slate-400 italic">No medical documents uploaded.</p>
                )}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* ABDM & ABHA National Interoperability Section */}
      <div className="mb-8">
        <AbdmStatusPanel
          sessionId={sessionId}
          patientId={caseDetail.patientId}
          isDoctorVerified={caseDetail.verified}
          fhirReady={caseDetail.verified === true}
          token={authToken}
        />
      </div>

      {/* Hospital Information System (HIS) Section */}
      <div className="mb-8">
        <HisStatusPanel
          sessionId={sessionId}
          patientId={caseDetail.patientId}
          isDoctorVerified={caseDetail.verified}
          token={authToken}
        />
      </div>

      {/* AWS HealthLake Cloud FHIR Section */}
      <div className="mb-8">
        <HealthLakeStatusPanel
          sessionId={sessionId}
          patientId={caseDetail.patientId}
          isDoctorVerified={caseDetail.verified}
          token={authToken}
        />
      </div>

      {/* Verification Modal */}
      {isVerifyModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900 bg-opacity-50 p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-lg w-full p-6 border border-slate-200">
            <h3 className="text-lg font-bold text-slate-900 mb-2">
              Confirm Clinical Verification & Sign-Off
            </h3>
            <p className="text-xs text-slate-600 mb-4">
              You are certifying that you have personally evaluated this patient intake case, reviewed the AI pre-consult summary, and verified it against original source responses and documents.
            </p>

            <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg mb-4 text-xs text-emerald-900">
              ✓ This action locks the pre-consult summary into <strong>VERIFIED</strong> status (v{caseDetail.currentVersion}) and records your physician identity in the immutable audit log.
            </div>

            <div className="mb-4">
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Clinical Verification Notes (Optional)
              </label>
              <textarea
                rows={3}
                value={verificationNotes}
                onChange={(e) => setVerificationNotes(e.target.value)}
                className="w-full p-2 text-xs border border-slate-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                placeholder="Enter clinical sign-off notes..."
              />
            </div>

            <div className="flex items-center space-x-2 mb-6">
              <input
                type="checkbox"
                id="verifyCheckbox"
                checked={confirmUnderstood}
                onChange={(e) => setConfirmUnderstood(e.target.checked)}
                className="h-4 w-4 text-emerald-600 rounded border-slate-300 focus:ring-emerald-500"
              />
              <label htmlFor="verifyCheckbox" className="text-xs font-semibold text-slate-800">
                I confirm that I have reviewed the intake data and verify this clinical summary.
              </label>
            </div>

            <div className="flex items-center justify-end space-x-3">
              <button
                onClick={() => setIsVerifyModalOpen(false)}
                disabled={verifying}
                className="px-4 py-2 text-xs font-semibold text-slate-600 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleConfirmVerification}
                disabled={!confirmUnderstood || verifying}
                className="px-5 py-2 text-xs font-bold text-white bg-emerald-600 rounded-lg hover:bg-emerald-700 transition-colors shadow-sm disabled:opacity-50"
              >
                {verifying ? 'Verifying...' : 'Sign & Complete Verification'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* FHIR R4 Interoperability Export Viewer Modal */}
      <FhirExportViewer
        sessionId={sessionId}
        patientName={caseDetail.patientName}
        authToken={authToken}
        isOpen={isFhirModalOpen}
        onClose={() => setIsFhirModalOpen(false)}
      />
    </div>
  );
};
