package com.jd.decoration.ai.service.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ZhipuRequest {
    private String erp;
    private List<Messages> messages;
    private String model;

    @Getter
    @Setter
    public static class Messages {
        private String role;
        private List<Content> content;
    }

    @Getter
    @Setter
    public static class Content {
        private String type;
        private String text;
        @SerializedName("image_url")
        private ImageUrl imageUrl;
    }

    @Getter
    @Setter
    public static class ImageUrl{
        private String url;

        public ImageUrl(String url) {
            this.url = url;
        }
    }

}
