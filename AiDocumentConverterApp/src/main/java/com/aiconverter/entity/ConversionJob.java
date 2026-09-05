package com.aiconverter.entity;

import com.aiconverter.domain.FileFormat;
import com.aiconverter.domain.JobStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "conversion_job")
@Getter
@Setter
@NoArgsConstructor
public class ConversionJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false, unique = true, updatable = false)
    private String jobId;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "stored_file_path", nullable = false)
    private String storedFilePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileFormat format;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    // Extracted plain text kept alongside the job so the Batch chunking step
    // can read it back via the job_id JobParameter (see TextChunkItemReader).

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
