package com.jd.decoration.ai.service.service;

import com.jd.decoration.ai.vearch.response.VectorSearchResponse;

public interface VearchService {

    /**
     * 文档向量化
     */
    void doc2Vector(String filePath);

    void deleteSpace();

    void spaceCreate();

    void documentUpsert(String text);

    VectorSearchResponse documentSearch(String text, Integer size, Double minScore);

    void renovateImgSpace();

    void renovateSpaceCreate();

    void renovateImgDocumentUpsert(String text);

    void renovateDocumentUpsert(String filePath);
}
