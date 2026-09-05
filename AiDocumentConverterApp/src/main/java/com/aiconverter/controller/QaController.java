package com.aiconverter.controller;

import com.aiconverter.domain.JobStatus;
import com.aiconverter.dto.QaRequest;
import com.aiconverter.dto.QaResponse;
import com.aiconverter.entity.ConversionJob;
import com.aiconverter.entity.DocumentChunk;
import com.aiconverter.exception.JobNotReadyException;
import com.aiconverter.repository.DocumentChunkRepository;
import com.aiconverter.service.JobService;
import com.aiconverter.service.OpenAiService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
public class QaController {

    private final JobService jobService;
    private final DocumentChunkRepository chunkRepository;
    private final OpenAiService openAiService;

    @Value("${app.max-context-chunks:8}")
    private int maxContextChunks;

    public QaController(JobService jobService,
                         DocumentChunkRepository chunkRepository,
                         OpenAiService openAiService) {
        this.jobService = jobService;
        this.chunkRepository = chunkRepository;
        this.openAiService = openAiService;
    }

    @PostMapping("/{jobId}/query")
    public ResponseEntity<QaResponse> query(@PathVariable String jobId, @Valid @RequestBody QaRequest request) {
        ConversionJob job = jobService.getStatus(jobId);

        if (job.getStatus() != JobStatus.COMPLETED) {
            throw new JobNotReadyException(jobId, job.getStatus());
        }

        // Naive context assembly: take the first N chunks in document order.
        // No embedding/vector similarity search -- see OpenAiService javadoc.
        List<DocumentChunk> chunks = chunkRepository.findByJobIdOrderByChunkIndexAsc(jobId);
        String context = chunks.stream()
                .limit(maxContextChunks)
                .map(DocumentChunk::getContent)
                .collect(Collectors.joining("\n\n"));

        String answer = openAiService.answerQuestion(context, request.question());

        return ResponseEntity.ok(new QaResponse(jobId, request.question(), answer));
    }
}
