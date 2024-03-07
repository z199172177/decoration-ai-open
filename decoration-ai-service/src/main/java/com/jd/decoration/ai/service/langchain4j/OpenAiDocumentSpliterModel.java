package com.jd.decoration.ai.service.langchain4j;

import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.net.Proxy;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class OpenAiDocumentSpliterModel extends OpenAiChatModel implements DocumentSpliterModel {
    private String modelName;
    private String fileType;
    private String filePath;

    @Builder(builderMethodName = "OpenAiDocumentSpliterModel")
    public OpenAiDocumentSpliterModel(String baseUrl,
                                      String apiKey,
                                      String organizationId,
                                      String modelName,
                                      Double temperature,
                                      Double topP,
                                      List<String> stop,
                                      Integer maxTokens,
                                      Double presencePenalty,
                                      Double frequencyPenalty,
                                      Map<String, Integer> logitBias,
                                      String responseFormat,
                                      Integer seed,
                                      String user,
                                      Duration timeout,
                                      Integer maxRetries,
                                      Proxy proxy,
                                      Boolean logRequests,
                                      Boolean logResponses,
                                      Tokenizer tokenizer,
                                      String fileType,
                                      String filePath) {
        super(baseUrl,
                apiKey,
                organizationId,
                modelName,
                temperature,
                topP,
                stop,
                maxTokens,
                presencePenalty,
                frequencyPenalty,
                logitBias,
                responseFormat,
                seed,
                user,
                timeout,
                maxRetries,
                proxy,
                logRequests,
                logResponses,
                tokenizer);

        this.modelName = modelName;
        this.fileType = fileType;
        this.filePath = filePath;
    }
}
