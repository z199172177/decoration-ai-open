package com.jd.decoration.ai.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SSEResult {
    private String clientId;
    private Long time;
    private SseEmitter sseEmitter;
}
