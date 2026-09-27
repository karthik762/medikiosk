import React, { useState, useEffect } from 'react';
import {
  PrakritiData,
  NidanaData,
  ParikshaData,
  AyurvedaHistoryResponse,
  AyurvedaHistoryStatus,
} from '../../types/ayurveda';
import { ayurvedaService } from '../../services/ayurvedaService';
import { VoiceRecorder } from '../voice/VoiceRecorder';

interface AyurvedaSectionProps {
  sessionId: string;
  authToken?: string;
  language?: string;
  onConfirmed?: (response: AyurvedaHistoryResponse) => void;
  onBack?: () => void;
  onSkip?: () => void;
}

type TabType = 'prakriti' | 'nidana' | 'pariksha' | 'review';

export const AyurvedaSection: React.FC<AyurvedaSectionProps> = ({
  sessionId,
  authToken,
  language = 'en',
  onConfirmed,
  onBack,
  onSkip,
}) => {
  const [activeTab, setActiveTab] = useState<TabType>('prakriti');
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [status, setStatus] = useState<AyurvedaHistoryStatus>('DRAFT');
  const [confirmedAt, setConfirmedAt] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Form state
  const [prakriti, setPrakriti] = useState<PrakritiData>({
    bodyFrame: '',
    weatherPreference: '',
    digestionPattern: '',
    sleepPattern: '',
  });

  const [nidana, setNidana] = useState<NidanaData>({
    dietaryHabits: '',
    activityPattern: '',
    stressFactors: '',
    sleepSchedule: '',
  });

  const [pariksha, setPariksha] = useState<ParikshaData>({
    koshtha: '',
    bala: '',
    jihva: '',
    agni: '',
  });

  const [notes, setNotes] = useState<string>('');
  const [showVoiceForNotes, setShowVoiceForNotes] = useState<boolean>(false);

  // Load existing session history on mount
  useEffect(() => {
    let isMounted = true;
    async function loadData() {
      setLoading(true);
      setErrorMessage(null);
      try {
        const res = await ayurvedaService.getAyurvedaHistory(sessionId, authToken);
        if (isMounted && res.data) {
          if (res.data.prakriti) setPrakriti(res.data.prakriti);
          if (res.data.nidana) setNidana(res.data.nidana);
          if (res.data.pariksha) setPariksha(res.data.pariksha);
          if (res.data.notes) setNotes(res.data.notes);
          setStatus(res.data.status);
          if (res.data.confirmedAt) setConfirmedAt(res.data.confirmedAt);
        }
      } catch (err: unknown) {
        // 404 is normal if history is not yet started
        console.debug('No existing Ayurveda history or unable to load:', err);
      } finally {
        if (isMounted) setLoading(false);
      }
    }

    if (sessionId) {
      loadData();
    }
    return () => {
      isMounted = false;
    };
  }, [sessionId, authToken]);

  const handleSaveDraft = async () => {
    setSaving(true);
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      const res = await ayurvedaService.saveAyurvedaHistory(
        sessionId,
        {
          prakriti,
          nidana,
          pariksha,
          notes,
        },
        authToken
      );
      if (res.data) {
        setStatus(res.data.status);
        setSuccessMessage('Ayurveda assessment draft saved successfully.');
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to save draft.';
      setErrorMessage(msg);
    } finally {
      setSaving(false);
    }
  };

  const handleConfirm = async () => {
    setSaving(true);
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      // Ensure draft is saved first
      await ayurvedaService.saveAyurvedaHistory(
        sessionId,
        {
          prakriti,
          nidana,
          pariksha,
          notes,
        },
        authToken
      );

      const res = await ayurvedaService.confirmAyurvedaHistory(sessionId, authToken);
      if (res.data) {
        setStatus(res.data.status);
        setConfirmedAt(res.data.confirmedAt || new Date().toISOString());
        setSuccessMessage('Your Ayurvedic assessment has been confirmed and is ready for doctor review.');
        if (onConfirmed) {
          onConfirmed(res.data);
        }
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to confirm assessment.';
      setErrorMessage(msg);
    } finally {
      setSaving(false);
    }
  };

  const isConfirmed = status === 'PATIENT_CONFIRMED' || status === 'DOCTOR_REVIEWED';

  return (
    <div className="bg-white rounded-xl shadow-md border border-emerald-100 overflow-hidden my-6">
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-emerald-800 to-teal-800 text-white p-5">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <div className="flex items-center gap-2">
              <span className="text-2xl" role="img" aria-label="Ayurveda Leaf">🌿</span>
              <h2 className="text-xl font-bold tracking-tight">Ayurvedic Clinical Intake Assessment</h2>
            </div>
            <p className="text-xs text-emerald-100 mt-1">
              Prakriti (Constitution) • Nidana (Etiology & Lifestyle) • Pariksha (Clinical Indicators)
            </p>
          </div>
          <div className="flex items-center gap-2">
            {onBack && (
              <button
                type="button"
                onClick={onBack}
                className="px-3 py-1 text-xs font-semibold rounded-lg bg-emerald-900/60 hover:bg-emerald-900 text-emerald-100 border border-emerald-600 transition"
              >
                ← Back
              </button>
            )}
            {onSkip && (
              <button
                type="button"
                onClick={onSkip}
                className="px-3 py-1 text-xs font-semibold rounded-lg bg-teal-900/60 hover:bg-teal-900 text-teal-100 border border-teal-600 transition"
              >
                Skip Step ⏭️
              </button>
            )}
            <span
              id="ayurveda-status-badge"
              className={`px-3 py-1 text-xs font-semibold rounded-full border ${
                isConfirmed
                  ? 'bg-emerald-600 text-white border-emerald-400'
                  : 'bg-amber-100 text-amber-800 border-amber-300'
              }`}
            >
              {status}
            </span>
            {confirmedAt && (
              <span className="text-xs text-emerald-200">
                Confirmed: {new Date(confirmedAt).toLocaleDateString()}
              </span>
            )}
          </div>
        </div>
      </div>

      {/* Non-diagnostic clinical safety disclaimer */}
      <div className="bg-emerald-50 border-b border-emerald-200 p-3 px-5 text-xs text-emerald-900 flex items-start gap-2">
        <span className="font-bold text-emerald-800 uppercase tracking-wide">Clinical Notice:</span>
        <span>
          MediKiosk is NOT an autonomous diagnostic system. This intake records structured patient-reported observations
          to assist the consulting doctor. The doctor remains the final clinical decision-maker.
        </span>
      </div>

      {/* Messages */}
      {errorMessage && (
        <div className="bg-rose-50 border-l-4 border-rose-500 p-3 mx-5 mt-4 text-rose-800 text-sm" role="alert">
          {errorMessage}
        </div>
      )}
      {successMessage && (
        <div className="bg-teal-50 border-l-4 border-teal-500 p-3 mx-5 mt-4 text-teal-800 text-sm" role="status">
          {successMessage}
        </div>
      )}

      {/* Tab Navigation */}
      <div className="flex border-b border-gray-200 bg-gray-50 px-5 pt-3">
        <button
          type="button"
          onClick={() => setActiveTab('prakriti')}
          className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${
            activeTab === 'prakriti'
              ? 'border-emerald-600 text-emerald-800 font-semibold'
              : 'border-transparent text-gray-500 hover:text-gray-700'
          }`}
        >
          1. Prakriti (Constitution)
        </button>
        <button
          type="button"
          onClick={() => setActiveTab('nidana')}
          className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${
            activeTab === 'nidana'
              ? 'border-emerald-600 text-emerald-800 font-semibold'
              : 'border-transparent text-gray-500 hover:text-gray-700'
          }`}
        >
          2. Nidana (Lifestyle)
        </button>
        <button
          type="button"
          onClick={() => setActiveTab('pariksha')}
          className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${
            activeTab === 'pariksha'
              ? 'border-emerald-600 text-emerald-800 font-semibold'
              : 'border-transparent text-gray-500 hover:text-gray-700'
          }`}
        >
          3. Pariksha (Clinical)
        </button>
        <button
          type="button"
          onClick={() => setActiveTab('review')}
          className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${
            activeTab === 'review'
              ? 'border-emerald-600 text-emerald-800 font-semibold'
              : 'border-transparent text-gray-500 hover:text-gray-700'
          }`}
        >
          4. Review & Confirm
        </button>
      </div>

      {/* Form Content */}
      <div className="p-6">
        {loading ? (
          <div className="text-center py-8 text-gray-500 text-sm">Loading intake assessment...</div>
        ) : (
          <div>
            {/* TAB 1: PRAKRITI */}
            {activeTab === 'prakriti' && (
              <div className="space-y-6">
                <div>
                  <h3 className="text-base font-semibold text-gray-800">Physical Constitution (Prakriti)</h3>
                  <p className="text-xs text-gray-500">Describe your natural physical tendencies and baseline traits.</p>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                  {/* Body Frame */}
                  <div>
                    <label htmlFor="prakriti-body-frame" className="block text-xs font-semibold text-gray-700 mb-1">
                      Body Frame / Physical Build
                    </label>
                    <select
                      id="prakriti-body-frame"
                      disabled={isConfirmed}
                      value={prakriti.bodyFrame || ''}
                      onChange={(e) => setPrakriti({ ...prakriti, bodyFrame: e.target.value })}
                      className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-emerald-500 focus:border-emerald-500 disabled:bg-gray-100"
                    >
                      <option value="">-- Select Build --</option>
                      <option value="SLENDER">Slender / Thin / Light build</option>
                      <option value="MEDIUM">Medium / Athletic / Balanced build</option>
                      <option value="LARGE">Broad / Heavy / Solid build</option>
                    </select>
                  </div>

                  {/* Weather Preference */}
                  <div>
                    <label htmlFor="prakriti-weather" className="block text-xs font-semibold text-gray-700 mb-1">
                      Weather / Climate Sensitivity
                    </label>
                    <select
                      id="prakriti-weather"
                      disabled={isConfirmed}
                      value={prakriti.weatherPreference || ''}
                      onChange={(e) => setPrakriti({ ...prakriti, weatherPreference: e.target.value })}
                      className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-emerald-500 focus:border-emerald-500 disabled:bg-gray-100"
                    >
                      <option value="">-- Select Sensitivity --</option>
                      <option value="DISLIKES_COLD">Dislikes Cold / Prefers Warmth</option>
                      <option value="DISLIKES_HEAT">Dislikes Heat / Prefers Cool</option>
                      <option value="DISLIKES_HUMIDITY">Dislikes Dampness & Heavy Humidity</option>
                      <option value="TOLERATES_ALL">Comfortable in most climates</option>
                    </select>
                  </div>

                  {/* Digestion Pattern */}
                  <div>
                    <label htmlFor="prakriti-digestion" className="block text-xs font-semibold text-gray-700 mb-1">
                      Baseline Digestion Pattern
                    </label>
                    <select
                      id="prakriti-digestion"
                      disabled={isConfirmed}
                      value={prakriti.digestionPattern || ''}
                      onChange={(e) => setPrakriti({ ...prakriti, digestionPattern: e.target.value })}
                      className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-emerald-500 focus:border-emerald-500 disabled:bg-gray-100"
                    >
                      <option value="">-- Select Digestion --</option>
                      <option value="IRREGULAR">Variable / Irregular appetite and digestion</option>
                      <option value="QUICK_STRONG">Strong / Intense hunger and quick digestion</option>
                      <option value="SLOW_HEAVY">Slow / Heavy digestion even with small meals</option>
                      <option value="BALANCED">Regular and comfortable digestion</option>
                    </select>
                  </div>

                  {/* Sleep Pattern */}
                  <div>
                    <label htmlFor="prakriti-sleep" className="block text-xs font-semibold text-gray-700 mb-1">
                      Baseline Sleep Pattern
                    </label>
                    <select
                      id="prakriti-sleep"
                      disabled={isConfirmed}
                      value={prakriti.sleepPattern || ''}
                      onChange={(e) => setPrakriti({ ...prakriti, sleepPattern: e.target.value })}
                      className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-emerald-500 focus:border-emerald-500 disabled:bg-gray-100"
                    >
                      <option value="">-- Select Sleep Pattern --</option>
                      <option value="LIGHT_INTERRUPTED">Light, easily disturbed, variable</option>
                      <option value="SOUND_MODERATE">Moderate, 6-7 hours, restful</option>
                      <option value="DEEP_HEAVY">Heavy, deep, difficult to wake</option>
                    </select>
                  </div>
                </div>

                <div className="flex justify-between pt-4">
                  {onBack ? (
                    <button
                      type="button"
                      onClick={onBack}
                      className="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-600 hover:bg-gray-50"
                    >
                      ← Back
                    </button>
                  ) : (
                    <div />
                  )}
                  <div className="flex items-center gap-3">
                    {onSkip && (
                      <button
                        type="button"
                        onClick={onSkip}
                        className="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-600 hover:bg-gray-50"
                      >
                        Skip this Step
                      </button>
                    )}
                    <button
                      type="button"
                      onClick={() => setActiveTab('nidana')}
                      className="px-5 py-2 bg-emerald-700 text-white rounded-lg text-sm font-medium hover:bg-emerald-800 transition"
                    >
                      Next: Nidana (Lifestyle) →
                    </button>
                  </div>
                </div>
              </div>
            )}

            {/* TAB 2: NIDANA */}
            {activeTab === 'nidana' && (
              <div className="space-y-6">
                <div>
                  <h3 className="text-base font-semibold text-gray-800">Etiological & Lifestyle Factors (Nidana)</h3>
                  <p className="text-xs text-gray-500">Record daily routine habits and aggravating triggers.</p>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                  {/* Dietary Habits */}
                  <div>
                    <label htmlFor="nidana-diet" className="block text-xs font-semibold text-gray-700 mb-1">
                      Dietary Habits & Preferences
                    </label>
                    <select
                      id="nidana-diet"
                      disabled={isConfirmed}
                      value={nidana.dietaryHabits || ''}
                      onChange={(e) => setNidana({ ...nidana, dietaryHabits: e.target.value })}
                      className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-emerald-500 focus:border-emerald-500 disabled:bg-gray-100"
                    >
                      <option value="">-- Select Habit --</option>
                      <option value="IRREGULAR_MEALS">Irregular meal timings / Skipping meals</option>
                      <option value="SPICY_PUNGENT">Frequent spicy, sour, pungent, or fried food</option>
                      <option value="SWEET_OILY_COLD">Frequent dairy, sweets, oily, cold food</option>
                      <option value="BALANCED">Balanced home-cooked meals at regular times</option>
                    </select>
                  </div>

                  {/* Physical Activity */}
                  <div>
                    <label htmlFor="nidana-activity" className="block text-xs font-semibold text-gray-700 mb-1">
                      Daily Physical Activity Level
                    </label>
                    <select
                      id="nidana-activity"
                      disabled={isConfirmed}
                      value={nidana.activityPattern || ''}
                      onChange={(e) => setNidana({ ...nidana, activityPattern: e.target.value })}
                      className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-emerald-500 focus:border-emerald-500 disabled:bg-gray-100"
                    >
                      <option value="">-- Select Activity --</option>
                      <option value="SEDENTARY">Sedentary (desk job, minimal movement)</option>
                      <option value="MODERATE">Moderate regular walking or exercise</option>
                      <option value="HEAVY">Strenuous physical labor or intense training</option>
                    </select>
                  </div>

                  {/* Stress Factors */}
                  <div>
                    <label htmlFor="nidana-stress" className="block text-xs font-semibold text-gray-700 mb-1">
                      Mental Strain & Stress Factors
                    </label>
                    <select
                      id="nidana-stress"
                      disabled={isConfirmed}
                      value={nidana.stressFactors || ''}
                      onChange={(e) => setNidana({ ...nidana, stressFactors: e.target.value })}
                      className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-emerald-500 focus:border-emerald-500 disabled:bg-gray-100"
                    >
                      <option value="">-- Select Stress Level --</option>
                      <option value="LOW">Low / Well-managed</option>
                      <option value="MODERATE">Moderate work or personal stress</option>
                      <option value="HIGH_MENTAL_STRESS">High continuous mental strain / anxiety</option>
                    </select>
                  </div>

                  {/* Sleep Schedule */}
                  <div>
                    <label htmlFor="nidana-sleep-sched" className="block text-xs font-semibold text-gray-700 mb-1">
                      Daily Sleep Schedule
                    </label>
                    <select
                      id="nidana-sleep-sched"
                      disabled={isConfirmed}
                      value={nidana.sleepSchedule || ''}
                      onChange={(e) => setNidana({ ...nidana, sleepSchedule: e.target.value })}
                      className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-emerald-500 focus:border-emerald-500 disabled:bg-gray-100"
                    >
                      <option value="">-- Select Schedule --</option>
                      <option value="REGULAR">Regular bedtime (before 10:30 PM)</option>
                      <option value="LATE_NIGHT">Late night awake (after midnight / night shifts)</option>
                      <option value="DAY_SLEEP">Frequent daytime naps</option>
                    </select>
                  </div>
                </div>

                <div className="flex justify-between pt-4">
                  <button
                    type="button"
                    onClick={() => setActiveTab('prakriti')}
                    className="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-600 hover:bg-gray-50"
                  >
                    ← Back to Prakriti
                  </button>
                  <button
                    type="button"
                    onClick={() => setActiveTab('pariksha')}
                    className="px-5 py-2 bg-emerald-700 text-white rounded-lg text-sm font-medium hover:bg-emerald-800 transition"
                  >
                    Next: Pariksha (Clinical) →
                  </button>
                </div>
              </div>
            )}

            {/* TAB 3: PARIKSHA */}
            {activeTab === 'pariksha' && (
              <div className="space-y-6">
                <div>
                  <h3 className="text-base font-semibold text-gray-800">Clinical Examination Indicators (Pariksha)</h3>
                  <p className="text-xs text-gray-500">Observed physiological parameters to assist doctor examination.</p>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                  {/* Koshtha */}
                  <div>
                    <label htmlFor="pariksha-koshtha" className="block text-xs font-semibold text-gray-700 mb-1">
                      Bowel Motility Pattern (Koshtha)
                    </label>
                    <select
                      id="pariksha-koshtha"
                      disabled={isConfirmed}
                      value={pariksha.koshtha || ''}
                      onChange={(e) => setPariksha({ ...pariksha, koshtha: e.target.value })}
                      className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-emerald-500 focus:border-emerald-500 disabled:bg-gray-100"
                    >
                      <option value="">-- Select Bowel Pattern --</option>
                      <option value="KRURA">Krura (Hard, constipated, irregular)</option>
                      <option value="MADHYA">Madhya (Regular, normal once/twice daily)</option>
                      <option value="MRIDU">Mridu (Soft, loose, frequent elimination)</option>
                    </select>
                  </div>

                  {/* Bala */}
                  <div>
                    <label htmlFor="pariksha-bala" className="block text-xs font-semibold text-gray-700 mb-1">
                      Vital Strength & Stamina (Bala)
                    </label>
                    <select
                      id="pariksha-bala"
                      disabled={isConfirmed}
                      value={pariksha.bala || ''}
                      onChange={(e) => setPariksha({ ...pariksha, bala: e.target.value })}
                      className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-emerald-500 focus:border-emerald-500 disabled:bg-gray-100"
                    >
                      <option value="">-- Select Stamina --</option>
                      <option value="PRAVARA">Pravara (High stamina, endurance)</option>
                      <option value="MADHYAMA">Madhyama (Moderate stamina)</option>
                      <option value="AVARA">Avara (Low stamina, tires easily)</option>
                    </select>
                  </div>

                  {/* Jihva */}
                  <div>
                    <label htmlFor="pariksha-jihva" className="block text-xs font-semibold text-gray-700 mb-1">
                      Morning Tongue Coating (Jihva)
                    </label>
                    <select
                      id="pariksha-jihva"
                      disabled={isConfirmed}
                      value={pariksha.jihva || ''}
                      onChange={(e) => setPariksha({ ...pariksha, jihva: e.target.value })}
                      className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-emerald-500 focus:border-emerald-500 disabled:bg-gray-100"
                    >
                      <option value="">-- Select Coating --</option>
                      <option value="CLEAR_UNCOATED">Clear / Pinkish / No coating (Nirama)</option>
                      <option value="WHITE_COATING">White thick coating (Ama presence)</option>
                      <option value="YELLOW_COATING">Yellow / Brownish coating</option>
                    </select>
                  </div>

                  {/* Agni */}
                  <div>
                    <label htmlFor="pariksha-agni" className="block text-xs font-semibold text-gray-700 mb-1">
                      Digestive Fire State (Agni)
                    </label>
                    <select
                      id="pariksha-agni"
                      disabled={isConfirmed}
                      value={pariksha.agni || ''}
                      onChange={(e) => setPariksha({ ...pariksha, agni: e.target.value })}
                      className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-emerald-500 focus:border-emerald-500 disabled:bg-gray-100"
                    >
                      <option value="">-- Select Agni State --</option>
                      <option value="SAMA">Sama Agni (Balanced, digests easily)</option>
                      <option value="VISHAMA">Vishama Agni (Variable, bloating/gas)</option>
                      <option value="TIKSHNA">Tikshna Agni (Sharp, hyperacidity, burning)</option>
                      <option value="MANDA">Manda Agni (Sluggish, heaviness after food)</option>
                    </select>
                  </div>
                </div>


                <div className="flex justify-between pt-4">
                  <button
                    type="button"
                    onClick={() => setActiveTab('nidana')}
                    className="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-600 hover:bg-gray-50"
                  >
                    ← Back to Nidana
                  </button>
                  <button
                    type="button"
                    onClick={() => setActiveTab('review')}
                    className="px-5 py-2 bg-emerald-700 text-white rounded-lg text-sm font-medium hover:bg-emerald-800 transition"
                  >
                    Next: Review & Confirm →
                  </button>
                </div>
              </div>
            )}

            {/* TAB 4: REVIEW & CONFIRM */}
            {activeTab === 'review' && (
              <div className="space-y-6">
                <div>
                  <h3 className="text-base font-semibold text-gray-800">Review & Confirm Ayurvedic Assessment</h3>
                  <p className="text-xs text-gray-500">
                    Verify the recorded observations. Once confirmed, this data will be presented directly to your doctor.
                  </p>
                </div>

                {/* Summary Cards */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="bg-gray-50 p-4 rounded-lg border border-gray-200">
                    <h4 className="text-xs font-bold uppercase text-emerald-800 tracking-wider mb-2">1. Prakriti</h4>
                    <ul className="text-xs space-y-1 text-gray-700">
                      <li><strong>Build:</strong> {prakriti.bodyFrame || 'Not answered'}</li>
                      <li><strong>Climate:</strong> {prakriti.weatherPreference || 'Not answered'}</li>
                      <li><strong>Digestion:</strong> {prakriti.digestionPattern || 'Not answered'}</li>
                      <li><strong>Sleep:</strong> {prakriti.sleepPattern || 'Not answered'}</li>
                    </ul>
                  </div>

                  <div className="bg-gray-50 p-4 rounded-lg border border-gray-200">
                    <h4 className="text-xs font-bold uppercase text-emerald-800 tracking-wider mb-2">2. Nidana</h4>
                    <ul className="text-xs space-y-1 text-gray-700">
                      <li><strong>Diet:</strong> {nidana.dietaryHabits || 'Not answered'}</li>
                      <li><strong>Activity:</strong> {nidana.activityPattern || 'Not answered'}</li>
                      <li><strong>Stress:</strong> {nidana.stressFactors || 'Not answered'}</li>
                      <li><strong>Schedule:</strong> {nidana.sleepSchedule || 'Not answered'}</li>
                    </ul>
                  </div>

                  <div className="bg-gray-50 p-4 rounded-lg border border-gray-200">
                    <h4 className="text-xs font-bold uppercase text-emerald-800 tracking-wider mb-2">3. Pariksha</h4>
                    <ul className="text-xs space-y-1 text-gray-700">
                      <li><strong>Koshtha:</strong> {pariksha.koshtha || 'Not answered'}</li>
                      <li><strong>Bala:</strong> {pariksha.bala || 'Not answered'}</li>
                      <li><strong>Jihva:</strong> {pariksha.jihva || 'Not answered'}</li>
                      <li><strong>Agni:</strong> {pariksha.agni || 'Not answered'}</li>
                    </ul>
                  </div>
                </div>

                {/* Patient Notes & Voice Input */}
                <div className="bg-gray-50 p-4 rounded-lg border border-gray-200 space-y-3">
                  <div className="flex items-center justify-between">
                    <label htmlFor="ayurveda-notes" className="text-xs font-bold text-gray-700">
                      Additional Observations or Symptoms (Optional)
                    </label>
                    <button
                      type="button"
                      disabled={isConfirmed}
                      onClick={() => setShowVoiceForNotes(!showVoiceForNotes)}
                      className="text-xs text-emerald-700 font-medium hover:text-emerald-900 flex items-center gap-1 disabled:opacity-50"
                    >
                      🎤 {showVoiceForNotes ? 'Hide Voice Dictation' : 'Use Voice Dictation'}
                    </button>
                  </div>

                  {showVoiceForNotes && !isConfirmed && (
                    <div className="p-3 bg-white rounded border border-emerald-300">
                      <p className="text-xs text-emerald-800 mb-2 font-medium">
                        Record voice observation in {language.toUpperCase()}:
                      </p>
                      <VoiceRecorder
                        sessionId={sessionId}
                        initialLanguage={language as any}
                        onConfirm={(text: string) => {
                          setNotes((prev) => (prev ? `${prev} ${text}` : text));
                          setShowVoiceForNotes(false);
                        }}
                      />
                    </div>
                  )}

                  <textarea
                    id="ayurveda-notes"
                    disabled={isConfirmed}
                    rows={3}
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                    placeholder="Describe any other natural tendencies, specific dietary intolerances, or symptoms you wish to bring to your doctor..."
                    className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-emerald-500 focus:border-emerald-500 disabled:bg-gray-100"
                  />
                </div>

                {/* Action Buttons */}
                <div className="flex flex-wrap items-center justify-between gap-3 pt-4 border-t border-gray-200">
                  <button
                    type="button"
                    onClick={() => setActiveTab('pariksha')}
                    className="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-600 hover:bg-gray-50"
                  >
                    ← Back to Pariksha
                  </button>

                  <div className="flex items-center gap-3">
                    {!isConfirmed && (
                      <button
                        type="button"
                        id="btn-save-draft"
                        disabled={saving}
                        onClick={handleSaveDraft}
                        className="px-4 py-2 border border-emerald-600 text-emerald-700 rounded-lg text-sm font-medium hover:bg-emerald-50 disabled:opacity-50"
                      >
                        {saving ? 'Saving...' : 'Save Draft'}
                      </button>
                    )}

                    {!isConfirmed ? (
                      <button
                        type="button"
                        id="btn-confirm-ayurveda"
                        disabled={saving}
                        onClick={handleConfirm}
                        className="px-6 py-2 bg-emerald-700 text-white rounded-lg text-sm font-semibold hover:bg-emerald-800 shadow-sm transition disabled:opacity-50"
                      >
                        {saving ? 'Confirming...' : 'Confirm Assessment for Doctor'}
                      </button>
                    ) : (
                      <div className="flex items-center gap-2">
                        <span className="text-xs text-emerald-700 font-semibold flex items-center gap-1">
                          ✓ Confirmed by Patient
                        </span>
                        <button
                          type="button"
                          id="btn-edit-assessment"
                          onClick={() => {
                            if (confirm('Editing will set the assessment back to Draft. You will need to confirm again.')) {
                              setStatus('DRAFT');
                            }
                          }}
                          className="text-xs text-gray-500 hover:text-gray-700 underline"
                        >
                          Edit
                        </button>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
