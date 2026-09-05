package com.aiconverter.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChunkUtilsTest {

    @Test
    void splitsTextIntoFixedSizeChunks() {
        String text = "a".repeat(25);
        List<String> chunks = ChunkUtils.splitIntoChunks(text, 10);

        assertEquals(3, chunks.size());
        assertEquals(10, chunks.get(0).length());
        assertEquals(10, chunks.get(1).length());
        assertEquals(5, chunks.get(2).length());
    }

    @Test
    void returnsEmptyListForBlankText() {
        assertTrue(ChunkUtils.splitIntoChunks("", 100).isEmpty());
        assertTrue(ChunkUtils.splitIntoChunks("   ", 100).isEmpty());
        assertTrue(ChunkUtils.splitIntoChunks(null, 100).isEmpty());
    }

    @Test
    void dropsEmptyChunksAfterTrimming() {
        String text = "hello" + " ".repeat(20) + "world";
        List<String> chunks = ChunkUtils.splitIntoChunks(text, 5);

        assertTrue(chunks.stream().noneMatch(String::isBlank));
    }

    @Test
    void rejectsNonPositiveChunkSize() {
        assertThrows(IllegalArgumentException.class, () -> ChunkUtils.splitIntoChunks("text", 0));
        assertThrows(IllegalArgumentException.class, () -> ChunkUtils.splitIntoChunks("text", -5));
    }

    @Test
    void singleChunkWhenTextSmallerThanChunkSize() {
        List<String> chunks = ChunkUtils.splitIntoChunks("short text", 1000);
        assertEquals(1, chunks.size());
        assertEquals("short text", chunks.get(0));
    }
}
