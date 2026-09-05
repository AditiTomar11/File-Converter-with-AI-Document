package com.aiconverter.service;

import com.aiconverter.exception.OpenAiServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Thin client for OpenAI's Chat Completions API. Note: this implementation
 * does naive "context stuffing" -- it concatenates the top N stored chunks
 * and passes them in the prompt. There is no embedding-based vector search
 * here; that would be the natural next step for a real RAG pipeline, but is
 * out of scope for this project's current version.
 */
@Service
public class OpenAiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.base-url}")
    private String baseUrl;

    public OpenAiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String answerQuestion(String documentContext, String question) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new OpenAiServiceException("OPENAI_API_KEY is not configured on the server");
        }

        String systemPrompt = "You are a helpful assistant. Answer the user's question using ONLY the "
                + "document context provided below. If the answer is not contained in the context, "
                + "say you don't know based on the document.\n\nDOCUMENT CONTEXT:\n" + documentContext;

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", question)
                ),
                "temperature", 0.2
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);
            return parseAnswer(response.getBody());
        } catch (RestClientException e) {
            throw new OpenAiServiceException("Failed to call OpenAI API: " + e.getMessage(), e);
        }
    }

    private String parseAnswer(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode()) {
                throw new OpenAiServiceException("Unexpected OpenAI response shape: " + rawJson);
            }
            return content.asText().trim();
        } catch (Exception e) {
            throw new OpenAiServiceException("Failed to parse OpenAI response: " + e.getMessage(), e);
        }
    }
}
