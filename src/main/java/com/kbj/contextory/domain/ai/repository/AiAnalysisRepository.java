package com.kbj.contextory.domain.ai.repository;

import com.kbj.contextory.domain.ai.entity.AiAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiAnalysisRepository extends JpaRepository<AiAnalysis, Long> {
    Optional<AiAnalysis> findByFastapiJobId(String fastapiJobId);
}