package com.aiconverter.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure text-chunking logic used by the Batch chunk-oriented step
 * (TextChunkItemReader). Kept dependency-free so it can be unit
 * tested without spinning up Spring/Batch context.
 */
public final class ChunkUtils {

    private ChunkUtils() {
    }

    /**
     * Splits text into fixed-size chunks (by character count), trimming
     * whitespace and dropping empty chunks. Never returns null entries.
     */
    public static List<String> splitIntoChunks(String text, int chunkSize) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be positive");
        }
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        int length = text.length();
        for (int start = 0; start < length; start += chunkSize) {
            int end = Math.min(start + chunkSize, length);
            String piece = text.substring(start, end).trim();
            if (!piece.isEmpty()) {
                chunks.add(piece);
            }
        }
        return chunks;
    }
}
