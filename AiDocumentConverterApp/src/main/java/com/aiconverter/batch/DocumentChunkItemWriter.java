package com.aiconverter.batch;

import com.aiconverter.entity.DocumentChunk;
import com.aiconverter.repository.DocumentChunkRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Step 2's ItemWriter: persists a commit-interval's worth of chunks in one
 * batch DB write (saveAll), rather than one INSERT per chunk.
 */
public class DocumentChunkItemWriter implements ItemWriter<String> {

    private final DocumentChunkRepository chunkRepository;
    private final String jobId;
    private int runningIndex = 0;

    public DocumentChunkItemWriter(DocumentChunkRepository chunkRepository, String jobId) {
        this.chunkRepository = chunkRepository;
        this.jobId = jobId;
    }

    @Override
    public void write(Chunk<? extends String> items) {
        List<DocumentChunk> toSave = new ArrayList<>();
        for (String content : items) {
            toSave.add(new DocumentChunk(jobId, runningIndex++, content));
        }
        chunkRepository.saveAll(toSave);
    }
}
