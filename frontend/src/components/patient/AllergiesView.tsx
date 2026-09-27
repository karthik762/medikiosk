import React, { useState } from 'react';
import { KioskButton } from './KioskButton';
import { getTranslation } from '../../i18n/translations';

export interface AllergyItem {
  allergen: string;
  reaction: string;
}

interface AllergiesViewProps {
  onSubmit: (allergies: AllergyItem[]) => Promise<void>;
  onBack: () => void;
  loading?: boolean;
  language?: 'en' | 'hi' | 'te';
}

export const AllergiesView: React.FC<AllergiesViewProps> = ({
  onSubmit,
  onBack,
  loading = false,
  language = 'en',
}) => {
  const [hasAllergies, setHasAllergies] = useState<boolean | null>(null);
  const [selectedAllergens, setSelectedAllergens] = useState<string[]>([]);
  const [customAllergen, setCustomAllergen] = useState('');
  const [reactionText, setReactionText] = useState('Hives / Rash');

  const t = (key: Parameters<typeof getTranslation>[1]) => getTranslation(language, key);

  const commonAllergens = [
    'Penicillin / Amoxicillin',
    'Sulfa Drugs (Sulfonamides)',
    'Aspirin / NSAIDs',
    'Latex',
    'Peanuts / Tree nuts',
    'Shellfish / Seafood',
  ];

  const toggleAllergen = (item: string) => {
    setSelectedAllergens((prev) =>
      prev.includes(item) ? prev.filter((a) => a !== item) : [...prev, item]
    );
  };

  const handleSubmit = async (override?: AllergyItem[]) => {
    if (override) {
      await onSubmit(override);
      return;
    }

    if (hasAllergies === false) {
      await onSubmit([]);
      return;
    }

    const items: AllergyItem[] = selectedAllergens.map((a) => ({
      allergen: a,
      reaction: reactionText,
    }));

    if (customAllergen.trim()) {
      items.push({
        allergen: customAllergen.trim(),
        reaction: reactionText,
      });
    }

    await onSubmit(items);
  };

  return (
    <div className="max-w-2xl mx-auto py-8 px-6 animate-fadeIn">
      <div className="mb-6 text-center">
        <div className="inline-flex items-center justify-center w-12 h-12 rounded-2xl bg-[#AEA989]/20 text-[#AEA989] text-2xl mb-2 shadow-xs">
          🛡️
        </div>
        <h2 className="text-3xl font-extrabold text-white mb-2">{t('allergiesTitle')}</h2>
        <p className="text-slate-300 text-sm">{t('allergiesSubtitle')}</p>
      </div>

      <div className="bg-[#131936] rounded-3xl p-8 border border-[#AEA989]/40 shadow-xl space-y-6">
        {/* Primary Choice Buttons */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <button
            type="button"
            onClick={() => {
              setHasAllergies(false);
              setSelectedAllergens([]);
            }}
            className={`p-6 rounded-2xl border-2 font-bold text-lg flex items-center justify-center gap-3 transition-all active:scale-98 ${
              hasAllergies === false
                ? 'bg-[#AEA989] border-[#AEA989] text-[#131936] shadow-md ring-4 ring-[#AEA989]/40 font-extrabold'
                : 'bg-[#0b0e1e] border-[#AEA989]/30 text-white hover:border-[#AEA989]'
            }`}
          >
            <span className="text-2xl">🛡️</span>
            <span>{t('noKnownAllergies')}</span>
          </button>

          <button
            type="button"
            onClick={() => setHasAllergies(true)}
            className={`p-6 rounded-2xl border-2 font-bold text-lg flex items-center justify-center gap-3 transition-all active:scale-98 ${
              hasAllergies === true
                ? 'bg-rose-600 border-rose-600 text-white shadow-md ring-4 ring-rose-900/50'
                : 'bg-[#0b0e1e] border-[#AEA989]/30 text-white hover:border-[#AEA989]'
            }`}
          >
            <span className="text-2xl">⚠️</span>
            <span>{t('allergiesTitle')}</span>
          </button>
        </div>

        {/* Detailed Allergens Selection when hasAllergies is true */}
        {hasAllergies === true && (
          <div className="space-y-4 pt-4 border-t border-slate-700/60 animate-fadeIn">
            <label className="block text-sm font-bold text-[#AEA989]">
              {t('allergenName')}:
            </label>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
              {commonAllergens.map((item) => {
                const isSelected = selectedAllergens.includes(item);
                return (
                  <button
                    type="button"
                    key={item}
                    onClick={() => toggleAllergen(item)}
                    className={`p-3 rounded-xl border text-left font-bold text-xs transition-all flex items-center justify-between ${
                      isSelected
                        ? 'bg-rose-950 border-rose-500 text-rose-200 shadow-xs'
                        : 'bg-[#0b0e1e] border-[#AEA989]/30 text-slate-200 hover:bg-[#1a2147] hover:text-white'
                    }`}
                  >
                    <span>{item}</span>
                    <span>{isSelected ? '✓' : '+'}</span>
                  </button>
                );
              })}
            </div>

            <div className="pt-2">
              <label className="block text-xs font-bold text-[#AEA989] mb-1">
                {t('allergenName')}
              </label>
              <input
                type="text"
                value={customAllergen}
                onChange={(e) => setCustomAllergen(e.target.value)}
                placeholder={t('addAllergyPlaceholder')}
                className="w-full px-3.5 py-2.5 rounded-xl bg-[#0b0e1e] border border-[#AEA989]/50 text-sm text-white placeholder-slate-400"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-[#AEA989] mb-1">
                {t('typicalReaction')}
              </label>
              <select
                value={reactionText}
                onChange={(e) => setReactionText(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl bg-[#0b0e1e] border border-[#AEA989]/50 text-sm text-white"
              >
                <option value="Hives / Rash / Itching">{t('reactionRash')}</option>
                <option value="Swelling of Face / Lips">{t('reactionSwelling')}</option>
                <option value="Difficulty Breathing / Severe Anaphylaxis">{t('reactionBreathing')}</option>
                <option value="Nausea / Upset Stomach">{t('reactionNausea')}</option>
              </select>
            </div>
          </div>
        )}

        {/* Not Sure Shortcut */}
        <div className="pt-2">
          <button
            type="button"
            onClick={() => handleSubmit([{ allergen: t('notSure'), reaction: 'Unknown' }])}
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
            disabled={hasAllergies === null}
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
