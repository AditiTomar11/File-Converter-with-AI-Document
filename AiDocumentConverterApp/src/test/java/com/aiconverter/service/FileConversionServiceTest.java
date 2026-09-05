package com.aiconverter.service;

import com.aiconverter.domain.FileFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileConversionServiceTest {

    private final FileConversionService service = new FileConversionService();

    @Test
    void extractsPlainTextFromTxtFile(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("sample.txt");
        Files.writeString(file, "Hello from a text file.");

        String result = service.extractText(file.toFile(), FileFormat.TXT);

        assertEquals("Hello from a text file.", result);
    }

    @Test
    void extractsPlainTextFromCsvFile(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("sample.csv");
        Files.writeString(file, "name,age\nAditi,22");

        String result = service.extractText(file.toFile(), FileFormat.CSV);

        assertEquals("name,age\nAditi,22", result);
    }

    @Test
    void throwsForMissingFile(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("does-not-exist.txt");
        assertThrows(IllegalStateException.class, () -> service.extractText(missing.toFile(), FileFormat.TXT));
    }
}
