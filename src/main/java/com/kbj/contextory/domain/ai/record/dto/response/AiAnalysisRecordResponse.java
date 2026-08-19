package com.kbj.contextory.domain.ai.record.dto.response;

import com.kbj.contextory.domain.ai.record.entity.ProjectRecord;
import com.kbj.contextory.domain.ai.record.entity.ProjectRecordStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AiAnalysisRecordResponse {

    private Long recordId;
    private Long projectId;
    private Long analysisId;
    private Integer prNumber;

    private ProjectRecordStatus recordStatus;
    private Object analysisResult;

    private Long editedBy;

    private Long approvedBy;
    private LocalDateTime approvedAt;

    private boolean memoryEnabled;
    private Long memoryEnabledBy;
    private LocalDateTime memoryEnabledAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AiAnalysisRecordResponse of(
            ProjectRecord record,
            Object analysisResult
    ) {
        return AiAnalysisRecordResponse.builder()
                .recordId(record.getRecordId())
                .projectId(record.getProjectId())
                .analysisId(record.getAnalysisId())
                .prNumber(record.getPrNumber())
                .recordStatus(record.getStatus())
                .analysisResult(analysisResult)
                .editedBy(record.getEditedBy())
                .approvedBy(record.getApprovedBy())
                .approvedAt(record.getApprovedAt())
                .memoryEnabled(record.isMemoryEnabled())
                .memoryEnabledBy(record.getMemoryEnabledBy())
                .memoryEnabledAt(record.getMemoryEnabledAt())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
