package com.kbj.contextory.domain.ai.record.repository;

import com.kbj.contextory.domain.ai.record.entity.ProjectRecord;
import com.kbj.contextory.domain.ai.record.entity.ProjectRecordStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRecordRepository extends JpaRepository<ProjectRecord, Long> {

    Optional<ProjectRecord> findByProjectIdAndAnalysisId(
            Long projectId,
            Long analysisId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select r
            from ProjectRecord r
            where r.projectId = :projectId
              and r.analysisId = :analysisId
            """)
    Optional<ProjectRecord> findByProjectIdAndAnalysisIdForUpdate(
            @Param("projectId") Long projectId,
            @Param("analysisId") Long analysisId
    );

    List<ProjectRecord> findTop20ByProjectIdAndStatusAndMemoryEnabledTrueOrderByApprovedAtDesc(
            Long projectId,
            ProjectRecordStatus status
    );
}
