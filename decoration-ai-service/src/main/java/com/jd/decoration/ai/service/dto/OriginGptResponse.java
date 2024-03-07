package com.jd.decoration.ai.service.dto;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OriginGptResponse {
    private String id;
    private String object;
    private Long created;
    private String model;
    @SerializedName("system_fingerprint")
    private String system_fingerprint;
    private List<Choices> choices;

    @Getter
    @Setter
    public static class Message {
        private String role;
        private String content;
    }

    @Getter
    @Setter
    public static class Choices {
        private Integer index;
        private Message delta;
        private String logprobs;
        @SerializedName("finish_reason")
        private String finish_reason;

    }

    @Getter
    @Setter
    public static class Usage {
        @SerializedName("prompt_tokens")
        private Integer prompt_tokens;
        private Integer completion_tokens;
        private Integer total_tokens;
    }

    public static OriginGptResponse buildByToken(String token) {
        Message delta = new Message();
        delta.setContent(token);

        Choices choice = new Choices();
        choice.setDelta(delta);

        List<Choices> choiceList = new ArrayList<>();
        choiceList.add(choice);

        OriginGptResponse response = new OriginGptResponse();
        response.setChoices(choiceList);

        return response;
    }
}
