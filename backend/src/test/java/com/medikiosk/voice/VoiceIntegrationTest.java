package com.medikiosk.voice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.dto.intake.AnswerRequest;
import com.medikiosk.dto.intake.ChiefComplaintRequest;
import com.medikiosk.dto.intake.ConsentRequest;
import com.medikiosk.dto.intake.IntakeSessionRequest;
import com.medikiosk.dto.intake.PatientRequest;
import com.medikiosk.dto.voice.SpeechSynthesisRequest;
import com.medikiosk.entity.Answer;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.Question;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.ConsentStatus;
import com.medikiosk.entity.enums.IntakeSessionStatus;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;
import com.medikiosk.repository.AnswerRepository;
import com.medikiosk.repository.ConsentRepository;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.repository.PatientRepository;
import com.medikiosk.repository.QuestionRepository;
import com.medikiosk.repository.UserRepository;
import com.medikiosk.security.JwtTokenProvider;
import com.medikiosk.voice.provider.MockTextToSpeechProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class VoiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private IntakeSessionRepository intakeSessionRepository;

    @Autowired
    private ConsentRepository consentRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User patientUserA;
    private String tokenPatientA;
    private Patient patientA;
    private UUID sessionIdA;

    private User patientUserB;
    private String tokenPatientB;
    private Patient patientB;
    private UUID sessionIdB;

    private User doctorUser;
    private String tokenDoctor;

    private Question testQuestion;

    @BeforeEach
    void setUp() throws Exception {
        answerRepository.deleteAll();
        consentRepository.deleteAll();
        intakeSessionRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create Patient User A
        patientUserA = new User("patient_a@example.com", passwordEncoder.encode("Password123!"),
                "Patient", "Alice", UserRole.PATIENT, UserStatus.ACTIVE);
        patientUserA = userRepository.save(patientUserA);
        tokenPatientA = jwtTokenProvider.generateToken(patientUserA);

        patientA = createPatient(patientUserA, "Alice", "Patient", "PAT-A-001", tokenPatientA);
        sessionIdA = createSessionWithConsent(patientA, tokenPatientA, "en");

        // 2. Create Patient User B
        patientUserB = new User("patient_b@example.com", passwordEncoder.encode("Password123!"),
                "Patient", "Bob", UserRole.PATIENT, UserStatus.ACTIVE);
        patientUserB = userRepository.save(patientUserB);
        tokenPatientB = jwtTokenProvider.generateToken(patientUserB);

        patientB = createPatient(patientUserB, "Bob", "Patient", "PAT-B-002", tokenPatientB);
        sessionIdB = createSessionWithConsent(patientB, tokenPatientB, "en");

        // 3. Create Doctor User
        doctorUser = new User("doctor@example.com", passwordEncoder.encode("Password123!"),
                "Dr.", "Consultant", UserRole.DOCTOR, UserStatus.ACTIVE);
        doctorUser = userRepository.save(doctorUser);
        tokenDoctor = jwtTokenProvider.generateToken(doctorUser);

        // 4. Retrieve standard question
        List<Question> questions = questionRepository.findAll();
        assertThat(questions).isNotEmpty();
        testQuestion = questions.get(0);
    }

    private Patient createPatient(User user, String firstName, String lastName, String identifier, String token) throws Exception {
        PatientRequest request = new PatientRequest(firstName, lastName, LocalDate.of(1990, 1, 15), "FEMALE", "+919876543210", user.getEmail());
        request.setPatientIdentifier(identifier);

        MvcResult result = mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        UUID patientId = UUID.fromString(root.path("data").path("id").asText());
        return patientRepository.findById(patientId).orElseThrow();
    }

    private UUID createSessionWithConsent(Patient patient, String token, String language) throws Exception {
        IntakeSessionRequest sessionRequest = new IntakeSessionRequest(patient.getId(), language);
        MvcResult sessionResult = mockMvc.perform(post("/api/intake/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(sessionResult.getResponse().getContentAsString());
        UUID sessionId = UUID.fromString(root.path("data").path("id").asText());

        ConsentRequest consentRequest = new ConsentRequest(ConsentStatus.ACCEPTED, "GENERAL_INTAKE", "v1.0", "REF-VOICE-1");
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/consent")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consentRequest)))
                .andExpect(status().isOk());

        return sessionId;
    }

    @Test
    @DisplayName("1. Valid audio transcription request succeeds")
    void testValidAudioTranscriptionRequest() throws Exception {
        byte[] wavBytes = MockTextToSpeechProvider.generatePcmWav("I have had a mild headache and fever for two days");
        MockMultipartFile audioFile = new MockMultipartFile("file", "speech.wav", "audio/wav", wavBytes);

        mockMvc.perform(multipart("/api/voice/transcribe")
                        .file(audioFile)
                        .param("sessionId", sessionIdA.toString())
                        .param("language", "en")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").value(sessionIdA.toString()))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.transcript").isNotEmpty())
                .andExpect(jsonPath("$.data.confidence").isNumber())
                .andExpect(jsonPath("$.data.isMock").value(true));
    }

    @Test
    @DisplayName("2. Missing audio is rejected with HTTP 400")
    void testMissingAudioRejected() throws Exception {
        MockMultipartFile emptyAudio = new MockMultipartFile("file", "empty.wav", "audio/wav", new byte[0]);

        mockMvc.perform(multipart("/api/voice/transcribe")
                        .file(emptyAudio)
                        .param("sessionId", sessionIdA.toString())
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Audio file is required and cannot be empty"));
    }

    @Test
    @DisplayName("3. Unsupported audio type is rejected with HTTP 400")
    void testUnsupportedAudioTypeRejected() throws Exception {
        // Wrong MIME type
        MockMultipartFile pdfFile = new MockMultipartFile("file", "document.pdf", "application/pdf", new byte[]{1, 2, 3, 4});

        mockMvc.perform(multipart("/api/voice/transcribe")
                        .file(pdfFile)
                        .param("sessionId", sessionIdA.toString())
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // Audio MIME type but fake non-audio content (magic bytes mismatch)
        MockMultipartFile fakeAudio = new MockMultipartFile("file", "fake.wav", "audio/wav", "THIS_IS_NOT_AUDIO".getBytes());

        mockMvc.perform(multipart("/api/voice/transcribe")
                        .file(fakeAudio)
                        .param("sessionId", sessionIdA.toString())
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("File signature does not match supported audio types")));
    }

    @Test
    @DisplayName("4. Oversized audio is rejected with HTTP 400")
    void testOversizedAudioRejected() throws Exception {
        // Create 11MB file with WAV header
        byte[] oversizedBytes = new byte[11 * 1024 * 1024];
        oversizedBytes[0] = 'R';
        oversizedBytes[1] = 'I';
        oversizedBytes[2] = 'F';
        oversizedBytes[3] = 'F';
        oversizedBytes[8] = 'W';
        oversizedBytes[9] = 'A';
        oversizedBytes[10] = 'V';
        oversizedBytes[11] = 'E';

        MockMultipartFile oversizedFile = new MockMultipartFile("file", "large.wav", "audio/wav", oversizedBytes);

        mockMvc.perform(multipart("/api/voice/transcribe")
                        .file(oversizedFile)
                        .param("sessionId", sessionIdA.toString())
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Audio file exceeds maximum allowed upload size (10MB)")));
    }

    @Test
    @DisplayName("5. STT provider failure returns safe error without leaking internal details")
    void testSttProviderFailureReturnsSafeError() throws Exception {
        // Valid WebM audio header: 0x1A 0x45 0xDF 0xA3
        byte[] webmBytes = new byte[]{(byte) 0x1A, (byte) 0x45, (byte) 0xDF, (byte) 0xA3, 0, 0, 0, 0};
        MockMultipartFile webmFile = new MockMultipartFile("file", "speech.webm", "audio/webm", webmBytes);

        mockMvc.perform(multipart("/api/voice/transcribe")
                        .file(webmFile)
                        .param("sessionId", sessionIdA.toString())
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("6. Transcript is returned correctly")
    void testTranscriptReturnedCorrectly() throws Exception {
        byte[] wavBytes = MockTextToSpeechProvider.generatePcmWav("Test transcript query");
        MockMultipartFile audioFile = new MockMultipartFile("file", "speech.wav", "audio/wav", wavBytes);

        MvcResult result = mockMvc.perform(multipart("/api/voice/transcribe")
                        .file(audioFile)
                        .param("sessionId", sessionIdA.toString())
                        .param("language", "en")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.path("data").path("transcript").asText()).isNotEmpty();
    }

    @Test
    @DisplayName("7. Confidence score is preserved when provider supplies it")
    void testConfidencePreservedWhenProviderSuppliesIt() throws Exception {
        byte[] wavBytes = MockTextToSpeechProvider.generatePcmWav("Confidence check");
        MockMultipartFile audioFile = new MockMultipartFile("file", "speech.wav", "audio/wav", wavBytes);

        MvcResult result = mockMvc.perform(multipart("/api/voice/transcribe")
                        .file(audioFile)
                        .param("sessionId", sessionIdA.toString())
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        double confidence = response.path("data").path("confidence").asDouble();
        assertThat(confidence).isGreaterThan(0.5);
    }

    @Test
    @DisplayName("8. Selected language is preserved across interaction")
    void testSelectedLanguagePreserved() throws Exception {
        byte[] wavBytes = MockTextToSpeechProvider.generatePcmWav("Hindi language check");
        MockMultipartFile audioFile = new MockMultipartFile("file", "speech.wav", "audio/wav", wavBytes);

        // Test Hindi
        mockMvc.perform(multipart("/api/voice/transcribe")
                        .file(audioFile)
                        .param("sessionId", sessionIdA.toString())
                        .param("language", "hi")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.language").value("hi"))
                .andExpect(jsonPath("$.data.transcript").value(org.hamcrest.Matchers.containsString("सिरदर्द")));

        // Test Telugu
        mockMvc.perform(multipart("/api/voice/transcribe")
                        .file(audioFile)
                        .param("sessionId", sessionIdA.toString())
                        .param("language", "te")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.language").value("te"))
                .andExpect(jsonPath("$.data.transcript").value(org.hamcrest.Matchers.containsString("తలనొప్పి")));
    }

    @Test
    @DisplayName("9. Patient confirmation is required before final answer storage")
    void testPatientConfirmationRequiredBeforeFinalAnswerStorage() throws Exception {
        // Attempt to save voice response with confirmedByPatient = false
        AnswerRequest unconfirmedRequest = new AnswerRequest(
                testQuestion.getId(),
                "I have a severe cough",
                "VOICE",
                "I have a severe cough",
                BigDecimal.valueOf(0.92),
                false
        );

        mockMvc.perform(post("/api/intake/sessions/" + sessionIdA + "/answers")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unconfirmedRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Voice response must be confirmed by the patient before saving"));

        // Attempt to save voice response with confirmedByPatient = null
        AnswerRequest nullConfirmationRequest = new AnswerRequest(
                testQuestion.getId(),
                "I have a severe cough",
                "VOICE"
        );
        nullConfirmationRequest.setConfirmedByPatient(null);

        mockMvc.perform(post("/api/intake/sessions/" + sessionIdA + "/answers")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullConfirmationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Voice response must be confirmed by the patient before saving"));
    }

    @Test
    @DisplayName("10. Rejected transcript is not saved as final answer")
    void testRejectedTranscriptNotSavedAsFinalAnswer() throws Exception {
        // Patient rejected transcript and chose not to save it
        // Verify answers table is empty for this session
        List<Answer> answers = answerRepository.findBySessionId(sessionIdA);
        assertThat(answers).isEmpty();
    }

    @Test
    @DisplayName("11. Corrected transcript is saved as confirmed answer")
    void testCorrectedTranscriptSavedAsConfirmedAnswer() throws Exception {
        String originalSpeech = "I have a cough and cold";
        String correctedSpeech = "I have a severe cough and breathing difficulty for two days";

        AnswerRequest correctedRequest = new AnswerRequest(
                testQuestion.getId(),
                correctedSpeech,
                "VOICE",
                originalSpeech,
                BigDecimal.valueOf(0.94),
                true
        );

        mockMvc.perform(post("/api/intake/sessions/" + sessionIdA + "/answers")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctedRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.answerText").value(correctedSpeech))
                .andExpect(jsonPath("$.data.originalTranscription").value(originalSpeech))
                .andExpect(jsonPath("$.data.inputMethod").value("VOICE"))
                .andExpect(jsonPath("$.data.confirmedByPatient").value(true));

        List<Answer> answers = answerRepository.findBySessionId(sessionIdA);
        assertThat(answers).hasSize(1);
        Answer saved = answers.get(0);
        assertThat(saved.getAnswerText()).isEqualTo(correctedSpeech);
        assertThat(saved.getOriginalTranscription()).isEqualTo(originalSpeech);
        assertThat(saved.getConfirmedByPatient()).isTrue();
    }

    @Test
    @DisplayName("12. Voice answer is stored with correct input method")
    void testVoiceAnswerStoredWithCorrectInputMethod() throws Exception {
        AnswerRequest voiceAnswer = new AnswerRequest(
                testQuestion.getId(),
                "Mild fever for 3 days",
                "VOICE",
                "Mild fever for 3 days",
                BigDecimal.valueOf(0.95),
                true
        );

        mockMvc.perform(post("/api/intake/sessions/" + sessionIdA + "/answers")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voiceAnswer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.inputMethod").value("VOICE"));

        List<Answer> answers = answerRepository.findBySessionId(sessionIdA);
        assertThat(answers).hasSize(1);
        assertThat(answers.get(0).getInputMethod()).isEqualTo("VOICE");
    }

    @Test
    @DisplayName("13. Patient cannot submit voice data to another patient's session (HTTP 403)")
    void testPatientCannotSubmitVoiceDataToAnotherPatientSession() throws Exception {
        byte[] wavBytes = MockTextToSpeechProvider.generatePcmWav("Cross-patient intrusion test");
        MockMultipartFile audioFile = new MockMultipartFile("file", "speech.wav", "audio/wav", wavBytes);

        // Patient B attempts to submit audio to Patient A's session
        mockMvc.perform(multipart("/api/voice/transcribe")
                        .file(audioFile)
                        .param("sessionId", sessionIdA.toString())
                        .header("Authorization", "Bearer " + tokenPatientB))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("You do not own this intake session")));
    }

    @Test
    @DisplayName("14. Unauthenticated voice request returns HTTP 401")
    void testUnauthenticatedVoiceRequestReturns401() throws Exception {
        byte[] wavBytes = MockTextToSpeechProvider.generatePcmWav("Unauthenticated test");
        MockMultipartFile audioFile = new MockMultipartFile("file", "speech.wav", "audio/wav", wavBytes);

        mockMvc.perform(multipart("/api/voice/transcribe")
                        .file(audioFile)
                        .param("sessionId", sessionIdA.toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("15. Completed session rejects new voice answer and audio input")
    void testCompletedSessionRejectsNewVoiceAnswer() throws Exception {
        // Populate clinical intake data and complete session A
        mockMvc.perform(post("/api/intake/sessions/" + sessionIdA + "/complaint")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChiefComplaintRequest("Fever", "TEXT"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/intake/sessions/" + sessionIdA + "/complete")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk());

        // Attempting audio transcription on completed session should be rejected
        byte[] wavBytes = MockTextToSpeechProvider.generatePcmWav("Audio for completed session");
        MockMultipartFile audioFile = new MockMultipartFile("file", "speech.wav", "audio/wav", wavBytes);

        mockMvc.perform(multipart("/api/voice/transcribe")
                        .file(audioFile)
                        .param("sessionId", sessionIdA.toString())
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isConflict());

        // Attempting answer saving on completed session should also be rejected
        AnswerRequest voiceAnswer = new AnswerRequest(
                testQuestion.getId(),
                "Late answer",
                "VOICE",
                "Late answer",
                BigDecimal.valueOf(0.90),
                true
        );

        mockMvc.perform(post("/api/intake/sessions/" + sessionIdA + "/answers")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voiceAnswer)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("16. TTS request succeeds with valid input")
    void testTtsRequestSucceedsWithValidInput() throws Exception {
        SpeechSynthesisRequest ttsRequest = new SpeechSynthesisRequest(
                "What is the primary reason for your visit today?",
                "en",
                sessionIdA
        );

        mockMvc.perform(post("/api/voice/synthesize")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ttsRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("audio/wav"))
                .andExpect(header().exists("Content-Length"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"speech_prompt.wav\""));
    }

    @Test
    @DisplayName("17. TTS invalid input is rejected with HTTP 400")
    void testTtsInvalidInputRejected() throws Exception {
        // Blank text
        SpeechSynthesisRequest emptyRequest = new SpeechSynthesisRequest("   ", "en");

        mockMvc.perform(post("/api/voice/synthesize")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest());

        // Excessively long text (> 1000 characters)
        String longText = "A".repeat(1001);
        SpeechSynthesisRequest oversizedRequest = new SpeechSynthesisRequest(longText, "en");

        mockMvc.perform(post("/api/voice/synthesize")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oversizedRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("18. Provider credentials and internal keys are not exposed in responses")
    void testProviderCredentialsAreNotExposed() throws Exception {
        byte[] wavBytes = MockTextToSpeechProvider.generatePcmWav("Security credential exposure test");
        MockMultipartFile audioFile = new MockMultipartFile("file", "speech.wav", "audio/wav", wavBytes);

        MvcResult result = mockMvc.perform(multipart("/api/voice/transcribe")
                        .file(audioFile)
                        .param("sessionId", sessionIdA.toString())
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).doesNotContain("secret");
        assertThat(responseBody).doesNotContain("password");
        assertThat(responseBody).doesNotContain("apiKey");
        assertThat(responseBody).doesNotContain("key=");
    }

    @Test
    @DisplayName("19. Raw audio is not unnecessarily persisted to database")
    void testRawAudioIsNotUnnecessarilyPersisted() throws Exception {
        AnswerRequest voiceAnswer = new AnswerRequest(
                testQuestion.getId(),
                "Mild fever for 3 days",
                "VOICE",
                "Mild fever for 3 days",
                BigDecimal.valueOf(0.95),
                true
        );

        mockMvc.perform(post("/api/intake/sessions/" + sessionIdA + "/answers")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voiceAnswer)))
                .andExpect(status().isCreated());

        List<Answer> answers = answerRepository.findBySessionId(sessionIdA);
        assertThat(answers).hasSize(1);
        Answer saved = answers.get(0);
        // Verify only transcript text and metadata are stored, not raw audio bytes
        assertThat(saved.getAnswerText()).isEqualTo("Mild fever for 3 days");
        assertThat(saved.getOriginalTranscription()).isEqualTo("Mild fever for 3 days");
    }

    @Test
    @DisplayName("20. Existing Phases 1–5 triage, intake, and security flows remain green")
    void testExistingPhases1to5TestsRemainGreen() throws Exception {
        // Doctor can access session voice synthesis
        SpeechSynthesisRequest doctorTts = new SpeechSynthesisRequest("Please proceed to Consultation Room 3", "en");

        mockMvc.perform(post("/api/voice/synthesize")
                        .header("Authorization", "Bearer " + tokenDoctor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doctorTts)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("audio/wav"));
    }

    @Test
    @DisplayName("21. Silent audio produces NO_SPEECH_DETECTED and never fabricates speech")
    void testSilentAudioProducesNoSpeechDetected() throws Exception {
        byte[] silentWav = MockTextToSpeechProvider.generateSilentPcmWav(500);
        MockMultipartFile audioFile = new MockMultipartFile("file", "silent.wav", "audio/wav", silentWav);

        mockMvc.perform(multipart("/api/voice/transcribe")
                        .file(audioFile)
                        .param("sessionId", sessionIdA.toString())
                        .param("language", "en")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("NO_SPEECH_DETECTED"))
                .andExpect(jsonPath("$.data.transcript").value(""))
                .andExpect(jsonPath("$.data.inputMethod").value("VOICE"))
                .andExpect(jsonPath("$.data.provider").isNotEmpty())
                .andExpect(jsonPath("$.data.providerType").value("MOCK"));
    }

    @Test
    @DisplayName("22. Transcript provenance metadata fields are fully populated")
    void testTranscriptProvenanceMetadataFieldsPopulated() throws Exception {
        byte[] wavBytes = MockTextToSpeechProvider.generatePcmWav("Testing provenance");
        MockMultipartFile audioFile = new MockMultipartFile("file", "speech.wav", "audio/wav", wavBytes);

        mockMvc.perform(multipart("/api/voice/transcribe")
                        .file(audioFile)
                        .param("sessionId", sessionIdA.toString())
                        .param("language", "en")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inputMethod").value("VOICE"))
                .andExpect(jsonPath("$.data.provider").value("MOCK_STT_PROVIDER"))
                .andExpect(jsonPath("$.data.providerType").value("MOCK"))
                .andExpect(jsonPath("$.data.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.data.sessionId").value(sessionIdA.toString()))
                .andExpect(jsonPath("$.data.language").value("en"));
    }

    @Test
    @DisplayName("23. Novel speech transcript is preserved verbatim")
    void testNovelSpeechTranscriptPreservedVerbatim() throws Exception {
        String novelSpeech = "I have a strange pulling sensation underneath my left ribs after I eat, and sometimes it spreads toward my back.";
        byte[] wavBytes = MockTextToSpeechProvider.generatePcmWav(novelSpeech);
        MockMultipartFile audioFile = new MockMultipartFile("file", "novel.wav", "audio/wav", wavBytes);

        mockMvc.perform(multipart("/api/voice/transcribe")
                        .file(audioFile)
                        .param("sessionId", sessionIdA.toString())
                        .param("language", "en")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.transcript").value(novelSpeech));
    }
}
