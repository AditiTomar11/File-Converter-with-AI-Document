package com.aiconverter.dto;

public record QaResponse(
        String jobId,
        String question,
        String answer
) {}
