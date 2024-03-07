package com.jd.decoration.ai.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.jd.decoration.ai.service.langchain4j.OpenAiDocumentSpliterModel;
import com.jd.decoration.ai.service.langchain4j.OpenAiDocumentSplitter;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiModelName;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.jd.decoration.ai.service.util.JdWikiCrawlerUtil.getFileAry;


@Slf4j
class DocumentBySentenceSplitterExtTest {
    private static OpenAiTokenizer openAiTokenizer = new OpenAiTokenizer(OpenAiModelName.GPT_3_5_TURBO_16K_0613);
    private static String openAiApiKey = "";
    private static String baseUrl = "";

    private static OpenAiEmbeddingModel openAiEmbeddingModel = OpenAiEmbeddingModel.builder()
            .apiKey(openAiApiKey)
            .baseUrl(baseUrl)
            .modelName(OpenAiModelName.TEXT_EMBEDDING_ADA_002)
            .build();

    public static OpenAiDocumentSpliterModel documentSpliterModel = OpenAiDocumentSpliterModel.OpenAiDocumentSpliterModel()
            .apiKey(openAiApiKey)
            .baseUrl(baseUrl)
            .modelName(OpenAiModelName.GPT_3_5_TURBO_16K_0613)
            .timeout(Duration.ofHours(1L))
            .temperature(0.0)
            .build();

    @Test
    void split() {
        // 文档拆分参数，token上限配置
        int maxSegmentSizeInTokens = 200;
        int maxOverlapSizeInTokens = 0;
        OpenAiTokenizer tokenizer = new OpenAiTokenizer(OpenAiModelName.GPT_3_5_TURBO_16K_0613);
        DocumentSplitter splitter = new OpenAiDocumentSplitter(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenizer);

        // String basePath = Jd4applets4zxzs4gwApplication.class.getClassLoader().getResource("").getPath();
        File file = FileUtil.file("/Users/ext.zhoukun9/IdeaProjects/pfinder-view/llmTrail-jd4applets4zxzs4gw/target/classes/doc/wiki-txt/方案设计-方案设计-动线设计.txt");
        System.out.println("file.getPath() = " + file.getPath());
        DocumentParser documentParser = new TextDocumentParser();
        Document document = FileSystemDocumentLoader.loadDocument(file.getPath(), documentParser);
        List<TextSegment> split = splitter.split(document);
        for (TextSegment textSegment : split) {
            System.out.println(textSegment.text());
            System.out.println("-----");
        }
    }


    @Test
    @SneakyThrows
    public void loadDoc2EmbeddingStore() {
        List<String> skipDocName = new ArrayList<>();
        File[] files = getFileAry("classpath:doc/wiki-txt/方案设计-方案设计-动线设计.txt");
        for (File file : files) {
            try {
                String fileName = file.getName();
                if (CollectionUtil.contains(skipDocName, fileName)) {
                    log.info("skip:{}", fileName);
                    continue;
                }

                log.info("current fileName:{}", fileName);
                EmbeddingModel embeddingModel = openAiEmbeddingModel;
                List<TextSegment> segments = getTextSegments(file, openAiApiKey);
                if (CollectionUtil.isEmpty(segments)) {
                    log.error("{} - segments is empty", fileName);
                    return;
                }

                log.info("{} - segments.size:{}", fileName, segments.size());

                EmbeddingStore<TextSegment> embeddingStore = getEmbeddingStore(1536, false);
                Boolean saveRet = saveTextSegment2EmbeddingStore(embeddingModel, segments, embeddingStore);

                log.info("{} - save-ret:{}", fileName, saveRet);
            } catch (Exception e) {
                log.error("error.msg:{}", e.getMessage(), e);
                TimeUnit.SECONDS.sleep(60);
            }
        }
    }


    private static List<TextSegment> getTextSegments(File file, String apiKey) {
        // 加载文档
        DocumentParser documentParser = new TextDocumentParser();
        Document document = FileSystemDocumentLoader.loadDocument(file.getPath(), documentParser);

        //创建一个tokenizer
        //在深度学习的语言模型中，文本通常被分解成一个个标记（tokens），这些标记可以是单词、子词或字符等。
        // Tokenizer 负责将输入文本切分成这些标记，并为每个标记分配相应的索引或嵌入向量。
        Tokenizer tokenizer = null;
        if (StrUtil.isNotBlank(apiKey)) {
            tokenizer = openAiTokenizer;
        }

        // 文档拆分参数，token上限配置
        int maxSegmentSizeInTokens = 200;
        int maxOverlapSizeInTokens = 0;

        documentSpliterModel.setFileType(FileUtil.getType(file));
        documentSpliterModel.setFilePath(FileUtil.getAbsolutePath(file));

        // 创建拆分工具splitter
        DocumentSplitter splitter;
        splitter = new OpenAiDocumentSplitter(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenizer, null, documentSpliterModel);
//        splitter = new DocumentBySentenceSplitterExt(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenizer);
        return splitter.split(document);
    }


    private static Boolean saveTextSegment2EmbeddingStore(EmbeddingModel embeddingModel, List<TextSegment> segments, EmbeddingStore<TextSegment> embeddingStore) {
//        List<List<TextSegment>> split = CollectionUtil.split(segments, 25);
//        split.forEach(item -> {
//        });
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        embeddingStore.addAll(embeddings, segments);
        return Boolean.TRUE;
    }

    private static EmbeddingStore<TextSegment> getEmbeddingStore(int dimension, Boolean dropTableFirst) {
        return null;

    }
}