package com.aiconverter.controller;

import com.aiconverter.dto.ConversionJobResponse;
import com.aiconverter.entity.ConversionJob;
import com.aiconverter.service.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/convert")
public class ConversionController {

    private final JobService jobService;

    public ConversionController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ConversionJobResponse> convert(@RequestParam("file") MultipartFile file) {
        ConversionJob job = jobService.submit(file);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ConversionJobResponse.from(job));
    }

    @GetMapping("/{jobId}/status")
    public ResponseEntity<ConversionJobResponse> status(@PathVariable String jobId) {
        ConversionJob job = jobService.getStatus(jobId);
        return ResponseEntity.ok(ConversionJobResponse.from(job));
    }
}
