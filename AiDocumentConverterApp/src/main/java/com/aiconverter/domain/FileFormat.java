package com.aiconverter.domain;

import com.aiconverter.exception.UnsupportedFileFormatException;

public enum FileFormat {
    PDF, DOCX, CSV, TXT;

    public static FileFormat fromFilename(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new UnsupportedFileFormatException("File has no extension: " + filename);
        }
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toUpperCase();
        try {
            return FileFormat.valueOf(ext);
        } catch (IllegalArgumentException e) {
            throw new UnsupportedFileFormatException("Unsupported file format: ." + ext.toLowerCase());
        }
    }
}
