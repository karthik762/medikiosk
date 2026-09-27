export interface TranslationDictionary {
  // Header & General
  headerTitle: string;
  headerSubtitle: string;
  patientSelfService: string;
  staffLogin: string;
  switchToClinician: string;
  textSize: string;
  language: string;
  step: string;
  of: string;

  // Progress Steps
  stepRegistration: string;
  stepConsent: string;
  stepComplaint: string;
  stepQuestions: string;
  stepSymptoms: string;
  stepHistory: string;
  stepMedications: string;
  stepAllergies: string;
  stepAyurveda: string;
  stepDocuments: string;
  stepReview: string;

  // Symptom Inner Options
  severityLabel: string;
  mild: string;
  moderate: string;
  severe: string;
  durationLabel: string;
  duration24h: string;
  duration1_3d: string;
  duration1wPlus: string;

  // Medications Voice
  tellUsMedicationsVoice: string;
  dontRememberMedName: string;
  medicationVoiceCaptured: string;
  useSpokenMedication: string;
  addToList: string;
  addedMedications: string;
  remove: string;

  // Camera Modal
  capturePhoto: string;
  retakePhoto: string;
  usePhoto: string;
  cameraUnavailable: string;
  demoExtractionNotice: string;

  // Buttons & Common Actions
  continue: string;
  back: string;
  submit: string;
  tryAgain: string;
  confirmContinue: string;
  skip: string;
  notSure: string;
  tapToSpeak: string;
  stopListening: string;

  // Voice Status
  voiceReady: string;
  voiceListening: string;
  voiceUnderstanding: string;
  voiceCaptured: string;
  voiceError: string;
  micPermissionDenied: string;
  micNotFound: string;
  micInUse: string;
  noSpeechDetected: string;
  voiceNetworkError: string;
  retryMicrophone: string;

  // Landing
  welcomeTitle: string;
  welcomeSubtitle: string;
  startCheckIn: string;
  continueSession: string;
  demoPatient: string;
  voiceFirstNotice: string;
  clinicalKioskActive: string;
  aiPoweredClinicalIntake: string;
  landingDescription: string;
  demoPatientQuickFill: string;
  voiceOrTouch: string;
  voiceOrTouchDesc: string;
  scanDocumentsTitle: string;
  scanDocumentsDesc: string;
  doctorVerifiedTitle: string;
  doctorVerifiedDesc: string;
  privacyNotice: string;

  // Footer & Navigation
  footerRights: string;
  abdmReady: string;
  staffDoctorPortal: string;

  // Registration
  registrationTitle: string;
  registrationSubtitle: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: string;
  male: string;
  female: string;
  other: string;
  phone: string;
  email: string;
  nameRequiredError: string;
  registrationFailedError: string;

  // Consent
  consentTitle: string;
  consentSubtitle: string;
  consentBody: string;
  consentCheckbox: string;
  acceptConsent: string;
  purpose1Title: string;
  purpose1Desc: string;
  purpose2Title: string;
  purpose2Desc: string;
  purpose3Title: string;
  purpose3Desc: string;
  consentPrivacyNotice: string;

  // Chief Complaint
  complaintTitle: string;
  complaintSubtitle: string;
  complaintPlaceholder: string;
  transcriptionReviewTitle: string;
  transcriptionConfirmCheckbox: string;
  complaintHeader: string;
  complaintDescription: string;
  tapMicToSpeak: string;
  listeningSeconds: string;
  processingSpeech: string;
  transcriptionReady: string;
  voiceOffline: string;
  voiceInstructions: string;
  primaryHealthConcern: string;
  voiceTranscribedNotice: string;
  confirmTextCheckbox: string;
  commonConcernsHeader: string;
  chipChestPain: string;
  chipHighFever: string;
  chipHeadache: string;
  chipBreathing: string;
  chipStomach: string;
  chipDizziness: string;
  confirmCheckNotice: string;
  continueToQuestions: string;

  // Symptoms
  symptomsTitle: string;
  symptomsSubtitle: string;
  selectSymptoms: string;
  symptomHeadache: string;
  symptomFever: string;
  symptomCough: string;
  symptomStomach: string;
  symptomChestPain: string;
  symptomSob: string;
  symptomDizziness: string;
  symptomVomiting: string;
  symptomDiarrhea: string;
  symptomInjury: string;
  symptomSkin: string;
  symptomFatigue: string;

  // Adaptive Questions & Universal Answer Input
  adaptiveTitle: string;
  adaptiveSubtitle: string;
  touchOptionsShortcut: string;
  clinicalQuestionStage: string;
  universalInputEnabled: string;
  speakingState: string;
  readAloud: string;
  intakeHistoryTitle: string;
  show: string;
  hide: string;
  questionInstruction: string;
  speakNaturallyInstruction: string;
  speakNowInstruction: string;
  reviewVoiceInstruction: string;
  typePlaceholder: string;
  typeCustomDetails: string;
  hideTypingBox: string;

  // Medical History
  historyTitle: string;
  historySubtitle: string;
  conditionHypertension: string;
  conditionDiabetes: string;
  conditionAsthma: string;
  conditionHeartDisease: string;
  conditionThyroid: string;
  conditionReflux: string;
  pastSurgeries: string;
  familyHistory: string;
  addHistoryPlaceholder: string;

  // Medications
  medicationsTitle: string;
  medicationsSubtitle: string;
  medicationName: string;
  dosage: string;
  frequency: string;
  noMedications: string;
  addMedicationPlaceholder: string;

  // Allergies
  allergiesTitle: string;
  allergiesSubtitle: string;
  noKnownAllergies: string;
  allergenName: string;
  addAllergyPlaceholder: string;
  typicalReaction: string;
  reactionRash: string;
  reactionSwelling: string;
  reactionBreathing: string;
  reactionNausea: string;

  // Ayurveda
  ayurvedaTitle: string;
  ayurvedaSubtitle: string;
  ayurvedaOptIn: string;
  ayurvedaSkip: string;
  yesProvideDetails: string;
  skipThisStep: string;
  continueToDocuments: string;

  // Documents
  documentsTitle: string;
  documentsSubtitle: string;
  scanWithCamera: string;
  uploadFromDevice: string;
  processingDocument: string;
  extractionSummary: string;
  confidenceScore: string;
  noDocuments: string;

  // Review
  reviewTitle: string;
  reviewSubtitle: string;
  confirmIntake: string;
  personalDetails: string;
  patientProvided: string;
  edit: string;
  name: string;
  dob: string;
  aiStructuredIntake: string;
  symptomsAndResponses: string;

  // Completion
  completionTitle: string;
  completionSubtitle: string;
  yourTokenNumber: string;
  triageStatus: string;
  proceedToWaitingArea: string;
  finish: string;
  yourConsultationToken: string;
  waitingAreaNotice: string;
  priorityAttentionNotice: string;
  openClinicianQueue: string;
  sessionExpiredError: string;
}

export const translations: Record<'en' | 'hi' | 'te', TranslationDictionary> = {
  en: {
    headerTitle: 'MediKiosk Clinical Intake',
    headerSubtitle: 'Pre-Consultation Digital Clinical Intake',
    patientSelfService: 'Patient Self-Service',
    staffLogin: 'Doctor Login',
    switchToClinician: 'Clinician Console',
    textSize: 'Text Size',
    language: 'Language',
    step: 'Step',
    of: 'of',

    stepRegistration: 'Registration',
    stepConsent: 'Consent',
    stepComplaint: 'Complaint',
    stepQuestions: 'Questions',
    stepSymptoms: 'Symptoms',
    stepHistory: 'History',
    stepMedications: 'Medications',
    stepAllergies: 'Allergies',
    stepAyurveda: 'Ayurveda',
    stepDocuments: 'Documents',
    stepReview: 'Review',

    severityLabel: 'Severity',
    mild: 'Mild',
    moderate: 'Moderate',
    severe: 'Severe',
    durationLabel: 'Duration',
    duration24h: '< 24 hrs',
    duration1_3d: '1–3 days',
    duration1wPlus: '> 1 week',

    tellUsMedicationsVoice: 'Speak your medications naturally (e.g. I take a BP tablet every morning)',
    dontRememberMedName: "Not sure / Don't know medication name",
    medicationVoiceCaptured: 'Medication details captured from your voice',
    useSpokenMedication: '✓ Use Spoken Medication',
    addToList: '+ Add to List',
    addedMedications: 'Added Medications',
    remove: 'Remove',

    capturePhoto: '📸 Capture Image',
    retakePhoto: '🔄 Retake',
    usePhoto: '✅ Use Photo',
    cameraUnavailable: 'Camera unavailable. You can upload a photo or PDF instead.',
    demoExtractionNotice: 'Demo extraction',

    continue: 'Continue',
    back: 'Back',
    submit: 'Submit',
    tryAgain: 'Try Again',
    confirmContinue: 'Confirm & Continue',
    skip: 'Skip this step',
    notSure: "Not Sure / Don't Know",
    tapToSpeak: 'Tap to Speak',
    stopListening: 'Stop Listening',

    voiceReady: '🟢 Voice Ready — Speak naturally',
    voiceListening: '🔴 Listening to your voice...',
    voiceUnderstanding: '🟣 Understanding clinical meaning...',
    voiceCaptured: '✓ Answer Captured',
    voiceError: '⚠️ Voice input unavailable. You can tap or type your response below.',
    micPermissionDenied: 'Microphone permission is required. Please allow microphone access in your browser and try again.',
    micNotFound: 'No microphone detected on this device. You can tap or type your response below.',
    micInUse: 'Microphone is currently in use by another application. Please try again.',
    noSpeechDetected: 'No speech detected. Tap microphone to try again or type below.',
    voiceNetworkError: 'Voice recognition network issue. You can tap or type below.',
    retryMicrophone: 'Retry Microphone',

    welcomeTitle: 'Welcome to MediKiosk',
    welcomeSubtitle: 'Express your symptoms naturally by speaking or tapping the screen.',
    startCheckIn: 'Start Check-In',
    continueSession: 'Continue Existing Session',
    demoPatient: '1-Click Demo Patient',
    voiceFirstNotice: '🎤 Voice-First & 👆 Touch-First for easy consultation check-in',
    clinicalKioskActive: 'Clinical Intake Kiosk Active',
    aiPoweredClinicalIntake: 'AI-Powered Clinical Intake',
    landingDescription: "Tell us what you're experiencing. MediKiosk will collect your information and prepare it for your clinician.",
    demoPatientQuickFill: '1-Click Demo Patient (Pre-fills Aarav Sharma, Age 28)',
    voiceOrTouch: 'Voice or Touch',
    voiceOrTouchDesc: 'Speak or type your symptoms in your preferred language.',
    scanDocumentsTitle: 'Scan Documents',
    scanDocumentsDesc: 'Easily upload previous prescriptions and medical reports.',
    doctorVerifiedTitle: 'Doctor Verified',
    doctorVerifiedDesc: 'Your clinician reviews and verifies every detail with you.',
    privacyNotice: 'Your health data is encrypted, strictly private, and only shared with your consultation team.',

    footerRights: 'MediKiosk Patient Intake System • Designed for Accessibility & Touch-First Care',
    abdmReady: 'ABDM & FHIR R4 Ready',
    staffDoctorPortal: 'Staff / Doctor Portal',

    registrationTitle: 'Patient Registration',
    registrationSubtitle: 'Please provide basic patient identification details.',
    firstName: 'First Name',
    lastName: 'Last Name',
    dateOfBirth: 'Date of Birth',
    gender: 'Gender',
    male: 'Male',
    female: 'Female',
    other: 'Other',
    phone: 'Phone Number',
    email: 'Email Address',
    nameRequiredError: 'Please provide both your first and last name.',
    registrationFailedError: 'Registration could not be completed. Please try again.',

    consentTitle: 'Patient Consent',
    consentSubtitle: 'Please review and accept consent for clinical intake and data collection.',
    consentBody: 'I authorize MediKiosk to collect my health symptoms and medical history for preliminary clinical structuring. Final diagnosis and care decisions are made by the consulting physician.',
    consentCheckbox: 'I agree to the clinical intake consent terms.',
    acceptConsent: 'Accept & Proceed',
    purpose1Title: 'Clinical Intake Purpose',
    purpose1Desc: 'Information collected here organizes your symptoms, medical history, and past records so your consulting doctor can prepare for your visit.',
    purpose2Title: 'Doctor Verification & Final Decision',
    purpose2Desc: 'MediKiosk assists clinical workflows. It does not replace medical diagnosis. Your doctor remains the final authority on all clinical evaluations.',
    purpose3Title: 'Review & Corrections',
    purpose3Desc: 'You will have the opportunity to review, edit, or remove any entered information before final submission.',
    consentPrivacyNotice: 'Your data is stored securely in compliance with healthcare privacy regulations.',

    complaintTitle: 'What is your primary health concern today?',
    complaintSubtitle: 'Speak naturally or type why you are visiting the clinic.',
    complaintPlaceholder: 'e.g. I have had a severe headache and fever for 3 days...',
    transcriptionReviewTitle: 'Voice Transcribed — Review Required',
    transcriptionConfirmCheckbox: 'I confirm this text accurately reflects what I said.',
    complaintHeader: 'What brings you in today?',
    complaintDescription: 'Please describe your main symptom or health concern in your own words.',
    tapMicToSpeak: 'Tap microphone to speak',
    listeningSeconds: 'Listening...',
    processingSpeech: 'Processing speech...',
    transcriptionReady: 'Transcription ready for your review',
    voiceOffline: 'Voice Input Offline',
    voiceInstructions: 'Speak clearly into your microphone in English, Hindi, or Telugu. You can edit or type your concerns directly below.',
    primaryHealthConcern: 'Primary Health Concern / Chief Complaint',
    voiceTranscribedNotice: 'Voice Transcribed — Review Required',
    confirmTextCheckbox: 'I confirm this text accurately reflects what I said.',
    commonConcernsHeader: 'Or tap common concerns:',
    chipChestPain: 'Chest pain or heaviness',
    chipHighFever: 'High fever & body chills',
    chipHeadache: 'Severe headache or migraine',
    chipBreathing: 'Difficulty breathing / cough',
    chipStomach: 'Stomach pain & indigestion',
    chipDizziness: 'Dizziness & lightheadedness',
    confirmCheckNotice: 'Please check "I confirm this text accurately reflects what I said" above to enable Confirm & Continue.',
    continueToQuestions: 'Continue to Health Questions',

    symptomsTitle: 'Select any additional symptoms you are experiencing',
    symptomsSubtitle: 'Tap any cards that apply, or skip if you have already described everything.',
    selectSymptoms: 'Selected Symptoms',
    symptomHeadache: 'Headache / Head Pain',
    symptomFever: 'Fever / Chills',
    symptomCough: 'Cough / Cold',
    symptomStomach: 'Stomach / Abdominal Pain',
    symptomChestPain: 'Chest Pain / Pressure',
    symptomSob: 'Shortness of Breath',
    symptomDizziness: 'Dizziness / Fainting',
    symptomVomiting: 'Vomiting / Nausea',
    symptomDiarrhea: 'Diarrhea / Loose Stools',
    symptomInjury: 'Injury / Body Pain',
    symptomSkin: 'Skin Rash / Itching',
    symptomFatigue: 'Fatigue / Weakness',

    adaptiveTitle: 'Clinical Questions',
    adaptiveSubtitle: 'Please answer follow-up questions for your reported symptoms.',
    touchOptionsShortcut: 'Touch options shortcut',
    clinicalQuestionStage: 'Clinical Question',
    universalInputEnabled: 'Universal Input Enabled',
    speakingState: 'Speaking...',
    readAloud: 'Read Aloud',
    intakeHistoryTitle: 'Intake History',
    show: 'Show',
    hide: 'Hide',
    questionInstruction: 'Speak your response into the microphone OR tap one of the answer buttons below.',
    speakNaturallyInstruction: 'Speak naturally in English, Hindi, or Telugu.',
    speakNowInstruction: 'Speak now. Tap button when finished.',
    reviewVoiceInstruction: 'Confirm or edit your spoken response before continuing.',
    typePlaceholder: 'Selected option, spoken speech, or type answer here...',
    typeCustomDetails: 'Custom details? Tap to type',
    hideTypingBox: 'Hide typing box',

    historyTitle: 'Medical & Past Health History',
    historySubtitle: 'Select existing conditions or speak/type your medical background.',
    conditionHypertension: 'High Blood Pressure / BP',
    conditionDiabetes: 'Diabetes / High Sugar',
    conditionAsthma: 'Asthma / Breathing Issue',
    conditionHeartDisease: 'Heart Condition',
    conditionThyroid: 'Thyroid Problem',
    conditionReflux: 'Acid Reflux / Gas',
    pastSurgeries: 'Past Surgeries / Operations',
    familyHistory: 'Family Medical History',
    addHistoryPlaceholder: 'Describe past treatments, surgeries, or family conditions...',

    medicationsTitle: 'Current Medications',
    medicationsSubtitle: 'List any regular prescription medicines, tablets, or supplements.',
    medicationName: 'Medication Name',
    dosage: 'Dosage / Strength',
    frequency: 'Frequency',
    noMedications: 'No Regular Medications',
    addMedicationPlaceholder: 'e.g. BP tablet every morning, Metformin 500mg...',

    allergiesTitle: 'Allergies & Sensitivities',
    allergiesSubtitle: 'Do you have any known allergies to medicines, foods, or substances?',
    noKnownAllergies: 'No Known Allergies',
    allergenName: 'Allergen / Substance Name',
    addAllergyPlaceholder: 'e.g. Penicillin, Sulfa drugs, Peanuts...',
    typicalReaction: 'Typical Reaction',
    reactionRash: 'Hives / Rash / Itching',
    reactionSwelling: 'Swelling of Face / Lips',
    reactionBreathing: 'Difficulty Breathing / Severe Anaphylaxis',
    reactionNausea: 'Nausea / Upset Stomach',

    ayurvedaTitle: 'Ayurveda Holistic History (Optional)',
    ayurvedaSubtitle: 'Include traditional Ayurvedic constitutional parameters (Prakriti / Digestion)?',
    ayurvedaOptIn: 'Include Ayurvedic History',
    ayurvedaSkip: 'Skip Ayurvedic Section',
    yesProvideDetails: 'Yes, Provide Details',
    skipThisStep: 'Skip this Step',
    continueToDocuments: 'Continue to Documents',

    documentsTitle: 'Medical Documents & Records',
    documentsSubtitle: 'Scan prescriptions or reports using your camera or upload files.',
    scanWithCamera: '📷 Scan with Camera',
    uploadFromDevice: '📁 Upload from Device',
    processingDocument: 'Processing document OCR & medical extraction...',
    extractionSummary: 'Extracted Medical Summary',
    confidenceScore: 'Confidence Score',
    noDocuments: 'No documents attached',

    reviewTitle: 'Review Your Clinical Summary',
    reviewSubtitle: 'Please verify your information before receiving your queue token.',
    confirmIntake: 'Confirm & Submit Intake',
    personalDetails: 'Personal Details',
    patientProvided: 'Patient-Provided',
    edit: 'Edit',
    name: 'Name',
    dob: 'DOB',
    aiStructuredIntake: 'AI-Structured Intake',
    symptomsAndResponses: 'Symptoms & Question Responses',

    completionTitle: 'Intake Completed Successfully',
    completionSubtitle: 'Your clinical information has been sent to the doctor console.',
    yourTokenNumber: 'Your Queue Token Number',
    triageStatus: 'Triage Status',
    proceedToWaitingArea: 'Please proceed to the patient waiting area. The doctor will call your token number.',
    finish: 'Finish & Return to Home',
    yourConsultationToken: 'Your Consultation Queue Token',
    waitingAreaNotice: 'Please proceed to the waiting area. A clinician will call your queue token when ready.',
    priorityAttentionNotice: 'Your responses have been flagged for earlier clinical attention.',
    openClinicianQueue: 'Open Clinician Queue',
    sessionExpiredError: 'Your kiosk session has expired. Please start check-in again.',
  },
  hi: {
    headerTitle: 'मेडीकियोस्क क्लिनिकल इंटेक',
    headerSubtitle: 'परामर्श से पहले डिजिटल क्लिनिकल इंटेक',
    patientSelfService: 'मरीज स्वयं सेवा',
    staffLogin: 'डॉक्टर लॉगिन',
    switchToClinician: 'डॉक्टर कंसोल',
    textSize: 'अक्षर आकार',
    language: 'भाषा',
    step: 'चरण',
    of: 'का',

    stepRegistration: 'पंजीकरण',
    stepConsent: 'सहमति',
    stepComplaint: 'शिकायत',
    stepQuestions: 'प्रश्न',
    stepSymptoms: 'लक्षण',
    stepHistory: 'इतिहास',
    stepMedications: 'दवाएं',
    stepAllergies: 'एलर्जी',
    stepAyurveda: 'आयुर्वेद',
    stepDocuments: 'दस्तावेज',
    stepReview: 'समीक्षा',

    severityLabel: 'गंभीरता',
    mild: 'हल्का',
    moderate: 'मध्यम',
    severe: 'गंभीर',
    durationLabel: 'अवधि',
    duration24h: '< 24 घंटे',
    duration1_3d: '1–3 दिन',
    duration1wPlus: '> 1 सप्ताह',

    tellUsMedicationsVoice: 'अपनी दवाओं के बारे में बोलकर बताएं (जैसे मैं रोज सुबह बीपी की गोली लेता हूं)',
    dontRememberMedName: 'दवा का नाम पक्का मालूम नहीं',
    medicationVoiceCaptured: 'आपकी आवाज से दवा विवरण दर्ज किया गया',
    useSpokenMedication: '✓ बोली गई दवा का उपयोग करें',
    addToList: '+ सूची में जोड़ें',
    addedMedications: 'जोड़ी गई दवाएं',
    remove: 'हटाएं',

    capturePhoto: '📸 फोटो खींचें',
    retakePhoto: '🔄 फिर से लें',
    usePhoto: '✅ फोटो का उपयोग करें',
    cameraUnavailable: 'कैमरा उपलब्ध नहीं है। आप फोटो या दस्तावेज अपलोड कर सकते हैं।',
    demoExtractionNotice: 'डेमो निष्कर्षण',

    continue: 'आगे बढ़ें',
    back: 'पीछे',
    submit: 'जमा करें',
    tryAgain: 'पुनः प्रयास करें',
    confirmContinue: 'पुष्टि करें और आगे बढ़ें',
    skip: 'इस चरण को छोड़ें',
    notSure: 'पक्का मालूम नहीं',
    tapToSpeak: 'बोलने के लिए टैप करें',
    stopListening: 'सुनना बंद करें',

    voiceReady: '🟢 आवाज तैयार — स्वाभाविक रूप से बोलें',
    voiceListening: '🔴 आपकी आवाज सुनी जा रही है...',
    voiceUnderstanding: '🟣 चिकित्सीय अर्थ समझा जा रहा है...',
    voiceCaptured: '✓ उत्तर दर्ज हो गया',
    voiceError: '⚠️ आवाज इनपुट उपलब्ध नहीं है। आप नीचे टैप या टाइप कर सकते हैं।',
    micPermissionDenied: 'माइक्रोफोन अनुमति आवश्यक है। कृपया अपने ब्राउज़र में माइक्रोफोन एक्सेस की अनुमति दें और पुनः प्रयास करें।',
    micNotFound: 'इस डिवाइस पर कोई माइक्रोफोन नहीं मिला। आप नीचे टैप या टाइप कर सकते हैं।',
    micInUse: 'माइक्रोफोन वर्तमान में किसी अन्य ऐप द्वारा उपयोग में है। कृपया पुनः प्रयास करें।',
    noSpeechDetected: 'कोई आवाज नहीं पाई गई। पुनः प्रयास करने के लिए माइक्रोफोन टैप करें या नीचे टाइप करें।',
    voiceNetworkError: 'आवाज पहचान नेटवर्क समस्या। आप नीचे टैप या टाइप कर सकते हैं।',
    retryMicrophone: 'माइक्रोफोन पुनः प्रयास करें',

    welcomeTitle: 'मेडीकियोस्क में आपका स्वागत है',
    welcomeSubtitle: 'बोलकर या स्क्रीन पर टैप करके अपने लक्षणों को स्वाभाविक रूप से बताएं।',
    startCheckIn: 'चेक-इन शुरू करें',
    continueSession: 'मौजूदा सत्र जारी रखें',
    demoPatient: '1-क्लिक डेमो मरीज',
    voiceFirstNotice: '🎤 आसानी के लिए आवाज और टच प्राथमिक है',
    clinicalKioskActive: 'क्लिनिकल इंटेक कियोस्क सक्रिय',
    aiPoweredClinicalIntake: 'AI-संचालित क्लिनिकल जानकारी संग्रह',
    landingDescription: 'आप क्या महसूस कर रहे हैं, हमें बताएं। MediKiosk आपकी जानकारी एकत्र करेगा और उसे आपके डॉक्टर के लिए तैयार करेगा।',
    demoPatientQuickFill: '1-क्लिक डेमो मरीज (आरव शर्मा, उम्र 28)',
    voiceOrTouch: 'आवाज़ या टच',
    voiceOrTouchDesc: 'अपनी पसंदीदा भाषा में बोलकर या टाइप करके लक्षण बताएं।',
    scanDocumentsTitle: 'दस्तावेज़ स्कैन करें',
    scanDocumentsDesc: 'आसानी से पुराने पर्चे और लैब रिपोर्ट अपलोड करें।',
    doctorVerifiedTitle: 'डॉक्टर द्वारा सत्यापित',
    doctorVerifiedDesc: 'आपके डॉक्टर आपके साथ हर विवरण की समीक्षा और पुष्टि करते हैं।',
    privacyNotice: 'आपकी स्वास्थ्य जानकारी एन्क्रिप्टेड, पूरी तरह से निजी और केवल आपकी चिकित्सा टीम के साथ साझा की जाती है।',

    footerRights: 'MediKiosk मरीज इंटेक प्रणाली • सुगमता और टच-फ़र्स्ट देखभाल के लिए डिज़ाइन किया गया',
    abdmReady: 'ABDM और FHIR R4 तैयार',
    staffDoctorPortal: 'डॉक्टर / स्टाफ पोर्टल',

    registrationTitle: 'मरीज पंजीकरण',
    registrationSubtitle: 'कृपया मूल पहचान विवरण प्रदान करें।',
    firstName: 'पहला नाम',
    lastName: 'अंतिम नाम',
    dateOfBirth: 'जन्म तिथि',
    gender: 'लिंग',
    male: 'पुरुष',
    female: 'महिला',
    other: 'अन्य',
    phone: 'फोन नंबर',
    email: 'ईमेल पता',
    nameRequiredError: 'कृपया अपना पहला और अंतिम नाम दोनों प्रदान करें।',
    registrationFailedError: 'पंजीकरण पूरा नहीं हो सका। कृपया पुनः प्रयास करें।',

    consentTitle: 'मरीज सहमति',
    consentSubtitle: 'कृपया चिकित्सीय इंटेक के लिए सहमति स्वीकार करें।',
    consentBody: 'मैं मेडीकियोस्क को अपनी स्वास्थ्य जानकारी एकत्र करने के लिए अधिकृत करता हूं। अंतिम निदान और देखभाल डॉक्टर द्वारा की जाएगी।',
    consentCheckbox: 'मैं क्लिनिकल इंटेक की शर्तों से सहमत हूं।',
    acceptConsent: 'स्वीकार करें और आगे बढ़ें',
    purpose1Title: 'क्लिनिकल इंटेक का उद्देश्य',
    purpose1Desc: 'यहां एकत्र की गई जानकारी आपके लक्षणों, चिकित्सा इतिहास और पुराने रिकॉर्ड को व्यवस्थित करती है ताकि आपके डॉक्टर परामर्श के लिए तैयार हो सकें।',
    purpose2Title: 'डॉक्टर सत्यापन और अंतिम निर्णय',
    purpose2Desc: 'MediKiosk नैदानिक प्रक्रियाओं में सहायता करता है। यह चिकित्सा निदान की जगह नहीं लेता। अंतिम निर्णय डॉक्टर का ही होगा।',
    purpose3Title: 'समीक्षा और संशोधन',
    purpose3Desc: 'अंतिम जमा करने से पहले आपके पास अपनी दर्ज की गई जानकारी की समीक्षा करने और सुधारने का अवसर होगा।',
    consentPrivacyNotice: 'आपका डेटा स्वास्थ्य सेवा गोपनीयता नियमों के अनुपालन में सुरक्षित रूप से संग्रहीत है।',

    complaintTitle: 'आज आपकी मुख्य स्वास्थ्य समस्या क्या है?',
    complaintSubtitle: 'स्वाभाविक रूप से बोलें या टाइप करें।',
    complaintPlaceholder: 'जैसे मुझे 3 दिनों से तेज सिरदर्द और बुखार है...',
    transcriptionReviewTitle: 'आवाज ट्रांसक्राइब हो गई — समीक्षा करें',
    transcriptionConfirmCheckbox: 'मैं पुष्टि करता हूं कि यह पाठ मेरे द्वारा बोली गई बात को सही से दर्शाता है।',
    complaintHeader: 'आज आप किस समस्या के लिए आए हैं?',
    complaintDescription: 'कृपया अपनी मुख्य समस्या को अपने शब्दों में बताएं।',
    tapMicToSpeak: 'बोलने के लिए माइक पर टैप करें',
    listeningSeconds: 'सुना जा रहा है...',
    processingSpeech: 'आवाज का विश्लेषण हो रहा है...',
    transcriptionReady: 'ट्रांसक्रिप्शन समीक्षा के लिए तैयार है',
    voiceOffline: 'आवाज इनपुट ऑफ़लाइन है',
    voiceInstructions: 'अंग्रेजी, हिंदी या तेलुगु में स्पष्ट रूप से बोलें। आप नीचे टाइप भी कर सकते हैं।',
    primaryHealthConcern: 'मुख्य स्वास्थ्य समस्या / शिकायत',
    voiceTranscribedNotice: 'आवाज दर्ज - समीक्षा आवश्यक',
    confirmTextCheckbox: 'मैं पुष्टि करता हूं कि यह पाठ मेरे द्वारा बोली गई बात को सही से दर्शाता है।',
    commonConcernsHeader: 'या सामान्य समस्याओं पर टैप करें:',
    chipChestPain: 'छाती में दर्द या भारीपन',
    chipHighFever: 'तेज बुखार और ठंड लगना',
    chipHeadache: 'तेज सिरदर्द या माइग्रेन',
    chipBreathing: 'सांस लेने में तकलीफ / खांसी',
    chipStomach: 'पेट दर्द और अपच',
    chipDizziness: 'चक्कर आना और कमजोरी',
    confirmCheckNotice: 'कृपया "मैं पुष्टि करता हूं" चेकबॉक्स पर निशान लगाएं।',
    continueToQuestions: 'स्वास्थ्य प्रश्नों की ओर बढ़ें',

    symptomsTitle: 'अन्य लक्षण चुनें जो आप अनुभव कर रहे हैं',
    symptomsSubtitle: 'लागू होने वाले कार्ड टैप करें या छोड़ें।',
    selectSymptoms: 'चुने गए लक्षण',
    symptomHeadache: 'सिरदर्द',
    symptomFever: 'बुखार / ठंड',
    symptomCough: 'खांसी / जुकाम',
    symptomStomach: 'पेट दर्द',
    symptomChestPain: 'छाती में दर्द / दबाव',
    symptomSob: 'सांस लेने में तकलीफ',
    symptomDizziness: 'चक्कर आना / बेहोशी',
    symptomVomiting: 'उल्टी / मिचली',
    symptomDiarrhea: 'दस्त',
    symptomInjury: 'चोट / शरीर में दर्द',
    symptomSkin: 'त्वचा पर चकत्ते / खुजली',
    symptomFatigue: 'थकान / कमजोरी',

    adaptiveTitle: 'चिकित्सीय प्रश्न',
    adaptiveSubtitle: 'कृपया अपने लक्षणों के बारे में अनुवर्ती प्रश्नों का उत्तर दें।',
    touchOptionsShortcut: 'विकल्प शॉर्टकट',
    clinicalQuestionStage: 'चिकित्सीय प्रश्न',
    universalInputEnabled: 'सार्वभौमिक इनपुट सक्षम',
    speakingState: 'बोल रहा है...',
    readAloud: 'बोलकर सुनाएं',
    intakeHistoryTitle: 'इंटेक इतिहास',
    show: 'दिखाएं',
    hide: 'छिपाएं',
    questionInstruction: 'माइक में बोलें या नीचे दिए गए उत्तर बटनों में से किसी एक पर टैप करें।',
    speakNaturallyInstruction: 'अंग्रेजी, हिंदी या तेलुगु में स्वाभाविक रूप से बोलें।',
    speakNowInstruction: 'अब बोलें। पूरा होने पर बटन टैप करें।',
    reviewVoiceInstruction: 'आगे बढ़ने से पहले अपने बोले गए उत्तर की पुष्टि करें या सुधारें।',
    typePlaceholder: 'चुना गया विकल्प, बोली गई बात, या यहाँ उत्तर टाइप करें...',
    typeCustomDetails: 'अन्य विवरण? टाइप करने के लिए टैप करें',
    hideTypingBox: 'टाइपिंग बॉक्स छिपाएं',

    historyTitle: 'चिकित्सा और पिछला स्वास्थ्य इतिहास',
    historySubtitle: 'मौजूदा स्थितियों को चुनें या बोलकर/टाइप करके बताएं।',
    conditionHypertension: 'उच्च रक्तचाप / बीपी',
    conditionDiabetes: 'मधुमेह / शुगर',
    conditionAsthma: 'अस्थमा / सांस की बीमारी',
    conditionHeartDisease: 'हृदय रोग',
    conditionThyroid: 'थायराइड',
    conditionReflux: 'एसिड रिफ्लक्स / गैस',
    pastSurgeries: 'पिछली सर्जरी / ऑपरेशन',
    familyHistory: 'पारिवारिक चिकित्सा इतिहास',
    addHistoryPlaceholder: 'पुराने इलाज, ऑपरेशन या पारिवारिक बीमारियों का विवरण दें...',

    medicationsTitle: 'वर्तमान दवाएं',
    medicationsSubtitle: 'नियमित रूप से ली जाने वाली दवाइयों की सूची दें।',
    medicationName: 'दवा का नाम',
    dosage: 'खुराक',
    frequency: 'कितनी बार',
    noMedications: 'कोई नियमित दवा नहीं',
    addMedicationPlaceholder: 'जैसे रोज सुबह बीपी की गोली...',

    allergiesTitle: 'एलर्जी',
    allergiesSubtitle: 'क्या आपको किसी दवा या भोजन से एलर्जी है?',
    noKnownAllergies: 'कोई ज्ञात एलर्जी नहीं',
    allergenName: 'एलर्जन का नाम',
    addAllergyPlaceholder: 'जैसे पेनिसिलिन...',
    typicalReaction: 'प्रतिक्रिया',
    reactionRash: 'चकत्ते / खुजली',
    reactionSwelling: 'चेहरे / होठों पर सूजन',
    reactionBreathing: 'सांस लेने में गंभीर कठिनाई',
    reactionNausea: 'मिचली / पेट खराब',

    ayurvedaTitle: 'आयुर्वेद इतिहास (वैकल्पिक)',
    ayurvedaSubtitle: 'क्या आप पारंपरिक आयुर्वेदिक प्रकृति/पाचन जानकारी शामिल करना चाहते हैं?',
    ayurvedaOptIn: 'आयुर्वेदिक इतिहास शामिल करें',
    ayurvedaSkip: 'आयुर्वेदिक अनुभाग छोड़ें',
    yesProvideDetails: 'हां, विवरण प्रदान करें',
    skipThisStep: 'इस चरण को छोड़ें',
    continueToDocuments: 'दस्तावेज़ों की ओर बढ़ें',

    documentsTitle: 'चिकित्सा दस्तावेज',
    documentsSubtitle: 'कैमरे से पर्चे की फोटो खींचें या फाइल अपलोड करें।',
    scanWithCamera: '📷 कैमरे से स्कैन करें',
    uploadFromDevice: '📁 डिवाइस से अपलोड करें',
    processingDocument: 'दस्तावेज का विश्लेषण किया जा रहा है...',
    extractionSummary: 'निकाला गया चिकित्सा सारांश',
    confidenceScore: 'विश्वसनीयता स्कोर',
    noDocuments: 'कोई दस्तावेज संलग्न नहीं है',

    reviewTitle: 'अपने चिकित्सीय सारांश की समीक्षा करें',
    reviewSubtitle: 'टोकन नंबर प्राप्त करने से पहले अपनी जानकारी की पुष्टि करें।',
    confirmIntake: 'पुष्टि करें और जमा करें',
    personalDetails: 'व्यक्तिगत विवरण',
    patientProvided: 'मरीज द्वारा प्रदान किया गया',
    edit: 'संशोधन करें',
    name: 'नाम',
    dob: 'जन्म तिथि',
    aiStructuredIntake: 'AI-संरचित इंटेक',
    symptomsAndResponses: 'लक्षण और प्रश्नों के उत्तर',

    completionTitle: 'इंटेक सफलतापूर्वक पूरा हुआ',
    completionSubtitle: 'आपकी जानकारी डॉक्टर कंसोल पर भेज दी गई है।',
    yourTokenNumber: 'आपका टोकन नंबर',
    triageStatus: 'प्राथमिकता स्तर',
    proceedToWaitingArea: 'कृपया प्रतीक्षा क्षेत्र में बैठें। डॉक्टर आपका टोकन नंबर पुकारेंगे।',
    finish: 'समाप्त करें और होम पर लौटें',
    yourConsultationToken: 'आपका परामर्श टोकन नंबर',
    waitingAreaNotice: 'कृपया प्रतीक्षा क्षेत्र में बैठें। डॉक्टर आपका टोकन नंबर पुकारेंगे।',
    priorityAttentionNotice: 'आपकी जानकारी को प्राथमिकता के आधार पर चिह्नित किया गया है।',
    openClinicianQueue: 'डॉक्टर कतार खोलें',
    sessionExpiredError: 'आपका सत्र समाप्त हो गया है। कृपया पुनः चेक-इन शुरू करें।',
  },
  te: {
    headerTitle: 'మెడికియోస్క్ పేషెంట్ ఇన్టేక్',
    headerSubtitle: 'ముందస్తు క్లినికల్ వివరాల నమోదు',
    patientSelfService: 'పేషెంట్ స్వీయ సేవ',
    staffLogin: 'డాక్టర్ ప్రవేశం',
    switchToClinician: 'వైద్యుల డాష్‌బోర్డ్',
    textSize: 'అక్షర పరిమాణం',
    language: 'భాష',
    step: 'దశ',
    of: '/',

    stepRegistration: 'నమోదు',
    stepConsent: 'సమ్మతి',
    stepComplaint: 'ప్రధాన ఫిర్యాదు',
    stepQuestions: 'ప్రశ్నలు',
    stepSymptoms: 'లక్షణాలు',
    stepHistory: 'వైద్య చరిత్ర',
    stepMedications: 'మందులు',
    stepAllergies: 'అలర్జీలు',
    stepAyurveda: 'ఆయుర్వేదం',
    stepDocuments: 'పత్రాలు',
    stepReview: 'పరిశీలన',

    severityLabel: 'తీవ్రత',
    mild: 'స్వల్పంగా',
    moderate: 'మోస్తరుగా',
    severe: 'తీవ్రంగా',
    durationLabel: 'వ్యవధి',
    duration24h: '24 గంటలలోపు',
    duration1_3d: '1–3 రోజులు',
    duration1wPlus: '1 వారానికి పైగా',

    tellUsMedicationsVoice: 'మీరు వాడుతున్న మందులను చెప్పండి (ఉదా: ప్రతి ఉదయం బిపి టాబ్లెట్ వేసుకుంటాను)',
    dontRememberMedName: 'తెలియదు / ఖచ్చితంగా తెలియదు',
    medicationVoiceCaptured: 'మీ మాటల నుండి మందు వివరాలు నమోదయ్యాయి',
    useSpokenMedication: '✓ చెప్పిన మందును ఉపయోగించండి',
    addToList: '+ జాబితాలో చేర్చండి',
    addedMedications: 'చేర్చబడిన మందులు',
    remove: 'తొలగించు',

    capturePhoto: '📸 ఫోటో తీయండి',
    retakePhoto: '🔄 మళ్లీ తీయండి',
    usePhoto: '✅ ఫోటోను ఉపయోగించండి',
    cameraUnavailable: 'కెమెరా అందుబాటులో లేదు. మీరు ఫోటో లేదా నివేదికను అప్‌లోడ్ చేయవచ్చు.',
    demoExtractionNotice: 'డెమో విశ్లేషణ',

    continue: 'ముందుకు సాగండి',
    back: 'వెనుకకు',
    submit: 'సమర్పించండి',
    tryAgain: 'మళ్లీ ప్రయత్నించండి',
    confirmContinue: 'సరిచూసుకొని ముందుకు సాగండి',
    skip: 'ఈ దశను దాటవేయండి',
    notSure: 'సమాచారం ఖచ్చితంగా తెలియదు',
    tapToSpeak: 'మాట్లాడటానికి నొక్కండి',
    stopListening: 'వినడం ఆపివేయండి',

    voiceReady: '🟢 వాయిస్ సిద్ధంగా ఉంది — సహజంగా మాట్లాడండి',
    voiceListening: '🔴 మీ మాటలను వింటున్నాము...',
    voiceUnderstanding: '🟣 వైద్య సమాచారాన్ని విశ్లేషిస్తున్నాము...',
    voiceCaptured: '✓ సమాచారం నమోదైనది',
    voiceError: '⚠️ వాయిస్ సౌకర్యం అందుబాటులో లేదు. మీరు కింద నొక్కి లేదా టైప్ చేయవచ్చు.',
    micPermissionDenied: 'మైక్రోఫోన్ అనుమతి అవసరం. దయచేసి మీ బ్రౌజర్‌లో మైక్రోఫోన్ అనుమతించి మళ్లీ ప్రయత్నించండి.',
    micNotFound: 'ఈ పరికరంలో మైక్రోఫోన్ కనుగొనబడలేదు. మీరు కింద నొక్కి లేదా టైప్ చేయవచ్చు.',
    micInUse: 'మైక్రోఫోన్ ప్రస్తుతం మరొక అప్లికేషన్ ద్వారా ఉపయోగించబడుతోంది. దయచేసి మళ్లీ ప్రయత్నించండి.',
    noSpeechDetected: 'మాటలు ఏవీ వినబడలేదు. మళ్లీ ప్రయత్నించడానికి మైక్రోఫోన్ నొక్కండి లేదా టైప్ చేయండి.',
    voiceNetworkError: 'వాయిస్ గుర్తింపు నెట్‌వర్క్ సమస్య. మీరు కింద నొక్కి లేదా టైప్ చేయవచ్చు.',
    retryMicrophone: 'మైక్రోఫోన్ మళ్లీ ప్రయత్నించండి',

    welcomeTitle: 'మెడికియోస్క్‌కు స్వాగతం',
    welcomeSubtitle: 'మీ ఆరోగ్య సమస్యలను సహజంగా మాట్లాడి లేదా స్క్రీన్ పై నొక్కి తెలియజేయండి.',
    startCheckIn: 'పేషెంట్ రిజిస్ట్రేషన్ ప్రారంభించండి',
    continueSession: 'పాత సెషన్‌ను కొనసాగించండి',
    demoPatient: '1-క్లిక్ డెమో పేషెంట్',
    voiceFirstNotice: '🎤 సులభమైన ఉపయోగం కోసం మాటలు మరియు స్పర్శ ప్రాథమికమైనవి',
    clinicalKioskActive: 'క్లినికల్ ఇన్టేక్ కియోస్క్ సక్రియం',
    aiPoweredClinicalIntake: 'AI-ఆధారిత క్లినికల్ సమాచార సేకరణ',
    landingDescription: 'మీరు అనుభవిస్తున్న ఆరోగ్య సమస్యలను తెలియజేయండి. MediKiosk మీ వివరాలను సేకరించి వైద్యుల పరిశీలనకు సిద్ధం చేస్తుంది.',
    demoPatientQuickFill: '1-క్లిక్ డెమో పేషెంట్ (ఆరవ్ శర్మ, వయస్సు 28)',
    voiceOrTouch: 'మాటలు లేదా స్పర్శ',
    voiceOrTouchDesc: 'మీకు సౌకర్యవంతమైన భాషలో మాట్లాడి లేదా టైప్ చేసి తెలుపండి.',
    scanDocumentsTitle: 'పత్రాలను స్కాన్ చేయండి',
    scanDocumentsDesc: 'పాత డాక్టర్ ప్రిస్క్రిప్షన్లు మరియు నివేదికలను అప్‌లోడ్ చేయండి.',
    doctorVerifiedTitle: 'వైద్యుల ద్వారా ప్రామాణీకరించబడింది',
    doctorVerifiedDesc: 'వైద్యులు మీ ప్రతీ వివరాలను పరిశీలించి సరిచూస్తారు.',
    privacyNotice: 'మీ ఆరోగ్య వివరాలు భద్రపరచబడతాయి మరియు కేవలం వైద్య బృందానికి మాత్రమే లభ్యమవుతాయి.',

    footerRights: 'MediKiosk పేషెంట్ ఇన్టేక్ వ్యవస్థ • సులభమైన ఉపయోగానికి రూపకల్పన చేయబడింది',
    abdmReady: 'ABDM & FHIR R4 సిద్ధంగా ఉంది',
    staffDoctorPortal: 'వైద్యుల పోర్టల్',

    registrationTitle: 'పేషెంట్ నమోదు వివరాలు',
    registrationSubtitle: 'దయచేసి మీ ప్రాథమిక వివరాలను నమోదు చేయండి.',
    firstName: 'మొదటి పేరు',
    lastName: 'ఇంటి పేరు / చివరి పేరు',
    dateOfBirth: 'పుట్టిన తేదీ',
    gender: 'లింగం',
    male: 'పురుషుడు',
    female: 'స్త్రీ',
    other: 'ఇతర',
    phone: 'ఫోన్ నంబర్',
    email: 'ఈమెయిల్ అడ్రస్',
    nameRequiredError: 'దయచేసి మీ మొదటి మరియు చివరి పేరు రెండింటినీ నమోదు చేయండి.',
    registrationFailedError: 'నమోదు పూర్తి కాలేదు. దయచేసి మళ్లీ ప్రయత్నించండి.',

    consentTitle: 'పేషెంట్ అంగీకారం (సమ్మతి)',
    consentSubtitle: 'వైద్య వివరాల సేకరణకు దయచేసి అంగీకరించండి.',
    consentBody: 'నా ఆరోగ్య సమస్యలు మరియు వివరాలను వైద్యుల పరిశీలన కోసం సేకరించడానికి నేను అంగీకరిస్తున్నాను. తుది వైద్య నిర్ణయం వైద్యుల ద్వారానే జరుగుతుంది.',
    consentCheckbox: 'నేను వైద్య వివరాల నిబంధనలకు అంగీకరిస్తున్నాను.',
    acceptConsent: 'అంగీకరించి ముందుకు సాగండి',
    purpose1Title: 'క్లినికల్ ఇన్టేక్ ఉద్దేశ్యం',
    purpose1Desc: 'ఇక్కడ సేకరించిన సమాచారం మీ లక్షణాలు, వైద్య చరిత్రను క్రమబద్ధీకరిస్తుంది.',
    purpose2Title: 'వైద్యుల పరిశీలన మరియు తుది నిర్ణయం',
    purpose2Desc: 'MediKiosk వైద్య సేవలకు సహాయపడుతుంది. తుది వైద్య నిర్ణయం డాక్టర్ ద్వారానే జరుగుతుంది.',
    purpose3Title: 'పరిశీలన మరియు సవరణలు',
    purpose3Desc: 'సమర్పించే ముందు మీ వివరాలను పరిశీలించి, సవరించే అవకాశం మీకు ఉంటుంది.',
    consentPrivacyNotice: 'మీ వివరాలు ఆరోగ్య భద్రతా నిబంధనల ప్రకారం సురక్షితంగా నిల్వ చేయబడతాయి.',

    complaintTitle: 'ఈ రోజు మీకు ఉన్న ముఖ్యమైన ఆరోగ్య సమస్య ఏమిటి?',
    complaintSubtitle: 'మీకు ఎదురవుతున్న ఇబ్బందిని సహజంగా మాట్లాడండి లేదా టైప్ చేయండి.',
    complaintPlaceholder: 'ఉదా: నాకు మూడు రోజుల నుండి విపరీతమైన తలనొప్పి మరియు జ్వరం ఉంది...',
    transcriptionReviewTitle: 'మీ మాటలు రికార్డ్ అయ్యాయి — సరిచూసుకోండి',
    transcriptionConfirmCheckbox: 'నేను చెప్పిన వివరాలు సరిగ్గా నమోదయ్యాయని ధృవీకరిస్తున్నాను.',
    complaintHeader: 'ఈ రోజు మీ ఆరోగ్య సమస్య ఏమిటి?',
    complaintDescription: 'మీ ప్రధాన ఆరోగ్య ఇబ్బందిని మీ స్వంత మాటలలో తెలపండి.',
    tapMicToSpeak: 'మాట్లాడటానికి మైక్ నొక్కండి',
    listeningSeconds: 'వింటున్నాము...',
    processingSpeech: 'మాటలను విశ్లేషిస్తున్నాము...',
    transcriptionReady: 'మీ వివరాలు నమోదయ్యాయి',
    voiceOffline: 'వాయిస్ ఇన్ పుట్ అందుబాటులో లేదు',
    voiceInstructions: 'ఇంగ్లీష్, హిందీ లేదా తెలుగులో స్పష్టంగా మాట్లాడండి. కింద టైప్ కూడా చేయవచ్చు.',
    primaryHealthConcern: 'ప్రధాన ఆరోగ్య సమస్య',
    voiceTranscribedNotice: 'మాటలు నమోదయ్యాయి - పరిశీలించండి',
    confirmTextCheckbox: 'నేను చెప్పిన వివరాలు సరిగ్గా నమోదయ్యాయని ధృవీకరిస్తున్నాను.',
    commonConcernsHeader: 'లేదా తరచుగా వచ్చే సమస్యలను ఎంచుకోండి:',
    chipChestPain: 'ఛాతీ నొప్పి లేదా బరువుగా ఉండటం',
    chipHighFever: 'తీవ్రమైన జ్వరం మరియు చలి',
    chipHeadache: 'తీవ్రమైన తలనొప్పి',
    chipBreathing: 'ఆయాసం / దగ్గు',
    chipStomach: 'కడుపు నొప్పి మరియు అజీర్ణం',
    chipDizziness: 'కళ్లు తిరగడం మరియు నీరసం',
    confirmCheckNotice: 'దయచేసి ధృవీకరణ బాక్స్‌ను ఎంచుకోండి.',
    continueToQuestions: 'ఆరోగ్య ప్రశ్నలకు వెళ్లండి',

    symptomsTitle: 'మీకు ఉన్న ఇతర ఆరోగ్య లక్షణాలను ఎంచుకోండి',
    symptomsSubtitle: 'మీకు వర్తించే కార్డ్‌లను నొక్కండి, లేదా ఇప్పటికే చెప్పి ఉంటే ముందుకు సాగండి.',
    selectSymptoms: 'ఎంచుకున్న లక్షణాలు',
    symptomHeadache: 'తలనొప్పి',
    symptomFever: 'జ్వరం / చలి',
    symptomCough: 'దగ్గు / జలుబు',
    symptomStomach: 'కడుపు నొప్పి',
    symptomChestPain: 'ఛాతీ నొప్పి / ఒత్తిడి',
    symptomSob: 'ఆయాసం / ఊపిరి తిత్తుల ఇబ్బంది',
    symptomDizziness: 'కళ్లు తిరగడం / స్పృహ తప్పడం',
    symptomVomiting: 'వాంతులు / వికారం',
    symptomDiarrhea: 'విరేచనాలు',
    symptomInjury: 'గాయం / ఒంటి నొప్పులు',
    symptomSkin: 'చర్మంపై దద్దుర్లు / దురద',
    symptomFatigue: 'నీరసం / బలహీనత',

    adaptiveTitle: 'వైద్యుల తదుపరి ప్రశ్నలు',
    adaptiveSubtitle: 'మీరు తెలిపిన లక్షణాలపై దయచేసి ఈ క్రింది ప్రశ్నలకు సమాధానమివ్వండి.',
    touchOptionsShortcut: 'సమాధానాల ఎంపిక',
    clinicalQuestionStage: 'వైద్య సమాచార ప్రశ్న',
    universalInputEnabled: 'ఇన్‌పుట్ అందుబాటులో ఉంది',
    speakingState: 'మాట్లాడుతోంది...',
    readAloud: 'చదివి వినిపించు',
    intakeHistoryTitle: 'నమోదైన వివరాలు',
    show: 'చూపించు',
    hide: 'దాచు',
    questionInstruction: 'మైక్‌లో మాట్లాడండి లేదా కింద ఉన్న బటన్‌ను నొక్కండి.',
    speakNaturallyInstruction: 'తెలుగు, ఇంగ్లీష్ లేదా హిందీలో సహజంగా మాట్లాడండి.',
    speakNowInstruction: 'ఇప్పుడు మాట్లాడండి. పూర్తయిన తర్వాత బటన్ నొక్కండి.',
    reviewVoiceInstruction: 'ముందుకు సాగే ముందు మీ సమాధానాన్ని సరిచూసుకోండి.',
    typePlaceholder: 'ఎంచుకున్న వివరాలు లేదా సమాధానాన్ని ఇక్కడ టైప్ చేయండి...',
    typeCustomDetails: 'ఇతర వివరాలు టైప్ చేయాలా?',
    hideTypingBox: 'టైపింగ్ బాక్స్ దాచు',

    historyTitle: 'గత వైద్య చరిత్ర వివరాలు',
    historySubtitle: 'మీకున్న పాత ఆరోగ్య సమస్యలను ఎంచుకోండి లేదా మాట్లాడి తెలపండి.',
    conditionHypertension: 'హై బిపి (అధిక రక్తపోటు)',
    conditionDiabetes: 'షుగర్ వ్యాధి (మధుమేహం)',
    conditionAsthma: 'ఆస్తమా / ఊపిరితిత్తుల సమస్య',
    conditionHeartDisease: 'గుండె సంబంధిత సమస్యలు',
    conditionThyroid: 'థైరాయిడ్ సమస్య',
    conditionReflux: 'గ్యాస్ / ఎసిడిటీ',
    pastSurgeries: 'గతంలో జరిగిన ఆపరేషన్లు / శస్త్రచికిత్సలు',
    familyHistory: 'కుటుంబ సభ్యుల వైద్య చరిత్ర',
    addHistoryPlaceholder: 'పాత చికిత్సలు, ఆపరేషన్లు లేదా కుటుంబ సమస్యల వివరాలు తెలపండి...',

    medicationsTitle: 'ప్రస్తుతం వాడుతున్న మందులు',
    medicationsSubtitle: 'మీరు క్రమం తప్పకుండా వాడుతున్న మందులు, టాబ్లెట్లు తెలపండి.',
    medicationName: 'మందు పేరు',
    dosage: 'పాళ్లు / డోసేజ్',
    frequency: 'ఎన్ని సార్లు',
    noMedications: 'ఏలాంటి క్రమబద్ధమైన మందులు వాడటం లేదు',
    addMedicationPlaceholder: 'ఉదా: ప్రతి ఉదయం వాడే బిపి టాబ్లెట్...',

    allergiesTitle: 'అలర్జీ వివరాలు',
    allergiesSubtitle: 'మందులు లేదా ఆహారం వల్ల మీకు ఏవైనా అలర్జీలు ఉన్నాయा?',
    noKnownAllergies: 'ఏలాంటి అలర్జీలు లేవు',
    allergenName: 'అలర్జీ కలిగించే పదార్థం/మందు',
    addAllergyPlaceholder: 'ఉదా: పెన్సిలిన్ మందుల అలర్జీ...',
    typicalReaction: 'అలర్జీ ప్రభావం',
    reactionRash: 'దద్దుర్లు / దురద',
    reactionSwelling: 'ముఖం / పెదవుల వాపు',
    reactionBreathing: 'తీవ్రమైన శ్వాస ఇబ్బంది',
    reactionNausea: 'వికారం / కడుపులో తిప్పడం',

    ayurvedaTitle: 'ఆయుర్వేద ఆరోగ్య వివరాలు (ఐచ్ఛికం)',
    ayurvedaSubtitle: 'సాంప్రదాయ ఆయుర్వేద జీర్ణవ్యవస్థ మరియు శరీర తత్వ వివరాలు జోడించాలా?',
    ayurvedaOptIn: 'ఆయుర్వేద వివరాలు జోడించండి',
    ayurvedaSkip: 'ఈ విభాగం దాటవేయండి',
    yesProvideDetails: 'అవును, వివరాలు తెలపండి',
    skipThisStep: 'ఈ దశను దాటవేయండి',
    continueToDocuments: 'పత్రాల విభాగం వెళ్లండి',

    documentsTitle: 'వైద్య పత్రాలు మరియు నివేదికలు',
    documentsSubtitle: 'మీ వద్ద ఉన్న డాక్టర్ ప్రిస్క్రిప్షన్ లేదా రిపోర్ట్‌లను స్కాన్ చేయండి.',
    scanWithCamera: '📷 కెమెరాతో స్కాన్ చేయండి',
    uploadFromDevice: '📁 పరికరం నుండి అప్‌లోడ్ చేయండి',
    processingDocument: 'పత్రంలోని సమాచారాన్ని పరిశీలిస్తున్నాము...',
    extractionSummary: 'పత్రం నుండి గ్రహించిన వైద్య సారాంశం',
    confidenceScore: 'ఖచ్చితత్వ శాతం',
    noDocuments: 'ఏలాంటి పత్రాలు జోడించబడలేదు',

    reviewTitle: 'మీ వైద్య సారాంశాన్ని సరిచూసుకోండి',
    reviewSubtitle: 'టోకెన్ పొందే ముందు మీ వివరాలను ఒకసారి సరిచూసుకోండి.',
    confirmIntake: 'ధృవీకరించి సమర్పించండి',
    personalDetails: 'వ్యక్తిగత వివరాలు',
    patientProvided: 'పేషెంట్ తెలిపిన వివరాలు',
    edit: 'సవరించు',
    name: 'పేరు',
    dob: 'పుట్టిన తేదీ',
    aiStructuredIntake: 'AI-సమర్పించిన వివరాలు',
    symptomsAndResponses: 'లక్షణాలు మరియు సమాధానాలు',

    completionTitle: 'వివరాల నమోదు విజయవంతంగా పూర్తయింది',
    completionSubtitle: 'మీ సమాచారం డాక్టర్ డాష్‌బోర్డ్‌కు పంపబడింది.',
    yourTokenNumber: 'మీ టోకెన్ నంబర్',
    triageStatus: 'ప్రాధాన్యత స్థాయి',
    proceedToWaitingArea: 'దయచేసి వేచి ఉండే గదిలో కూర్చోండి. డాక్టర్ గారు మీ టోకెన్ నంబర్ పిలుస్తారు.',
    finish: 'పూర్తి చేసి హోమ్‌కి వెళ్లండి',
    yourConsultationToken: 'మీ వైధ్య సేవ టోకెన్ నంబర్',
    waitingAreaNotice: 'దయచేసి వేచి ఉండే గదిలో కూర్చోండి. డాక్టర్ గారు మీ టోకెన్ పిలుస్తారు.',
    priorityAttentionNotice: 'మీ వివరాలు ప్రాధాన్యత ఆధారంగా పరిశీలించబడతాయి.',
    openClinicianQueue: 'వైద్యుల క్యూ తెరవండి',
    sessionExpiredError: 'మీ సెషన్ గడువు ముగిసింది. దయచేసి మళ్లీ ప్రారంభించండి.',
  },
};

export function getTranslation(lang: 'en' | 'hi' | 'te' | string, key: keyof TranslationDictionary): string {
  const selectedLang = (lang === 'hi' || lang === 'te') ? lang : 'en';
  const dict = translations[selectedLang] || translations.en;
  return dict[key] || translations.en[key] || String(key);
}
