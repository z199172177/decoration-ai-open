package com.jd.decoration.ai.service.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import com.google.gson.Gson;
import com.jd.decoration.ai.service.dto.ConversationRequest;
import com.jd.decoration.ai.service.dto.OriginGptResponse;
import com.jd.decoration.ai.service.dto.SSEResult;
import com.jd.decoration.ai.service.service.WikiService;
import com.jd.decoration.ai.service.util.SseEmitterUTF8;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static dev.langchain4j.data.message.UserMessage.userMessage;

@Slf4j
@Api(tags = "wikiController")
@RestController
@RequestMapping("/wiki")
public class WikiController {

    @Resource
    private WikiService wikiService;

    @Resource
    private ChatLanguageModel chatLanguageSseModel;

    @Resource
    private StreamingChatLanguageModel openAiStreamingChatModel;

    // 用于保存每个请求对应的 SseEmitter
    private final Map<String, SSEResult> sseEmitterMap = new ConcurrentHashMap<>();

    @Resource
    private Gson gson;

    /**
     * 对话
     *
     * @return SseEmitter
     */
    @ResponseBody
    @CrossOrigin("*")
    @PostMapping(value = "/conversation", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter conversation(HttpServletRequest request, @RequestBody ConversationRequest requestBody) {
        String userPin = request.getRemoteAddr() + "-conversation";
        SSEResult sseResult = getSseResult(userPin);
//
        StreamingChatLanguageModel model = OpenAiStreamingChatModel.builder()
                .baseUrl("")
                .apiKey("")
                .modelName("gpt-3.5-turbo")
                .build();

        SseEmitter sseEmitter = sseResult.getSseEmitter();
        List<ConversationRequest.Message> messageList = requestBody.getMessages();
        List<ChatMessage> llmMessageList = messageList.stream()
                .map(item -> userMessage(MD5.create().digestHex(userPin), item.getContent()))
                .collect(Collectors.toList());

//        List<ChatMessage> messages = asList(
//                systemMessage("You are a very sarcastic assistant"),
//                userMessage("Tell me a joke")
//        );
//        openAiStreamingChatModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
        openAiStreamingChatModel.generate(llmMessageList, new StreamingResponseHandler<AiMessage>() {
            @Override
            public void onNext(String token) {
                try {
                    log.info("token:{}", token);
                    if (StrUtil.isBlank(token)) {
                        return;
                    }

                    OriginGptResponse originGptResponse = OriginGptResponse.buildByToken(token);
                    String data = gson.toJson(originGptResponse);

                    SseEmitter.SseEventBuilder sseEvent = SseEmitter.event().name("msg").data(data);
                    sseEmitter.send(sseEvent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                try {
                    log.info("response:{}", response);
                    SseEmitter.SseEventBuilder sseEvent = SseEmitter.event().name("msg").data("[DONE]");
                    sseEmitter.send(sseEvent);
                    //sseEmitter.complete();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(Throwable error) {
                log.error("Error while receiving answer: {}", error.getMessage(), error);
            }
        });
        return sseEmitter;
    }


    @SneakyThrows
    @ResponseBody
    @CrossOrigin("*")
    @ApiOperation(value = "chat", notes = "聊天")
    @PostMapping(value = "/chat", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter chat(HttpServletRequest request, @RequestBody ConversationRequest requestBody) {
        Assert.notNull(requestBody, "参数不能为空");

        List<ConversationRequest.Message> messages = requestBody.getMessages();
        String question = messages.get(messages.size() - 1).getContent();
        Assert.hasText(question, "参数不能为空");

        String userPin = request.getRemoteAddr() + "-chat";
        SSEResult sseResult = getSseResult(userPin);

        SseEmitter sseEmitter = sseResult.getSseEmitter();
        wikiService.chat(question, sseEmitter);

        return sseEmitter;
    }


    @ResponseBody
    @SneakyThrows
    @CrossOrigin("*")
    @ApiOperation(value = "generatorPost", notes = "生成文档")
    @PostMapping(value = "/generatorPost", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter generatorPost(HttpServletRequest request, @RequestBody ConversationRequest requestBody) {
        Assert.notNull(requestBody, "参数不能为空");

        List<ConversationRequest.Message> messages = requestBody.getMessages();
        String question = messages.get(messages.size() - 1).getContent();
        Assert.hasText(question, "参数不能为空");

        String userPin = request.getRemoteAddr() + "-generatorPost";
        SSEResult sseResult = getSseResult(userPin);
        SseEmitter sseEmitter = sseResult.getSseEmitter();

        wikiService.generatorPostByUser(question, sseEmitter);
        return sseEmitter;
    }

    @ResponseBody
    @SneakyThrows
    @CrossOrigin("*")
    @ApiOperation(value = "recommend", notes = "商品推荐")
    @PostMapping(value = "/recommend", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter recommend(HttpServletRequest request, @RequestBody ConversationRequest requestBody) {
        Assert.notNull(requestBody, "参数不能为空");

        List<ConversationRequest.Message> messages = requestBody.getMessages();
        String question = messages.get(messages.size() - 1).getContent();
        Assert.hasText(question, "参数不能为空");

        String userPin = request.getRemoteAddr() + "-recommend";
        SSEResult sseResult = getSseResult(userPin);
        SseEmitter sseEmitter = sseResult.getSseEmitter();

        wikiService.recommend(question, sseEmitter);

        return sseEmitter;
    }

    @NotNull
    private SSEResult getSseResult(String userPin) {
        SSEResult sseResult = sseEmitterMap.get(userPin);
        if (sseResult == null || sseResult.getSseEmitter() == null) {
            sseResult = new SSEResult(userPin, System.currentTimeMillis(), new SseEmitterUTF8(0L));
            sseEmitterMap.put(userPin, sseResult);
        }

        return sseResult;
    }
}
