package com.aiconverter.exception;

import com.aiconverter.domain.JobStatus;

public class JobNotReadyException extends RuntimeException {
    public JobNotReadyException(String jobId, JobStatus currentStatus) {
        super("Job " + jobId + " is not ready for Q&A yet (current status: " + currentStatus + ")");
    }
}
