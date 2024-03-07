package com.jd.decoration.ai.service.dto;

import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.Response;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
public class CustomStreamingResponseHandler {

    private SseEmitter sseEmitter;

    public CustomStreamingResponseHandler(SseEmitter sseEmitter) {
        this.sseEmitter = sseEmitter;
    }

    public void onNext(String token) {
        try {
            log.info("token:{}", token);
            if (StrUtil.isBlank(token)) {
                return;
            }

            OriginGptResponse originGptResponse = OriginGptResponse.buildByToken(token);
            String data = new Gson().toJson(originGptResponse);

            if (sseEmitter == null) {
                return;
            }
            SseEmitter.SseEventBuilder sseEvent = SseEmitter.event().name("msg").data(data);
            sseEmitter.send(sseEvent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onComplete(Response<AiMessage> response) {
        try {
            log.info("response:{}", response);
            if (sseEmitter == null) {
                return;
            }

            SseEmitter.SseEventBuilder sseEvent = SseEmitter.event().name("msg").data("[DONE]");
            sseEmitter.send(sseEvent);
            //sseEmitter.complete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onError(Throwable error) {
        log.error("Error while receiving answer: {}", error.getMessage(), error);
    }
}
