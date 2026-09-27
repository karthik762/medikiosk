export type AyurvedaHistoryStatus = 'DRAFT' | 'PATIENT_CONFIRMED' | 'DOCTOR_REVIEWED';

export interface PrakritiData {
  bodyFrame?: string;
  weatherPreference?: string;
  digestionPattern?: string;
  sleepPattern?: string;
  skinTexture?: string;
  hairTexture?: string;
  temperament?: string;
  additionalPrakritiDetails?: Record<string, unknown>;
}

export interface NidanaData {
  dietaryHabits?: string;
  activityPattern?: string;
  stressFactors?: string;
  sleepSchedule?: string;
  seasonalAggravation?: string;
  additionalNidanaDetails?: Record<string, unknown>;
}

export interface ParikshaData {
  koshtha?: string;
  bala?: string;
  jihva?: string;
  agni?: string;
  mutra?: string;
  additionalParikshaDetails?: Record<string, unknown>;
}

export interface AyurvedaTerminology {
  conceptName: string;
  sanskritTerm: string;
  namasteCode?: string | null;
  namasteDisplay?: string | null;
  icd11Tm2Code?: string | null;
  icd11Tm2Display?: string | null;
  mappingStatus: 'RESOLVED' | 'UNRESOLVED';
  description?: string;
}

export interface AyurvedaHistoryRequest {
  prakriti?: PrakritiData;
  nidana?: NidanaData;
  pariksha?: ParikshaData;
  notes?: string;
  terminology?: AyurvedaTerminology[];
}

export interface AyurvedaHistoryResponse {
  id: string;
  patientId: string;
  sessionId: string;
  status: AyurvedaHistoryStatus;
  patientConfirmed: boolean;
  confirmedAt?: string | null;
  prakriti?: PrakritiData;
  nidana?: NidanaData;
  pariksha?: ParikshaData;
  terminology?: AyurvedaTerminology[];
  notes?: string;
  createdAt: string;
  updatedAt: string;
}
