import React, { useState, useEffect, useCallback } from 'react';
import { ClinicalSummary } from '../../types/clinicalSummary';
import { clinicalSummaryService } from '../../services/clinicalSummaryService';

interface ClinicalSummarySectionProps {
  sessionId: string;
  authToken?: string;
  onSummaryLoaded?: (summary: ClinicalSummary) => void;
}

export const ClinicalSummarySection: React.FC<ClinicalSummarySectionProps> = ({
  sessionId,
  authToken,
  onSummaryLoaded,
}) => {
  const [summary, setSummary] = useState<ClinicalSummary | null>(null);
  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const loadSummary = useCallback(async () => {
    if (!sessionId) return;
    setLoading(true);
    try {
      const res = await clinicalSummaryService.getSummaryBySession(sessionId, authToken);
      if (res.data) {
        setSummary(res.data);
        if (onSummaryLoaded) {
          onSummaryLoaded(res.data);
        }
      }
    } catch {
      // Summary might not have been generated yet
      setSummary(null);
    } fontally: {
      setLoading(false);
    }
  }, [sessionId, authToken, onSummaryLoaded]);

  useEffect(() => {
    loadSummary();
  }, [loadSummary]);

  const handleGenerate = async () => {
    setGenerating(true);
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      const res = await clinicalSummaryService.generateClinicalSummary(sessionId, authToken);
      if (res.data) {
        setSummary(res.data);
        setSuccessMessage('AI Clinical Summary generated successfully.');
        if (onSummaryLoaded) {
          onSummaryLoaded(res.data);
        }
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to generate clinical summary.';
      setErrorMessage(msg);
    } finally {
      setGenerating(false);
    }
  };

  const getPriorityColor = (priority?: string) => {
    switch (priority) {
      case 'EMERGENCY':
        return 'bg-red-600 text-white border-red-700';
      case 'URGENT':
        return 'bg-amber-600 text-white border-amber-700';
      case 'PRIORITY':
        return 'bg-blue-600 text-white border-blue-700';
      case 'ROUTINE':
      default:
        return 'bg-emerald-600 text-white border-emerald-700';
    }
  };

  const renderProvenanceBadge = (source?: string) => {
    const s = (source || 'PATIENT_REPORTED').toUpperCase();
    if (s.includes('OCR') || s.includes('DOCUMENT')) {
      return (
        <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-100 text-amber-800 border border-amber-200">
          📄 OCR / Document
        </span>
      );
    }
    if (s.includes('TRIAGE')) {
      return (
        <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-red-100 text-red-800 border border-red-200">
          ⚡ Triage Engine
        </span>
      );
    }
    if (s.includes('AYURVEDA')) {
      return (
        <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-100 text-emerald-800 border border-emerald-200">
          🌿 Ayurveda Engine
        </span>
      );
    }
    if (s.includes('AI') || s.includes('NLP')) {
      return (
        <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-violet-100 text-violet-800 border border-violet-200">
          🤖 AI Structuring
        </span>
      );
    }
    if (s.includes('CLINICIAN') || s.includes('DOCTOR')) {
      return (
        <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-purple-100 text-purple-800 border border-purple-200">
          🩺 Clinician Verified
        </span>
      );
    }
    return (
      <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-slate-100 text-slate-700 border border-slate-200">
        👤 Patient Reported
      </span>
    );
  };

  const ayurvedaData = summary?.ayurveda || summary?.ayurvedaSummary;

  return (
    <div className="bg-white rounded-xl shadow-lg border border-slate-200 overflow-hidden my-6">
      {/* Non-Diagnostic Doctor Header Disclaimer */}
      <div className="bg-amber-50 border-b border-amber-200 p-4 flex items-start space-x-3">
        <svg
          className="w-6 h-6 text-amber-600 flex-shrink-0 mt-0.5"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
          />
        </svg>
        <div className="text-xs md:text-sm text-amber-900 leading-relaxed">
          <span className="font-bold uppercase tracking-wide">
            Pre-Consultation Assistive Summary — For Doctor Review Only:
          </span>{' '}
          MediKiosk is NOT an autonomous diagnostic system. This summary structures intake facts and
          does NOT diagnose, prescribe, or verify clinical decisions. The attending physician
          remains the sole clinical authority.
        </div>
      </div>

      <div className="p-6">
        {/* Title Bar & Actions */}
        <div className="flex flex-wrap items-center justify-between gap-4 pb-6 border-b border-slate-200">
          <div>
            <h2 className="text-xl font-bold text-slate-900 flex items-center gap-2">
              <span>🩺 Pre-Consult Clinical Summary</span>
              {summary && (
                <span className="text-xs px-2.5 py-0.5 rounded-full font-semibold bg-indigo-50 text-indigo-700 border border-indigo-200">
                  {summary.metadata?.isMock ? 'Mock AI' : summary.metadata?.generationModel || summary.metadata?.providerName} (v
                  {summary.metadata?.version || 1})
                </span>
              )}
            </h2>
            <p className="text-sm text-slate-500 mt-1">
              Automated clinical pre-consult synthesis with end-to-end source grounding
            </p>
          </div>

          <div className="flex items-center space-x-3">
            <button
              id="generate-summary-btn"
              onClick={handleGenerate}
              disabled={generating || loading}
              className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 disabled:bg-slate-300 text-white text-sm font-semibold rounded-lg shadow-sm transition-all duration-150 flex items-center space-x-2"
            >
              {generating ? (
                <>
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  <span>Structuring Intake...</span>
                </>
              ) : summary ? (
                <>
                  <span>🔄 Regenerate Summary</span>
                </>
              ) : (
                <>
                  <span>✨ Generate AI Summary</span>
                </>
              )}
            </button>
          </div>
        </div>

        {/* Notifications */}
        {errorMessage && (
          <div className="mt-4 p-4 rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm flex items-center justify-between">
            <span>{errorMessage}</span>
            <button onClick={() => setErrorMessage(null)} className="text-red-500 hover:text-red-800">
              ✕
            </button>
          </div>
        )}
        {successMessage && (
          <div className="mt-4 p-4 rounded-lg bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm flex items-center justify-between">
            <span>{successMessage}</span>
            <button onClick={() => setSuccessMessage(null)} className="text-emerald-500 hover:text-emerald-800">
              ✕
            </button>
          </div>
        )}

        {/* Loading placeholder */}
        {loading && (
          <div className="py-12 text-center text-slate-500">
            <div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
            <p className="text-sm font-medium">Loading clinical summary...</p>
          </div>
        )}

        {/* Empty state */}
        {!loading && !summary && (
          <div className="py-12 text-center text-slate-500">
            <div className="text-4xl mb-3">📋</div>
            <h3 className="text-base font-semibold text-slate-700">No Summary Generated Yet</h3>
            <p className="text-sm text-slate-500 max-w-md mx-auto mt-1 mb-5">
              Click &quot;Generate AI Summary&quot; to synthesize patient responses, symptoms, medical
              history, OCR document findings, and triage indicators into a structured report.
            </p>
          </div>
        )}

        {/* Render Generated Summary */}
        {!loading && summary && (
          <div className="space-y-6 mt-6">
            {/* Patient Info Header if available */}
            {summary.patientInfo && (
              <div className="bg-slate-50 p-4 rounded-xl border border-slate-200">
                <div className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">
                  Patient Context & Demographics
                </div>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-3 text-xs text-slate-800">
                  <div>
                    <span className="text-slate-400 block">Identifier:</span>
                    <span className="font-mono font-semibold">{summary.patientInfo.patientIdentifier || summary.patientId}</span>
                  </div>
                  <div>
                    <span className="text-slate-400 block">Name:</span>
                    <span className="font-semibold">{summary.patientInfo.firstName} {summary.patientInfo.lastName}</span>
                  </div>
                  <div>
                    <span className="text-slate-400 block">Age / Gender:</span>
                    <span className="font-semibold">{summary.patientInfo.age ? `${summary.patientInfo.age} yrs` : 'N/A'}, {summary.patientInfo.gender || 'N/A'}</span>
                  </div>
                  <div>
                    <span className="text-slate-400 block">Encounter / Session:</span>
                    <span className="font-mono text-slate-600">{summary.sessionId}</span>
                  </div>
                </div>
              </div>
            )}

            {/* Triage Banner & Verification Banner */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Deterministic Triage Card */}
              {summary.triage && (
                <div
                  className={`p-4 rounded-xl border flex flex-col justify-between ${getPriorityColor(
                    summary.triage.priority
                  )}`}
                >
                  <div>
                    <div className="flex items-center justify-between">
                      <span className="text-xs uppercase tracking-wider font-bold opacity-90">
                        Rapid Triage Result (Rule Engine)
                      </span>
                      <span className="px-2 py-0.5 rounded bg-black/20 text-xs font-semibold">
                        {summary.triage.priority}
                      </span>
                    </div>
                    <p className="text-sm mt-2 font-medium">
                      {summary.triage.summaryReason || summary.triage.explanation || 'Triage completed'}
                    </p>
                    {summary.triage.triggeredRules && summary.triage.triggeredRules.length > 0 && (
                      <div className="mt-2 text-xs opacity-90">
                        Triggered rules: {Array.isArray(summary.triage.triggeredRules)
                          ? summary.triage.triggeredRules.map(r => typeof r === 'string' ? r : r.ruleCode || r.ruleName || JSON.stringify(r)).join(', ')
                          : String(summary.triage.triggeredRules)}
                      </div>
                    )}
                  </div>
                  <div className="text-[11px] opacity-75 mt-3 pt-2 border-t border-white/20">
                    Source: RULE_ENGINE (Deterministic, non-LLM)
                  </div>
                </div>
              )}

              {/* Verification Status Card */}
              <div className="p-4 rounded-xl border border-slate-200 bg-slate-50 flex flex-col justify-between">
                <div>
                  <div className="flex items-center justify-between">
                    <span className="text-xs uppercase tracking-wider font-bold text-slate-500">
                      Doctor Verification
                    </span>
                    <span
                      className={`px-2.5 py-0.5 rounded text-xs font-semibold ${
                        summary.doctorVerification?.status === 'VERIFIED'
                          ? 'bg-emerald-100 text-emerald-800'
                          : summary.doctorVerification?.status === 'AMENDED'
                          ? 'bg-blue-100 text-blue-800'
                          : 'bg-amber-100 text-amber-800'
                      }`}
                    >
                      {summary.doctorVerification?.status || 'PENDING'}
                    </span>
                  </div>
                  <p className="text-sm text-slate-700 mt-2">
                    {summary.doctorVerification?.status === 'PENDING'
                      ? 'Awaiting clinical review and verification by the attending doctor.'
                      : `Verified by Dr. ${summary.doctorVerification?.verifiedByName || summary.doctorVerification?.verifiedBy || 'Physician'}`}
                  </p>
                  {(summary.doctorVerification?.clinicalNotes || summary.doctorVerification?.comments) && (
                    <div className="mt-2 text-xs text-slate-600 bg-white p-2 rounded border border-slate-200">
                      <strong>Notes:</strong> {summary.doctorVerification.clinicalNotes || summary.doctorVerification.comments}
                    </div>
                  )}
                </div>
                <div className="text-[11px] text-slate-400 mt-3 pt-2 border-t border-slate-200">
                  Status: {summary.status}
                </div>
              </div>
            </div>

            {/* Chief Complaint */}
            <div className="bg-indigo-50/50 p-4 rounded-xl border border-indigo-100">
              <span className="text-xs font-bold text-indigo-700 uppercase tracking-wide">
                Chief Complaint
              </span>
              <p className="text-base font-semibold text-slate-900 mt-1">
                {summary.chiefComplaint || 'None recorded'}
              </p>
            </div>

            {/* Symptoms Grid */}
            <div>
              <h3 className="text-sm font-bold text-slate-900 uppercase tracking-wide mb-3 flex items-center justify-between">
                <span>Symptoms ({summary.symptoms?.length || 0})</span>
              </h3>
              {summary.symptoms && summary.symptoms.length > 0 ? (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  {summary.symptoms.map((sym, idx) => (
                    <div
                      key={idx}
                      className="p-3.5 rounded-lg border border-slate-200 bg-white shadow-sm flex flex-col justify-between"
                    >
                      <div>
                        <div className="flex items-start justify-between">
                          <span className="font-semibold text-slate-800 text-sm">{sym.name}</span>
                          <span
                            className={`text-xs px-2 py-0.5 rounded-full font-bold ${
                              sym.severity >= 8
                                ? 'bg-red-100 text-red-700'
                                : sym.severity >= 5
                                ? 'bg-amber-100 text-amber-700'
                                : 'bg-slate-100 text-slate-700'
                            }`}
                          >
                            Severity: {sym.severity}/10
                          </span>
                        </div>
                        <div className="grid grid-cols-2 gap-x-2 gap-y-1 mt-2 text-xs text-slate-600">
                          <div>
                            <span className="text-slate-400">Duration:</span> {sym.duration || 'Unspecified'}
                          </div>
                          <div>
                            <span className="text-slate-400">Onset:</span> {sym.onset || 'Unspecified'}
                          </div>
                          <div>
                            <span className="text-slate-400">Location:</span> {sym.location || 'Unspecified'}
                          </div>
                          <div>
                            <span className="text-slate-400">Frequency:</span> {sym.frequency || 'Unspecified'}
                          </div>
                        </div>

                        {/* Codings badges if present */}
                        {sym.coding && sym.coding.length > 0 && (
                          <div className="mt-2 flex flex-wrap gap-1">
                            {sym.coding.map((cd, cIdx) => (
                              <span key={cIdx} className="text-[10px] px-1.5 py-0.5 rounded font-mono bg-indigo-50 text-indigo-700 border border-indigo-200">
                                {cd.system}: {cd.code} ({cd.display})
                              </span>
                            ))}
                          </div>
                        )}
                      </div>

                      <div className="mt-2 pt-2 border-t border-slate-100 text-[11px] text-slate-400 flex items-center justify-between">
                        <span>Provenance:</span>
                        {renderProvenanceBadge(sym.source)}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-xs text-slate-500 italic">No symptoms recorded.</p>
              )}
            </div>

            {/* Medical History & Current Medications */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Medical History */}
              <div className="p-4 rounded-xl border border-slate-200 bg-slate-50/50">
                <h4 className="text-xs font-bold text-slate-700 uppercase tracking-wide mb-2">
                  Past Medical History ({summary.medicalHistory?.length || 0})
                </h4>
                {summary.medicalHistory && summary.medicalHistory.length > 0 ? (
                  <ul className="space-y-2">
                    {summary.medicalHistory.map((item, idx) => (
                      <li
                        key={idx}
                        className="text-xs bg-white p-2.5 rounded border border-slate-200 flex flex-col space-y-1"
                      >
                        <div className="flex items-center justify-between">
                          <div>
                            <span className="font-semibold text-slate-800">{item.description}</span>
                            <span className="ml-2 text-slate-400">({item.category})</span>
                          </div>
                          {renderProvenanceBadge(item.source)}
                        </div>
                        {item.coding && item.coding.length > 0 && (
                          <div className="flex flex-wrap gap-1 mt-1">
                            {item.coding.map((cd, cIdx) => (
                              <span key={cIdx} className="text-[10px] px-1.5 py-0.5 rounded font-mono bg-indigo-50 text-indigo-700">
                                {cd.system}: {cd.code}
                              </span>
                            ))}
                          </div>
                        )}
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="text-xs text-slate-400 italic">No prior medical history reported.</p>
                )}
              </div>

              {/* Current Medications */}
              <div className="p-4 rounded-xl border border-slate-200 bg-slate-50/50">
                <h4 className="text-xs font-bold text-slate-700 uppercase tracking-wide mb-2">
                  Medications ({summary.medications?.length || 0})
                </h4>
                {summary.medications && summary.medications.length > 0 ? (
                  <ul className="space-y-2">
                    {summary.medications.map((item, idx) => (
                      <li
                        key={idx}
                        className="text-xs bg-white p-2.5 rounded border border-slate-200 flex flex-col space-y-1"
                      >
                        <div className="flex items-center justify-between">
                          <div>
                            <span className="font-semibold text-slate-800">{item.medicationName}</span>
                            <span className="ml-2 text-slate-500">
                              {item.dosage} {item.frequency && `• ${item.frequency}`}
                            </span>
                          </div>
                          {renderProvenanceBadge(item.source)}
                        </div>
                        {item.verificationNotes && (
                          <div className="text-[11px] text-amber-700 font-medium">
                            {item.verificationNotes}
                          </div>
                        )}
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="text-xs text-slate-400 italic">No medications reported.</p>
                )}
              </div>
            </div>

            {/* Allergies & Ayurveda History */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Allergies */}
              <div className="p-4 rounded-xl border border-slate-200 bg-slate-50/50">
                <h4 className="text-xs font-bold text-slate-700 uppercase tracking-wide mb-2">
                  Allergies ({summary.allergies?.length || 0})
                </h4>
                {summary.allergies && summary.allergies.length > 0 ? (
                  <ul className="space-y-2">
                    {summary.allergies.map((item, idx) => (
                      <li
                        key={idx}
                        className="text-xs bg-white p-2.5 rounded border border-slate-200 flex items-center justify-between"
                      >
                        <div>
                          <span className="font-semibold text-red-700">{item.allergen}</span>
                          {item.reaction && (
                            <span className="ml-2 text-slate-600">— {item.reaction}</span>
                          )}
                        </div>
                        {renderProvenanceBadge(item.source)}
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="text-xs text-slate-400 italic">No known allergies reported.</p>
                )}
              </div>

              {/* Ayurveda Intake Section */}
              <div className="p-4 rounded-xl border border-emerald-200 bg-emerald-50/30">
                <h4 className="text-xs font-bold text-emerald-800 uppercase tracking-wide mb-2 flex items-center justify-between">
                  <span>Ayurveda Profile</span>
                  <span className="text-[10px] font-mono px-2 py-0.5 bg-emerald-100 text-emerald-800 rounded">
                    Source: {ayurvedaData?.source || 'PATIENT_REPORTED'}
                  </span>
                </h4>
                {ayurvedaData ? (
                  <div className="text-xs space-y-1.5 text-slate-700">
                    <div>
                      <span className="font-semibold text-slate-800">Prakriti:</span>{' '}
                      {ayurvedaData.prakriti || ayurvedaData.prakritiAssessment || 'Not assessed'}
                    </div>
                    <div>
                      <span className="font-semibold text-slate-800">Vikriti:</span>{' '}
                      {ayurvedaData.vikriti || 'Not assessed'}
                    </div>
                    <div>
                      <span className="font-semibold text-slate-800">Agni:</span>{' '}
                      {ayurvedaData.agni || ayurvedaData.agniType || 'Not assessed'}
                    </div>
                    {ayurvedaData.kosthaType && (
                      <div>
                        <span className="font-semibold text-slate-800">Kostha:</span>{' '}
                        {ayurvedaData.kosthaType}
                      </div>
                    )}
                    {ayurvedaData.nidanaFactors && (
                      <div>
                        <span className="font-semibold text-slate-800">Nidana:</span>{' '}
                        {ayurvedaData.nidanaFactors}
                      </div>
                    )}
                    {ayurvedaData.documentedObservations && ayurvedaData.documentedObservations.length > 0 && (
                      <div className="mt-2 text-[11px] text-emerald-900 bg-emerald-100/40 p-2 rounded">
                        {ayurvedaData.documentedObservations.join('; ')}
                      </div>
                    )}
                  </div>
                ) : (
                  <p className="text-xs text-slate-400 italic">No Ayurveda intake recorded.</p>
                )}
              </div>
            </div>

            {/* OCR Document Findings */}
            {summary.documentFindings && summary.documentFindings.length > 0 && (
              <div className="p-4 rounded-xl border border-slate-200 bg-slate-50/50">
                <h4 className="text-xs font-bold text-slate-700 uppercase tracking-wide mb-3">
                  Document Intelligence Findings ({summary.documentFindings.length})
                </h4>
                <div className="space-y-3">
                  {summary.documentFindings.map((doc, idx) => (
                    <div
                      key={idx}
                      className="p-3 bg-white rounded-lg border border-slate-200 text-xs text-slate-700"
                    >
                      <div className="flex items-center justify-between font-semibold text-slate-900 mb-1">
                        <span>{doc.fileName} ({doc.documentType})</span>
                        {doc.confidence !== undefined && (
                          <span
                            className={`px-2 py-0.5 rounded font-mono text-[10px] ${
                              doc.confidence >= 0.75
                                ? 'bg-emerald-50 text-emerald-700'
                                : 'bg-amber-50 text-amber-700'
                            }`}
                          >
                            OCR Confidence: {Math.round(doc.confidence * 100)}%
                          </span>
                        )}
                      </div>
                      <p className="text-slate-600">{doc.findings || doc.extractedTextPreview || 'Extraction completed'}</p>
                      {doc.uncertaintyWarning && (
                        <div className="mt-2 p-2 rounded bg-amber-50 border border-amber-200 text-amber-800 text-[11px] flex items-center space-x-1.5">
                          <span>⚠️</span>
                          <span>{doc.uncertaintyWarning}</span>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Dual Terminology / Concept Coding Section */}
            {summary.dualCodedConcepts && summary.dualCodedConcepts.length > 0 && (
              <div className="p-4 rounded-xl border border-indigo-200 bg-indigo-50/30">
                <h4 className="text-xs font-bold text-indigo-900 uppercase tracking-wide mb-3 flex items-center justify-between">
                  <span>🏷️ Dual Terminology & Concept Coding (ICD-10 / NAMASTE)</span>
                  <span className="text-[10px] font-mono px-2 py-0.5 bg-indigo-100 text-indigo-800 rounded">
                    Authoritative Concept Registry
                  </span>
                </h4>
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-xs">
                    <thead>
                      <tr className="border-b border-indigo-200 text-indigo-950 font-semibold bg-indigo-100/50">
                        <th className="py-2 px-3">Clinical Concept</th>
                        <th className="py-2 px-3">Coding System</th>
                        <th className="py-2 px-3">Code</th>
                        <th className="py-2 px-3">Display Name</th>
                        <th className="py-2 px-3">Confidence</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-indigo-100 bg-white">
                      {summary.dualCodedConcepts.map((dc, idx) => (
                        <React.Fragment key={idx}>
                          {dc.coding && dc.coding.length > 0 ? (
                            dc.coding.map((cd, cdIdx) => (
                              <tr key={`${idx}-${cdIdx}`} className="hover:bg-indigo-50/30">
                                {cdIdx === 0 && (
                                  <td className="py-2 px-3 font-semibold text-slate-800" rowSpan={dc.coding.length}>
                                    {dc.concept}
                                  </td>
                                )}
                                <td className="py-2 px-3">
                                  <span className={`px-1.5 py-0.5 rounded text-[10px] font-bold ${
                                    cd.system === 'ICD' ? 'bg-blue-100 text-blue-800' : cd.system === 'NAMASTE' ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-100 text-slate-600'
                                  }`}>
                                    {cd.system}
                                  </span>
                                </td>
                                <td className="py-2 px-3 font-mono text-slate-700">{cd.code}</td>
                                <td className="py-2 px-3 text-slate-700">{cd.display || cd.code}</td>
                                <td className="py-2 px-3 text-slate-600">{Math.round((cd.confidence || 0.9) * 100)}%</td>
                              </tr>
                            ))
                          ) : (
                            <tr key={idx} className="hover:bg-indigo-50/30">
                              <td className="py-2 px-3 font-semibold text-slate-800">{dc.concept}</td>
                              <td className="py-2 px-3"><span className="px-1.5 py-0.5 rounded text-[10px] bg-slate-100 text-slate-600">UNMAPPED</span></td>
                              <td className="py-2 px-3 font-mono text-slate-400">UNMAPPED</td>
                              <td className="py-2 px-3 text-slate-500">{dc.concept}</td>
                              <td className="py-2 px-3 text-slate-400">0%</td>
                            </tr>
                          )}
                        </React.Fragment>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {/* Clinical Conflicts & Missing Information Alerts */}
            {(((summary.potentialConflicts || summary.conflicts) && ((summary.potentialConflicts?.length || 0) + (summary.conflicts?.length || 0) > 0)) ||
              (summary.missingInformation && summary.missingInformation.length > 0)) && (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {/* Conflicts */}
                {((summary.potentialConflicts?.length || 0) + (summary.conflicts?.length || 0)) > 0 && (
                  <div className="p-4 rounded-xl border border-amber-300 bg-amber-50">
                    <h4 className="text-xs font-bold text-amber-900 uppercase tracking-wide mb-2 flex items-center space-x-1.5">
                      <span>⚠️ Detected Clinical Conflicts ({(summary.potentialConflicts?.length || 0) + (summary.conflicts?.length || 0)})</span>
                    </h4>
                    <ul className="space-y-2">
                      {(summary.potentialConflicts || summary.conflicts || []).map((conf, idx) => (
                        <li key={idx} className="p-2 bg-white rounded border border-amber-200 text-xs">
                          <div className="font-semibold text-slate-900">{conf.field}</div>
                          <div className="text-slate-600 mt-1">{conf.conflictDescription || 'Conflict requires doctor verification.'}</div>
                          {conf.patientStated && (
                            <div className="mt-1.5 grid grid-cols-2 gap-2 text-[11px] text-slate-500">
                              <div>
                                <span className="font-medium text-slate-700">Patient:</span> {conf.patientStated}
                              </div>
                              <div>
                                <span className="font-medium text-slate-700">Document:</span> {conf.documentStated}
                              </div>
                            </div>
                          )}
                        </li>
                      ))}
                    </ul>
                  </div>
                )}

                {/* Missing Information */}
                {summary.missingInformation && summary.missingInformation.length > 0 && (
                  <div className="p-4 rounded-xl border border-blue-200 bg-blue-50">
                    <h4 className="text-xs font-bold text-blue-900 uppercase tracking-wide mb-2 flex items-center space-x-1.5">
                      <span>ℹ️ Missing Clinical Details ({summary.missingInformation.length})</span>
                    </h4>
                    <ul className="space-y-2">
                      {summary.missingInformation.map((item, idx) => (
                        <li key={idx} className="p-2 bg-white rounded border border-blue-200 text-xs">
                          <div className="font-semibold text-slate-900">{item.category || item.field}</div>
                          <div className="text-slate-600">{item.description || item.reason}</div>
                          {item.clinicalRelevance && (
                            <div className="mt-1 text-[11px] text-blue-700">
                              Relevance: {item.clinicalRelevance}
                            </div>
                          )}
                        </li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
export default ClinicalSummarySection;
