package com.jd.decoration.ai.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ConversationRequest {
    @SerializedName(value = "max_tokens")
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    private List<Message> messages;
    private String model;
    private Integer n;
    private Boolean stream;
    private Double temperature;

    @Getter
    @Setter
    public static class Message{
        String content;
        String role;
    }
}
