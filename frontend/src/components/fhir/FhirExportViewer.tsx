import React, { useState, useEffect, useCallback } from 'react';
import { FhirExportSummary, FhirValidationResponse } from '../../types/fhir';
import { fhirService } from '../../services/fhirService';

interface FhirExportViewerProps {
  sessionId: string;
  patientName?: string;
  authToken?: string;
  isOpen: boolean;
  onClose: () => void;
}

export const FhirExportViewer: React.FC<FhirExportViewerProps> = ({
  sessionId,
  patientName,
  authToken,
  isOpen,
  onClose,
}) => {
  const [summary, setSummary] = useState<FhirExportSummary | null>(null);
  const [validation, setValidation] = useState<FhirValidationResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [validating, setValidating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'JSON' | 'RESOURCES' | 'VALIDATION'>('JSON');
  const [copied, setCopied] = useState(false);

  const loadExportPreview = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fhirService.getExportSummary(sessionId, authToken);
      if (res.data) {
        setSummary(res.data);
      }
      const valRes = await fhirService.validateSession(sessionId, authToken);
      if (valRes.data) {
        setValidation(valRes.data);
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to load FHIR export preview';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, [sessionId, authToken]);

  useEffect(() => {
    if (isOpen) {
      loadExportPreview();
    }
  }, [isOpen, loadExportPreview]);

  const handleExport = async () => {
    setExporting(true);
    setError(null);
    try {
      const res = await fhirService.exportClinicalBundle(sessionId, authToken);
      if (res.data) {
        setSummary(res.data);
      }
      const valRes = await fhirService.validateSession(sessionId, authToken);
      if (valRes.data) {
        setValidation(valRes.data);
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to export and persist FHIR bundle';
      setError(msg);
    } finally {
      setExporting(false);
    }
  };

  const handleValidate = async () => {
    setValidating(true);
    try {
      const valRes = await fhirService.validateSession(sessionId, authToken);
      if (valRes.data) {
        setValidation(valRes.data);
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Validation failed';
      setError(msg);
    } finally {
      setValidating(false);
    }
  };

  const handleCopy = () => {
    if (summary?.fhirBundleJson) {
      navigator.clipboard.writeText(summary.fhirBundleJson);
      setCopied(true);
      setTimeout(() => setCopied(false), 2500);
    }
  };

  const handleDownload = () => {
    if (summary?.fhirBundleJson) {
      const filename = `fhir_bundle_${sessionId.substring(0, 8)}.json`;
      fhirService.downloadJson(filename, summary.fhirBundleJson);
    }
  };

  if (!isOpen) return null;

  const isVerified = summary?.verificationStatus === 'VERIFIED';

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/75 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl border border-slate-200 w-full max-w-5xl max-h-[90vh] flex flex-col overflow-hidden animate-in fade-in zoom-in-95 duration-200">
        
        {/* Header */}
        <div className="px-6 py-4 border-b border-slate-200 bg-slate-50 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="p-2 bg-indigo-100 text-indigo-700 rounded-lg">
              <span className="text-xl">📦</span>
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <h2 className="text-lg font-bold text-slate-900">FHIR R4 Clinical Document Export — Local Demo</h2>
                <span className="text-xs font-mono px-2 py-0.5 bg-slate-200 text-slate-700 rounded">
                  v4.0.1
                </span>
              </div>
              <p className="text-xs text-slate-500">
                Patient: {patientName || 'Intake Patient'} | Session: {sessionId.substring(0, 8)}...
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-2">
            <button
              onClick={onClose}
              className="p-1.5 text-slate-400 hover:text-slate-600 rounded-lg hover:bg-slate-200 transition-colors"
              aria-label="Close"
            >
              ✕
            </button>
          </div>
        </div>

        {/* Verification & Compliance Status Bar */}
        <div className="px-6 py-3 bg-slate-100 border-b border-slate-200 flex flex-wrap items-center justify-between gap-3 text-sm">
          <div className="flex items-center space-x-2">
            <span className="px-2.5 py-1 rounded bg-indigo-100 text-indigo-800 border border-indigo-200 text-xs font-bold font-mono">
              FHIR R4
            </span>
            <span className="px-2.5 py-1 rounded bg-blue-100 text-blue-800 border border-blue-200 text-xs font-bold font-mono">
              VALIDATED
            </span>
            {validation && validation.valid && (
              <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold bg-blue-100 text-blue-800 border border-blue-200">
                ✓ Valid FHIR R4 Document
              </span>
            )}
            {isVerified ? (
              <span className="inline-flex items-center px-2.5 py-1 rounded bg-emerald-100 text-emerald-800 border border-emerald-300 text-xs font-bold font-mono">
                ✓ VERIFIED CLINICAL DOCUMENT (Composition.status = &quot;final&quot;)
              </span>
            ) : (
              <span className="inline-flex items-center px-2.5 py-1 rounded bg-amber-100 text-amber-800 border border-amber-300 text-xs font-bold font-mono">
                ⚠ UNVERIFIED PRE-CONSULT (Composition.status = &quot;preliminary&quot;)
              </span>
            )}
            <span className="px-2.5 py-1 rounded bg-purple-100 text-purple-800 border border-purple-200 text-xs font-bold font-mono">
              LOCAL DEMO
            </span>
          </div>

          <div className="flex items-center space-x-2 text-xs text-slate-600">
            <span>Resources: <strong>{summary?.totalResources || 0}</strong></span>
            <span>•</span>
            <span>Bundle Type: <strong>{summary?.bundleType || 'document'}</strong></span>
          </div>
        </div>

        {/* Medical / Governance Disclaimer */}
        <div className="px-6 py-2 bg-amber-50 border-b border-amber-200 text-amber-900 text-xs flex items-start space-x-2">
          <span className="text-base leading-none">⚖️</span>
          <div>
            <strong>Interoperability Export Boundary: </strong>
            MySQL 8.x is the single primary clinical source of truth. FHIR R4 is a verified interoperability projection.
            MediKiosk is not an autonomous diagnostic system; external delivery to ABDM/HIS/HealthLake adapters is logged as NOT_TRANSMITTED / PENDING.
          </div>
        </div>

        {/* Error message */}
        {error && (
          <div className="mx-6 mt-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700 flex items-center justify-between">
            <span>{error}</span>
            <button onClick={() => setError(null)} className="text-red-500 hover:text-red-700 font-bold ml-2">✕</button>
          </div>
        )}

        {/* Resource Type Breakdown Chips */}
        {summary?.resourceTypeCounts && Object.keys(summary.resourceTypeCounts).length > 0 && (
          <div className="px-6 py-2.5 border-b border-slate-100 bg-white flex flex-wrap items-center gap-1.5">
            <span className="text-xs font-semibold text-slate-500 mr-1">Resources in Bundle:</span>
            {Object.entries(summary.resourceTypeCounts).map(([type, count]) => (
              <span
                key={type}
                className="inline-flex items-center px-2 py-0.5 rounded text-xs font-mono bg-slate-100 text-slate-700 border border-slate-200"
              >
                {type}: <strong className="ml-1 text-slate-900">{count}</strong>
              </span>
            ))}
          </div>
        )}

        {/* Tab Navigation */}
        <div className="px-6 border-b border-slate-200 bg-white flex space-x-4">
          <button
            onClick={() => setActiveTab('JSON')}
            className={`py-2.5 px-3 text-xs font-bold border-b-2 transition-colors ${
              activeTab === 'JSON'
                ? 'border-indigo-600 text-indigo-600'
                : 'border-transparent text-slate-500 hover:text-slate-700'
            }`}
          >
            Formatted FHIR R4 JSON
          </button>
          <button
            onClick={() => setActiveTab('RESOURCES')}
            className={`py-2.5 px-3 text-xs font-bold border-b-2 transition-colors ${
              activeTab === 'RESOURCES'
                ? 'border-indigo-600 text-indigo-600'
                : 'border-transparent text-slate-500 hover:text-slate-700'
            }`}
          >
            Resource Details
          </button>
          <button
            onClick={() => setActiveTab('VALIDATION')}
            className={`py-2.5 px-3 text-xs font-bold border-b-2 transition-colors ${
              activeTab === 'VALIDATION'
                ? 'border-indigo-600 text-indigo-600'
                : 'border-transparent text-slate-500 hover:text-slate-700'
            }`}
          >
            Validation & Invariants {validation?.errors && validation.errors.length > 0 && `(${validation.errors.length})`}
          </button>
        </div>

        {/* Content Body */}
        <div className="p-6 flex-1 overflow-y-auto bg-slate-900 text-slate-100 font-mono text-xs">
          {loading ? (
            <div className="py-20 text-center text-slate-400">
              <div className="inline-block animate-spin rounded-full h-8 w-8 border-2 border-indigo-400 border-t-transparent mb-3"></div>
              <p>Constructing FHIR R4 Clinical Bundle...</p>
            </div>
          ) : activeTab === 'JSON' ? (
            <pre className="whitespace-pre-wrap select-all leading-relaxed text-slate-200">
              {summary?.fhirBundleJson || '// No FHIR bundle generated yet.'}
            </pre>
          ) : activeTab === 'RESOURCES' ? (
            <div className="font-sans text-slate-200 space-y-4">
              <div className="bg-slate-800 p-4 rounded-xl border border-slate-700">
                <h4 className="text-sm font-bold text-white mb-2">Bundle Specification</h4>
                <div className="grid grid-cols-2 gap-2 text-xs">
                  <div><span className="text-slate-400">Bundle ID:</span> {summary?.bundleId}</div>
                  <div><span className="text-slate-400">Bundle Type:</span> {summary?.bundleType}</div>
                  <div><span className="text-slate-400">Verification State:</span> {summary?.verificationStatus}</div>
                  <div><span className="text-slate-400">Composition Status:</span> {summary?.compositionStatus}</div>
                  <div><span className="text-slate-400">Export Timestamp:</span> {summary?.exportedAt || 'Preview'}</div>
                  <div><span className="text-slate-400">Total Entries:</span> {summary?.totalResources}</div>
                </div>
              </div>

              <div className="bg-slate-800 p-4 rounded-xl border border-slate-700">
                <h4 className="text-sm font-bold text-white mb-3">Clinical Resource Entries</h4>
                <div className="space-y-2 text-xs">
                  {summary?.resourceTypeCounts && Object.entries(summary.resourceTypeCounts).map(([type, count]) => (
                    <div key={type} className="flex items-center justify-between p-2 bg-slate-900/50 rounded border border-slate-700">
                      <span className="font-semibold text-indigo-300">{type}</span>
                      <span className="px-2 py-0.5 bg-slate-700 rounded text-slate-300">{count} resource{count > 1 ? 's' : ''}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          ) : (
            <div className="font-sans text-slate-200 space-y-4">
              <div className="bg-slate-800 p-4 rounded-xl border border-slate-700">
                <div className="flex items-center justify-between mb-3">
                  <h4 className="text-sm font-bold text-white">Profile Invariant Validation</h4>
                  <button
                    onClick={handleValidate}
                    disabled={validating}
                    className="px-3 py-1 bg-indigo-600 text-white rounded text-xs font-semibold hover:bg-indigo-700 transition-colors disabled:opacity-50"
                  >
                    {validating ? 'Validating...' : 'Re-run Validation'}
                  </button>
                </div>

                {validation?.valid ? (
                  <div className="p-3 bg-emerald-950/60 border border-emerald-600/40 rounded-lg text-emerald-300 text-xs flex items-center space-x-2">
                    <span>✓</span>
                    <span>All {validation.totalChecked} resources conform to MediKiosk FHIR R4 profile specifications.</span>
                  </div>
                ) : (
                  <div className="space-y-2">
                    <p className="text-xs text-red-400 font-bold">Validation Invariant Failures:</p>
                    <ul className="list-disc list-inside space-y-1 text-xs text-red-300">
                      {validation?.errors.map((err, idx) => (
                        <li key={idx}>{err}</li>
                      ))}
                    </ul>
                  </div>
                )}

                {validation?.warnings && validation.warnings.length > 0 && (
                  <div className="mt-3 space-y-1">
                    <p className="text-xs text-amber-400 font-bold">Warnings:</p>
                    <ul className="list-disc list-inside space-y-1 text-xs text-amber-300">
                      {validation.warnings.map((warn, idx) => (
                        <li key={idx}>{warn}</li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Action Footer */}
        <div className="px-6 py-3 bg-slate-50 border-t border-slate-200 flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-center space-x-2">
            <button
              onClick={handleCopy}
              disabled={!summary?.fhirBundleJson}
              className="px-3 py-1.5 text-xs font-semibold text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-100 transition-colors shadow-sm disabled:opacity-50"
            >
              {copied ? '✓ Copied!' : '📋 Copy JSON'}
            </button>
            <button
              onClick={handleDownload}
              disabled={!summary?.fhirBundleJson}
              className="px-3 py-1.5 text-xs font-semibold text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-100 transition-colors shadow-sm disabled:opacity-50"
            >
              ⬇ Download .json
            </button>
          </div>

          <div className="flex items-center space-x-3">
            <button
              onClick={handleExport}
              disabled={exporting}
              className="px-4 py-2 text-xs font-bold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg transition-colors shadow-sm disabled:opacity-50 flex items-center space-x-1.5"
            >
              <span>{exporting ? 'Exporting...' : '💾 Export & Persist Mappings'}</span>
            </button>
            <button
              onClick={onClose}
              className="px-3.5 py-2 text-xs font-semibold text-slate-600 hover:text-slate-800 transition-colors"
            >
              Close
            </button>
          </div>
        </div>

      </div>
    </div>
  );
};
