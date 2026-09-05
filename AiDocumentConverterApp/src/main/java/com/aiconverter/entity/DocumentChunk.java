package com.aiconverter.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "document_chunk", indexes = {
        @Index(name = "idx_chunk_job_id", columnList = "job_id")
})
@Getter
@Setter
@NoArgsConstructor
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // References ConversionJob.jobId (string), not a JPA relation, to keep
    // the Batch ItemWriter decoupled from a full entity graph fetch per chunk.
    @Column(name = "job_id", nullable = false)
    private String jobId;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;


    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public DocumentChunk(String jobId, Integer chunkIndex, String content) {
        this.jobId = jobId;
        this.chunkIndex = chunkIndex;
        this.content = content;
    }
}
