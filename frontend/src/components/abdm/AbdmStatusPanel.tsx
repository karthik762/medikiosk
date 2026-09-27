import React, { useState, useEffect, useCallback } from 'react';
import {
  AbdmStatus,
  AbhaStatus,
  IntegrationRecordItem,
} from '../../types/abdm';
import { abdmService } from '../../services/abdmService';
import {
  Shield,
  CheckCircle,
  AlertTriangle,
  XCircle,
  RefreshCw,
  Send,
  Lock,
  FileCheck,
  Clock,
} from 'lucide-react';

interface AbdmStatusPanelProps {
  sessionId: string;
  patientId: string;
  initialAbhaId?: string | null;
  initialAbhaStatus?: AbhaStatus;
  isDoctorVerified?: boolean;
  fhirReady?: boolean;
  token?: string;
  onStatusChange?: () => void;
}

export const AbdmStatusPanel: React.FC<AbdmStatusPanelProps> = ({
  sessionId,
  patientId,
  initialAbhaId = null,
  initialAbhaStatus = 'NOT_PROVIDED',
  isDoctorVerified = false,
  fhirReady = true,
  token,
  onStatusChange,
}) => {
  const [abdmStatus, setAbdmStatus] = useState<AbdmStatus | null>(null);
  const [abhaIdInput, setAbhaIdInput] = useState(initialAbhaId || '');
  const [currentAbhaStatus, setCurrentAbhaStatus] = useState<AbhaStatus>(initialAbhaStatus);
  const [maskedAbha, setMaskedAbha] = useState<string | null>(initialAbhaId ? `XX-XXXX-${initialAbhaId.slice(-4)}` : null);

  const [consentGranted, setConsentGranted] = useState<boolean>(false);
  const [consentRevoked, setConsentRevoked] = useState<boolean>(false);
  const [consentArtefactId, setConsentArtefactId] = useState<string | null>(null);

  const [history, setHistory] = useState<IntegrationRecordItem[]>([]);
  const [isLoadingStatus, setIsLoadingStatus] = useState<boolean>(false);
  const [isVerifyingAbha, setIsVerifyingAbha] = useState<boolean>(false);
  const [isUpdatingConsent, setIsUpdatingConsent] = useState<boolean>(false);
  const [isExchanging, setIsExchanging] = useState<boolean>(false);

  const [feedbackMessage, setFeedbackMessage] = useState<{
    type: 'success' | 'error' | 'info';
    text: string;
  } | null>(null);

  const loadStatusAndHistory = useCallback(async () => {
    setIsLoadingStatus(true);
    try {
      const statusRes = await abdmService.getStatus(token);
      if (statusRes.data) {
        setAbdmStatus(statusRes.data);
      }

      const historyRes = await abdmService.getSessionStatus(sessionId, token);
      if (historyRes.data) {
        setHistory(historyRes.data);

        // Deduce consent status from history if available
        const consentRec = historyRes.data.find(
          (h) => h.operation === 'ABDM_CONSENT_GRANTED' && h.status === 'SUCCESS'
        );
        const revokeRec = historyRes.data.find(
          (h) => h.operation === 'ABDM_CONSENT_REVOKED' && h.status === 'SUCCESS'
        );

        if (consentRec && (!revokeRec || new Date(consentRec.attemptedAt) > new Date(revokeRec.attemptedAt))) {
          setConsentGranted(true);
          setConsentRevoked(false);
          setConsentArtefactId(consentRec.externalReference);
        } else if (revokeRec) {
          setConsentGranted(false);
          setConsentRevoked(true);
        }
      }
    } catch {
      // Graceful fallback for offline mode
      setAbdmStatus({
        mode: 'MOCK',
        isMock: true,
        enabled: true,
        gatewayBaseUrl: 'mock://localhost',
        connected: false,
        message: 'ABDM Mock Mode active',
        timestamp: new Date().toISOString(),
      });
    } finally {
      setIsLoadingStatus(false);
    }
  }, [sessionId, token]);

  useEffect(() => {
    loadStatusAndHistory();
  }, [loadStatusAndHistory]);

  const handleVerifyAbha = async () => {
    if (!abhaIdInput.trim()) {
      setFeedbackMessage({ type: 'error', text: 'Please enter an ABHA ID or address' });
      return;
    }

    setIsVerifyingAbha(true);
    setFeedbackMessage(null);
    try {
      const res = await abdmService.verifyAbha(patientId, { abhaId: abhaIdInput.trim() }, token);
      if (res.data) {
        setCurrentAbhaStatus(res.data.status);
        setMaskedAbha(res.data.maskedAbha);
        if (res.data.verified) {
          setFeedbackMessage({ type: 'success', text: res.data.message });
        } else {
          setFeedbackMessage({ type: 'error', text: res.data.message });
        }
        if (onStatusChange) onStatusChange();
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'ABHA verification failed';
      setFeedbackMessage({ type: 'error', text: msg });
    } finally {
      setIsVerifyingAbha(false);
      loadStatusAndHistory();
    }
  };

  const handleGrantConsent = async () => {
    setIsUpdatingConsent(true);
    setFeedbackMessage(null);
    try {
      const res = await abdmService.grantConsent(sessionId, { patientId }, token);
      if (res.data) {
        setConsentGranted(true);
        setConsentRevoked(false);
        setConsentArtefactId(res.data.externalArtefactId);
        setFeedbackMessage({ type: 'success', text: 'ABDM data-sharing consent granted successfully' });
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to grant consent';
      setFeedbackMessage({ type: 'error', text: msg });
    } finally {
      setIsUpdatingConsent(false);
      loadStatusAndHistory();
    }
  };

  const handleRevokeConsent = async () => {
    setIsUpdatingConsent(true);
    setFeedbackMessage(null);
    try {
      const res = await abdmService.revokeConsent(sessionId, token);
      if (res.data) {
        setConsentGranted(false);
        setConsentRevoked(true);
        setFeedbackMessage({ type: 'info', text: 'ABDM data-sharing consent revoked' });
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to revoke consent';
      setFeedbackMessage({ type: 'error', text: msg });
    } finally {
      setIsUpdatingConsent(false);
      loadStatusAndHistory();
    }
  };

  const handleExchange = async () => {
    if (!isDoctorVerified) {
      setFeedbackMessage({
        type: 'error',
        text: 'Exchange blocked: Summary must be verified by a doctor prior to ABDM exchange',
      });
      return;
    }

    if (!consentGranted || consentRevoked) {
      setFeedbackMessage({
        type: 'error',
        text: 'Exchange blocked: Active ABDM patient consent is required',
      });
      return;
    }

    setIsExchanging(true);
    setFeedbackMessage(null);
    try {
      const res = await abdmService.exchangeClinicalData(sessionId, { notes: 'Clinical care exchange' }, token);
      if (res.data && res.data.status === 'SUCCESS') {
        setFeedbackMessage({
          type: 'success',
          text: `Exchange successful! Transaction ID: ${res.data.transactionId || 'MOCK-TXN'}`,
        });
      } else {
        setFeedbackMessage({
          type: 'error',
          text: res.data?.errorMessage || 'ABDM exchange failed',
        });
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'ABDM exchange failed';
      setFeedbackMessage({ type: 'error', text: msg });
    } finally {
      setIsExchanging(false);
      loadStatusAndHistory();
    }
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 text-slate-100 shadow-xl space-y-6">
      {/* Header & Mode Badge */}
      <div className="flex items-center justify-between border-b border-slate-800 pb-3">
        <div className="flex items-center space-x-2">
          <Shield className="w-5 h-5 text-emerald-400" />
          <h3 className="font-bold text-base tracking-wide">ABDM / ABHA Interoperability</h3>
        </div>
        <div className="flex items-center space-x-2">
          <span
            data-testid="abdm-mode-badge"
            className={`px-2.5 py-0.5 rounded-full text-xs font-semibold uppercase tracking-wider ${
              abdmStatus?.mode === 'PRODUCTION' && abdmStatus?.connected
                ? 'bg-emerald-950 text-emerald-300 border border-emerald-800'
                : 'bg-amber-950 text-amber-300 border border-amber-800'
            }`}
          >
            {abdmStatus?.connected
              ? `${abdmStatus?.mode || 'LIVE'} MODE`
              : `${abdmStatus?.mode || 'MOCK'} MODE (NOT CONNECTED)`}
          </span>
          <button
            onClick={loadStatusAndHistory}
            disabled={isLoadingStatus}
            className="p-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 transition"
            title="Refresh ABDM Status"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isLoadingStatus ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      {/* Mock Mode Explicit Warning */}
      {abdmStatus?.isMock && (
        <div className="p-3 bg-amber-950/40 border border-amber-800/60 rounded-lg flex items-start space-x-2.5 text-xs text-amber-200">
          <AlertTriangle className="w-4 h-4 text-amber-400 mt-0.5 shrink-0" />
          <div>
            <span className="font-bold">Deterministic Mock Adapter Active:</span> External network calls are safely
            isolated. No live government ABDM sandbox/production connection is established.
          </div>
        </div>
      )}

      {/* Feedback Banner */}
      {feedbackMessage && (
        <div
          className={`p-3 rounded-lg text-xs flex items-center space-x-2 ${
            feedbackMessage.type === 'success'
              ? 'bg-emerald-950/60 border border-emerald-800 text-emerald-300'
              : feedbackMessage.type === 'error'
              ? 'bg-rose-950/60 border border-rose-800 text-rose-300'
              : 'bg-cyan-950/60 border border-cyan-800 text-cyan-300'
          }`}
        >
          {feedbackMessage.type === 'success' ? (
            <CheckCircle className="w-4 h-4 shrink-0" />
          ) : feedbackMessage.type === 'error' ? (
            <XCircle className="w-4 h-4 shrink-0" />
          ) : (
            <AlertTriangle className="w-4 h-4 shrink-0" />
          )}
          <span>{feedbackMessage.text}</span>
        </div>
      )}

      {/* Grid: ABHA & Consent */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Section 1: ABHA Digital Identifier */}
        <div className="bg-slate-950/70 border border-slate-800 rounded-lg p-3.5 space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-300">National Health ID (ABHA)</span>
            <span
              className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider ${
                currentAbhaStatus === 'VERIFIED'
                  ? 'bg-emerald-900/60 text-emerald-300 border border-emerald-700'
                  : currentAbhaStatus === 'UNVERIFIED'
                  ? 'bg-amber-900/60 text-amber-300 border border-amber-700'
                  : 'bg-slate-800 text-slate-400 border border-slate-700'
              }`}
            >
              {currentAbhaStatus}
            </span>
          </div>

          <div className="space-y-1.5">
            <div className="flex space-x-1.5">
              <input
                type="text"
                value={abhaIdInput}
                onChange={(e) => setAbhaIdInput(e.target.value)}
                placeholder="e.g. 12-3456-7890-1234 or name@sbx"
                className="flex-1 bg-slate-900 border border-slate-700 rounded px-2.5 py-1.5 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500"
              />
              <button
                onClick={handleVerifyAbha}
                disabled={isVerifyingAbha}
                className="px-3 py-1.5 bg-cyan-600 hover:bg-cyan-500 text-white rounded text-xs font-semibold transition disabled:opacity-50"
              >
                {isVerifyingAbha ? 'Verifying...' : 'Verify'}
              </button>
            </div>
            {maskedAbha && (
              <p className="text-[11px] text-slate-400">
                Masked ID: <span className="font-mono text-cyan-300 font-semibold">{maskedAbha}</span>
              </p>
            )}
          </div>
        </div>

        {/* Section 2: ABDM Data Sharing Consent */}
        <div className="bg-slate-950/70 border border-slate-800 rounded-lg p-3.5 space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-300">ABDM Data Consent</span>
            <span
              className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider ${
                consentGranted && !consentRevoked
                  ? 'bg-emerald-900/60 text-emerald-300 border border-emerald-700'
                  : consentRevoked
                  ? 'bg-rose-900/60 text-rose-300 border border-rose-700'
                  : 'bg-slate-800 text-slate-400 border border-slate-700'
              }`}
            >
              {consentGranted && !consentRevoked ? 'GRANTED' : consentRevoked ? 'REVOKED' : 'REQUIRED'}
            </span>
          </div>

          <div className="flex items-center justify-between pt-1">
            <div className="text-[11px] text-slate-400">
              {consentArtefactId ? (
                <span className="font-mono text-[10px] text-slate-300 truncate max-w-[140px] inline-block">
                  {consentArtefactId}
                </span>
              ) : (
                'Consent required for sharing'
              )}
            </div>
            <div className="space-x-1.5">
              {!consentGranted || consentRevoked ? (
                <button
                  onClick={handleGrantConsent}
                  disabled={isUpdatingConsent}
                  className="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold transition disabled:opacity-50"
                >
                  Grant Consent
                </button>
              ) : (
                <button
                  onClick={handleRevokeConsent}
                  disabled={isUpdatingConsent}
                  className="px-2.5 py-1 bg-rose-700 hover:bg-rose-600 text-white rounded text-xs font-semibold transition disabled:opacity-50"
                >
                  Revoke
                </button>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Gating Checks Bar */}
      <div className="bg-slate-950/80 border border-slate-800 rounded-lg p-3 grid grid-cols-2 md:grid-cols-4 gap-2 text-xs">
        <div className="flex items-center space-x-1.5">
          {isDoctorVerified ? (
            <CheckCircle className="w-3.5 h-3.5 text-emerald-400" />
          ) : (
            <Lock className="w-3.5 h-3.5 text-amber-400" />
          )}
          <span className={isDoctorVerified ? 'text-emerald-300' : 'text-amber-300'}>
            {isDoctorVerified ? 'Doctor Verified' : 'Doctor Unverified'}
          </span>
        </div>

        <div className="flex items-center space-x-1.5">
          {consentGranted && !consentRevoked ? (
            <CheckCircle className="w-3.5 h-3.5 text-emerald-400" />
          ) : (
            <XCircle className="w-3.5 h-3.5 text-rose-400" />
          )}
          <span className={consentGranted && !consentRevoked ? 'text-emerald-300' : 'text-rose-300'}>
            {consentGranted && !consentRevoked ? 'Consent Active' : 'Consent Missing'}
          </span>
        </div>

        <div className="flex items-center space-x-1.5">
          {fhirReady ? (
            <FileCheck className="w-3.5 h-3.5 text-emerald-400" />
          ) : (
            <AlertTriangle className="w-3.5 h-3.5 text-amber-400" />
          )}
          <span className={fhirReady ? 'text-emerald-300' : 'text-amber-300'}>
            {fhirReady ? 'FHIR Bundle Ready' : 'FHIR Incomplete'}
          </span>
        </div>

        <div className="flex items-center space-x-1.5">
          {currentAbhaStatus === 'VERIFIED' ? (
            <CheckCircle className="w-3.5 h-3.5 text-emerald-400" />
          ) : (
            <AlertTriangle className="w-3.5 h-3.5 text-slate-400" />
          )}
          <span className={currentAbhaStatus === 'VERIFIED' ? 'text-emerald-300' : 'text-slate-400'}>
            {currentAbhaStatus === 'VERIFIED' ? 'ABHA Linked' : 'ABHA Optional'}
          </span>
        </div>
      </div>

      {/* Action Trigger */}
      <div className="flex flex-col sm:flex-row items-center justify-between bg-slate-950 p-3.5 rounded-lg border border-slate-800 gap-3">
        <div>
          <div className="text-xs font-semibold text-slate-200">Exchange Clinical Summary with ABDM</div>
          <p className="text-[11px] text-slate-400">
            Transmits the verified FHIR R4 clinical document bundle to the ABDM gateway.
          </p>
        </div>

        <button
          onClick={handleExchange}
          disabled={isExchanging || !isDoctorVerified || !consentGranted || consentRevoked}
          className={`px-4 py-2 rounded-lg text-xs font-bold inline-flex items-center space-x-2 transition ${
            !isDoctorVerified || !consentGranted || consentRevoked
              ? 'bg-slate-800 text-slate-500 cursor-not-allowed border border-slate-700'
              : 'bg-emerald-600 hover:bg-emerald-500 text-white shadow-lg shadow-emerald-600/20'
          }`}
        >
          {isExchanging ? (
            <>
              <RefreshCw className="w-3.5 h-3.5 animate-spin" />
              <span>Transmitting...</span>
            </>
          ) : (
            <>
              <Send className="w-3.5 h-3.5" />
              <span>Exchange with ABDM</span>
            </>
          )}
        </button>
      </div>

      {/* Past Transactions / History */}
      {history.length > 0 && (
        <div className="space-y-2 border-t border-slate-800 pt-3">
          <div className="flex items-center justify-between text-xs text-slate-400 font-semibold">
            <span>Transaction History</span>
            <span>{history.length} Record(s)</span>
          </div>
          <div className="max-h-36 overflow-y-auto space-y-1.5 pr-1">
            {history.map((rec) => (
              <div
                key={rec.id}
                className="bg-slate-950/60 border border-slate-800/80 rounded p-2 text-[11px] flex items-center justify-between"
              >
                <div className="space-y-0.5">
                  <div className="flex items-center space-x-2">
                    <span className="font-semibold text-slate-300">{rec.operation}</span>
                    <span
                      className={`px-1.5 py-0.2 rounded text-[9px] font-bold ${
                        rec.status === 'SUCCESS'
                          ? 'bg-emerald-950 text-emerald-400'
                          : rec.status === 'FAILED'
                          ? 'bg-rose-950 text-rose-400'
                          : 'bg-amber-950 text-amber-400'
                      }`}
                    >
                      {rec.status}
                    </span>
                  </div>
                  {rec.externalReference && (
                    <div className="font-mono text-[10px] text-slate-400 truncate max-w-xs">
                      Ref: {rec.externalReference}
                    </div>
                  )}
                  {rec.errorMessage && <div className="text-rose-400 text-[10px]">{rec.errorMessage}</div>}
                </div>
                <div className="text-[10px] text-slate-500 flex items-center space-x-1">
                  <Clock className="w-3 h-3" />
                  <span>{new Date(rec.attemptedAt).toLocaleTimeString()}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
