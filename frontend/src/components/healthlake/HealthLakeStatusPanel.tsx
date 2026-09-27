import React, { useState, useEffect, useCallback } from 'react';
import {
  HealthLakeStatus,
} from '../../types/healthlake';
import { IntegrationRecordItem } from '../../types/abdm';
import { healthlakeService } from '../../services/healthlakeService';
import {
  Cloud,
  CheckCircle,
  AlertTriangle,
  XCircle,
  RefreshCw,
  Send,
  Lock,
  Clock,
  Database,
} from 'lucide-react';

interface HealthLakeStatusPanelProps {
  sessionId: string;
  patientId: string;
  initialHealthLakeRef?: string | null;
  isDoctorVerified?: boolean;
  token?: string;
  onStatusChange?: () => void;
}

export const HealthLakeStatusPanel: React.FC<HealthLakeStatusPanelProps> = ({
  sessionId,
  patientId,
  initialHealthLakeRef = null,
  isDoctorVerified = false,
  token,
  onStatusChange,
}) => {
  const [hlStatus, setHlStatus] = useState<HealthLakeStatus | null>(null);
  const [currentHealthLakeRef, setCurrentHealthLakeRef] = useState<string | null>(initialHealthLakeRef);
  const [history, setHistory] = useState<IntegrationRecordItem[]>([]);
  const [isLoadingStatus, setIsLoadingStatus] = useState<boolean>(false);
  const [isExporting, setIsExporting] = useState<boolean>(false);
  const [datastoreOverride, setDatastoreOverride] = useState<string>('');
  const [exportNotes, setExportNotes] = useState<string>('');

  const [feedbackMessage, setFeedbackMessage] = useState<{
    type: 'success' | 'error' | 'info';
    text: string;
  } | null>(null);

  const loadStatusAndHistory = useCallback(async () => {
    setIsLoadingStatus(true);
    try {
      const statusRes = await healthlakeService.getStatus(token);
      if (statusRes.data) {
        setHlStatus(statusRes.data);
      }

      const historyRes = await healthlakeService.getSessionStatus(sessionId, token);
      if (historyRes.data) {
        setHistory(historyRes.data);
      }
    } catch {
      setHlStatus({
        mode: 'MOCK',
        isMock: true,
        enabled: true,
        endpoint: 'mock://healthlake.ap-south-1.amazonaws.com/datastore/mock-ds/r4/',
        datastoreId: 'mock-healthlake-datastore',
        region: 'ap-south-1',
        connected: false,
        message: 'AWS HealthLake Mock Mode active. External live connection: NOT CONNECTED.',
        timestamp: new Date().toISOString(),
      });
    } finally {
      setIsLoadingStatus(false);
    }
  }, [sessionId, token]);

  useEffect(() => {
    loadStatusAndHistory();
  }, [loadStatusAndHistory]);

  const handleExport = async () => {
    if (!isDoctorVerified) {
      setFeedbackMessage({
        type: 'error',
        text: 'Doctor Verification Gate: Clinical summary must be verified by a licensed doctor prior to cloud transmission.',
      });
      return;
    }

    setIsExporting(true);
    setFeedbackMessage(null);
    try {
      const res = await healthlakeService.exportClinicalData(
        sessionId,
        {
          healthlakeRef: currentHealthLakeRef || undefined,
          datastoreId: datastoreOverride.trim() ? datastoreOverride.trim() : undefined,
          idempotencyKey: `HL-UI-${sessionId}-${Date.now()}`,
          notes: exportNotes.trim() ? exportNotes.trim() : undefined,
        },
        token
      );

      if (res.data) {
        if (res.data.status === 'SUCCESS') {
          setFeedbackMessage({
            type: 'success',
            text: `Successfully exported to AWS HealthLake. Bundle Ref: ${res.data.bundleId} | Datastore: ${res.data.datastoreId || 'Default'}`,
          });
          if (res.data.healthlakeRef && !currentHealthLakeRef) {
            setCurrentHealthLakeRef(res.data.healthlakeRef);
          }
        } else {
          setFeedbackMessage({
            type: 'error',
            text: res.data.errorMessage || 'Export failed',
          });
        }
      }
      await loadStatusAndHistory();
      if (onStatusChange) onStatusChange();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Export failed';
      setFeedbackMessage({
        type: 'error',
        text: msg,
      });
    } finally {
      setIsExporting(false);
    }
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6 space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-100 pb-4">
        <div className="flex items-center space-x-3">
          <div className="p-2.5 bg-sky-50 text-sky-600 rounded-lg">
            <Cloud className="w-6 h-6" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-lg font-bold text-slate-800">AWS HealthLake Integration</h3>
              <span
                data-testid="healthlake-mock-badge"
                className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-amber-100 text-amber-800 border border-amber-200"
              >
                MOCK MODE (NOT CONNECTED)
              </span>
            </div>
            <p className="text-xs text-slate-500">
              Cloud FHIR R4 document bundle export and datastore synchronization
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={loadStatusAndHistory}
            disabled={isLoadingStatus}
            className="inline-flex items-center px-3 py-1.5 text-xs font-medium text-slate-600 bg-slate-50 hover:bg-slate-100 border border-slate-200 rounded-md transition-colors"
            title="Refresh Status"
          >
            <RefreshCw className={`w-3.5 h-3.5 mr-1.5 ${isLoadingStatus ? 'animate-spin' : ''}`} />
            Refresh
          </button>
        </div>
      </div>

      {/* Safety & Connection Notice */}
      <div className="bg-amber-50 border-l-4 border-amber-500 p-4 rounded-r-lg">
        <div className="flex items-start">
          <AlertTriangle className="w-5 h-5 text-amber-600 mt-0.5 mr-3 flex-shrink-0" />
          <div className="text-sm text-amber-800">
            <span className="font-semibold">AWS HealthLake connection: NOT CONNECTED (MOCK ONLY).</span>
            <p className="mt-1 text-xs text-amber-700">
              This system operates in safe deterministic simulation mode. No live patient data or credentials are transmitted to Amazon Web Services.
            </p>
          </div>
        </div>
      </div>

      {/* Datastore & Adapter Configuration Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="p-3.5 bg-slate-50 border border-slate-200 rounded-lg">
          <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider block">Adapter State</span>
          <div className="mt-1.5 flex items-center space-x-2">
            <span
              className={`inline-block w-2.5 h-2.5 rounded-full ${
                hlStatus?.connected ? 'bg-emerald-500' : 'bg-amber-500'
              }`}
            ></span>
            <span className="text-sm font-medium text-slate-700" data-testid="healthlake-adapter-state">
              {!hlStatus?.enabled
                ? 'Disabled'
                : hlStatus?.connected
                  ? 'Connected'
                  : hlStatus?.isMock
                    ? 'Mock Enabled (Not Connected)'
                    : 'Enabled (Not Connected)'}
            </span>
          </div>
          <span className="text-xs text-slate-400 block mt-1">Region: {hlStatus?.region || 'ap-south-1'}</span>
        </div>

        <div className="p-3.5 bg-slate-50 border border-slate-200 rounded-lg">
          <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider block">Target Datastore</span>
          <div className="mt-1.5 flex items-center space-x-2">
            <Database className="w-4 h-4 text-slate-500" />
            <span className="text-sm font-medium text-slate-700 truncate">
              {hlStatus?.datastoreId || 'mock-healthlake-datastore'}
            </span>
          </div>
          <span className="text-xs text-slate-400 block mt-1 truncate" data-testid="healthlake-endpoint-state">
            {hlStatus?.connected
              ? 'FHIR R4 Endpoint Connected'
              : 'FHIR R4 Endpoint: Not Connected (Mock)'}
          </span>
        </div>

        <div className="p-3.5 bg-slate-50 border border-slate-200 rounded-lg">
          <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider block">Patient Cloud Ref</span>
          <div className="mt-1.5 flex items-center justify-between">
            <span className="text-sm font-medium text-slate-700 font-mono">
              {currentHealthLakeRef || 'Unassigned'}
            </span>
          </div>
          <span className="text-xs text-slate-400 block mt-1">
            {currentHealthLakeRef ? `Linked to Cloud Datastore (Patient: ${patientId.substring(0, 8)})` : `Patient ID: ${patientId.substring(0, 8)}...`}
          </span>
        </div>
      </div>

      {/* Doctor Verification Gate Banner */}
      <div className={`p-4 rounded-lg border flex items-center justify-between ${
        isDoctorVerified ? 'bg-emerald-50 border-emerald-200' : 'bg-rose-50 border-rose-200'
      }`}>
        <div className="flex items-center space-x-3">
          {isDoctorVerified ? (
            <CheckCircle className="w-5 h-5 text-emerald-600 flex-shrink-0" />
          ) : (
            <Lock className="w-5 h-5 text-rose-600 flex-shrink-0" />
          )}
          <div>
            <div
              data-testid="healthlake-gate-status"
              className={`text-sm font-semibold ${isDoctorVerified ? 'text-emerald-900' : 'text-rose-900'}`}
            >
              {isDoctorVerified
                ? 'Doctor Verification Gate: PASSED'
                : 'Doctor Verification Gate: LOCKED (UNVERIFIED / PENDING)'}
            </div>
            <p className={`text-xs ${isDoctorVerified ? 'text-emerald-700' : 'text-rose-700'}`}>
              {isDoctorVerified
                ? 'Clinical summary is verified by a licensed doctor. Eligible for AWS HealthLake export.'
                : 'Export strictly blocked. A licensed doctor must review and verify the summary before transmission.'}
            </p>
          </div>
        </div>
      </div>

      {/* Feedback Banner */}
      {feedbackMessage && (
        <div className={`p-4 rounded-lg flex items-start space-x-3 text-sm ${
          feedbackMessage.type === 'success'
            ? 'bg-emerald-50 text-emerald-800 border border-emerald-200'
            : feedbackMessage.type === 'error'
            ? 'bg-rose-50 text-rose-800 border border-rose-200'
            : 'bg-blue-50 text-blue-800 border border-blue-200'
        }`}>
          {feedbackMessage.type === 'success' && <CheckCircle className="w-5 h-5 text-emerald-600 mt-0.5 flex-shrink-0" />}
          {feedbackMessage.type === 'error' && <XCircle className="w-5 h-5 text-rose-600 mt-0.5 flex-shrink-0" />}
          {feedbackMessage.type === 'info' && <AlertTriangle className="w-5 h-5 text-blue-600 mt-0.5 flex-shrink-0" />}
          <div className="flex-1">{feedbackMessage.text}</div>
          <button
            onClick={() => setFeedbackMessage(null)}
            className="text-slate-400 hover:text-slate-600 text-xs"
          >
            Dismiss
          </button>
        </div>
      )}

      {/* Export Controls */}
      <div className="bg-slate-50 border border-slate-200 rounded-lg p-4 space-y-4">
        <h4 className="text-sm font-semibold text-slate-700">Export Clinical Document Bundle</h4>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">
              Datastore Override (Optional)
            </label>
            <input
              type="text"
              value={datastoreOverride}
              onChange={(e) => setDatastoreOverride(e.target.value)}
              placeholder="Leave empty for default mock datastore"
              className="w-full text-xs px-3 py-2 border border-slate-300 rounded-md focus:ring-1 focus:ring-sky-500 focus:border-sky-500"
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">
              Export Notes / Tags (Optional)
            </label>
            <input
              type="text"
              value={exportNotes}
              onChange={(e) => setExportNotes(e.target.value)}
              placeholder="e.g. Clinical Intake Verified Export"
              className="w-full text-xs px-3 py-2 border border-slate-300 rounded-md focus:ring-1 focus:ring-sky-500 focus:border-sky-500"
            />
          </div>
        </div>

        <div className="flex items-center justify-between pt-2">
          <span className="text-xs text-slate-500">
            Generates FHIR R4 Composition, Encounter, Conditions, and Allergies.
          </span>
          <button
            data-testid="healthlake-export-button"
            onClick={handleExport}
            disabled={!isDoctorVerified || isExporting}
            className={`inline-flex items-center px-4 py-2 text-xs font-medium rounded-md shadow-sm text-white transition-colors ${
              !isDoctorVerified
                ? 'bg-slate-400 cursor-not-allowed'
                : isExporting
                ? 'bg-sky-400 cursor-wait'
                : 'bg-sky-600 hover:bg-sky-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-sky-500'
            }`}
          >
            <Send className={`w-3.5 h-3.5 mr-1.5 ${isExporting ? 'animate-pulse' : ''}`} />
            {isExporting ? 'Exporting to HealthLake...' : 'Export to AWS HealthLake'}
          </button>
        </div>
      </div>

      {/* Export History */}
      <div>
        <h4 className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-3">
          AWS HealthLake Transaction History ({history.length})
        </h4>
        {history.length === 0 ? (
          <div className="text-center py-6 border border-dashed border-slate-200 rounded-lg text-slate-400 text-xs">
            No AWS HealthLake transmissions recorded for this session yet.
          </div>
        ) : (
          <div className="overflow-x-auto border border-slate-200 rounded-lg">
            <table className="min-w-full divide-y divide-slate-200 text-xs">
              <thead className="bg-slate-50">
                <tr>
                  <th className="px-3 py-2 text-left font-medium text-slate-500">Status</th>
                  <th className="px-3 py-2 text-left font-medium text-slate-500">Operation</th>
                  <th className="px-3 py-2 text-left font-medium text-slate-500">External Ref</th>
                  <th className="px-3 py-2 text-left font-medium text-slate-500">Attempts</th>
                  <th className="px-3 py-2 text-left font-medium text-slate-500">Timestamp</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-slate-100">
                {history.map((record) => (
                  <tr key={record.id} className="hover:bg-slate-50">
                    <td className="px-3 py-2 whitespace-nowrap">
                      {record.status === 'SUCCESS' ? (
                        <span className="inline-flex items-center text-emerald-700 font-medium">
                          <CheckCircle className="w-3.5 h-3.5 mr-1 text-emerald-500" />
                          SUCCESS
                        </span>
                      ) : record.status === 'FAILED' ? (
                        <span className="inline-flex items-center text-rose-700 font-medium" title={record.errorMessage || undefined}>
                          <XCircle className="w-3.5 h-3.5 mr-1 text-rose-500" />
                          FAILED
                        </span>
                      ) : (
                        <span className="inline-flex items-center text-amber-700 font-medium">
                          <Clock className="w-3.5 h-3.5 mr-1 text-amber-500" />
                          PENDING
                        </span>
                      )}
                    </td>
                    <td className="px-3 py-2 whitespace-nowrap font-mono text-slate-600">
                      {record.operation}
                    </td>
                    <td className="px-3 py-2 whitespace-nowrap font-mono text-slate-700">
                      {record.externalReference || '—'}
                    </td>
                    <td className="px-3 py-2 whitespace-nowrap text-slate-500">
                      {record.attemptCount}
                    </td>
                    <td className="px-3 py-2 whitespace-nowrap text-slate-500">
                      {new Date(record.attemptedAt).toLocaleString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
