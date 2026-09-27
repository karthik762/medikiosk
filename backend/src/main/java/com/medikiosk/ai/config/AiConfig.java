package com.medikiosk.ai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.ai.gateway.AiModelGateway;
import com.medikiosk.ai.provider.AiProvider;
import com.medikiosk.ai.provider.LlmAiProvider;
import com.medikiosk.ai.provider.MockAiProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    @ConditionalOnProperty(name = "medikiosk.ai.provider", havingValue = "mock", matchIfMissing = true)
    public AiProvider mockAiProvider(ObjectMapper objectMapper) {
        return new MockAiProvider(objectMapper);
    }

    @Bean
    @ConditionalOnProperty(name = "medikiosk.ai.provider", havingValue = "openai")
    public AiProvider openAiProvider(AiModelGateway aiModelGateway, ObjectMapper objectMapper) {
        return new LlmAiProvider(aiModelGateway, objectMapper);
    }

    @Bean
    @ConditionalOnProperty(name = "medikiosk.ai.provider", havingValue = "openrouter")
    public AiProvider openRouterAiProvider(AiModelGateway aiModelGateway, ObjectMapper objectMapper) {
        return new LlmAiProvider(aiModelGateway, objectMapper);
    }

    @Bean
    @ConditionalOnProperty(name = "medikiosk.ai.provider", havingValue = "llm")
    public AiProvider genericLlmAiProvider(AiModelGateway aiModelGateway, ObjectMapper objectMapper) {
        return new LlmAiProvider(aiModelGateway, objectMapper);
    }
}
