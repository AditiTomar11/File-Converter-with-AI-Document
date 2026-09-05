package com.aiconverter.dto;

import com.aiconverter.domain.JobStatus;
import com.aiconverter.entity.ConversionJob;

public record ConversionJobResponse(
        String jobId,
        String originalFilename,
        JobStatus status,
        String errorMessage
) {
    public static ConversionJobResponse from(ConversionJob job) {
        return new ConversionJobResponse(
                job.getJobId(),
                job.getOriginalFilename(),
                job.getStatus(),
                job.getErrorMessage()
        );
    }
}
