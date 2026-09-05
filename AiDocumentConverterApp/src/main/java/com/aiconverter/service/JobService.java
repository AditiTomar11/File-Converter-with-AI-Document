package com.aiconverter.service;

import com.aiconverter.domain.FileFormat;
import com.aiconverter.domain.JobStatus;
import com.aiconverter.entity.ConversionJob;
import com.aiconverter.exception.JobNotFoundException;
import com.aiconverter.repository.ConversionJobRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class JobService {

    private final ConversionJobRepository jobRepository;
    private final JobLauncher jobLauncher;
    private final Job aiDocumentConversionJob;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public JobService(ConversionJobRepository jobRepository,
                       JobLauncher jobLauncher,
                       Job aiDocumentConversionJob) {
        this.jobRepository = jobRepository;
        this.jobLauncher = jobLauncher;
        this.aiDocumentConversionJob = aiDocumentConversionJob;
    }

    public ConversionJob submit(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        FileFormat format = FileFormat.fromFilename(file.getOriginalFilename());
        String jobId = UUID.randomUUID().toString();

        Path storedPath = storeFile(file, jobId);

        ConversionJob job = new ConversionJob();
        job.setJobId(jobId);
        job.setOriginalFilename(file.getOriginalFilename());
        job.setStoredFilePath(storedPath.toString());
        job.setFormat(format);
        job.setStatus(JobStatus.PENDING);
        job = jobRepository.save(job);

        launchBatchJob(jobId);

        return job;
    }

    public ConversionJob getStatus(String jobId) {
        return jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
    }

    private Path storeFile(MultipartFile file, String jobId) {
        try {
            Path dir = Path.of(uploadDir, jobId).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Path target = dir.resolve(file.getOriginalFilename());
            file.transferTo(target.toFile());
            return target;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store uploaded file", e);
        }
    }

    private void launchBatchJob(String jobId) {
        try {
            jobLauncher.run(aiDocumentConversionJob,
                    new JobParametersBuilder()
                            .addString("jobId", jobId)
                            .addLong("startedAt", System.currentTimeMillis())
                            .toJobParameters());
        } catch (Exception e) {
            ConversionJob job = jobRepository.findByJobId(jobId).orElseThrow();
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage("Failed to launch batch job: " + e.getMessage());
            jobRepository.save(job);
        }
    }
}
