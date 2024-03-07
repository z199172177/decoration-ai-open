package com.jd.decoration.ai.service.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiModelName;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.V;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apikey;

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.chat-model.sse.base-url}")
    private String sseBaseUrl;

    @Value("${langchain4j.open-ai.chat-model.model-name}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.temperature}")
    private Double temperature;

    @Bean
    public OpenAiEmbeddingModel openAiEmbeddingModel(){
        return OpenAiEmbeddingModel.builder()
                .apiKey(apikey)
                .baseUrl(baseUrl)
                .modelName(OpenAiModelName.TEXT_EMBEDDING_ADA_002)
                .build();

    }

    @Bean
    public ChatLanguageModel chatLanguageSseModel(){
        return OpenAiChatModel.builder()
                .apiKey(apikey)
                .baseUrl(sseBaseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .build();

    }

    @Bean
    public StreamingChatLanguageModel openAiStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apikey)
                .baseUrl(sseBaseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .build();

    }


}
