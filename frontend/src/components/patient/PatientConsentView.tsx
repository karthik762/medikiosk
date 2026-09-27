import React from 'react';
import { KioskButton } from './KioskButton';
import { getTranslation } from '../../i18n/translations';

interface PatientConsentViewProps {
  onAccept: () => Promise<void>;
  onDecline: () => void;
  loading?: boolean;
  language?: 'en' | 'hi' | 'te';
}

export const PatientConsentView: React.FC<PatientConsentViewProps> = ({
  onAccept,
  onDecline,
  loading = false,
  language = 'en',
}) => {
  const t = (key: Parameters<typeof getTranslation>[1]) => getTranslation(language, key);

  return (
    <div className="max-w-2xl mx-auto py-8 px-6 animate-fadeIn">
      <div className="mb-6 text-center">
        <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-[#AEA989]/20 border border-[#AEA989]/40 text-[#AEA989] text-3xl mb-3 shadow-xs">
          📝
        </div>
        <h2 className="text-3xl font-extrabold text-white mb-2" aria-label="Consent for Digital Intake">{t('consentTitle')}</h2>
        <p className="text-slate-300 text-sm">{t('consentSubtitle')}</p>
      </div>

      <div className="bg-[#131936] rounded-3xl p-8 border border-[#AEA989]/40 shadow-xl space-y-6">
        {/* Purpose Bullet 1 */}
        <div className="flex items-start gap-4 p-5 rounded-2xl bg-[#0b0e1e] border border-[#AEA989]/30">
          <div className="text-2xl mt-0.5">🎯</div>
          <div>
            <h4 className="font-bold text-[#AEA989] text-base mb-1">{t('purpose1Title')}</h4>
            <p className="text-slate-300 text-sm leading-relaxed">
              {t('purpose1Desc')}
            </p>
          </div>
        </div>

        {/* Purpose Bullet 2 */}
        <div className="flex items-start gap-4 p-5 rounded-2xl bg-[#0b0e1e] border border-[#AEA989]/30">
          <div className="text-2xl mt-0.5">👨‍⚕️</div>
          <div>
            <h4 className="font-bold text-[#AEA989] text-base mb-1">{t('purpose2Title')}</h4>
            <p className="text-slate-300 text-sm leading-relaxed">
              {t('purpose2Desc')}
            </p>
          </div>
        </div>

        {/* Purpose Bullet 3 */}
        <div className="flex items-start gap-4 p-5 rounded-2xl bg-[#0b0e1e] border border-[#AEA989]/30">
          <div className="text-2xl mt-0.5">👁️</div>
          <div>
            <h4 className="font-bold text-[#AEA989] text-base mb-1">{t('purpose3Title')}</h4>
            <p className="text-slate-300 text-sm leading-relaxed">
              {t('purpose3Desc')}
            </p>
          </div>
        </div>

        {/* Reassurance Notice */}
        <div className="p-4 rounded-2xl bg-[#AEA989]/15 border border-[#AEA989]/40 text-[#AEA989] text-xs font-semibold flex items-center gap-2">
          <span>🔒</span>
          <span>{t('consentPrivacyNotice')}</span>
        </div>

        {/* Explicit Action Buttons */}
        <div className="pt-4 flex flex-col sm:flex-row items-center justify-between gap-4 border-t border-slate-700/60">
          <KioskButton
            type="button"
            variant="outline"
            size="lg"
            onClick={onDecline}
            className="w-full sm:w-auto"
          >
            {t('back')}
          </KioskButton>

          <KioskButton
            type="button"
            variant="primary"
            size="lg"
            loading={loading}
            onClick={onAccept}
            className="w-full sm:w-auto px-8"
            aria-label={t('acceptConsent')}
            icon={<span>✓</span>}
          >
            {t('acceptConsent')}
          </KioskButton>
        </div>

      </div>
    </div>
  );
};
