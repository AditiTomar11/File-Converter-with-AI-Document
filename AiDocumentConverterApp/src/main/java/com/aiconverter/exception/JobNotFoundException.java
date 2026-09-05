package com.aiconverter.exception;

public class JobNotFoundException extends RuntimeException {
    public JobNotFoundException(String jobId) {
        super("No conversion job found with id: " + jobId);
    }
}
