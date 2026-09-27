import React, { useState, useEffect, useCallback } from 'react';
import {
  HisStatus,
  HisPatientCandidate,
} from '../../types/his';
import { IntegrationRecordItem } from '../../types/abdm';
import { hisService } from '../../services/hisService';
import {
  Building2,
  CheckCircle,
  AlertTriangle,
  XCircle,
  RefreshCw,
  Send,
  Lock,
  Search,
  Link,
  UserCheck,
  Clock,
} from 'lucide-react';

interface HisStatusPanelProps {
  sessionId: string;
  patientId: string;
  initialHisId?: string | null;
  isDoctorVerified?: boolean;
  token?: string;
  onStatusChange?: () => void;
}

export const HisStatusPanel: React.FC<HisStatusPanelProps> = ({
  sessionId,
  patientId,
  initialHisId = null,
  isDoctorVerified = false,
  token,
  onStatusChange,
}) => {
  const [hisStatus, setHisStatus] = useState<HisStatus | null>(null);
  const [currentHisId, setCurrentHisId] = useState<string | null>(initialHisId);
  const [history, setHistory] = useState<IntegrationRecordItem[]>([]);
  const [isLoadingStatus, setIsLoadingStatus] = useState<boolean>(false);
  const [isExporting, setIsExporting] = useState<boolean>(false);
  const [intakeQueue, setIntakeQueue] = useState<string>('OPD_INTAKE');

  // MPI Search Modal State
  const [isSearchModalOpen, setIsSearchModalOpen] = useState<boolean>(false);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [searchType, setSearchType] = useState<string>('ALL');
  const [isSearching, setIsSearching] = useState<boolean>(false);
  const [searchResults, setSearchResults] = useState<HisPatientCandidate[]>([]);
  const [isLinking, setIsLinking] = useState<boolean>(false);

  // Manual link input
  const [manualHisIdInput, setManualHisIdInput] = useState<string>('');

  const [feedbackMessage, setFeedbackMessage] = useState<{
    type: 'success' | 'error' | 'info';
    text: string;
  } | null>(null);

  const loadStatusAndHistory = useCallback(async () => {
    setIsLoadingStatus(true);
    try {
      const statusRes = await hisService.getStatus(token);
      if (statusRes.data) {
        setHisStatus(statusRes.data);
      }

      const historyRes = await hisService.getSessionStatus(sessionId, token);
      if (historyRes.data) {
        setHistory(historyRes.data);
      }
    } catch {
      setHisStatus({
        mode: 'MOCK',
        isMock: true,
        enabled: true,
        baseUrl: 'mock://localhost/his-api',
        connected: false,
        message: 'HIS Mock Mode active. External live connection: NOT CONNECTED.',
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
        text: 'Doctor Verification Gate: Clinical summary must be verified by a licensed doctor prior to hospital transmission.',
      });
      return;
    }

    setIsExporting(true);
    setFeedbackMessage(null);
    try {
      const res = await hisService.exportClinicalData(
        sessionId,
        {
          intakeQueue,
          hisId: currentHisId || undefined,
          idempotencyKey: `HIS-UI-${sessionId}-${Date.now()}`,
        },
        token
      );

      if (res.data) {
        if (res.data.status === 'SUCCESS') {
          setFeedbackMessage({
            type: 'success',
            text: `Successfully exported to HIS intake queue (${intakeQueue}). Encounter Ref: ${res.data.encounterReference}`,
          });
          if (res.data.hisId) {
            setCurrentHisId(res.data.hisId);
          }
        } else {
          setFeedbackMessage({
            type: 'error',
            text: `HIS Export Failed: ${res.data.errorMessage || res.data.message}`,
          });
        }
        if (onStatusChange) onStatusChange();
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'HIS export failed';
      setFeedbackMessage({ type: 'error', text: msg });
    } finally {
      setIsExporting(false);
      loadStatusAndHistory();
    }
  };

  const handleSearchMpi = async () => {
    if (!searchQuery.trim()) {
      setFeedbackMessage({ type: 'error', text: 'Enter search term for patient lookup' });
      return;
    }

    setIsSearching(true);
    setSearchResults([]);
    try {
      const res = await hisService.searchPatients(searchQuery.trim(), searchType, token);
      if (res.data) {
        setSearchResults(res.data.candidates || []);
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Hospital MPI search failed';
      setFeedbackMessage({ type: 'error', text: msg });
    } finally {
      setIsSearching(false);
    }
  };

  const handleLinkPatient = async (selectedHisId: string) => {
    setIsLinking(true);
    setFeedbackMessage(null);
    try {
      const res = await hisService.linkPatient(
        patientId,
        {
          hisId: selectedHisId.trim(),
          notes: 'Linked via MediKiosk review workstation',
        },
        token
      );

      if (res.data) {
        setCurrentHisId(res.data.hisId);
        setFeedbackMessage({
          type: 'success',
          text: `Patient associated with hospital MRN: ${res.data.hisId}`,
        });
        setIsSearchModalOpen(false);
        setManualHisIdInput('');
        if (onStatusChange) onStatusChange();
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to link hospital ID';
      setFeedbackMessage({ type: 'error', text: msg });
    } finally {
      setIsLinking(false);
      loadStatusAndHistory();
    }
  };

  return (
    <div className="bg-white rounded-xl shadow-md border border-slate-200 p-6 space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 pb-4 border-b border-slate-100">
        <div className="flex items-center gap-3">
          <div className="p-2.5 bg-blue-50 text-blue-600 rounded-lg">
            <Building2 className="w-6 h-6" />
          </div>
          <div>
            <h3 className="font-semibold text-lg text-slate-800">Hospital Information System (HIS)</h3>
            <p className="text-xs text-slate-500">Outpatient department intake queue & verified bundle export</p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {hisStatus?.isMock && (
            <span
              data-testid="his-mock-badge"
              className="px-2.5 py-1 text-xs font-semibold rounded-full bg-amber-50 text-amber-700 border border-amber-200"
            >
              MOCK MODE (NOT CONNECTED)
            </span>
          )}

          <button
            onClick={loadStatusAndHistory}
            disabled={isLoadingStatus}
            className="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-md transition"
            title="Refresh HIS status"
          >
            <RefreshCw className={`w-4 h-4 ${isLoadingStatus ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      {/* Feedback Banner */}
      {feedbackMessage && (
        <div
          className={`p-3.5 rounded-lg flex items-start gap-2.5 text-sm ${
            feedbackMessage.type === 'success'
              ? 'bg-emerald-50 text-emerald-800 border border-emerald-200'
              : feedbackMessage.type === 'error'
              ? 'bg-rose-50 text-rose-800 border border-rose-200'
              : 'bg-blue-50 text-blue-800 border border-blue-200'
          }`}
        >
          {feedbackMessage.type === 'success' ? (
            <CheckCircle className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
          ) : (
            <AlertTriangle className="w-4 h-4 text-rose-600 shrink-0 mt-0.5" />
          )}
          <span>{feedbackMessage.text}</span>
        </div>
      )}

      {/* Doctor Verification Gate Status */}
      <div
        data-testid="his-gate-status"
        className={`p-4 rounded-xl border flex items-start gap-3.5 ${
          isDoctorVerified
            ? 'bg-emerald-50/70 border-emerald-200 text-emerald-900'
            : 'bg-amber-50/70 border-amber-200 text-amber-900'
        }`}
      >
        {isDoctorVerified ? (
          <CheckCircle className="w-5 h-5 text-emerald-600 shrink-0 mt-0.5" />
        ) : (
          <Lock className="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />
        )}
        <div className="space-y-1 text-sm">
          <div className="font-semibold flex items-center gap-2">
            <span>Doctor Verification Gate:</span>
            <span
              className={`px-2 py-0.5 text-xs rounded-full font-medium ${
                isDoctorVerified ? 'bg-emerald-100 text-emerald-800' : 'bg-amber-100 text-amber-800'
              }`}
            >
              {isDoctorVerified ? 'VERIFIED' : 'UNVERIFIED / PENDING'}
            </span>
          </div>
          <p className="text-xs opacity-90">
            {isDoctorVerified
              ? 'Clinical intake summary has been verified by a licensed physician. It is approved for external hospital synchronization.'
              : 'External HIS export is strictly gated. Unverified AI draft summaries cannot be transmitted to hospital queues.'}
          </p>
        </div>
      </div>

      {/* Hospital MRN / ID Linking Section */}
      <div className="p-4 bg-slate-50 rounded-xl border border-slate-200/80 space-y-3">
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-2">
          <div>
            <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Hospital Identifier (HIS MRN)</div>
            <div className="text-sm font-medium text-slate-800 flex items-center gap-2 mt-0.5">
              {currentHisId ? (
                <>
                  <span className="font-mono bg-blue-100 text-blue-800 px-2 py-0.5 rounded text-xs">
                    {currentHisId}
                  </span>
                  <span className="text-xs text-emerald-600 flex items-center gap-1">
                    <CheckCircle className="w-3.5 h-3.5" /> Linked
                  </span>
                </>
              ) : (
                <span className="text-xs text-slate-400 italic">No hospital MRN associated yet</span>
              )}
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => setIsSearchModalOpen(true)}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium bg-white text-slate-700 border border-slate-300 rounded-lg hover:bg-slate-50 shadow-sm transition"
            >
              <Search className="w-3.5 h-3.5 text-blue-600" />
              Search Hospital MPI
            </button>
          </div>
        </div>

        {/* Quick manual link if not linked */}
        {!currentHisId && (
          <div className="flex items-center gap-2 pt-2 border-t border-slate-200/60">
            <input
              type="text"
              placeholder="Or enter hospital MRN directly..."
              value={manualHisIdInput}
              onChange={(e) => setManualHisIdInput(e.target.value)}
              className="px-3 py-1.5 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-blue-500 flex-1"
            />
            <button
              onClick={() => handleLinkPatient(manualHisIdInput)}
              disabled={isLinking || !manualHisIdInput.trim()}
              className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-medium bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 transition"
            >
              <Link className="w-3.5 h-3.5" />
              Link
            </button>
          </div>
        )}
      </div>

      {/* Export Action Section */}
      <div className="p-4 bg-slate-50 rounded-xl border border-slate-200/80 space-y-4">
        <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Clinical Summary Export</div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div>
            <label className="block text-xs font-medium text-slate-700 mb-1">Designated Intake Queue</label>
            <select
              value={intakeQueue}
              onChange={(e) => setIntakeQueue(e.target.value)}
              className="w-full text-xs border border-slate-300 rounded-lg p-2 bg-white focus:outline-none focus:ring-1 focus:ring-blue-500"
            >
              <option value="OPD_INTAKE">OPD Intake Queue</option>
              <option value="TRIAGE">Triage & Assessment Queue</option>
              <option value="EMERGENCY">Emergency Fast-Track</option>
            </select>
          </div>

          <div className="flex items-end">
            <button
              data-testid="his-export-button"
              onClick={handleExport}
              disabled={!isDoctorVerified || isExporting}
              className={`w-full inline-flex items-center justify-center gap-2 px-4 py-2 text-xs font-semibold rounded-lg shadow-sm transition ${
                isDoctorVerified
                  ? 'bg-blue-600 text-white hover:bg-blue-700 cursor-pointer'
                  : 'bg-slate-200 text-slate-400 cursor-not-allowed border border-slate-300'
              }`}
            >
              <Send className={`w-3.5 h-3.5 ${isExporting ? 'animate-pulse' : ''}`} />
              {isExporting ? 'Transmitting to HIS...' : 'Export to Hospital Queue'}
            </button>
          </div>
        </div>
      </div>

      {/* History Table */}
      <div className="space-y-3">
        <div className="flex justify-between items-center">
          <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-500">HIS Integration History</h4>
          <span className="text-xs text-slate-400">{history.length} event(s)</span>
        </div>

        {history.length === 0 ? (
          <div className="text-center py-6 border border-dashed border-slate-200 rounded-xl text-xs text-slate-400">
            No hospital integration transactions recorded for this session.
          </div>
        ) : (
          <div className="overflow-x-auto border border-slate-200 rounded-xl">
            <table className="w-full text-left text-xs text-slate-600">
              <thead className="bg-slate-50 text-slate-700 font-semibold border-b border-slate-200">
                <tr>
                  <th className="py-2.5 px-3">Operation</th>
                  <th className="py-2.5 px-3">Status</th>
                  <th className="py-2.5 px-3">Encounter / Ref</th>
                  <th className="py-2.5 px-3">Time</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {history.map((item) => (
                  <tr key={item.id} className="hover:bg-slate-50/50">
                    <td className="py-2.5 px-3 font-mono text-slate-800">{item.operation}</td>
                    <td className="py-2.5 px-3">
                      <span
                        className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-[10px] font-semibold ${
                          item.status === 'SUCCESS'
                            ? 'bg-emerald-50 text-emerald-700'
                            : item.status === 'FAILED'
                            ? 'bg-rose-50 text-rose-700'
                            : 'bg-amber-50 text-amber-700'
                        }`}
                      >
                        {item.status === 'SUCCESS' ? (
                          <CheckCircle className="w-3 h-3" />
                        ) : item.status === 'FAILED' ? (
                          <XCircle className="w-3 h-3" />
                        ) : (
                          <Clock className="w-3 h-3" />
                        )}
                        {item.status}
                      </span>
                    </td>
                    <td className="py-2.5 px-3 font-mono text-[11px] text-slate-600">
                      {item.externalReference || '—'}
                    </td>
                    <td className="py-2.5 px-3 text-slate-400 text-[11px]">
                      {new Date(item.attemptedAt).toLocaleTimeString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Hospital MPI Search Modal */}
      {isSearchModalOpen && (
        <div
          data-testid="mpi-search-modal"
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-xs p-4"
        >
          <div className="bg-white rounded-2xl shadow-xl max-w-xl w-full p-6 space-y-4 max-h-[85vh] flex flex-col">
            <div className="flex justify-between items-center border-b border-slate-100 pb-3">
              <div className="flex items-center gap-2">
                <Building2 className="w-5 h-5 text-blue-600" />
                <h3 className="font-semibold text-slate-800 text-base">Hospital Master Patient Index (MPI)</h3>
              </div>
              <button
                onClick={() => setIsSearchModalOpen(false)}
                className="text-slate-400 hover:text-slate-600 text-lg leading-none"
              >
                &times;
              </button>
            </div>

            <div className="space-y-3">
              <div className="flex gap-2">
                <input
                  type="text"
                  placeholder="Search by Name, Phone, or MRN..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleSearchMpi()}
                  className="px-3 py-2 text-xs border border-slate-300 rounded-lg flex-1 focus:outline-none focus:ring-1 focus:ring-blue-500"
                />
                <select
                  value={searchType}
                  onChange={(e) => setSearchType(e.target.value)}
                  className="px-3 py-2 text-xs border border-slate-300 rounded-lg bg-white focus:outline-none focus:ring-1 focus:ring-blue-500"
                >
                  <option value="ALL">All Fields</option>
                  <option value="NAME">Name</option>
                  <option value="PHONE">Phone</option>
                  <option value="MRN">MRN</option>
                </select>
                <button
                  data-testid="mpi-search-submit-btn"
                  onClick={handleSearchMpi}
                  disabled={isSearching || !searchQuery.trim()}
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg text-xs font-semibold hover:bg-blue-700 disabled:opacity-50 inline-flex items-center gap-1.5"
                >
                  <Search className={`w-3.5 h-3.5 ${isSearching ? 'animate-spin' : ''}`} />
                  Search
                </button>
              </div>
            </div>

            {/* Candidate List */}
            <div className="overflow-y-auto space-y-2 flex-1 pr-1">
              {searchResults.length === 0 ? (
                <div className="text-center py-8 text-xs text-slate-400">
                  {isSearching ? 'Querying hospital directory...' : 'Enter query and search for hospital records.'}
                </div>
              ) : (
                searchResults.map((cand) => (
                  <div
                    key={cand.hisId}
                    className="p-3 rounded-xl border border-slate-200 hover:border-blue-300 hover:bg-blue-50/30 transition flex justify-between items-center gap-3"
                  >
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span className="font-semibold text-xs text-slate-800">{cand.displayName}</span>
                        <span className="font-mono text-[10px] bg-blue-100 text-blue-800 px-2 py-0.5 rounded">
                          {cand.hisId}
                        </span>
                      </div>
                      <div className="text-[11px] text-slate-500 flex flex-wrap gap-x-3 gap-y-1">
                        <span>Phone: {cand.phone || 'N/A'}</span>
                        <span>Gender: {cand.gender}</span>
                        <span>Dept: {cand.department}</span>
                        {cand.roomOrWard && <span>Location: {cand.roomOrWard}</span>}
                      </div>
                    </div>

                    <button
                      data-testid={`link-candidate-${cand.hisId}`}
                      onClick={() => handleLinkPatient(cand.hisId)}
                      disabled={isLinking}
                      className="px-3 py-1.5 bg-blue-600 text-white rounded-lg text-xs font-medium hover:bg-blue-700 disabled:opacity-50 shrink-0 inline-flex items-center gap-1"
                    >
                      <UserCheck className="w-3.5 h-3.5" />
                      Link
                    </button>
                  </div>
                ))
              )}
            </div>

            <div className="pt-2 border-t border-slate-100 flex justify-end">
              <button
                onClick={() => setIsSearchModalOpen(false)}
                className="px-4 py-1.5 text-xs text-slate-600 hover:bg-slate-100 rounded-lg"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
