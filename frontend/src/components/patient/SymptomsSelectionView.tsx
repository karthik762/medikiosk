import React, { useState } from 'react';
import { KioskButton } from './KioskButton';
import { getTranslation } from '../../i18n/translations';

export interface SelectedSymptom {
  name: string;
  severity: number;
  duration: string;
}

interface SymptomsSelectionViewProps {
  onSubmit: (symptoms: SelectedSymptom[]) => Promise<void>;
  onBack: () => void;
  loading?: boolean;
  language?: 'en' | 'hi' | 'te';
}

export const SymptomsSelectionView: React.FC<SymptomsSelectionViewProps> = ({
  onSubmit,
  onBack,
  loading = false,
  language = 'en',
}) => {
  const [selectedSymptoms, setSelectedSymptoms] = useState<Record<string, SelectedSymptom>>({});
  const t = (key: Parameters<typeof getTranslation>[1]) => getTranslation(language, key);

  const commonSymptoms = [
    { id: 'chest_pain', name: t('symptomChestPain'), icon: '🫀' },
    { id: 'shortness_of_breath', name: t('symptomSob'), icon: '🫁' },
    { id: 'fever', name: t('symptomFever'), icon: '🌡️' },
    { id: 'headache', name: t('symptomHeadache'), icon: '🤕' },
    { id: 'cough', name: t('symptomCough'), icon: '🗣️' },
    { id: 'dizziness', name: t('symptomDizziness'), icon: '💫' },
    { id: 'abdominal_pain', name: t('symptomStomach'), icon: '🤢' },
    { id: 'fatigue', name: t('symptomFatigue'), icon: '🔋' },
  ];


  const toggleSymptom = (name: string) => {
    setSelectedSymptoms((prev) => {
      const copy = { ...prev };
      if (copy[name]) {
        delete copy[name];
      } else {
        copy[name] = { name, severity: 5, duration: '1-3 days' };
      }
      return copy;
    });
  };

  const updateSeverity = (name: string, severity: number) => {
    setSelectedSymptoms((prev) => {
      if (!prev[name]) return prev;
      return {
        ...prev,
        [name]: { ...prev[name], severity },
      };
    });
  };

  const updateDuration = (name: string, duration: string) => {
    setSelectedSymptoms((prev) => {
      if (!prev[name]) return prev;
      return {
        ...prev,
        [name]: { ...prev[name], duration },
      };
    });
  };

  const handleSubmit = async () => {
    const list = Object.values(selectedSymptoms);
    await onSubmit(list);
  };

  return (
    <div className="max-w-3xl mx-auto py-8 px-6 animate-fadeIn">
      <div className="mb-6 text-center">
        <h2 className="text-3xl font-extrabold text-white mb-2">{t('symptomsTitle')}</h2>
        <p className="text-slate-300 text-sm">{t('symptomsSubtitle')}</p>
      </div>

      <div className="space-y-4 mb-8">
        {commonSymptoms.map((item) => {
          const isSelected = !!selectedSymptoms[item.name];
          const current = selectedSymptoms[item.name];

          return (
            <div
              key={item.id}
              className={`p-5 rounded-2xl border-2 transition-all ${
                isSelected
                  ? 'bg-[#131936] border-[#AEA989] shadow-md'
                  : 'bg-[#0b0e1e] border-[#AEA989]/30 hover:border-[#AEA989]/60'
              }`}
            >
              <div
                onClick={() => toggleSymptom(item.name)}
                className="flex items-center justify-between cursor-pointer select-none"
              >
                <div className="flex items-center gap-3">
                  <span className="text-3xl">{item.icon}</span>
                  <span className="text-lg font-bold text-white">{item.name}</span>
                </div>
                <div
                  className={`w-8 h-8 rounded-full flex items-center justify-center font-bold text-sm transition-all ${
                    isSelected ? 'bg-[#AEA989] text-[#131936]' : 'bg-[#131936] text-[#AEA989] border border-[#AEA989]/40'
                  }`}
                >
                  {isSelected ? '✓' : '+'}
                </div>
              </div>

              {isSelected && current && (
                <div className="mt-4 pt-4 border-t border-[#AEA989]/30 grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-bold text-[#AEA989] mb-2">
                      {t('severityLabel')}: <span className="text-white font-extrabold">{current.severity}/10</span>
                    </label>
                    <input
                      type="range"
                      min="1"
                      max="10"
                      value={current.severity}
                      onChange={(e) => updateSeverity(item.name, parseInt(e.target.value, 10))}
                      className="w-full accent-[#AEA989] cursor-pointer"
                    />
                    <div className="flex justify-between text-2xs text-slate-300 mt-1 font-bold">
                      <span>{t('mild')}</span>
                      <span>{t('moderate')}</span>
                      <span>{t('severe')}</span>
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-bold text-[#AEA989] mb-2">{t('durationLabel')}</label>
                    <div className="grid grid-cols-3 gap-1.5">
                      {[
                        { raw: '< 24 hrs', label: t('duration24h') },
                        { raw: '1-3 days', label: t('duration1_3d') },
                        { raw: '> 1 week', label: t('duration1wPlus') },
                      ].map((d) => (
                        <button
                          type="button"
                          key={d.raw}
                          onClick={() => updateDuration(item.name, d.raw)}
                          className={`py-1.5 px-2 rounded-lg text-xs font-bold border transition-all ${
                            current.duration === d.raw
                              ? 'bg-[#AEA989] text-[#131936] border-[#AEA989] shadow-xs'
                              : 'bg-[#0b0e1e] text-slate-200 border-[#AEA989]/30 hover:border-[#AEA989]'
                          }`}
                        >
                          {d.label}
                        </button>
                      ))}
                    </div>
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>

      <div className="flex items-center justify-between gap-4 pt-4 border-t border-slate-700/60">
        <KioskButton type="button" variant="outline" size="lg" onClick={onBack}>
          {t('back')}
        </KioskButton>
        <KioskButton
          type="button"
          size="lg"
          loading={loading}
          onClick={handleSubmit}
          className="px-8"
        >
          {t('confirmContinue')} →
        </KioskButton>
      </div>
    </div>
  );
};
