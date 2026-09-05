package com.aiconverter.service;

import com.aiconverter.domain.FileFormat;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Extracts plain text from supported document formats. This is deliberately
 * a straightforward per-format extraction step -- it is NOT chunk-oriented;
 * chunking happens afterwards in the Spring Batch chunk step
 * (see batch.TextChunkItemReader), which is where large-document handling
 * actually benefits from Batch's chunk-oriented processing model.
 */
@Service
public class FileConversionService {

    public String extractText(File file, FileFormat format) {
        try {
            return switch (format) {
                case PDF -> extractPdf(file);
                case DOCX -> extractDocx(file);
                case CSV, TXT -> extractPlainText(file);
            };
        } catch (IOException e) {
            throw new IllegalStateException("Failed to extract text from file: " + file.getName(), e);
        }
    }

    private String extractPdf(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractDocx(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                sb.append(paragraph.getText()).append(System.lineSeparator());
            }
            return sb.toString();
        }
    }

    private String extractPlainText(File file) throws IOException {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }
}
