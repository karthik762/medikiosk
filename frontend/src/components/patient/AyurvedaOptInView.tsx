import React, { useState } from 'react';
import { KioskButton } from './KioskButton';
import { getTranslation } from '../../i18n/translations';

export interface AyurvedaData {
  prakritiPreference: string;
  digestionPattern: string;
  sleepPattern: string;
  notes?: string;
}

interface AyurvedaOptInViewProps {
  onSubmit: (data: AyurvedaData | null) => Promise<void>;
  onBack: () => void;
  loading?: boolean;
  language?: 'en' | 'hi' | 'te';
}

export const AyurvedaOptInView: React.FC<AyurvedaOptInViewProps> = ({
  onSubmit,
  onBack,
  loading = false,
  language = 'en',
}) => {
  const [optIn, setOptIn] = useState<boolean | null>(null);
  const [prakriti, setPrakriti] = useState('Vata-Pitta');
  const [digestion, setDigestion] = useState('Samagni (Balanced)');
  const [sleep, setSleep] = useState('Moderate (6-7 hrs)');

  const t = (key: Parameters<typeof getTranslation>[1]) => getTranslation(language, key);

  const handleFinish = async () => {
    if (optIn === false) {
      await onSubmit(null);
    } else {
      await onSubmit({
        prakritiPreference: prakriti,
        digestionPattern: digestion,
        sleepPattern: sleep,
      });
    }
  };

  return (
    <div className="max-w-2xl mx-auto py-8 px-6 animate-fadeIn">
      <div className="mb-6 text-center">
        <div className="inline-flex items-center justify-center w-12 h-12 rounded-2xl bg-[#AEA989]/20 text-[#AEA989] text-2xl mb-2 shadow-xs">
          🌿
        </div>
        <h2 className="text-3xl font-extrabold text-white mb-2">{t('ayurvedaTitle')}</h2>
        <p className="text-slate-300 text-sm">{t('ayurvedaSubtitle')}</p>
      </div>

      <div className="bg-[#131936] rounded-3xl p-8 border border-[#AEA989]/40 shadow-xl space-y-6">
        {/* Opt-in Choices */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <button
            type="button"
            onClick={() => setOptIn(true)}
            className={`p-6 rounded-2xl border-2 font-bold text-lg flex items-center justify-center gap-3 transition-all active:scale-98 ${
              optIn === true
                ? 'bg-[#AEA989] border-[#AEA989] text-[#131936] shadow-md ring-4 ring-[#AEA989]/40 font-extrabold'
                : 'bg-[#0b0e1e] border-[#AEA989]/30 text-white hover:border-[#AEA989]'
            }`}
          >
            <span className="text-2xl">🌿</span>
            <span>{t('yesProvideDetails')}</span>
          </button>

          <button
            type="button"
            onClick={() => setOptIn(false)}
            className={`p-6 rounded-2xl border-2 font-bold text-lg flex items-center justify-center gap-3 transition-all active:scale-98 ${
              optIn === false
                ? 'bg-[#AEA989] border-[#AEA989] text-[#131936] shadow-md ring-4 ring-[#AEA989]/40 font-extrabold'
                : 'bg-[#0b0e1e] border-[#AEA989]/30 text-white hover:border-[#AEA989]'
            }`}
          >
            <span className="text-2xl">⏭️</span>
            <span>{t('skipThisStep')}</span>
          </button>
        </div>

        {/* Detailed Ayurveda Questions when opted in */}
        {optIn === true && (
          <div className="space-y-4 pt-4 border-t border-slate-700/60 animate-fadeIn">
            <div>
              <label className="block text-xs font-bold text-[#AEA989] mb-2">
                Primary Prakriti Constitution (Self-assessed or known)
              </label>
              <select
                value={prakriti}
                onChange={(e) => setPrakriti(e.target.value)}
                className="w-full px-3.5 py-3 rounded-xl bg-[#0b0e1e] border border-[#AEA989]/50 text-sm font-semibold text-white"
              >
                <option value="Vata-Pitta">Vata-Pitta (Light, energetic, sensitive to cold)</option>
                <option value="Pitta-Kapha">Pitta-Kapha (Moderate build, strong appetite)</option>
                <option value="Kapha-Vata">Kapha-Vata (Calm, steady, heavy sleeper)</option>
                <option value="Tridosha">Tridoshic (Balanced constitution)</option>
                <option value="Unsure">Unsure / Need Doctor Assessment</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-bold text-[#AEA989] mb-2">
                Digestion & Appetite Pattern (Agni)
              </label>
              <select
                value={digestion}
                onChange={(e) => setDigestion(e.target.value)}
                className="w-full px-3.5 py-3 rounded-xl bg-[#0b0e1e] border border-[#AEA989]/50 text-sm font-semibold text-white"
              >
                <option value="Samagni (Balanced)">Samagni — Normal regular digestion</option>
                <option value="Tikshnagni (Sharp/Hyperactive)">Tikshnagni — Intense appetite, frequent hunger, acidity</option>
                <option value="Mandagni (Sluggish)">Mandagni — Slow digestion, heaviness after meals</option>
                <option value="Vishamagni (Irregular)">Vishamagni — Variable appetite, gas and bloating</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-bold text-[#AEA989] mb-2">
                Sleep & Rest (Nidra)
              </label>
              <select
                value={sleep}
                onChange={(e) => setSleep(e.target.value)}
                className="w-full px-3.5 py-3 rounded-xl bg-[#0b0e1e] border border-[#AEA989]/50 text-sm font-semibold text-white"
              >
                <option value="Moderate (6-7 hrs)">Sound, uninterrupted sleep (6-8 hours)</option>
                <option value="Restless / Light">Light sleeper, difficulty falling asleep</option>
                <option value="Excessive Sleep">Heavy sleep, difficulty waking up</option>
              </select>
            </div>
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
            disabled={optIn === null}
            loading={loading}
            onClick={handleFinish}
            className="px-8"
          >
            {t('continueToDocuments')} →
          </KioskButton>
        </div>
      </div>
    </div>
  );
};
