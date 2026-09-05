package com.aiconverter.repository;

import com.aiconverter.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    List<DocumentChunk> findByJobIdOrderByChunkIndexAsc(String jobId);
}
