package com.jd.decoration.ai.service.service;


import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface WikiService  {

    /**
     * 问答聊天
     */
    void chat(String question, SseEmitter sseEmitter);

    /**
     * 生成文档
     */
    void generatorPost(String about, String title, String keywords, SseEmitter sseEmitter);

    /**
     * 根据用户需求生成文档
     */
    void generatorPostByUser(String userRequirement, SseEmitter sseEmitter);

    /**
     * 生成推荐
     */
    void recommend(String keywords, SseEmitter sseEmitter);
}
