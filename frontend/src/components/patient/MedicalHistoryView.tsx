import React, { useState } from 'react';
import { KioskButton } from './KioskButton';
import { getTranslation } from '../../i18n/translations';

export interface HistoryItem {
  category: string;
  description: string;
}

interface MedicalHistoryViewProps {
  onSubmit: (histories: HistoryItem[]) => Promise<void>;
  onBack: () => void;
  loading?: boolean;
  language?: 'en' | 'hi' | 'te';
}

export const MedicalHistoryView: React.FC<MedicalHistoryViewProps> = ({
  onSubmit,
  onBack,
  loading = false,
  language = 'en',
}) => {
  const [selectedConditions, setSelectedConditions] = useState<string[]>([]);
  const [pastSurgeries, setPastSurgeries] = useState<string>('');
  const [familyHistory, setFamilyHistory] = useState<string>('');

  const t = (key: Parameters<typeof getTranslation>[1]) => getTranslation(language, key);

  const commonConditions = [
    { key: 'conditionHypertension', label: t('conditionHypertension') },
    { key: 'conditionDiabetes', label: t('conditionDiabetes') },
    { key: 'conditionAsthma', label: t('conditionAsthma') },
    { key: 'conditionHeartDisease', label: t('conditionHeartDisease') },
    { key: 'conditionThyroid', label: t('conditionThyroid') },
    { key: 'conditionReflux', label: t('conditionReflux') },
  ];

  const toggleCondition = (cond: string) => {
    setSelectedConditions((prev) =>
      prev.includes(cond) ? prev.filter((c) => c !== cond) : [...prev, cond]
    );
  };

  const handleSubmit = async (overrideItems?: HistoryItem[]) => {
    if (overrideItems) {
      await onSubmit(overrideItems);
      return;
    }

    const items: HistoryItem[] = [];
    selectedConditions.forEach((c) => {
      items.push({ category: 'CHRONIC_CONDITION', description: c });
    });

    if (pastSurgeries.trim()) {
      items.push({ category: 'SURGERY', description: pastSurgeries.trim() });
    }

    if (familyHistory.trim()) {
      items.push({ category: 'FAMILY_HISTORY', description: familyHistory.trim() });
    }

    await onSubmit(items);
  };

  return (
    <div className="max-w-2xl mx-auto py-8 px-6 animate-fadeIn">
      <div className="mb-6 text-center">
        <h2 className="text-3xl font-extrabold text-white mb-2">{t('historyTitle')}</h2>
        <p className="text-slate-300 text-sm">{t('historySubtitle')}</p>
      </div>

      <div className="bg-[#131936] rounded-3xl p-8 border border-[#AEA989]/40 shadow-xl space-y-6">
        {/* Chronic Conditions */}
        <div>
          <label className="block text-sm font-bold text-[#AEA989] mb-3">
            {t('historyTitle')}
          </label>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
            {commonConditions.map((cond) => {
              const isSelected = selectedConditions.includes(cond.label);
              return (
                <button
                  type="button"
                  key={cond.key}
                  onClick={() => toggleCondition(cond.label)}
                  className={`p-3.5 rounded-xl border text-left font-bold text-sm transition-all flex items-center justify-between ${
                    isSelected
                      ? 'bg-[#AEA989] border-[#AEA989] text-[#131936] shadow-md font-extrabold'
                      : 'bg-[#0b0e1e] border-[#AEA989]/30 text-slate-200 hover:bg-[#1a2147] hover:text-white'
                  }`}
                >
                  <span>{cond.label}</span>
                  <span className={`text-xs ${isSelected ? 'text-[#131936] font-extrabold' : 'text-[#AEA989]'}`}>
                    {isSelected ? '✓' : '+'}
                  </span>
                </button>
              );
            })}
          </div>
        </div>

        {/* Past Surgeries */}
        <div>
          <label className="block text-sm font-bold text-[#AEA989] mb-2">
            {t('pastSurgeries')}
          </label>
          <input
            type="text"
            value={pastSurgeries}
            onChange={(e) => setPastSurgeries(e.target.value)}
            placeholder={t('addHistoryPlaceholder')}
            className="w-full px-4 py-3.5 rounded-xl bg-[#0b0e1e] border border-[#AEA989]/50 text-white placeholder-slate-400 text-base focus:ring-2 focus:ring-[#AEA989]"
          />
        </div>

        {/* Family Medical History */}
        <div>
          <label className="block text-sm font-bold text-[#AEA989] mb-2">
            {t('familyHistory')}
          </label>
          <input
            type="text"
            value={familyHistory}
            onChange={(e) => setFamilyHistory(e.target.value)}
            placeholder={t('addHistoryPlaceholder')}
            className="w-full px-4 py-3.5 rounded-xl bg-[#0b0e1e] border border-[#AEA989]/50 text-white placeholder-slate-400 text-base focus:ring-2 focus:ring-[#AEA989]"
          />
        </div>

        {/* Not Sure Shortcut */}
        <div className="pt-2">
          <button
            type="button"
            onClick={() => handleSubmit([{ category: 'UNSURE', description: t('notSure') }])}
            className="w-full py-3.5 px-4 rounded-xl border border-[#AEA989]/40 bg-[#0b0e1e] hover:bg-[#1a2147] text-[#AEA989] hover:text-white text-sm font-bold transition-all flex items-center justify-center gap-2"
          >
            <span>❓</span>
            <span>{t('notSure')}</span>
          </button>
        </div>

        {/* Action Buttons */}
        <div className="pt-4 flex items-center justify-between gap-4 border-t border-slate-700/60">
          <KioskButton type="button" variant="outline" size="lg" onClick={onBack}>
            {t('back')}
          </KioskButton>
          <KioskButton
            type="button"
            size="lg"
            loading={loading}
            onClick={() => handleSubmit()}
            className="px-8"
          >
            {t('confirmContinue')} →
          </KioskButton>
        </div>
      </div>
    </div>
  );
};

