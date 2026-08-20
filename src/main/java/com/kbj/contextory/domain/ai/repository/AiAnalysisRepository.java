package com.kbj.contextory.domain.ai.repository;

import com.kbj.contextory.domain.ai.entity.AiAnalysis;
import com.kbj.contextory.domain.ai.entity.AnalysisStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AiAnalysisRepository extends JpaRepository<AiAnalysis, Long> {

    Optional<AiAnalysis> findByFastapiJobId(String fastapiJobId);

    Optional<AiAnalysis> findByAnalysisIdAndProjectId(
            Long analysisId,
            Long projectId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AiAnalysis a where a.analysisId = :analysisId")
    Optional<AiAnalysis> findByIdForUpdate(
            @Param("analysisId") Long analysisId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select a
            from AiAnalysis a
            where a.analysisId = :analysisId
              and a.projectId = :projectId
            """)
    Optional<AiAnalysis> findByAnalysisIdAndProjectIdForUpdate(
            @Param("analysisId") Long analysisId,
            @Param("projectId") Long projectId
    );

    Page<AiAnalysis> findAllByProjectId(
            Long projectId,
            Pageable pageable
    );

    Page<AiAnalysis> findAllByProjectIdAndPrNumber(
            Long projectId,
            Integer prNumber,
            Pageable pageable
    );

    Page<AiAnalysis> findAllByProjectIdAndAnalysisStatus(
            Long projectId,
            AnalysisStatus analysisStatus,
            Pageable pageable
    );

    Page<AiAnalysis> findAllByProjectIdAndPrNumberAndAnalysisStatus(
            Long projectId,
            Integer prNumber,
            AnalysisStatus analysisStatus,
            Pageable pageable
    );
}
