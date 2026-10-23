package com.aiconverter.batch;

import com.aiconverter.domain.JobStatus;
import com.aiconverter.repository.ConversionJobRepository;
import com.aiconverter.repository.DocumentChunkRepository;
import com.aiconverter.service.FileConversionService;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
public class BatchConfig {

    @Value("${app.chunk-size:1000}")
    private int chunkSize;

    @Bean
    @Primary
    public JobLauncher asyncJobLauncher(JobRepository jobRepository) {
        TaskExecutorJobLauncher launcher = new TaskExecutorJobLauncher();
        launcher.setJobRepository(jobRepository);
        launcher.setTaskExecutor(new SimpleAsyncTaskExecutor("batch-job-"));
        return launcher;
    }

    @Bean
    public Step extractionStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                ConversionJobRepository conversionJobRepository,
                                FileConversionService fileConversionService) {
        return new StepBuilder("extractionStep", jobRepository)
                .tasklet(new ExtractionTasklet(conversionJobRepository, fileConversionService), transactionManager)
                .build();
    }

    @Bean
    public Step chunkingStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              ConversionJobRepository conversionJobRepository,
                              DocumentChunkRepository documentChunkRepository) {
        return new StepBuilder("chunkingStep", jobRepository)
                .<String, String>chunk(20, transactionManager)
                .reader(chunkItemReader(conversionJobRepository, null))
                .writer(chunkItemWriter(documentChunkRepository, null))
                .listener(new StepStatusListener(conversionJobRepository, JobStatus.CHUNKING))
                .build();
    }

    @Bean
    @org.springframework.context.annotation.Scope(value = "step", proxyMode = org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS)
    public ItemReader<String> chunkItemReader(
            ConversionJobRepository conversionJobRepository,
            @org.springframework.beans.factory.annotation.Value("#{jobParameters['jobId']}") String jobId) {
        return new TextChunkItemReader(conversionJobRepository, chunkSize, jobId);
    }

    @Bean
    @org.springframework.context.annotation.Scope(value = "step", proxyMode = org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS)
    public ItemWriter<String> chunkItemWriter(
            DocumentChunkRepository documentChunkRepository,
            @org.springframework.beans.factory.annotation.Value("#{jobParameters['jobId']}") String jobId) {
        return new DocumentChunkItemWriter(documentChunkRepository, jobId);
    }

    @Bean
    public Job aiDocumentConversionJob(JobRepository jobRepository,
                                        Step extractionStep,
                                        Step chunkingStep,
                                        ConversionJobRepository conversionJobRepository) {
        return new JobBuilder("aiDocumentConversionJob", jobRepository)
                .start(extractionStep)
                .next(chunkingStep)
                .listener(new JobCompletionListener(conversionJobRepository))
                .build();
    }

    /**
     * Flips the ConversionJob's status to COMPLETED/FAILED once the whole
     * Batch job finishes. The intermediate CHUNKING transition is recorded by
     * StepStatusListener on the chunking step, so the status only ever moves
     * forward (PENDING -> EXTRACTING -> EXTRACTED -> CHUNKING -> COMPLETED).
     */
    static class JobCompletionListener implements org.springframework.batch.core.JobExecutionListener {

        private final ConversionJobRepository conversionJobRepository;

        JobCompletionListener(ConversionJobRepository conversionJobRepository) {
            this.conversionJobRepository = conversionJobRepository;
        }

        @Override
        public void afterJob(org.springframework.batch.core.JobExecution jobExecution) {
            String jobId = jobExecution.getJobParameters().getString("jobId");
            conversionJobRepository.findByJobId(jobId).ifPresent(job -> {
                if (jobExecution.getStatus().isUnsuccessful()) {
                    job.setStatus(JobStatus.FAILED);
                    job.setErrorMessage("Batch job failed: " + jobExecution.getExitStatus().getExitDescription());
                } else {
                    job.setStatus(JobStatus.COMPLETED);
                }
                conversionJobRepository.save(job);
            });
        }
    }

    /**
     * Marks the job with a given status right before a step runs. Used to
     * record the CHUNKING transition at the start of chunkingStep.
     */
    static class StepStatusListener implements org.springframework.batch.core.StepExecutionListener {

        private final ConversionJobRepository conversionJobRepository;
        private final JobStatus status;

        StepStatusListener(ConversionJobRepository conversionJobRepository, JobStatus status) {
            this.conversionJobRepository = conversionJobRepository;
            this.status = status;
        }

        @Override
        public void beforeStep(org.springframework.batch.core.StepExecution stepExecution) {
            String jobId = stepExecution.getJobParameters().getString("jobId");
            conversionJobRepository.findByJobId(jobId).ifPresent(job -> {
                job.setStatus(status);
                conversionJobRepository.save(job);
            });
        }
    }
}
