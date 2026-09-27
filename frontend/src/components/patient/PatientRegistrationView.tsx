import React, { useState } from 'react';
import { KioskButton } from './KioskButton';
import { getTranslation } from '../../i18n/translations';

export interface RegistrationFormData {
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: string;
  phone: string;
  email: string;
}

interface PatientRegistrationViewProps {
  onSubmit: (data: RegistrationFormData) => Promise<void>;
  onCancel: () => void;
  loading?: boolean;
  language?: 'en' | 'hi' | 'te';
}

export const PatientRegistrationView: React.FC<PatientRegistrationViewProps> = ({
  onSubmit,
  onCancel,
  loading = false,
  language = 'en',
}) => {
  const [formData, setFormData] = useState<RegistrationFormData>({
    firstName: '',
    lastName: '',
    dateOfBirth: '1988-06-15',
    gender: 'MALE',
    phone: '+91 98765 43210',
    email: '',
  });

  const [error, setError] = useState<string | null>(null);
  const t = (key: Parameters<typeof getTranslation>[1]) => getTranslation(language, key);


  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!formData.firstName.trim() || !formData.lastName.trim()) {
      setError('Please provide both your first and last name.');
      return;
    }

    try {
      await onSubmit(formData);
    } catch (err: any) {
      setError(err.message || 'Registration could not be completed. Please try again.');
    }
  };

  return (
    <div className="max-w-2xl mx-auto py-8 px-6 animate-fadeIn">
      <div className="mb-6 text-center">
        <h2 className="text-3xl font-extrabold text-white mb-2">{t('registrationTitle')}</h2>
        <p className="text-slate-300 text-sm">{t('registrationSubtitle')}</p>
      </div>

      {error && (
        <div className="mb-6 p-4 bg-rose-950/80 border border-rose-500/50 text-rose-200 rounded-2xl text-sm font-medium flex items-center gap-3">
          <span className="text-xl">⚠️</span>
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} className="bg-[#131936] rounded-3xl p-8 border border-[#AEA989]/40 shadow-xl space-y-6">
        {/* Name Fields */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-bold text-[#AEA989] mb-2">{t('firstName')} *</label>
            <input
              type="text"
              required
              value={formData.firstName}
              onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
              placeholder="e.g. Ramesh"
              className="w-full px-4 py-3.5 rounded-xl bg-[#0b0e1e] border border-[#AEA989]/50 text-white placeholder-slate-400 text-lg focus:ring-2 focus:ring-[#AEA989] focus:border-[#AEA989]"
            />
          </div>
          <div>
            <label className="block text-sm font-bold text-[#AEA989] mb-2">{t('lastName')} *</label>
            <input
              type="text"
              required
              value={formData.lastName}
              onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
              placeholder="e.g. Rao"
              className="w-full px-4 py-3.5 rounded-xl bg-[#0b0e1e] border border-[#AEA989]/50 text-white placeholder-slate-400 text-lg focus:ring-2 focus:ring-[#AEA989] focus:border-[#AEA989]"
            />
          </div>
        </div>

        {/* Date of Birth & Gender */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-bold text-[#AEA989] mb-2">{t('dateOfBirth')}</label>
            <input
              type="date"
              value={formData.dateOfBirth}
              onChange={(e) => setFormData({ ...formData, dateOfBirth: e.target.value })}
              className="w-full px-4 py-3.5 rounded-xl bg-[#0b0e1e] border border-[#AEA989]/50 text-white text-lg focus:ring-2 focus:ring-[#AEA989] focus:border-[#AEA989]"
            />
          </div>
          <div>
            <label className="block text-sm font-bold text-[#AEA989] mb-2">{t('gender')}</label>
            <div className="grid grid-cols-3 gap-2">
              {['MALE', 'FEMALE', 'OTHER'].map((g) => (
                <button
                  type="button"
                  key={g}
                  onClick={() => setFormData({ ...formData, gender: g })}
                  className={`py-3.5 px-2 rounded-xl text-sm font-bold border transition-all ${
                    formData.gender === g
                      ? 'bg-[#AEA989] text-[#131936] border-[#AEA989] shadow-md font-extrabold'
                      : 'bg-[#0b0e1e] text-slate-200 border-[#AEA989]/30 hover:bg-[#1a2147] hover:text-white'
                  }`}
                >
                  {g === 'MALE' ? t('male') : g === 'FEMALE' ? t('female') : t('other')}
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* Contact Fields */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-bold text-[#AEA989] mb-2">{t('phone')}</label>
            <input
              type="tel"
              value={formData.phone}
              onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
              placeholder="+91 98765 43210"
              className="w-full px-4 py-3.5 rounded-xl bg-[#0b0e1e] border border-[#AEA989]/50 text-white placeholder-slate-400 text-lg focus:ring-2 focus:ring-[#AEA989] focus:border-[#AEA989]"
            />
          </div>
          <div>
            <label className="block text-sm font-bold text-[#AEA989] mb-2">{t('email')}</label>
            <input
              type="email"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              placeholder="patient@example.com"
              className="w-full px-4 py-3.5 rounded-xl bg-[#0b0e1e] border border-[#AEA989]/50 text-white placeholder-slate-400 text-lg focus:ring-2 focus:ring-[#AEA989] focus:border-[#AEA989]"
            />
          </div>
        </div>

        {/* Action Buttons */}
        <div className="pt-4 flex items-center justify-between gap-4 border-t border-slate-700/60">
          <KioskButton type="button" variant="outline" size="lg" onClick={onCancel}>
            {t('back')}
          </KioskButton>
          <KioskButton
            type="submit"
            size="lg"
            loading={loading}
            className="px-10"
            aria-label="Continue to Consent"
          >
            {t('confirmContinue')} →
          </KioskButton>
        </div>
      </form>
    </div>
  );
};

