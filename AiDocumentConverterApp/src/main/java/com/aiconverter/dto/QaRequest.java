package com.aiconverter.dto;

import jakarta.validation.constraints.NotBlank;

public record QaRequest(
        @NotBlank(message = "question must not be blank")
        String question
) {}
