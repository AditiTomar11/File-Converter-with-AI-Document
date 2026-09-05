package com.aiconverter.batch;

import com.aiconverter.entity.ConversionJob;
import com.aiconverter.repository.ConversionJobRepository;
import com.aiconverter.util.ChunkUtils;
import org.springframework.batch.item.ItemReader;

import java.util.Iterator;
import java.util.List;

/**
 * Step 2's ItemReader: this is where chunk-oriented processing genuinely
 * earns its keep. It loads the job's full extracted text (set by
 * ExtractionTasklet), splits it into fixed-size text chunks, and hands
 * them back to Spring Batch one at a time so they can be processed and
 * written in configurable commit-interval batches, instead of loading
 * every chunk into memory at once for the write.
 *
 * This bean is @StepScope so `jobId` can be late-bound from JobParameters
 * (#{jobParameters['jobId']}) -- see BatchConfig.
 */
public class TextChunkItemReader implements ItemReader<String> {

    private final ConversionJobRepository jobRepository;
    private final int chunkSize;
    private final String jobId;

    private Iterator<String> chunkIterator;

    public TextChunkItemReader(ConversionJobRepository jobRepository, int chunkSize, String jobId) {
        this.jobRepository = jobRepository;
        this.chunkSize = chunkSize;
        this.jobId = jobId;
    }

    @Override
    public String read() {
        if (chunkIterator == null) {
            chunkIterator = loadChunks().iterator();
        }
        return chunkIterator.hasNext() ? chunkIterator.next() : null;
    }

    private List<String> loadChunks() {
        ConversionJob job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new IllegalStateException("Job not found for chunking: " + jobId));
        return ChunkUtils.splitIntoChunks(job.getExtractedText(), chunkSize);
    }
}
