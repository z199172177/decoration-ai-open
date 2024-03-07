package com.jd.decoration.ai.service.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Lang4jGptResponse {
    private String id;
    private String object;
    private Long created;
    private String model;
    private List<Choices> choices;
    private Usage usage;
    @SerializedName("system_fingerprint")
    private String systemFingerprint;

    @Getter
    @Setter
    public static class Message {
        private String role;
        private String content;
    }

    @Getter
    @Setter
    public static class Choices {
        private int index;
        private Message message;
        private String logprobs;
        @SerializedName("finish_reason")
        private String finishReason;
    }


    @Getter
    @Setter
    public static class Usage {
        @SerializedName("prompt_tokens")
        private int promptTokens;
        @SerializedName("completion_tokens")
        private int completionTokens;
        @SerializedName("total_tokens")
        private int totalTokens;
    }
}
