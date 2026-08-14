package com.kbj.contextory.domain.ai.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_analyses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    private Long analysisId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "repository_id", nullable = false)
    private Long repositoryId;

    @Column(name = "requested_by", nullable = false)
    private Long requestedBy;

    @Column(name = "github_pr_id")
    private Long githubPrId;

    @Column(name = "pr_number", nullable = false)
    private Integer prNumber;

    @Column(name = "analyzed_head_sha", length = 40)
    private String analyzedHeadSha;

    @Column(name = "fastapi_job_id")
    private String fastapiJobId;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_status", length = 50, nullable = false)
    private AnalysisStatus analysisStatus;

    // MySQL/PostgreSQL의 JSON 컬럼 매핑
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_json", columnDefinition = "json")
    private String resultJson;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "credit_used")
    private Integer creditUsed;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.analysisStatus == null) {
            this.analysisStatus = AnalysisStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public AiAnalysis(Long projectId, Long repositoryId, Long requestedBy, Long githubPrId,
                      Integer prNumber, String analyzedHeadSha, Integer creditUsed, String modelName) {
        this.projectId = projectId;
        this.repositoryId = repositoryId;
        this.requestedBy = requestedBy;
        this.githubPrId = githubPrId;
        this.prNumber = prNumber;
        this.analyzedHeadSha = analyzedHeadSha;
        this.creditUsed = creditUsed != null ? creditUsed : 1;
        this.modelName = modelName;
        this.analysisStatus = AnalysisStatus.PENDING;
    }

    // 작업 시작 시 (FastAPI 접수 후 JobId 매핑)
    public void markProcessing(String fastapiJobId) {
        this.fastapiJobId = fastapiJobId;
        this.analysisStatus = AnalysisStatus.PROCESSING;
        this.startedAt = LocalDateTime.now();
    }

    // 성공 처리 (modelName 추가 반영)
    public void complete(String fastapiJobId, String modelName, String resultJson) {
        this.fastapiJobId = fastapiJobId;
        this.modelName = modelName;
        this.analysisStatus = AnalysisStatus.COMPLETED;
        this.resultJson = resultJson;
        this.completedAt = LocalDateTime.now();
    }

    // 실패 처리
    public void fail(String fastapiJobId, String modelName, String errorMessage) {
        this.fastapiJobId = fastapiJobId;
        this.modelName = modelName;
        this.analysisStatus = AnalysisStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }
}