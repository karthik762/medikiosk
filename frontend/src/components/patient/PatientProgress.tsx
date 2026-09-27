import React from 'react';
import { getTranslation } from '../../i18n/translations';

export interface StepItem {
  id: string;
  labelKey: Parameters<typeof getTranslation>[1];
}

const DEFAULT_STEPS: StepItem[] = [
  { id: 'registration', labelKey: 'stepRegistration' },
  { id: 'consent', labelKey: 'stepConsent' },
  { id: 'complaint', labelKey: 'stepComplaint' },
  { id: 'questions', labelKey: 'stepQuestions' },
  { id: 'symptoms', labelKey: 'stepSymptoms' },
  { id: 'history', labelKey: 'stepHistory' },
  { id: 'medications', labelKey: 'stepMedications' },
  { id: 'allergies', labelKey: 'stepAllergies' },
  { id: 'records', labelKey: 'stepDocuments' },
  { id: 'review', labelKey: 'stepReview' },
];

interface PatientProgressProps {
  currentStepIndex: number;
  steps?: StepItem[];
  language?: 'en' | 'hi' | 'te';
}

export const PatientProgress: React.FC<PatientProgressProps> = ({
  currentStepIndex,
  steps = DEFAULT_STEPS,
  language = 'en',
}) => {
  const t = (key: Parameters<typeof getTranslation>[1]) => getTranslation(language, key);

  return (
    <div className="w-full bg-[#131936] border-b border-slate-800 py-3 px-6 shadow-sm">
      <div className="max-w-5xl mx-auto flex items-center justify-between gap-2 overflow-x-auto pb-1 no-scrollbar">
        {steps.map((step, idx) => {
          const isCompleted = idx < currentStepIndex;
          const isCurrent = idx === currentStepIndex;
          const stepLabel = t(step.labelKey);

          return (
            <div key={step.id} className="flex items-center gap-2 shrink-0">
              <div
                className={`flex items-center justify-center w-7 h-7 rounded-full text-xs font-bold transition-all ${
                  isCompleted
                    ? 'bg-[#AEA989] text-[#131936] font-extrabold shadow-xs'
                    : isCurrent
                    ? 'bg-emerald-500 text-slate-950 font-extrabold ring-4 ring-emerald-500/30 shadow-sm'
                    : 'bg-slate-800 text-slate-400 border border-slate-700'
                }`}
              >
                {isCompleted ? '✓' : idx + 1}
              </div>
              <span
                className={`text-xs font-semibold whitespace-nowrap transition-colors ${
                  isCurrent
                    ? 'text-emerald-400 font-extrabold tracking-wide'
                    : isCompleted
                    ? 'text-[#AEA989] font-bold'
                    : 'text-slate-400'
                }`}
              >
                {stepLabel}
              </span>
              {idx < steps.length - 1 && (
                <div
                  className={`w-5 sm:w-8 h-0.5 rounded-full ${
                    idx < currentStepIndex ? 'bg-[#AEA989]' : 'bg-slate-800'
                  }`}
                />
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};

