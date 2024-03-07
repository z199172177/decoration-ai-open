package com.jd.decoration.ai.service.config;

import com.jd.decoration.ai.vearch.VearchClient;
import com.jd.decoration.ai.vearch.VearchEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.retriever.Retriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class VearchConfig {

    @Value("${vearch.url.master}")
    private String vearchUrlMaster;

    @Value("${vearch.url.router}")
    private String vearchUrlRouter;

    @Value("${vearch.db.name}")
    private String vearchDbName;

    @Value("${vearch.space.name}")
    private String vearchSpaceName;

    @Value("${vearch.field.vector}")
    private String vearchFieldVector;

    @Value("${vearch.field.text}")
    private String vearchFieldText;

    @Bean
    public VearchClient vearchRouterClient() {
        return new VearchClient(vearchUrlRouter, Duration.ofMinutes(5));
    }

    @Bean
    public VearchClient vearchMasterClient() {
        return new VearchClient(vearchUrlMaster, Duration.ofMinutes(5));
    }

    @Bean
    public VearchEmbeddingStore vearchEmbeddingStore() {
        return new VearchEmbeddingStore(vearchUrlRouter, vearchDbName, vearchSpaceName, vearchFieldVector, vearchFieldText, Duration.ofMinutes(1));
    }

    @Bean
    Retriever<TextSegment> retriever(EmbeddingStore<TextSegment> vearchEmbeddingStore, EmbeddingModel openAiEmbeddingModel) {
        // You will need to adjust these parameters to find the optimal setting, which will depend on two main factors:
        // - The nature of your data
        // - The embedding model you are using
        int maxResultsRetrieved = 1;
        double minScore = 0.6;

        return EmbeddingStoreRetriever.from(vearchEmbeddingStore, openAiEmbeddingModel, maxResultsRetrieved, minScore);
    }
}
