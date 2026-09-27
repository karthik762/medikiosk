import React from 'react';
import { getTranslation } from '../../i18n/translations';

interface PatientHeaderProps {
  language: string;
  onLanguageChange: (lang: string) => void;
  textSize?: 'normal' | 'large' | 'xlarge';
  onTextSizeChange?: (size: 'normal' | 'large' | 'xlarge') => void;
  textScale?: 'sm' | 'md' | 'lg';
  onTextScaleChange?: (scale: 'sm' | 'md' | 'lg') => void;
  onSwitchToClinician?: () => void;
  onStaffLogin?: () => void;
  isKioskActive?: boolean;
}

export const PatientHeader: React.FC<PatientHeaderProps> = ({
  language,
  onLanguageChange,
  textSize = 'normal',
  onTextSizeChange,
  textScale,
  onTextScaleChange,
  onSwitchToClinician,
  onStaffLogin,
  isKioskActive: _isKioskActive = false,
}) => {
  const currentScale = textScale || (textSize === 'xlarge' ? 'lg' : textSize === 'large' ? 'md' : 'sm');
  const t = (key: Parameters<typeof getTranslation>[1]) => getTranslation(language, key);

  const handleScaleClick = (scale: 'sm' | 'md' | 'lg') => {
    if (onTextScaleChange) {
      onTextScaleChange(scale);
    }
    if (onTextSizeChange) {
      onTextSizeChange(scale === 'sm' ? 'normal' : scale === 'md' ? 'large' : 'xlarge');
    }
  };

  const handleClinicianClick = () => {
    if (onSwitchToClinician) onSwitchToClinician();
    if (onStaffLogin) onStaffLogin();
  };

  return (
    <header className="bg-[#131936]/80 backdrop-blur-md border-b border-[#AEA989]/30 px-6 py-4 sticky top-0 z-30 shadow-md">
      <div className="max-w-6xl mx-auto flex flex-wrap items-center justify-between gap-4">
        {/* Logo & Facility Identification */}
        <div className="flex items-center gap-3">
          <div className="w-12 h-12 rounded-2xl bg-[#AEA989] text-[#131936] flex items-center justify-center text-2xl shadow-md font-bold">
            🏥
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-2xl font-bold text-white tracking-tight">MediKiosk</h1>
              <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-[#AEA989]/20 text-[#AEA989] border border-[#AEA989]/30">
                {t('patientSelfService')}
              </span>
            </div>
            <p className="text-xs text-slate-300 font-medium">{t('headerSubtitle')}</p>
          </div>
        </div>

        {/* Accessibility & Language Controls */}
        <div className="flex items-center flex-wrap gap-3">
          {/* Text Size Accessibility Controls */}
          <div className="flex items-center bg-slate-800 rounded-xl p-1 border border-slate-700 text-xs font-semibold text-slate-200">
            <button
              onClick={() => handleScaleClick('sm')}
              className={`px-3 py-1.5 rounded-lg transition-colors ${currentScale === 'sm' ? 'bg-[#AEA989] text-[#131936] font-bold shadow-xs' : 'hover:text-white'}`}
              title="Standard text size"
            >
              A
            </button>
            <button
              onClick={() => handleScaleClick('md')}
              className={`px-3 py-1.5 rounded-lg text-sm transition-colors ${currentScale === 'md' ? 'bg-[#AEA989] text-[#131936] font-bold shadow-xs' : 'hover:text-white'}`}
              title="Large text size"
            >
              A+
            </button>
            <button
              onClick={() => handleScaleClick('lg')}
              className={`px-3 py-1.5 rounded-lg text-base transition-colors ${currentScale === 'lg' ? 'bg-[#AEA989] text-[#131936] font-bold shadow-xs' : 'hover:text-white'}`}
              title="Extra large text size"
            >
              A++
            </button>
          </div>

          {/* Multilingual Selector */}
          <div className="flex items-center bg-slate-800 rounded-xl p-1 border border-slate-700 text-xs font-semibold">
            <button
              onClick={() => onLanguageChange('en')}
              className={`px-3 py-1.5 rounded-lg transition-colors ${language === 'en' ? 'bg-[#AEA989] text-[#131936] font-bold shadow-xs' : 'text-slate-300 hover:text-white'}`}
            >
              English
            </button>
            <button
              onClick={() => onLanguageChange('hi')}
              className={`px-3 py-1.5 rounded-lg transition-colors ${language === 'hi' ? 'bg-[#AEA989] text-[#131936] font-bold shadow-xs' : 'text-slate-300 hover:text-white'}`}
            >
              हिंदी
            </button>
            <button
              onClick={() => onLanguageChange('te')}
              className={`px-3 py-1.5 rounded-lg transition-colors ${language === 'te' ? 'bg-[#AEA989] text-[#131936] font-bold shadow-xs' : 'text-slate-300 hover:text-white'}`}
            >
              తెలుగు
            </button>
          </div>

          {/* Clinician Access Gateway */}
          <button
            onClick={handleClinicianClick}
            className="flex items-center gap-1.5 text-xs font-semibold px-3 py-2 text-slate-300 hover:text-white hover:bg-slate-800 rounded-xl border border-slate-700 transition-colors"
            title="Clinician workstation login"
          >
            <span>👨‍⚕️</span>
            <span>{t('staffLogin')}</span>
          </button>
        </div>
      </div>
    </header>
  );
};

