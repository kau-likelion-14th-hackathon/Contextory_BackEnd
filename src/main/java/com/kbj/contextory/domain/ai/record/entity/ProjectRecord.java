package com.kbj.contextory.domain.ai.record.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "project_record",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_project_record_analysis",
                        columnNames = {"analysis_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_project_record_memory",
                        columnList = "project_id,status,memory_enabled"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "analysis_id", nullable = false)
    private Long analysisId;

    @Column(name = "pr_number")
    private Integer prNumber;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_json", columnDefinition = "json", nullable = false)
    private String contentJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private ProjectRecordStatus status;

    @Column(name = "edited_by")
    private Long editedBy;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "memory_enabled", nullable = false)
    private boolean memoryEnabled;

    @Column(name = "memory_enabled_by")
    private Long memoryEnabledBy;

    @Column(name = "memory_enabled_at")
    private LocalDateTime memoryEnabledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private ProjectRecord(
            Long projectId,
            Long analysisId,
            Integer prNumber,
            String contentJson,
            Long editedBy
    ) {
        this.projectId = projectId;
        this.analysisId = analysisId;
        this.prNumber = prNumber;
        this.contentJson = contentJson;
        this.status = ProjectRecordStatus.DRAFT;
        this.editedBy = editedBy;
        this.memoryEnabled = false;
    }

    public static ProjectRecord createDraft(
            Long projectId,
            Long analysisId,
            Integer prNumber,
            String contentJson,
            Long editedBy
    ) {
        return new ProjectRecord(
                projectId,
                analysisId,
                prNumber,
                contentJson,
                editedBy
        );
    }

    public void updateDraft(String contentJson, Long editedBy) {
        this.contentJson = contentJson;
        this.editedBy = editedBy;
    }

    public void approve(Long approvedBy) {
        this.status = ProjectRecordStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now();
    }

    public void enableMemory(Long userId) {
        if (!this.memoryEnabled) {
            this.memoryEnabled = true;
            this.memoryEnabledBy = userId;
            this.memoryEnabledAt = LocalDateTime.now();
        }
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
