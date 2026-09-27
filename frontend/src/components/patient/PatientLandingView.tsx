import React from 'react';
import { KioskButton } from './KioskButton';
import { getTranslation } from '../../i18n/translations';

interface PatientLandingViewProps {
  onStartCheckIn: () => void;
  onContinueSession: () => void;
  onDemoPatientLogin?: () => void;
  isDemoEnabled?: boolean;
  language?: 'en' | 'hi' | 'te';
}

export const PatientLandingView: React.FC<PatientLandingViewProps> = ({
  onStartCheckIn,
  onContinueSession,
  onDemoPatientLogin,
  isDemoEnabled = false,
  language = 'en',
}) => {
  const t = (key: Parameters<typeof getTranslation>[1]) => getTranslation(language, key);

  return (
    <div className="max-w-3xl mx-auto py-12 px-6 flex flex-col items-center text-center animate-fadeIn">
      {/* Welcome Badge */}
      <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-[#AEA989]/20 border border-[#AEA989]/40 text-[#AEA989] text-sm font-semibold mb-6">
        <span className="w-2.5 h-2.5 rounded-full bg-[#AEA989] animate-pulse" />
        {t('clinicalKioskActive')}
      </div>

      {/* Main Hero Header */}
      <h1 className="text-4xl sm:text-5xl font-extrabold tracking-tight mb-4 text-white">
        <span className="text-[#AEA989] block text-3xl sm:text-4xl font-black mb-1">MEDIKIOSK</span>
        <span className="text-white">{t('aiPoweredClinicalIntake')}</span>
      </h1>

      <p className="text-lg sm:text-xl text-slate-300 max-w-2xl mb-10 leading-relaxed">
        {t('landingDescription')}
      </p>

      {/* Primary Action Buttons */}
      <div className="w-full max-w-md flex flex-col gap-4 mb-10">
        <KioskButton
          size="xl"
          onClick={onStartCheckIn}
          className="w-full bg-[#AEA989] hover:bg-[#b8b394] text-[#131936] font-bold shadow-md"
          icon={<span className="text-2xl">📋</span>}
        >
          {t('startCheckIn')}
        </KioskButton>

        <KioskButton
          variant="outline"
          size="lg"
          onClick={onContinueSession}
          className="w-full border-2 border-[#AEA989] text-[#AEA989] bg-[#131936] hover:bg-[#AEA989] hover:text-[#131936] font-bold"
          icon={<span className="text-xl">🔄</span>}
        >
          {t('continueSession')}
        </KioskButton>

        {isDemoEnabled && onDemoPatientLogin && (
          <button
            onClick={onDemoPatientLogin}
            className="mt-2 py-3 px-6 rounded-2xl bg-[#AEA989]/15 hover:bg-[#AEA989]/25 border border-[#AEA989]/40 text-[#AEA989] font-bold text-sm transition-all flex items-center justify-center gap-2 shadow-xs"
          >
            <span>⚡</span>
            <span>{t('demoPatientQuickFill')}</span>
          </button>
        )}
      </div>

      {/* Key Feature Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 w-full text-left max-w-2xl mb-12">
        <div className="p-5 rounded-2xl bg-[#131936]/85 backdrop-blur-md border border-[#AEA989]/30 shadow-md">
          <div className="text-2xl mb-2">🗣️</div>
          <h3 className="font-bold text-[#AEA989] text-base mb-1">{t('voiceOrTouch')}</h3>
          <p className="text-xs text-slate-300 leading-relaxed">{t('voiceOrTouchDesc')}</p>
        </div>
        <div className="p-5 rounded-2xl bg-[#131936]/85 backdrop-blur-md border border-[#AEA989]/30 shadow-md">
          <div className="text-2xl mb-2">📄</div>
          <h3 className="font-bold text-[#AEA989] text-base mb-1">{t('scanDocumentsTitle')}</h3>
          <p className="text-xs text-slate-300 leading-relaxed">{t('scanDocumentsDesc')}</p>
        </div>
        <div className="p-5 rounded-2xl bg-[#131936]/85 backdrop-blur-md border border-[#AEA989]/30 shadow-md">
          <div className="text-2xl mb-2">👨‍⚕️</div>
          <h3 className="font-bold text-[#AEA989] text-base mb-1">{t('doctorVerifiedTitle')}</h3>
          <p className="text-xs text-slate-300 leading-relaxed">{t('doctorVerifiedDesc')}</p>
        </div>
      </div>

      {/* Privacy & Safety Reassurance */}
      <div className="flex items-center gap-2 text-xs font-medium text-[#AEA989] bg-[#131936] px-4 py-2.5 rounded-xl border border-[#AEA989]/30">
        <span>🔒</span>
        <span>{t('privacyNotice')}</span>
      </div>
    </div>
  );
};
