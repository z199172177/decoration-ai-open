package com.jd.decoration.ai.service.tools;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;

public class ApachePdfBoxDocumentParser implements DocumentParser {
    public ApachePdfBoxDocumentParser() {
    }

    @Override
    public Document parse(InputStream inputStream) {
        try (PDDocument pdfDocument = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(pdfDocument);
            return Document.from(text);
        } catch (IOException var5) {
            throw new RuntimeException(var5);
        }
    }
}