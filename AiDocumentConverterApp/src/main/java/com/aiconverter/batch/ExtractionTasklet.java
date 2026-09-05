package com.aiconverter.batch;

import com.aiconverter.domain.JobStatus;
import com.aiconverter.entity.ConversionJob;
import com.aiconverter.repository.ConversionJobRepository;
import com.aiconverter.service.FileConversionService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.File;

/**
 * Step 1 (Tasklet, not chunk-oriented): reads the raw uploaded file off
 * disk and extracts plain text for the given jobId. This step is a plain
 * Tasklet on purpose -- there's exactly one file to read per job execution,
 * so chunk-oriented processing (reader/processor/writer per item) wouldn't
 * add anything here. Chunking is reserved for Step 2, where the extracted
 * text is genuinely split into many items.
 */
public class ExtractionTasklet implements Tasklet {

    private final ConversionJobRepository jobRepository;
    private final FileConversionService fileConversionService;

    public ExtractionTasklet(ConversionJobRepository jobRepository,
                              FileConversionService fileConversionService) {
        this.jobRepository = jobRepository;
        this.fileConversionService = fileConversionService;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        String jobId = chunkContext.getStepContext().getJobParameters().get("jobId").toString();

        ConversionJob job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new IllegalStateException("Job not found for extraction: " + jobId));

        job.setStatus(JobStatus.EXTRACTING);
        jobRepository.save(job);

        String text = fileConversionService.extractText(new File(job.getStoredFilePath()), job.getFormat());

        job.setExtractedText(text);
        job.setStatus(JobStatus.EXTRACTED);
        jobRepository.save(job);

        return RepeatStatus.FINISHED;
    }
}
