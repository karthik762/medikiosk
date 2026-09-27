import { fetchApi } from './api';

export interface KioskRegistrationRequest {
  firstName: string;
  lastName: string;
  dateOfBirth?: string;
  gender?: string;
  phone?: string;
  email?: string;
}

export interface KioskRegistrationResponse {
  accessToken: string;
  token?: string;
  tokenType: string;
  expiresIn: number;
  patientId: string;
  patientIdentifier: string;
  sessionId: string;
  firstName: string;
  lastName: string;
  email: string;
}

export interface NextQuestionData {
  questionId: string;
  questionCode: string;
  questionText: string;
  questionType: 'SCALE_0_10' | 'YES_NO' | 'CHOICE' | 'TEXT';
  category: string;
  clinicalConceptCode?: string;
  safetyCritical: boolean;
  priority: number;
  completed: boolean;
  progressStage?: string;
  options: string[];
  reason?: string;
  triggeringConcepts?: string[];
  missingConcepts?: string[];
  intent?: string;
  answerType?: string;
  generated?: boolean;
  provider?: string;
  safetyLevel?: string;
  requiresConfirmation?: boolean;
}

export interface ClinicalStateData {
  sessionId: string;
  patientId: string;
  patientIdentifier: string;
  patientName: string;
  chiefComplaint?: string;
  symptoms: string[];
  medicalHistories: string[];
  medications: string[];
  allergies: string[];
  conflicts: Array<{
    concept: string;
    description: string;
    primarySource: string;
    primaryValue: string;
    conflictingSource: string;
    conflictingValue: string;
  }>;
  redFlagSignals: string[];
  missingInformation: string[];
  triagePriority: string;
  overallConfidence: number;
  documentCount: number;
  ayurvedaCompleted: boolean;
}

export async function registerKioskPatient(data: KioskRegistrationRequest) {
  return fetchApi<{ success: boolean; data: KioskRegistrationResponse; message: string }>(
    '/api/auth/kiosk/register',
    {
      method: 'POST',
      body: JSON.stringify(data),
    }
  );
}

export async function recordConsent(
  sessionId: string,
  consentStatus: 'ACCEPTED' | 'DECLINED',
  token: string
) {
  return fetchApi<{ success: boolean; data: any; message: string }>(
    `/api/intake/sessions/${sessionId}/consent`,
    {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` },
      body: JSON.stringify({
        consentStatus,
        consentVersion: 'v1.0',
        consentType: 'GENERAL_INTAKE',
      }),
    }
  );
}

export async function recordChiefComplaint(
  sessionId: string,
  complaintText: string,
  inputMethod: 'TEXT' | 'VOICE' = 'TEXT',
  token: string
) {
  return fetchApi<{ success: boolean; data: any; message: string }>(
    `/api/intake/sessions/${sessionId}/complaint`,
    {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` },
      body: JSON.stringify({
        complaintText,
        inputMethod,
      }),
    }
  );
}

export async function getNextAdaptiveQuestion(sessionId: string, token: string) {
  return fetchApi<{ success: boolean; data: NextQuestionData }>(
    `/api/intake/sessions/${sessionId}/next-question`,
    {
      headers: { Authorization: `Bearer ${token}` },
    }
  );
}

export async function submitQuestionAnswer(
  sessionId: string,
  questionId: string,
  answerText: string,
  inputMethod: 'TEXT' | 'TOUCH' | 'VOICE',
  token: string
) {
  return fetchApi<{ success: boolean; data: any; message: string }>(
    `/api/intake/sessions/${sessionId}/answers`,
    {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` },
      body: JSON.stringify({
        questionId,
        answerText,
        inputMethod,
        confirmedByPatient: true,
      }),
    }
  );
}

export async function recordSymptom(
  sessionId: string,
  name: string,
  severity: number | string,
  duration: string,
  token: string
) {
  return fetchApi<{ success: boolean; data: any }>(
    `/api/intake/sessions/${sessionId}/symptoms`,
    {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` },
      body: JSON.stringify({
        symptomName: name,
        severity: typeof severity === 'number' ? severity : parseInt(severity, 10) || 5,
        duration,
        source: 'PATIENT_KIOSK',
      }),
    }
  );
}

export async function recordMedicalHistory(
  sessionId: string,
  category: string,
  description: string,
  token: string
) {
  return fetchApi<{ success: boolean; data: any }>(
    `/api/intake/sessions/${sessionId}/medical-history`,
    {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` },
      body: JSON.stringify({
        category,
        conditionName: description,
        description,
        source: 'PATIENT_KIOSK',
      }),
    }
  );
}

export async function recordMedication(
  sessionId: string,
  medicationName: string,
  dosage: string,
  frequency: string,
  token: string
) {
  return fetchApi<{ success: boolean; data: any }>(
    `/api/intake/sessions/${sessionId}/medications`,
    {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` },
      body: JSON.stringify({
        medicationName,
        dosage,
        frequency,
        source: 'PATIENT_KIOSK',
      }),
    }
  );
}

export async function recordAllergy(
  sessionId: string,
  allergen: string,
  reaction: string,
  token: string
) {
  return fetchApi<{ success: boolean; data: any }>(
    `/api/intake/sessions/${sessionId}/allergies`,
    {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` },
      body: JSON.stringify({
        allergen,
        reaction,
        severity: 'MODERATE',
        source: 'PATIENT_KIOSK',
      }),
    }
  );
}

export async function getClinicalState(sessionId: string, token: string) {
  return fetchApi<{ success: boolean; data: ClinicalStateData }>(
    `/api/intake/sessions/${sessionId}/clinical-state`,
    {
      headers: { Authorization: `Bearer ${token}` },
    }
  );
}

export async function getSessionDetails(sessionId: string, token: string) {
  return fetchApi<{
    success: boolean;
    data: {
      id: string;
      patientId: string;
      patientIdentifier: string;
      status: string;
      language?: string;
    };
  }>(`/api/intake/sessions/${sessionId}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
}

export async function completeIntakeSession(sessionId: string, token: string) {
  return fetchApi<{ success: boolean; data: any; message: string }>(
    `/api/intake/sessions/${sessionId}/complete`,
    {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` },
    }
  );
}

const KIOSK_TOKEN_KEY = 'medikiosk_kiosk_token';
const KIOSK_SESSION_ID_KEY = 'medikiosk_kiosk_session_id';
const KIOSK_PATIENT_ID_KEY = 'medikiosk_kiosk_patient_id';
const KIOSK_PATIENT_IDENTIFIER_KEY = 'medikiosk_kiosk_patient_identifier';
const KIOSK_STEP_KEY = 'medikiosk_kiosk_current_step';

let currentKioskToken: string = '';

export const kioskService = {
  clearMemoryTokenOnly: () => {
    currentKioskToken = '';
  },

  getToken: (): string => {
    if (currentKioskToken) return currentKioskToken;
    try {
      const saved = sessionStorage.getItem(KIOSK_TOKEN_KEY);
      if (saved && saved !== 'undefined' && saved !== 'null') {
        currentKioskToken = saved;
        return saved;
      }
    } catch {
      // sessionStorage unavailable
    }
    return '';
  },

  setToken: (token: string) => {
    currentKioskToken = token;
    try {
      if (token) {
        sessionStorage.setItem(KIOSK_TOKEN_KEY, token);
      } else {
        sessionStorage.removeItem(KIOSK_TOKEN_KEY);
      }
    } catch {}
  },

  saveSession: (data: {
    token: string;
    sessionId: string;
    patientId?: string;
    patientIdentifier?: string;
    currentStep?: string;
  }) => {
    currentKioskToken = data.token;
    try {
      if (data.token) sessionStorage.setItem(KIOSK_TOKEN_KEY, data.token);
      if (data.sessionId) sessionStorage.setItem(KIOSK_SESSION_ID_KEY, data.sessionId);
      if (data.patientId) sessionStorage.setItem(KIOSK_PATIENT_ID_KEY, data.patientId);
      if (data.patientIdentifier) sessionStorage.setItem(KIOSK_PATIENT_IDENTIFIER_KEY, data.patientIdentifier);
      if (data.currentStep) sessionStorage.setItem(KIOSK_STEP_KEY, data.currentStep);
    } catch {}
  },

  saveCurrentStep: (step: string) => {
    try {
      if (step) {
        sessionStorage.setItem(KIOSK_STEP_KEY, step);
      } else {
        sessionStorage.removeItem(KIOSK_STEP_KEY);
      }
    } catch {}
  },

  getPersistedSession: () => {
    try {
      const token = currentKioskToken || sessionStorage.getItem(KIOSK_TOKEN_KEY) || '';
      const sessionId = sessionStorage.getItem(KIOSK_SESSION_ID_KEY) || '';
      const patientId = sessionStorage.getItem(KIOSK_PATIENT_ID_KEY) || '';
      const patientIdentifier = sessionStorage.getItem(KIOSK_PATIENT_IDENTIFIER_KEY) || '';
      const currentStep = sessionStorage.getItem(KIOSK_STEP_KEY) || '';

      if (
        token &&
        token !== 'undefined' &&
        token !== 'null' &&
        sessionId &&
        sessionId !== 'undefined' &&
        sessionId !== 'null'
      ) {
        currentKioskToken = token;
        return { token, sessionId, patientId, patientIdentifier, currentStep };
      }
    } catch {}
    return null;
  },

  clearSession: () => {
    currentKioskToken = '';
    try {
      sessionStorage.removeItem(KIOSK_TOKEN_KEY);
      sessionStorage.removeItem(KIOSK_SESSION_ID_KEY);
      sessionStorage.removeItem(KIOSK_PATIENT_ID_KEY);
      sessionStorage.removeItem(KIOSK_PATIENT_IDENTIFIER_KEY);
      sessionStorage.removeItem(KIOSK_STEP_KEY);
    } catch {}
  },

  verifySession: async (sessionId: string) => {
    const token = kioskService.getToken();
    if (!token || !sessionId) return null;
    try {
      const res = await getSessionDetails(sessionId, token);
      if (res.success && res.data) {
        return res.data;
      }
    } catch (err) {
      kioskService.clearSession();
      throw err;
    }
    return null;
  },

  checkDemoStatus: async (): Promise<boolean> => {
    try {
      const res = await fetchApi<{ success: boolean; data: { demoEnabled: boolean } }>('/api/demo/status');
      return res.success && res.data?.demoEnabled === true;
    } catch {
      return false;
    }
  },

  demoPatientLogin: async () => {
    // Step 1: Authenticate as the demo patient user
    const authRes = await fetchApi<{ success: boolean; data: any }>('/api/demo/patient-login', {
      method: 'POST',
    });
    const authData = authRes.data;
    const token = authData.accessToken || authData.token;
    if (!token) throw new Error('Demo patient authentication failed — no token received');

    // Step 2: Register a kiosk intake session using the demo patient token
    const regRes = await fetchApi<{ success: boolean; data: any }>('/api/auth/kiosk/register', {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        firstName: 'Aarav',
        lastName: 'Sharma',
        dateOfBirth: '1998-05-12',
        gender: 'MALE',
        phone: '9876543210',
        email: 'patient@medikiosk.org',
      }),
    });
    const regData = regRes.data;
    const finalToken = regData.accessToken || regData.token || token;

    kioskService.saveSession({
      token: finalToken,
      sessionId: regData.sessionId,
      patientId: regData.patientId || authData.userId,
      patientIdentifier: regData.patientIdentifier || 'DEMO-PAT-001',
      currentStep: 'CONSENT',
    });

    return {
      sessionId: regData.sessionId,
      patientId: regData.patientId || authData.userId,
      patientIdentifier: regData.patientIdentifier || 'DEMO-PAT-001',
      token: finalToken,
    };
  },

  registerKioskPatient: async (data: KioskRegistrationRequest) => {
    const res = await registerKioskPatient(data);
    const token = res.data.accessToken || res.data.token || '';

    kioskService.saveSession({
      token,
      sessionId: res.data.sessionId,
      patientId: res.data.patientId,
      patientIdentifier: res.data.patientIdentifier,
      currentStep: 'CONSENT',
    });

    return {
      sessionId: res.data.sessionId,
      patientId: res.data.patientId,
      patientIdentifier: res.data.patientIdentifier,
      sessionStatus: 'IN_PROGRESS',
      sessionStartTime: new Date().toISOString(),
      token,
    };
  },

  recordConsent: async (sessionId: string, _type = 'INFORMATIVE_AND_CARE') => {
    return recordConsent(sessionId, 'ACCEPTED', kioskService.getToken());
  },

  submitChiefComplaint: async (sessionId: string, complaintText: string, method: 'TEXT' | 'VOICE') => {
    return recordChiefComplaint(sessionId, complaintText, method, kioskService.getToken());
  },

  getNextAdaptiveQuestion: async (sessionId: string) => {
    const res = await getNextAdaptiveQuestion(sessionId, kioskService.getToken());
    const qData = res.data;
    if (!qData || qData.completed) {
      return { hasMoreQuestions: false, question: null, pendingCriticalCount: 0, totalAsked: 0 };
    }
    return {
      hasMoreQuestions: true,
      pendingCriticalCount: qData.safetyCritical ? 1 : 0,
      totalAsked: 0,
      question: qData,
    };
  },

  submitAnswer: async (
    sessionId: string,
    questionId: string,
    answerText: string,
    method: 'TOUCH' | 'TEXT' | 'VOICE' = 'TOUCH'
  ) => {
    return submitQuestionAnswer(sessionId, questionId, answerText, method, kioskService.getToken());
  },

  addSymptom: async (sessionId: string, data: { symptomName: string; severity: number; duration: string }) => {
    return recordSymptom(sessionId, data.symptomName, data.severity, data.duration, kioskService.getToken());
  },

  addMedicalHistory: async (sessionId: string, data: { category: string; description: string }) => {
    return recordMedicalHistory(sessionId, data.category, data.description, kioskService.getToken());
  },

  addMedication: async (sessionId: string, data: { medicationName: string; dosage: string; frequency: string }) => {
    return recordMedication(sessionId, data.medicationName, data.dosage, data.frequency, kioskService.getToken());
  },

  addAllergy: async (sessionId: string, data: { allergen: string; reaction: string }) => {
    return recordAllergy(sessionId, data.allergen, data.reaction, kioskService.getToken());
  },

  completeIntake: async (sessionId: string) => {
    const token = kioskService.getToken();
    const res = await completeIntakeSession(sessionId, token);
    const data = res.data || {};
    // Clear active kiosk session after completion
    kioskService.clearSession();
    return {
      sessionId,
      sessionStatus: 'COMPLETED',
      tokenNumber: data.tokenNumber || `TK-${Math.floor(1000 + Math.random() * 9000)}`,
      queueNumber: data.queueNumber || `Q-${Math.floor(10 + Math.random() * 90)}`,
      triagePriority: data.triagePriority || 'NORMAL',
    };
  },
};

