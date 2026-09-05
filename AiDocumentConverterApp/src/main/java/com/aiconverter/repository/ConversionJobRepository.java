package com.aiconverter.repository;

import com.aiconverter.entity.ConversionJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversionJobRepository extends JpaRepository<ConversionJob, Long> {
    Optional<ConversionJob> findByJobId(String jobId);
}
