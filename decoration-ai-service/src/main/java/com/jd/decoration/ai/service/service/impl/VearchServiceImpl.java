package com.jd.decoration.ai.service.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jd.decoration.ai.service.langchain4j.OpenAiDocumentSpliterModel;
import com.jd.decoration.ai.service.langchain4j.OpenAiDocumentSplitter;
import com.jd.decoration.ai.service.service.VearchService;
import com.jd.decoration.ai.vearch.VearchClient;
import com.jd.decoration.ai.vearch.VearchEmbeddingStore;
import com.jd.decoration.ai.vearch.request.CreateSpaceRequest;
import com.jd.decoration.ai.vearch.request.VectorSearchRequest;
import com.jd.decoration.ai.vearch.response.VectorSearchResponse;
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
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.internal.Utils.randomUUID;

@Slf4j
@Service
public class VearchServiceImpl implements VearchService {
    @Resource
    private VearchClient vearchRouterClient;

    @Resource
    private VearchClient vearchMasterClient;

    @Resource
    private VearchEmbeddingStore vearchEmbeddingStore;

    @Resource
    private Gson gson;

    @Value("${vearch.db.name}")
    private String vearchDbName;

    @Value("${vearch.space.name}")
    private String vearchSpaceName;

    @Value("${vearch.field.vector}")
    private String vearchFieldVector;

    @Value("${vearch.field.text}")
    private String vearchFieldText;

    @Value("${vearch.renovate.space.name}")
    private String vearchRenovateSpaceName;

    @Value("${vearch.renovate.imgSpace.name}")
    private String vearchRenovateImgSpaceName;

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
            .modelName(OpenAiModelName.GPT_3_5_TURBO_16K)
            .fileType("HTML")
            .timeout(Duration.ofHours(1L))
            .temperature(0.1)
            .build();


    @Override
    public void doc2Vector(String filePath) {
        File[] files = getFileAry(filePath);
        if (ArrayUtil.isEmpty(files)) {
            log.error("doc2Vector files is empty");
            return;
        }

        for (File file : files) {
            try {
                String fileName = file.getName();
                log.info("current fileName:{}", fileName);

                int maxSegmentSizeInTokens = 200;
                int maxOverlapSizeInTokens = 0;
                List<TextSegment> segments = getTextSegments(file, maxSegmentSizeInTokens, maxOverlapSizeInTokens, openAiTokenizer);
                if (CollectionUtil.isEmpty(segments)) {
                    log.error("{} - segments is empty", fileName);
                    return;
                }

                log.info("{} - segments.size:{}", fileName, segments.size());

                EmbeddingModel embeddingModel = openAiEmbeddingModel;
                EmbeddingStore<TextSegment> embeddingStore = vearchEmbeddingStore;
                Boolean saveRet = saveTextSegment2EmbeddingStore(embeddingModel, segments, embeddingStore);

                log.info("{} - save-ret:{}", fileName, saveRet);
            } catch (Exception e) {
                log.error("error.msg:{}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void deleteSpace() {
        vearchMasterClient.deleteSpace(vearchDbName, vearchSpaceName);
    }

    @Override
    public void spaceCreate() {
        CreateSpaceRequest.Engine engine = new CreateSpaceRequest.Engine();
        engine.setIndexSize(1);
        engine.setIdType("String");
        engine.setRetrievalType("HNSW");

        CreateSpaceRequest.Field textField = new CreateSpaceRequest.Field();
        textField.setType("keyword");

        CreateSpaceRequest.Field vectorField = new CreateSpaceRequest.Field();
        vectorField.setType("vector");
        vectorField.setDimension(1536);
        vectorField.setIndex(true);

        Map<String, CreateSpaceRequest.Field> properties = new HashMap<>();
        properties.put(vearchFieldText, textField);
        properties.put(vearchFieldVector, vectorField);

        CreateSpaceRequest request = new CreateSpaceRequest();
        request.setName(vearchSpaceName);
        request.setPartitionNum(1);
        request.setReplicaNum(3);
        request.setEngine(engine);
        request.setProperties(properties);

        log.info("spaceCreate request:{}", gson.toJson(request));

        vearchMasterClient.spaceCreate(vearchDbName, request);
    }

    @Override
    public void documentUpsert(String text) {
        Response<Embedding> embeddingResponse = openAiEmbeddingModel.embed(text);
        Embedding embedding = embeddingResponse.content();

        Map<String, List<Float>> featureMap = new HashMap<>();
        featureMap.put("feature", embedding.vectorAsList());

        Map<String, Object> body = new HashMap<>();
        body.put(this.vearchFieldVector, featureMap);
        body.put(this.vearchFieldText, text);

        String id = randomUUID();
        vearchRouterClient.documentAdd(vearchDbName, vearchSpaceName, id, body);
    }

    @Override
    public VectorSearchResponse documentSearch(String text, Integer size, Double minScore) {
        Response<Embedding> embeddingResponse = openAiEmbeddingModel.embed(text);
        Embedding embedding = embeddingResponse.content();

        VectorSearchRequest.Vector vector = new VectorSearchRequest.Vector();
        vector.setField(this.vearchFieldVector);
        vector.setMinScore(minScore);
        vector.setFeature(embedding.vectorAsList());

        List<VectorSearchRequest.Vector> vectorList = new ArrayList<>();
        vectorList.add(vector);

        VectorSearchRequest.Query query = new VectorSearchRequest.Query();
        query.setSum(vectorList);

        VectorSearchRequest request = new VectorSearchRequest();
        request.setQuery(query);
        request.setSize(size);

        JsonObject ret = vearchRouterClient.vectorSearch(this.vearchDbName, this.vearchSpaceName, request);
        return gson.fromJson(ret, VectorSearchResponse.class);
    }

    @Override
    public void renovateImgSpace() {
        CreateSpaceRequest.Engine engine = new CreateSpaceRequest.Engine();
        engine.setIndexSize(1);
        engine.setIdType("String");
        engine.setRetrievalType("HNSW");

        CreateSpaceRequest.Field textField = new CreateSpaceRequest.Field();
        textField.setType("keyword");

        CreateSpaceRequest.Field vectorField = new CreateSpaceRequest.Field();
        vectorField.setType("vector");
        vectorField.setDimension(1536);
        vectorField.setIndex(true);

        Map<String, CreateSpaceRequest.Field> properties = new HashMap<>();
        properties.put(vearchFieldText, textField);
        properties.put(vearchFieldVector, vectorField);

        CreateSpaceRequest request = new CreateSpaceRequest();
        request.setName(vearchRenovateImgSpaceName);
        request.setPartitionNum(1);
        request.setReplicaNum(3);
        request.setEngine(engine);
        request.setProperties(properties);

        log.info("renovateImgSpace request:{}", gson.toJson(request));

        vearchMasterClient.spaceCreate(vearchDbName, request);
    }

    @Override
    public void renovateSpaceCreate() {
        CreateSpaceRequest.Engine engine = new CreateSpaceRequest.Engine();
        engine.setIndexSize(1);
        engine.setIdType("String");
        engine.setRetrievalType("HNSW");

        CreateSpaceRequest.Field textField = new CreateSpaceRequest.Field();
        textField.setType("keyword");

        CreateSpaceRequest.Field vectorField = new CreateSpaceRequest.Field();
        vectorField.setType("vector");
        vectorField.setDimension(1536);
        vectorField.setIndex(true);

        Map<String, CreateSpaceRequest.Field> properties = new HashMap<>();
        properties.put(vearchFieldText, textField);
        properties.put(vearchFieldVector, vectorField);

        CreateSpaceRequest request = new CreateSpaceRequest();
        request.setName(vearchRenovateSpaceName);
        request.setPartitionNum(1);
        request.setReplicaNum(3);
        request.setEngine(engine);
        request.setProperties(properties);

        log.info("renovateSpaceCreate request:{}", gson.toJson(request));

        vearchMasterClient.spaceCreate(vearchDbName, request);
    }

    @Override
    public void renovateImgDocumentUpsert(String text) {

    }

    @Override
    public void renovateDocumentUpsert(String filePath) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(FileUtil.getInputStream(filePath), StandardCharsets.UTF_8))) {
            StringBuilder textBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                if (StrUtil.isBlank(line)) {
                    continue;
                }

                if (!StrUtil.equals(line, "======page======")) {
                    textBuilder.append(line).append("\n");
                    continue;
                }

                String text = textBuilder.toString();
                if (StrUtil.isBlank(text)) {
                    continue;
                }

                //向量化
                Response<Embedding> embeddingResponse = openAiEmbeddingModel.embed(text);
                Embedding embedding = embeddingResponse.content();
                Map<String, List<Float>> featureMap = new HashMap<>();
                featureMap.put("feature", embedding.vectorAsList());

                Map<String, Object> body = new HashMap<>();
                body.put(this.vearchFieldVector, featureMap);
                body.put(this.vearchFieldText, text);
                String id = randomUUID();

                vearchRouterClient.documentAdd(vearchDbName, vearchRenovateSpaceName, id, body);

                textBuilder = new StringBuilder();
            }
        } catch (Exception e) {
            log.error("renovateDocumentUpsert errMsg:{}", e.getMessage(), e);
        }
    }

    private static List<TextSegment> getTextSegments(File file, int maxSegmentSizeInTokens, int maxOverlapSizeInTokens, Tokenizer tokenizer) {
        DocumentParser documentParser = new TextDocumentParser();
        Document document = FileSystemDocumentLoader.loadDocument(file.getPath(), documentParser);

        documentSpliterModel.setFileType(FileUtil.getType(file));
        documentSpliterModel.setFilePath(FileUtil.getAbsolutePath(file));
        DocumentSplitter splitter = new OpenAiDocumentSplitter(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenizer, null, documentSpliterModel);
        return splitter.split(document);
    }

    private static Boolean saveTextSegment2EmbeddingStore(EmbeddingModel embeddingModel, List<TextSegment> segments, EmbeddingStore<TextSegment> embeddingStore) {
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        embeddingStore.addAll(embeddings, segments);
        return Boolean.TRUE;
    }

    @SneakyThrows
    private File[] getFileAry(String path) {
        File file = getFile(path);
        if (file == null) {
            log.error("找不到文件");
            return new File[0];
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (ArrayUtil.isEmpty(files)) {
                log.error("文件目录是空的");
                return new File[0];
            }
            return files;
        }

        return new File[]{file};
    }

    /**
     * 获取要加载的文件
     */
    private File getFile(String filePath) {
        try {
            return ResourceUtils.getFile(filePath);
//            if (jdSsrfCheck(filePath)) {
//                return ResourceUtils.getFile(filePath);
//            } else {
//                return null;
//            }
        } catch (Exception e) {
            log.error("getFile errMsg:{}", e.getMessage(), e);
            return null;
        }
    }

    private boolean jdSsrfCheck(String url) throws MalformedURLException {
        URL urlObj = new URL(url);
        //定义请求协议白名单列表
        String[] allowProtocols = new String[]{"http", "https"};
        //定义请求域名白名单列表，根据业务需求进行配置
        String[] allowDomains = new String[]{"www.jd.com"};
        //定义请求端口白名单列表
        int[] allowPorts = new int[]{80, 443};
        boolean ssrfCheck = false, protocolCheck = false, domianCheck = false;

        // 首先进行协议校验，若协议校验不通过，SSRF校验不通过
        String protocol = urlObj.getProtocol();
        for (String item : allowProtocols) {
            if (protocol.equals(item)) {
                protocolCheck = true;
                break;
            }
        }
        // 协议校验通过后，再进行域名校验，反之不进行域名校验，SSRF校验不通过
        if (protocolCheck) {
            String host = urlObj.getHost();
            for (String domain : allowDomains) {
                if (domain.equals(host)) {
                    domianCheck = true;
                    break;
                }
            }
        }
        //域名校验通过后，再进行端口校验，反之不进行端口校验，SSRF校验不通过
        if (domianCheck) {
            int port = urlObj.getPort();
            if (port == -1) {
                port = 80;
            }
            for (Integer item : allowPorts) {
                if (item == port) {
                    ssrfCheck = true;
                    break;
                }
            }
        }
        if (ssrfCheck) {
            return true;
        } else {
            return false;
        }
    }

}
