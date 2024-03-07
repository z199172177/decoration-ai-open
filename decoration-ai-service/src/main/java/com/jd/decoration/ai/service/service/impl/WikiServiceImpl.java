package com.jd.decoration.ai.service.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.jd.decoration.ai.service.agent.WikiAgent;
import com.jd.decoration.ai.service.dto.CustomStreamingResponseHandler;
import com.jd.decoration.ai.service.service.WikiService;
import com.jd.decoration.ai.service.tools.WikiTools;
import com.jd.decoration.ai.vearch.VearchEmbeddingStore;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.retriever.Retriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@Slf4j
@Service
public class WikiServiceImpl implements WikiService {

    @Resource
    private OpenAiEmbeddingModel openAiEmbeddingModel;

    @Resource
    private VearchEmbeddingStore vearchEmbeddingStore;

    @Resource
    private ChatLanguageModel openAiChatModel;

    @Resource
    private ChatLanguageModel chatLanguageSseModel;

    @Resource
    private Retriever<TextSegment> retriever;

    @Resource
    private WikiTools wikiTools;

    @Resource
    private StreamingChatLanguageModel openAiStreamingChatModel;

    @Resource
    private Gson gson;

    /**
     * 原生llm
     */
    @Override
    public void chat(String question, SseEmitter sseEmitter) {
        log.info("chart question:{}", question);

        Response<Embedding> questionEmbedding = openAiEmbeddingModel.embed(question);

        int maxResultsRetrieved = 1;
        double minScore = 0.2;
        List<EmbeddingMatch<TextSegment>> relevantEmbeddings = vearchEmbeddingStore.findRelevant(questionEmbedding.content(), maxResultsRetrieved, minScore);
        String information = relevantEmbeddings.stream().map(match -> match.embedded().text()).collect(joining("\n\n"));
        log.info("chart information:{}", information);
        if (StrUtil.isBlank(information)) {
            log.info("未找到本地资料");
            return;
        }

        WikiAgent wikiAgent = AiServices.builder(WikiAgent.class)
                .streamingChatLanguageModel(openAiStreamingChatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .build();

        CustomStreamingResponseHandler responseHandler = new CustomStreamingResponseHandler(sseEmitter);
        wikiAgent.chat(information, question)
                .onNext(responseHandler::onNext)
                .onComplete(responseHandler::onComplete)
                .onError(responseHandler::onError)
                .start();

    }

    /**
     * 根据用户要求生成文章
     */
    @Override
    public void generatorPostByUser(String userRequirement, SseEmitter sseEmitter) {
        log.info("generatorPostByUser param userRequirement:{}", userRequirement);

        WikiAgent wikiAgent = AiServices.builder(WikiAgent.class)
                .chatLanguageModel(openAiChatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .build();

        String jsonResp = wikiAgent.analyzeUserInputForArticle("zhoukun", userRequirement);
        log.info("generatorPostByUser jsonResp:{}", jsonResp);
        JSONObject jsonObject = JSONUtil.parseObj(jsonResp);
        String about = jsonObject.getStr("about");
        String title = jsonObject.getStr("title");
        String keywords = jsonObject.getStr("keywords");

        this.generatorPost(about, title, keywords, sseEmitter);
    }

    /**
     * 根据关键词生成文章
     */
    @Override
    public void generatorPost(String about, String title, String keywords, SseEmitter sseEmitter) {
        log.info("generatorPost param about:{}, title:{}, keywords:{}", about, title, keywords);

        List<TextSegment> question = new ArrayList<>();
        question.add(TextSegment.from(about));
        question.add(TextSegment.from(title));
        question.add(TextSegment.from(keywords));
        Response<List<Embedding>> embeddedResponse = openAiEmbeddingModel.embedAll(question);
        if (embeddedResponse == null || CollectionUtil.isEmpty(embeddedResponse.content())) {
            return;
        }

        int maxResultsRetrieved = 3;
        double minScore = 0.3;
        List<Embedding> embeddingList = embeddedResponse.content();
        List<EmbeddingMatch<TextSegment>> embeddingMatchList = embeddingList.stream()
                .map(item -> vearchEmbeddingStore.findRelevant(item, maxResultsRetrieved, minScore))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        String information = embeddingMatchList.stream()
                .distinct()
                .map(match -> match.embedded().text())
                .collect(joining("\n\n"));

        log.info("chart information:{}", information);
        if (StrUtil.isBlank(information)) {
            return;
        }

        WikiAgent wikiAgent = AiServices.builder(WikiAgent.class)
                .streamingChatLanguageModel(openAiStreamingChatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .build();

        CustomStreamingResponseHandler responseHandler = new CustomStreamingResponseHandler(sseEmitter);
        wikiAgent.generatorPost(about, title, keywords, information)
                .onNext(responseHandler::onNext)
                .onComplete(responseHandler::onComplete)
                .onError(responseHandler::onError)
                .start();
    }

    /**
     * 根据关键词推荐商品
     */
    @Override
    public void recommend(String keywords, SseEmitter sseEmitter) {
        log.info("recommend keywords:{}", keywords);
        WikiAgent wikiAgent = AiServices.builder(WikiAgent.class)
                .streamingChatLanguageModel(openAiStreamingChatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .tools(wikiTools)
                .build();

        CustomStreamingResponseHandler responseHandler = new CustomStreamingResponseHandler(sseEmitter);
        wikiAgent.recommend("zk001", keywords)
                .onNext(responseHandler::onNext)
                .onComplete(responseHandler::onComplete)
                .onError(responseHandler::onError)
                .start();
    }

}
