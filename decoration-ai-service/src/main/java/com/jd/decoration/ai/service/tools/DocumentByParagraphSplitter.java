package com.jd.decoration.ai.service.tools;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.document.splitter.HierarchicalDocumentSplitter;
import dev.langchain4j.model.Tokenizer;

public class DocumentByParagraphSplitter extends HierarchicalDocumentSplitter {

    public DocumentByParagraphSplitter(int maxSegmentSizeInChars,
                                       int maxOverlapSizeInChars) {
        super(maxSegmentSizeInChars, maxOverlapSizeInChars, null, null);
    }

    public DocumentByParagraphSplitter(int maxSegmentSizeInChars,
                                       int maxOverlapSizeInChars,
                                       DocumentSplitter subSplitter) {
        super(maxSegmentSizeInChars, maxOverlapSizeInChars, null, subSplitter);
    }

    public DocumentByParagraphSplitter(int maxSegmentSizeInTokens,
                                       int maxOverlapSizeInTokens,
                                       Tokenizer tokenizer) {
        super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenizer, null);
    }

    public DocumentByParagraphSplitter(int maxSegmentSizeInTokens,
                                       int maxOverlapSizeInTokens,
                                       Tokenizer tokenizer,
                                       DocumentSplitter subSplitter) {
        super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenizer, subSplitter);
    }

    @Override
    public String[] split(String text) {
        return text.split("\\s*\\R\\s*\\R\\s*"); // additional whitespaces are ignored
    }

    @Override
    public String joinDelimiter() {
        return "\n\n";
    }

    @Override
    protected DocumentSplitter defaultSubSplitter() {
        return new DocumentBySentenceSplitter(maxSegmentSize, maxOverlapSize, tokenizer);
    }
}