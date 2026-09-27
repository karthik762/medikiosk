import React from 'react';
import { KioskButton } from './KioskButton';
import { getTranslation } from '../../i18n/translations';

interface PatientCompletionViewProps {
  patientIdentifier: string;
  patientName: string;
  triagePriority?: string;
  onReset: () => void;
  onSwitchToClinician?: () => void;
  language?: 'en' | 'hi' | 'te';
}

export const PatientCompletionView: React.FC<PatientCompletionViewProps> = ({
  patientIdentifier,
  patientName: _patientName,
  triagePriority = 'NORMAL',
  onReset,
  onSwitchToClinician,
  language = 'en',
}) => {
  const isPriority = triagePriority === 'PRIORITY' || triagePriority === 'URGENT';
  const t = (key: Parameters<typeof getTranslation>[1]) => getTranslation(language, key);

  return (
    <div className="max-w-2xl mx-auto py-12 px-6 text-center animate-fadeIn">
      {/* Success Badge */}
      <div className="w-20 h-20 rounded-full bg-[#AEA989] text-[#131936] flex items-center justify-center text-4xl mx-auto mb-6 shadow-lg animate-bounce font-black">
        ✓
      </div>

      <h2 className="text-4xl font-extrabold text-white tracking-tight mb-2">
        {t('completionTitle')}
      </h2>
      <p className="text-lg text-slate-300 mb-8 max-w-md mx-auto">
        {t('completionSubtitle')}
      </p>

      {/* Queue Token Display Card */}
      <div className="bg-[#131936] rounded-3xl p-8 border border-[#AEA989]/40 shadow-xl max-w-md mx-auto mb-8 space-y-4">
        <span className="text-xs font-bold text-[#AEA989] uppercase tracking-widest block">
          {t('yourConsultationToken')}
        </span>
        <div className="text-3xl sm:text-4xl font-mono font-extrabold text-[#131936] bg-[#AEA989] py-4 px-6 rounded-2xl border border-[#AEA989] tracking-wider shadow-md">
          {patientIdentifier}
        </div>
        <p className="text-xs text-slate-300 font-medium">
          {t('waitingAreaNotice')}
        </p>

        {isPriority && (
          <div className="p-3 bg-amber-950/80 border border-amber-500/40 rounded-xl text-xs text-amber-200 font-semibold flex items-center justify-center gap-2">
            <span>ℹ️</span>
            <span>{t('priorityAttentionNotice')}</span>
          </div>
        )}
      </div>

      {/* Action Buttons */}
      <div className="flex flex-col sm:flex-row items-center justify-center gap-4 max-w-md mx-auto">
        <KioskButton
          size="lg"
          onClick={onReset}
          className="w-full bg-[#AEA989] hover:bg-[#b8b394] text-[#131936] font-bold shadow-md"
          icon={<span>🏠</span>}
        >
          {t('finish')}
        </KioskButton>

        {onSwitchToClinician && (
          <button
            type="button"
            onClick={onSwitchToClinician}
            className="w-full py-4 px-6 rounded-2xl border border-[#AEA989]/40 hover:bg-[#AEA989] hover:text-[#131936] text-[#AEA989] font-bold text-sm transition-colors flex items-center justify-center gap-2 bg-[#131936]"
          >
            <span>👨‍⚕️</span>
            <span>{t('openClinicianQueue')}</span>
          </button>
        )}
      </div>
    </div>
  );
};
