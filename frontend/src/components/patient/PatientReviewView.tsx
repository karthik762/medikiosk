import React from 'react';
import { KioskButton } from './KioskButton';
import { RegistrationFormData } from './PatientRegistrationView';
import { SelectedSymptom } from './SymptomsSelectionView';
import { HistoryItem } from './MedicalHistoryView';
import { MedicationItem } from './MedicationsView';
import { AllergyItem } from './AllergiesView';
import { AyurvedaData } from './AyurvedaOptInView';
import { AyurvedaHistoryResponse } from '../../types/ayurveda';
import { UploadedDoc } from './DocumentUploadView';
import { getTranslation } from '../../i18n/translations';

interface PatientReviewViewProps {
  registration: RegistrationFormData;
  chiefComplaint: string;
  complaintMethod: 'TEXT' | 'VOICE';
  symptoms: SelectedSymptom[];
  histories: HistoryItem[];
  medications: MedicationItem[];
  allergies: AllergyItem[];
  ayurveda: AyurvedaHistoryResponse | AyurvedaData | any | null;
  documents: UploadedDoc[];
  answers: Record<string, string>;
  onEditSection: (stepIndex: number) => void;
  onConfirmSubmit: () => Promise<void>;
  loading?: boolean;
  language?: 'en' | 'hi' | 'te';
}

export const PatientReviewView: React.FC<PatientReviewViewProps> = ({
  registration,
  chiefComplaint,
  complaintMethod,
  symptoms,
  histories,
  medications,
  allergies,
  ayurveda,
  documents,
  answers,
  onEditSection,
  onConfirmSubmit,
  loading = false,
  language = 'en',
}) => {
  const t = (key: Parameters<typeof getTranslation>[1]) => getTranslation(language, key);

  return (
    <div className="max-w-3xl mx-auto py-8 px-6 animate-fadeIn">
      <div className="mb-6 text-center">
        <h2 className="text-3xl font-extrabold text-white mb-2">{t('reviewTitle')}</h2>
        <p className="text-slate-300 text-sm">{t('reviewSubtitle')}</p>
      </div>

      <div className="space-y-4 mb-8">
        {/* Section 1: Demographics */}
        <div className="p-6 rounded-3xl bg-[#131936] border border-[#AEA989]/40 shadow-xl space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="text-xl">👤</span>
              <h3 className="font-bold text-[#AEA989] text-base">{t('personalDetails')}</h3>
              <span className="text-2xs font-bold px-2 py-0.5 rounded-full bg-[#0b0e1e] text-[#AEA989] border border-[#AEA989]/30">
                {t('patientProvided')}
              </span>
            </div>
            <button
              type="button"
              onClick={() => onEditSection(0)}
              className="text-xs font-bold text-[#AEA989] hover:text-white underline"
            >
              {t('edit')}
            </button>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
            <div>
              <span className="text-slate-400 block font-medium">{t('name')}</span>
              <span className="font-bold text-white">{registration.firstName} {registration.lastName}</span>
            </div>
            <div>
              <span className="text-slate-400 block font-medium">{t('gender')}</span>
              <span className="font-bold text-white">{registration.gender}</span>
            </div>
            <div>
              <span className="text-slate-400 block font-medium">{t('dob')}</span>
              <span className="font-bold text-white">{registration.dateOfBirth}</span>
            </div>
            <div>
              <span className="text-slate-400 block font-medium">{t('phone')}</span>
              <span className="font-bold text-white">{registration.phone || '—'}</span>
            </div>
          </div>
        </div>

        {/* Section 2: Chief Complaint */}
        <div className="p-6 rounded-3xl bg-[#131936] border border-[#AEA989]/40 shadow-xl space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="text-xl">💬</span>
              <h3 className="font-bold text-[#AEA989] text-base">{t('primaryHealthConcern')}</h3>
              <span className="text-2xs font-bold px-2 py-0.5 rounded-full bg-[#0b0e1e] text-[#AEA989] border border-[#AEA989]/30">
                {complaintMethod === 'VOICE' ? t('voiceTranscribedNotice') : t('patientProvided')}
              </span>
            </div>
            <button
              type="button"
              onClick={() => onEditSection(1)}
              className="text-xs font-bold text-[#AEA989] hover:text-white underline"
            >
              {t('edit')}
            </button>
          </div>
          <p className="text-sm font-semibold text-white bg-[#0b0e1e] p-3 rounded-xl border border-[#AEA989]/30">
            "{chiefComplaint}"
          </p>
        </div>

        {/* Section 3: Symptoms & Adaptive Answers */}
        <div className="p-6 rounded-3xl bg-[#131936] border border-[#AEA989]/40 shadow-xl space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="text-xl">🩺</span>
              <h3 className="font-bold text-[#AEA989] text-base">{t('symptomsAndResponses')}</h3>
              <span className="text-2xs font-bold px-2 py-0.5 rounded-full bg-[#0b0e1e] text-[#AEA989] border border-[#AEA989]/30">
                {t('aiStructuredIntake')}
              </span>
            </div>
            <button
              type="button"
              onClick={() => onEditSection(3)}
              className="text-xs font-bold text-[#AEA989] hover:text-white underline"
            >
              {t('edit')}
            </button>
          </div>

          {symptoms.length > 0 ? (
            <div className="flex flex-wrap gap-2">
              {symptoms.map((s, idx) => (
                <span
                  key={idx}
                  className="px-3 py-1.5 rounded-xl bg-[#0b0e1e] text-slate-200 text-xs font-bold border border-[#AEA989]/30"
                >
                  {s.name} ({t('severityLabel')}: {s.severity}/10, {s.duration})
                </span>
              ))}
            </div>
          ) : (
            <p className="text-xs text-slate-400 italic">No specific additional symptoms selected.</p>
          )}

          {Object.keys(answers).length > 0 && (
            <div className="space-y-1.5 pt-2 border-t border-slate-700/60">
              {Object.entries(answers).map(([qKey, aVal], idx) => (
                <div key={idx} className="text-xs text-slate-300">
                  <span className="font-bold text-[#AEA989]">Q: {qKey}</span> →{' '}
                  <span className="text-white font-semibold">{aVal}</span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Section 4: Medical History */}
        <div className="p-6 rounded-3xl bg-[#131936] border border-[#AEA989]/40 shadow-xl space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="text-xl">📋</span>
              <h3 className="font-bold text-[#AEA989] text-base">{t('historyTitle')}</h3>
            </div>
            <button
              type="button"
              onClick={() => onEditSection(4)}
              className="text-xs font-bold text-[#AEA989] hover:text-white underline"
            >
              {t('edit')}
            </button>
          </div>

          {histories.length > 0 ? (
            <div className="flex flex-wrap gap-2">
              {histories.map((h, idx) => (
                <span
                  key={idx}
                  className="px-3 py-1 rounded-lg bg-[#0b0e1e] text-slate-200 text-xs font-semibold border border-[#AEA989]/30"
                >
                  {h.category}: {h.description}
                </span>
              ))}
            </div>
          ) : (
            <p className="text-xs text-slate-400 italic">{t('notSure')}</p>
          )}
        </div>

        {/* Section 5: Medications & Allergies */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="p-6 rounded-3xl bg-[#131936] border border-[#AEA989]/40 shadow-xl space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="font-bold text-[#AEA989] text-sm flex items-center gap-2">
                <span>💊</span>
                <span>{t('medicationsTitle')}</span>
              </h3>
              <button
                type="button"
                onClick={() => onEditSection(5)}
                className="text-xs font-bold text-[#AEA989] hover:text-white underline"
              >
                {t('edit')}
              </button>
            </div>
            {medications.length > 0 ? (
              <ul className="text-xs space-y-1 text-slate-200">
                {medications.map((m, idx) => (
                  <li key={idx}>• {m.name} ({m.dosage}, {m.frequency})</li>
                ))}
              </ul>
            ) : (
              <p className="text-xs text-slate-400 italic">{t('noMedications')}</p>
            )}
          </div>

          <div className="p-6 rounded-3xl bg-[#131936] border border-[#AEA989]/40 shadow-xl space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="font-bold text-[#AEA989] text-sm flex items-center gap-2">
                <span>🛡️</span>
                <span>{t('allergiesTitle')}</span>
              </h3>
              <button
                type="button"
                onClick={() => onEditSection(6)}
                className="text-xs font-bold text-[#AEA989] hover:text-white underline"
              >
                {t('edit')}
              </button>
            </div>
            {allergies.length > 0 ? (
              <ul className="text-xs space-y-1 text-rose-300 font-semibold">
                {allergies.map((a, idx) => (
                  <li key={idx}>⚠️ {a.allergen} ({a.reaction})</li>
                ))}
              </ul>
            ) : (
              <p className="text-xs text-slate-400 italic">{t('noKnownAllergies')}</p>
            )}
          </div>
        </div>

        {/* Section 6: Ayurveda & Documents Status */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="p-5 rounded-2xl bg-[#131936] border border-[#AEA989]/30 text-xs">
            <span className="font-bold text-[#AEA989] block mb-1">🌿 {t('ayurvedaTitle')}</span>
            <span className="text-slate-300">
              {ayurveda ? 'Ayurvedic parameters recorded' : t('skipThisStep')}
            </span>
          </div>
          <div className="p-5 rounded-2xl bg-[#131936] border border-[#AEA989]/30 text-xs">
            <span className="font-bold text-[#AEA989] block mb-1">📄 {t('documentsTitle')}</span>
            <span className="text-slate-300">
              {documents.length > 0 ? `${documents.length} document(s) uploaded & OCR processed` : t('noDocuments')}
            </span>
          </div>
        </div>
      </div>

      {/* Confirmation CTA */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4 pt-4 border-t border-slate-700/60">
        <KioskButton type="button" variant="outline" size="lg" onClick={() => onEditSection(0)}>
          {t('back')}
        </KioskButton>

        <KioskButton
          type="button"
          variant="primary"
          size="lg"
          loading={loading}
          onClick={onConfirmSubmit}
          className="w-full sm:w-auto px-10 font-black text-lg py-4 bg-[#AEA989] hover:bg-[#b8b394] text-[#131936]"
          icon={<span>✓</span>}
        >
          {t('confirmIntake')}
        </KioskButton>
      </div>
    </div>
  );
};
