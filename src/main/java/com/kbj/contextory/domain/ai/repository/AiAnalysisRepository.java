package com.kbj.contextory.domain.ai.repository;

import com.kbj.contextory.domain.ai.entity.AiAnalysis;
import com.kbj.contextory.domain.ai.entity.AnalysisStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiAnalysisRepository extends JpaRepository<AiAnalysis, Long> {
    Optional<AiAnalysis> findByFastapiJobId(String fastapiJobId);

    Optional<AiAnalysis> findByAnalysisIdAndProjectId(Long analysisId, Long projectId);

    Page<AiAnalysis> findAllByProjectId(Long projectId, Pageable pageable);

    Page<AiAnalysis> findAllByProjectIdAndAnalysisStatus(
            Long projectId,
            AnalysisStatus analysisStatus,
            Pageable pageable
    );
}
