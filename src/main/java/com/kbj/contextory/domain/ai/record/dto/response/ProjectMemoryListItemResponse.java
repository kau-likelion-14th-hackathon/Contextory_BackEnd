package com.kbj.contextory.domain.ai.record.dto.response;

import com.kbj.contextory.domain.ai.record.entity.ProjectRecord;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProjectMemoryListItemResponse {

    private Long recordId;
    private Long analysisId;
    private Integer prNumber;
    private LocalDateTime approvedAt;
    private Object analysisResult;

    public static ProjectMemoryListItemResponse of(
            ProjectRecord record,
            Object analysisResult
    ) {
        return ProjectMemoryListItemResponse.builder()
                .recordId(record.getRecordId())
                .analysisId(record.getAnalysisId())
                .prNumber(record.getPrNumber())
                .approvedAt(record.getApprovedAt())
                .analysisResult(analysisResult)
                .build();
    }
}
