package com.jd.decoration.ai.api;

public interface WikiOutService {
    /**
     * 问答聊天
     */
    String chart(String question);

    /**
     * 生成文档
     */
    String generatorPost(String about, String title, String keywords);

    /**
     * 生成推荐
     */
    String recommend(String keywords);
}
