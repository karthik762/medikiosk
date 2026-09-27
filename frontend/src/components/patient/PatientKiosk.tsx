import React, { useState, useEffect } from 'react';
import { PatientHeader } from './PatientHeader';
import { PatientProgress } from './PatientProgress';
import { PatientLandingView } from './PatientLandingView';
import { PatientRegistrationView, RegistrationFormData } from './PatientRegistrationView';
import { PatientConsentView } from './PatientConsentView';
import { ChiefComplaintVoiceView } from './ChiefComplaintVoiceView';
import { AdaptiveQuestionView } from './AdaptiveQuestionView';
import { SymptomsSelectionView, SelectedSymptom } from './SymptomsSelectionView';
import { MedicalHistoryView, HistoryItem } from './MedicalHistoryView';
import { MedicationsView, MedicationItem } from './MedicationsView';
import { AllergiesView, AllergyItem } from './AllergiesView';
import { AyurvedaSection } from '../ayurveda/AyurvedaSection';
import { AyurvedaHistoryResponse } from '../../types/ayurveda';
import { DocumentUploadView, UploadedDoc } from './DocumentUploadView';
import { PatientReviewView } from './PatientReviewView';
import { PatientCompletionView } from './PatientCompletionView';
import {
  kioskService,
  NextQuestionData,
} from '../../services/kioskService';
import { getTranslation } from '../../i18n/translations';

export type KioskStep =
  | 'LANDING'
  | 'REGISTRATION'
  | 'CONSENT'
  | 'COMPLAINT'
  | 'ADAPTIVE'
  | 'SYMPTOMS'
  | 'HISTORY'
  | 'MEDICATIONS'
  | 'ALLERGIES'
  | 'AYURVEDA'
  | 'DOCUMENTS'
  | 'REVIEW'
  | 'COMPLETION';

interface PatientKioskProps {
  onSwitchToClinician: () => void;
}

export const PatientKiosk: React.FC<PatientKioskProps> = ({ onSwitchToClinician }) => {
  // Navigation & Step State
  const [currentStep, setCurrentStep] = useState<KioskStep>('LANDING');
  const [language, setLanguage] = useState<'en' | 'hi' | 'te'>(() => {
    const saved = localStorage.getItem('medikiosk_language');
    if (saved === 'hi' || saved === 'te' || saved === 'en') return saved;
    return 'en';
  });
  const [textScale, setTextScale] = useState<'sm' | 'md' | 'lg'>('md');

  // Session & Authentication
  const [sessionId, setSessionId] = useState<string>('');
  const [_patientId, setPatientId] = useState<string>('');
  const [patientIdentifier, setPatientIdentifier] = useState<string>('');
  const [tokenNumber, setTokenNumber] = useState<string>('');
  const [triagePriority, setTriagePriority] = useState<string>('NORMAL');

  // Intake State
  const [registrationData, setRegistrationData] = useState<RegistrationFormData>({
    firstName: '',
    lastName: '',
    dateOfBirth: '',
    gender: 'MALE',
    phone: '',
    email: '',
  });
  const [chiefComplaint, setChiefComplaint] = useState<string>('');
  const [complaintMethod, setComplaintMethod] = useState<'TEXT' | 'VOICE'>('TEXT');
  const [currentQuestion, setCurrentQuestion] = useState<NextQuestionData | null>(null);
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [symptoms, setSymptoms] = useState<SelectedSymptom[]>([]);
  const [histories, setHistories] = useState<HistoryItem[]>([]);
  const [medications, setMedications] = useState<MedicationItem[]>([]);
  const [allergies, setAllergies] = useState<AllergyItem[]>([]);
  const [ayurveda, setAyurveda] = useState<AyurvedaHistoryResponse | any>(null);
  const [documents, setDocuments] = useState<UploadedDoc[]>([]);

  // Operational State
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [isDemoEnabled, setIsDemoEnabled] = useState<boolean>(true);

  // Check demo status & restore active kiosk session on initial load
  useEffect(() => {
    setError(null);
    const initKiosk = async () => {
      try {
        const demoStatus = await kioskService.checkDemoStatus();
        setIsDemoEnabled(demoStatus);
      } catch {
        setIsDemoEnabled(true);
      }

      // Restore persisted kiosk session if available
      const savedSession = kioskService.getPersistedSession();
      if (savedSession && savedSession.sessionId && savedSession.token) {
        try {
          const sessionDetails = await kioskService.verifySession(savedSession.sessionId);
          if (sessionDetails && sessionDetails.status === 'IN_PROGRESS') {
            setSessionId(savedSession.sessionId);
            if (savedSession.patientId) setPatientId(savedSession.patientId);
            if (savedSession.patientIdentifier) setPatientIdentifier(savedSession.patientIdentifier);

            // Restore step if valid and not LANDING or COMPLETION
            if (
              savedSession.currentStep &&
              savedSession.currentStep !== 'LANDING' &&
              savedSession.currentStep !== 'COMPLETION'
            ) {
              setCurrentStep(savedSession.currentStep as KioskStep);
            } else {
              setCurrentStep('COMPLAINT');
            }
          } else {
            kioskService.clearSession();
          }
        } catch (err: any) {
          kioskService.clearSession();
          if (err?.status === 401 || err?.status === 403) {
            setError('Your kiosk session has expired. Please start check-in again.');
          }
        }
      }
    };
    initKiosk();
  }, []);

  // Helper to change and persist step
  const navigateToStep = (step: KioskStep) => {
    setCurrentStep(step);
    kioskService.saveCurrentStep(step);
  };

  // Compute text scale CSS class
  const getTextScaleClass = () => {
    switch (textScale) {
      case 'sm':
        return 'text-sm';
      case 'lg':
        return 'text-lg';
      default:
        return 'text-base';
    }
  };

  // Convert step to progress index (0 to 9)
  const getStepProgressIndex = (): number => {
    switch (currentStep) {
      case 'REGISTRATION':
        return 0;
      case 'CONSENT':
        return 1;
      case 'COMPLAINT':
        return 2;
      case 'ADAPTIVE':
        return 3;
      case 'SYMPTOMS':
        return 4;
      case 'HISTORY':
        return 5;
      case 'MEDICATIONS':
        return 6;
      case 'ALLERGIES':
        return 7;
      case 'AYURVEDA':
      case 'DOCUMENTS':
        return 8;
      case 'REVIEW':
      case 'COMPLETION':
        return 9;
      default:
        return 0;
    }
  };

  // ----------------------------------------------------
  // Handlers
  // ----------------------------------------------------

  // 1. Start Check-In
  const handleStartCheckIn = () => {
    setError(null);
    navigateToStep('REGISTRATION');
  };

  // Continue existing session
  const handleContinueSession = () => {
    const inputId = window.prompt('Please enter your Session ID or Token:');
    if (inputId && inputId.trim()) {
      setSessionId(inputId.trim());
      navigateToStep('COMPLAINT');
    }
  };

  // 1-Click Demo Patient Quick Login
  const handleDemoPatientLogin = async () => {
    try {
      setLoading(true);
      setError(null);
      const demoRes = await kioskService.demoPatientLogin();
      if (!demoRes.sessionId) {
        throw new Error('Demo setup failed: could not create intake session. Please try again.');
      }
      setSessionId(demoRes.sessionId);
      setPatientId(demoRes.patientId);
      setPatientIdentifier(demoRes.patientIdentifier || 'DEMO-PAT-001');
      setRegistrationData({
        firstName: 'Aarav',
        lastName: 'Sharma',
        dateOfBirth: '1998-05-12',
        gender: 'MALE',
        phone: '9876543210',
        email: 'aarav.sharma@example.com',
      });
      navigateToStep('CONSENT');
    } catch (err: any) {
      if (err?.status === 401 || err?.status === 403) {
        kioskService.clearSession();
        navigateToStep('LANDING');
        setError('Session expired. Please restart demo login.');
      } else {
        setError(err.message || 'Demo patient setup failed. Is the backend running?');
      }
    } finally {
      setLoading(false);
    }
  };

  // 2. Submit Registration
  const handleRegistrationSubmit = async (data: RegistrationFormData) => {
    try {
      setLoading(true);
      setError(null);
      setRegistrationData(data);
      const regRes = await kioskService.registerKioskPatient(data);
      setSessionId(regRes.sessionId);
      setPatientId(regRes.patientId);
      setPatientIdentifier(regRes.patientIdentifier);
      navigateToStep('CONSENT');
    } catch (err: any) {
      if (err?.status === 429) {
        setError(err.message || 'Too many registration attempts. Please wait a moment and try again.');
      } else if (err?.status === 401 || err?.status === 403) {
        kioskService.clearSession();
        navigateToStep('LANDING');
        setError('Session expired during registration. Please try again.');
      } else {
        setError(err.message || 'Registration failed. Please check details.');
      }
    } finally {
      setLoading(false);
    }
  };

  // 3. Accept Consent
  const handleConsentAccept = async () => {
    try {
      setLoading(true);
      setError(null);
      if (sessionId) {
        await kioskService.recordConsent(sessionId, 'INFORMATIVE_AND_CARE');
      }
      navigateToStep('COMPLAINT');
    } catch (err: any) {
      if (err?.status === 401 || err?.status === 403) {
        kioskService.clearSession();
        navigateToStep('LANDING');
        setError('Session expired. Please restart check-in.');
      } else {
        setError(err.message || 'Could not record consent.');
      }
    } finally {
      setLoading(false);
    }
  };

  // 4. Submit Chief Complaint
  const handleChiefComplaintSubmit = async (complaintText: string, method: 'TEXT' | 'VOICE') => {
    try {
      setLoading(true);
      setError(null);
      setChiefComplaint(complaintText);
      setComplaintMethod(method);

      if (sessionId) {
        await kioskService.submitChiefComplaint(sessionId, complaintText, method);
        // Check for adaptive questions
        const nextQRes = await kioskService.getNextAdaptiveQuestion(sessionId);
        if (nextQRes && nextQRes.hasMoreQuestions && nextQRes.question) {
          setCurrentQuestion(nextQRes.question);
          navigateToStep('ADAPTIVE');
          return;
        }
      }
      // If no adaptive question is required, advance to symptoms
      navigateToStep('SYMPTOMS');
    } catch (err: any) {
      if (err?.status === 401 || err?.status === 403) {
        kioskService.clearSession();
        navigateToStep('LANDING');
        setError('Session expired. Please restart check-in.');
      } else {
        setError(err.message || 'Error processing complaint.');
        navigateToStep('SYMPTOMS');
      }
    } finally {
      setLoading(false);
    }
  };

  const [adaptiveHistory, setAdaptiveHistory] = useState<Array<{ questionText: string; answerText: string }>>([]);

  // 5. Submit Adaptive Question Answer
  const handleAdaptiveAnswer = async (answerText: string, method: 'TOUCH' | 'TEXT' | 'VOICE') => {
    try {
      setLoading(true);
      setError(null);
      if (currentQuestion) {
        setAnswers((prev) => ({ ...prev, [currentQuestion.questionText]: answerText }));
        setAdaptiveHistory((prev) => [...prev, { questionText: currentQuestion.questionText, answerText }]);

        if (sessionId) {
          const qId = (currentQuestion as any).questionId || (currentQuestion as any).id;
          await kioskService.submitAnswer(sessionId, qId, answerText, method);
          // Query next adaptive question
          const nextQRes = await kioskService.getNextAdaptiveQuestion(sessionId);
          if (nextQRes && nextQRes.hasMoreQuestions && nextQRes.question) {
            setCurrentQuestion(nextQRes.question);
            return;
          }
        }
      }
      // No more questions
      setCurrentQuestion(null);
      navigateToStep('SYMPTOMS');
    } catch (err: any) {
      if (err?.status === 401 || err?.status === 403) {
        kioskService.clearSession();
        navigateToStep('LANDING');
        setError('Session expired. Please restart check-in.');
      } else {
        console.warn('Error answering adaptive question:', err);
        navigateToStep('SYMPTOMS');
      }
    } finally {
      setLoading(false);
    }
  };

  // Skip Adaptive Question
  const handleSkipAdaptive = async () => {
    if (sessionId) {
      try {
        const nextQRes = await kioskService.getNextAdaptiveQuestion(sessionId);
        const currentQId = (currentQuestion as any)?.questionId || (currentQuestion as any)?.id;
        const nextQId = (nextQRes?.question as any)?.questionId || (nextQRes?.question as any)?.id;
        if (nextQRes && nextQRes.hasMoreQuestions && nextQRes.question && nextQId !== currentQId) {
          setCurrentQuestion(nextQRes.question);
          return;
        }
      } catch (err: any) {
        if (err?.status === 401 || err?.status === 403) {
          kioskService.clearSession();
          navigateToStep('LANDING');
          setError('Session expired. Please restart check-in.');
          return;
        }
      }
    }
    navigateToStep('SYMPTOMS');
  };

  // 6. Submit Symptoms
  const handleSymptomsSubmit = async (selected: SelectedSymptom[]) => {
    try {
      setLoading(true);
      setError(null);
      setSymptoms(selected);
      if (sessionId) {
        for (const s of selected) {
          await kioskService.addSymptom(sessionId, {
            symptomName: s.name,
            severity: s.severity,
            duration: s.duration,
          });
        }
      }
      navigateToStep('HISTORY');
    } catch (err: any) {
      if (err?.status === 401 || err?.status === 403) {
        kioskService.clearSession();
        navigateToStep('LANDING');
        setError('Session expired. Please restart check-in.');
      } else {
        setError(err.message || 'Error saving symptoms.');
        navigateToStep('HISTORY');
      }
    } finally {
      setLoading(false);
    }
  };

  // 7. Submit Medical History
  const handleHistorySubmit = async (items: HistoryItem[]) => {
    try {
      setLoading(true);
      setError(null);
      setHistories(items);
      if (sessionId) {
        for (const h of items) {
          await kioskService.addMedicalHistory(sessionId, {
            category: h.category,
            description: h.description,
          });
        }
      }
      navigateToStep('MEDICATIONS');
    } catch (err: any) {
      if (err?.status === 401 || err?.status === 403) {
        kioskService.clearSession();
        navigateToStep('LANDING');
        setError('Session expired. Please restart check-in.');
      } else {
        setError(err.message || 'Error saving medical history.');
        navigateToStep('MEDICATIONS');
      }
    } finally {
      setLoading(false);
    }
  };

  // 8. Submit Medications
  const handleMedicationsSubmit = async (meds: MedicationItem[]) => {
    try {
      setLoading(true);
      setError(null);
      setMedications(meds);
      if (sessionId) {
        for (const m of meds) {
          await kioskService.addMedication(sessionId, {
            medicationName: m.name,
            dosage: m.dosage,
            frequency: m.frequency,
          });
        }
      }
      navigateToStep('ALLERGIES');
    } catch (err: any) {
      if (err?.status === 401 || err?.status === 403) {
        kioskService.clearSession();
        navigateToStep('LANDING');
        setError('Session expired. Please restart check-in.');
      } else {
        setError(err.message || 'Error saving medications.');
        navigateToStep('ALLERGIES');
      }
    } finally {
      setLoading(false);
    }
  };

  // 9. Submit Allergies
  const handleAllergiesSubmit = async (items: AllergyItem[]) => {
    try {
      setLoading(true);
      setError(null);
      setAllergies(items);
      if (sessionId) {
        for (const a of items) {
          await kioskService.addAllergy(sessionId, {
            allergen: a.allergen,
            reaction: a.reaction,
          });
        }
      }
      navigateToStep('AYURVEDA');
    } catch (err: any) {
      if (err?.status === 401 || err?.status === 403) {
        kioskService.clearSession();
        navigateToStep('LANDING');
        setError('Session expired. Please restart check-in.');
      } else {
        setError(err.message || 'Error saving allergies.');
        navigateToStep('AYURVEDA');
      }
    } finally {
      setLoading(false);
    }
  };

  // 10. Submit Ayurveda
  const handleAyurvedaSubmit = async (ayurData: AyurvedaHistoryResponse | any) => {
    setAyurveda(ayurData);
    navigateToStep('DOCUMENTS');
  };

  // 11. Submit Documents
  const handleDocumentsSubmit = async (docs: UploadedDoc[]) => {
    setDocuments(docs);
    navigateToStep('REVIEW');
  };

  // 12. Final Confirmation and Submission
  const handleReviewConfirm = async () => {
    try {
      setLoading(true);
      setError(null);
      if (sessionId) {
        const compRes = await kioskService.completeIntake(sessionId);
        setTokenNumber(compRes.tokenNumber || `TK-${Math.floor(1000 + Math.random() * 9000)}`);
        setTriagePriority(compRes.triagePriority || 'NORMAL');
      } else {
        setTokenNumber(`TK-${Math.floor(1000 + Math.random() * 9000)}`);
        setTriagePriority('NORMAL');
      }
      navigateToStep('COMPLETION');
    } catch (err: any) {
      if (err?.status === 401 || err?.status === 403) {
        kioskService.clearSession();
        navigateToStep('LANDING');
        setError('Session expired. Intake could not be finalized.');
      } else {
        setError(err.message || 'Error completing check-in.');
        setTokenNumber(`TK-${Math.floor(1000 + Math.random() * 9000)}`);
        navigateToStep('COMPLETION');
      }
    } finally {
      setLoading(false);
    }
  };

  // Edit section from Review
  const handleEditSection = (stepIndex: number) => {
    switch (stepIndex) {
      case 0:
        navigateToStep('REGISTRATION');
        break;
      case 2:
        navigateToStep('COMPLAINT');
        break;
      case 4:
        navigateToStep('SYMPTOMS');
        break;
      case 5:
        navigateToStep('HISTORY');
        break;
      case 6:
        navigateToStep('MEDICATIONS');
        break;
      case 7:
        navigateToStep('ALLERGIES');
        break;
      case 8:
        navigateToStep('AYURVEDA');
        break;
      default:
        navigateToStep('REVIEW');
        break;
    }
  };

  // Reset to initial landing
  const handleReset = () => {
    kioskService.clearSession();
    setCurrentStep('LANDING');
    setSessionId('');
    setPatientId('');
    setPatientIdentifier('');
    setTokenNumber('');
    setTriagePriority('NORMAL');
    setChiefComplaint('');
    setComplaintMethod('TEXT');
    setCurrentQuestion(null);
    setAnswers({});
    setSymptoms([]);
    setHistories([]);
    setMedications([]);
    setAllergies([]);
    setAyurveda(null);
    setDocuments([]);
    setError(null);
  };

  return (
    <div className={`min-h-screen bg-transparent text-slate-100 flex flex-col font-sans select-none ${getTextScaleClass()}`}>
      {/* Universal Patient Kiosk Header */}
      <PatientHeader
        language={language}
        onLanguageChange={(l: string) => {
          const selected = (l === 'hi' || l === 'te' ? l : 'en') as 'en' | 'hi' | 'te';
          setLanguage(selected);
          localStorage.setItem('medikiosk_language', selected);
        }}
        textScale={textScale}
        onTextScaleChange={setTextScale}
        onStaffLogin={onSwitchToClinician}
      />

      {/* Progress Bar (Visible during active intake steps) */}
      {currentStep !== 'LANDING' && currentStep !== 'COMPLETION' && (
        <PatientProgress currentStepIndex={getStepProgressIndex()} language={language} />
      )}

      {/* Global Error Banner */}
      {error && (
        <div className="max-w-xl mx-auto mt-4 px-4 py-3 rounded-2xl bg-rose-50 border border-rose-200 text-rose-700 text-sm flex items-center justify-between shadow-xs animate-shake">
          <div className="flex items-center gap-2">
            <span className="text-lg">⚠️</span>
            <span>{error}</span>
          </div>
          <button
            onClick={() => setError(null)}
            className="text-rose-500 hover:text-rose-800 font-bold px-2 py-1"
          >
            ✕
          </button>
        </div>
      )}

      {/* Main Interactive Stage */}
      <main className="flex-1 flex flex-col justify-center py-4">
        {currentStep === 'LANDING' && (
          <PatientLandingView
            onStartCheckIn={handleStartCheckIn}
            onContinueSession={handleContinueSession}
            onDemoPatientLogin={handleDemoPatientLogin}
            isDemoEnabled={isDemoEnabled}
            language={language}
          />
        )}

        {currentStep === 'REGISTRATION' && (
          <PatientRegistrationView
            onSubmit={handleRegistrationSubmit}
            onCancel={() => setCurrentStep('LANDING')}
            loading={loading}
            language={language}
          />
        )}

        {currentStep === 'CONSENT' && (
          <PatientConsentView
            onAccept={handleConsentAccept}
            onDecline={() => setCurrentStep('LANDING')}
            loading={loading}
            language={language}
          />
        )}

        {currentStep === 'COMPLAINT' && (
          <ChiefComplaintVoiceView
            onSubmit={handleChiefComplaintSubmit}
            onBack={() => setCurrentStep('CONSENT')}
            loading={loading}
            language={language}
          />
        )}

        {currentStep === 'ADAPTIVE' && currentQuestion && (
          <AdaptiveQuestionView
            question={currentQuestion}
            onSubmitAnswer={handleAdaptiveAnswer}
            onSkip={handleSkipAdaptive}
            loading={loading}
            history={adaptiveHistory}
            language={language}
          />
        )}

        {currentStep === 'SYMPTOMS' && (
          <SymptomsSelectionView
            onSubmit={handleSymptomsSubmit}
            onBack={() => setCurrentStep('COMPLAINT')}
            loading={loading}
            language={language}
          />
        )}

        {currentStep === 'HISTORY' && (
          <MedicalHistoryView
            onSubmit={handleHistorySubmit}
            onBack={() => setCurrentStep('SYMPTOMS')}
            loading={loading}
            language={language}
          />
        )}

        {currentStep === 'MEDICATIONS' && (
          <MedicationsView
            onSubmit={handleMedicationsSubmit}
            onBack={() => setCurrentStep('HISTORY')}
            loading={loading}
            language={language}
          />
        )}

        {currentStep === 'ALLERGIES' && (
          <AllergiesView
            onSubmit={handleAllergiesSubmit}
            onBack={() => setCurrentStep('MEDICATIONS')}
            loading={loading}
            language={language}
          />
        )}

        {currentStep === 'AYURVEDA' && (
          <div className="max-w-4xl mx-auto px-4">
            <AyurvedaSection
              sessionId={sessionId}
              authToken={kioskService.getToken()}
              language={language}
              onConfirmed={handleAyurvedaSubmit}
              onSkip={() => handleAyurvedaSubmit(null)}
              onBack={() => setCurrentStep('ALLERGIES')}
            />
          </div>
        )}

        {currentStep === 'DOCUMENTS' && (
          <DocumentUploadView
            onSubmit={handleDocumentsSubmit}
            onBack={() => setCurrentStep('AYURVEDA')}
            loading={loading}
            language={language}
            sessionId={sessionId}
            authToken={kioskService.getToken()}
          />
        )}

        {currentStep === 'REVIEW' && (
          <PatientReviewView
            registration={registrationData}
            chiefComplaint={chiefComplaint}
            complaintMethod={complaintMethod}
            symptoms={symptoms}
            histories={histories}
            medications={medications}
            allergies={allergies}
            ayurveda={ayurveda}
            documents={documents}
            answers={answers}
            onEditSection={handleEditSection}
            onConfirmSubmit={handleReviewConfirm}
            loading={loading}
            language={language}
          />
        )}

        {currentStep === 'COMPLETION' && (
          <PatientCompletionView
            patientIdentifier={tokenNumber || patientIdentifier || 'TK-8492'}
            patientName={`${registrationData.firstName} ${registrationData.lastName}`}
            triagePriority={triagePriority}
            onReset={handleReset}
            onSwitchToClinician={onSwitchToClinician}
            language={language}
          />
        )}
      </main>

      {/* Clean Hospital Footer */}
      <footer className="py-4 border-t border-slate-800 bg-[#131936] text-center text-xs text-[#AEA989] flex flex-col sm:flex-row items-center justify-between px-6">
        <div>{getTranslation(language, 'footerRights')}</div>
        <div className="flex items-center gap-4 mt-2 sm:mt-0">
          <span>{getTranslation(language, 'abdmReady')}</span>
          <span>•</span>
          <button
            onClick={onSwitchToClinician}
            className="text-[#AEA989] hover:text-white font-semibold underline underline-offset-2"
          >
            {getTranslation(language, 'staffDoctorPortal')}
          </button>
        </div>
      </footer>
    </div>
  );
};
