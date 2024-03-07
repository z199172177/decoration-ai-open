package com.jd.decoration.ai.service.langchain4j;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.jd.decoration.ai.service.agent.OpenAiDocumentSplitterAgent;
import com.jd.decoration.ai.service.tools.OpenAiDocumentSplitterTools;
import com.jd.decoration.ai.service.util.SpringBeanUtil;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.document.splitter.HierarchicalDocumentSplitter;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

@Slf4j
public class OpenAiDocumentSplitter extends HierarchicalDocumentSplitter {
    private DocumentSpliterModel spliterModel;

    public OpenAiDocumentSplitter(int maxSegmentSizeInChars, int maxOverlapSizeInChars) {
        super(maxSegmentSizeInChars, maxOverlapSizeInChars);
    }

    public OpenAiDocumentSplitter(int maxSegmentSizeInChars, int maxOverlapSizeInChars, HierarchicalDocumentSplitter subSplitter) {
        super(maxSegmentSizeInChars, maxOverlapSizeInChars, subSplitter);
    }

    public OpenAiDocumentSplitter(int maxSegmentSizeInTokens, int maxOverlapSizeInTokens, Tokenizer tokenizer) {
        super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenizer);
    }

    public OpenAiDocumentSplitter(int maxSegmentSizeInTokens, int maxOverlapSizeInTokens, Tokenizer tokenizer, DocumentSplitter subSplitter) {
        super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenizer, subSplitter);
    }

    public OpenAiDocumentSplitter(int maxSegmentSizeInTokens, int maxOverlapSizeInTokens, Tokenizer tokenizer, DocumentSplitter subSplitter, DocumentSpliterModel spliterModel) {
        super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenizer, subSplitter);
        this.spliterModel = spliterModel;
    }

    @Override
    public String[] split(String text) {
        OpenAiDocumentSplitterTools openAiDocumentSplitterTools = SpringBeanUtil.getBean(OpenAiDocumentSplitterTools.class);

        OpenAiDocumentSpliterModel chatModel = (OpenAiDocumentSpliterModel) spliterModel;
        OpenAiDocumentSplitterAgent openAiDocumentSplitterAgent = AiServices.builder(OpenAiDocumentSplitterAgent.class)
                .chatLanguageModel(chatModel)
                .tools(openAiDocumentSplitterTools)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(50))
                .build();
        String doc = openAiDocumentSplitterAgent.split(chatModel.getFileType(), text, chatModel.getModelName(), super.maxSegmentSize);

        if (StrUtil.isBlank(doc)) {
            return new String[0];
        }

        List<String> fileRet = new ArrayList<>();
        fileRet.add("===docUrl: " + chatModel.getFilePath() + "===");
        fileRet.add(doc);
        fileRet.add("===nextFile===");
        FileUtil.appendUtf8Lines(fileRet, "/Users/ext.zhoukun9/IdeaProjects/decoration-ai/decoration-ai-service/src/main/resources/spliter_ret.txt");

        String segmentStartFlag = ":segment-start:";
        String segmentEndFlag = ":segment-end:";
        int segmentStartIndex = StrUtil.indexOf(doc, segmentStartFlag, 0, true);
        int segmentEndIndex = StrUtil.lastIndexOf(doc, segmentEndFlag, doc.length(), true);

        doc = StrUtil.sub(doc, segmentStartIndex, segmentEndIndex);

        List<String> docSplit = StrUtil.split(doc, segmentStartFlag);
        if (CollectionUtil.isEmpty(docSplit)) {
            return new String[0];
        }

        String[] docSplitAry = docSplit.stream()
                .map(item -> {
                    int segmentEndIndexItem = StrUtil.lastIndexOf(item, segmentEndFlag, item.length(), true);
                    return StrUtil.sub(item, 0, segmentEndIndexItem);
                })
                .filter(item -> StrUtil.isNotBlank(StrUtil.trim(item)))
                .toArray(String[]::new);

        return docSplitAry;
    }

    @Override
    public List<TextSegment> split(Document document) {
        ensureNotNull(document, "document");

        List<TextSegment> segments = new ArrayList<>();
        AtomicInteger index = new AtomicInteger(0);

        String[] parts = split(document.text());
        for (String part : parts) {
            segments.add(createSegment(part, document, index.getAndIncrement()));
        }
        return segments;

    }

    @Override
    public String joinDelimiter() {
        return "";
    }

    @Override
    public DocumentSplitter defaultSubSplitter() {
        return new DocumentBySentenceSplitter(maxSegmentSize, maxOverlapSize, tokenizer);
    }

    private static TextSegment createSegment(String text, Document document, int index) {
        Metadata metadata = document.metadata().copy().add("index", index);
        return TextSegment.from(text, metadata);
    }

}
