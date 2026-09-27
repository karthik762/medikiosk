package com.medikiosk.voice;

import com.medikiosk.voice.provider.MockSpeechToTextProvider;
import com.medikiosk.voice.provider.MockTextToSpeechProvider;
import com.medikiosk.voice.provider.TranscriptionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MockSpeechToTextProviderTest {

    private MockSpeechToTextProvider provider;

    @BeforeEach
    void setUp() {
        provider = new MockSpeechToTextProvider();
    }

    @Test
    @DisplayName("1. Provider identification returns MOCK_STT_PROVIDER")
    void testProviderIdentification() {
        assertEquals("MOCK_STT_PROVIDER", provider.getProviderName());
        assertTrue(provider.isAvailable());
    }

    @Test
    @DisplayName("2. Silence must NEVER become speech — returns NO_SPEECH_DETECTED")
    void testSilenceReturnsNoSpeechDetected() throws Exception {
        byte[] silentWav = MockTextToSpeechProvider.generateSilentPcmWav(500);

        TranscriptionResult result = provider.transcribe(silentWav, "audio/wav", "en", Collections.emptyMap());

        assertNotNull(result);
        assertEquals(TranscriptionResult.Status.NO_SPEECH_DETECTED, result.getStatus());
        assertTrue(result.getTranscript() == null || result.getTranscript().isEmpty(),
                "Transcript must be empty on silence; must never fabricate clinical sentences");
        assertNotEquals("I have had a mild headache and fever for two days.", result.getTranscript());
    }

    @Test
    @DisplayName("3. Empty or null audio input returns NO_SPEECH_DETECTED")
    void testEmptyAudioReturnsNoSpeechDetected() {
        TranscriptionResult result = provider.transcribe(new byte[0], "audio/wav", "en", Collections.emptyMap());
        assertEquals(TranscriptionResult.Status.NO_SPEECH_DETECTED, result.getStatus());

        TranscriptionResult nullResult = provider.transcribe(null, "audio/wav", "en", Collections.emptyMap());
        assertEquals(TranscriptionResult.Status.NO_SPEECH_DETECTED, nullResult.getStatus());
    }

    @Test
    @DisplayName("4. Deterministic test transcript from metadata is honored")
    void testTestTranscriptFromMetadata() throws Exception {
        byte[] audibleWav = MockTextToSpeechProvider.generatePcmWav("Testing voice input");
        Map<String, Object> metadata = Map.of("test_transcript", "I have severe retro-orbital pain");

        TranscriptionResult result = provider.transcribe(audibleWav, "audio/wav", "en", metadata);

        assertNotNull(result);
        assertEquals(TranscriptionResult.Status.SUCCESS, result.getStatus());
        assertEquals("I have severe retro-orbital pain", result.getTranscript());
        assertTrue(result.isMock());
    }

    @Test
    @DisplayName("5. Embedded audio transcript from test audio generator is recognized")
    void testEmbeddedAudioTranscript() throws Exception {
        String novelSpeech = "I have a strange pulling sensation underneath my left ribs after I eat, and sometimes it spreads toward my back.";
        byte[] wavWithEmbeddedText = MockTextToSpeechProvider.generatePcmWav(novelSpeech);

        TranscriptionResult result = provider.transcribe(wavWithEmbeddedText, "audio/wav", "en", Collections.emptyMap());

        assertNotNull(result);
        assertEquals(TranscriptionResult.Status.SUCCESS, result.getStatus());
        assertEquals(novelSpeech, result.getTranscript());
        assertTrue(result.isMock());
    }

    @Test
    @DisplayName("6. Simulated failure returns Status.FAILED")
    void testSimulatedFailure() throws Exception {
        byte[] audibleWav = MockTextToSpeechProvider.generatePcmWav("Testing failure");
        Map<String, Object> metadata = Map.of("simulate_failure", true);

        TranscriptionResult result = provider.transcribe(audibleWav, "audio/wav", "en", metadata);

        assertNotNull(result);
        assertEquals(TranscriptionResult.Status.FAILED, result.getStatus());
        assertTrue(result.isMock());
    }
}
