package com.kbj.contextory.domain.ai.dto.response;

import com.kbj.contextory.domain.ai.entity.AiAnalysis;
import com.kbj.contextory.domain.ai.entity.AnalysisStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AiAnalysisDetailResponse {

    private Long analysisId;
    private Long projectId;
    private Integer prNumber;
    private String analyzedHeadSha;
    private AnalysisStatus analysisStatus;
    private String modelName;

    // Notion API 명세의 analysisResult(Object) 구조를 그대로 응답하기 위해
    // Jackson JsonNode가 아닌 일반 Object(Map/List/String 등)로 반환한다.
    private Object analysisResult;

    private String errorMessage;
    private LocalDateTime requestedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public static AiAnalysisDetailResponse of(
            AiAnalysis analysis,
            Object analysisResult
    ) {
        return AiAnalysisDetailResponse.builder()
                .analysisId(analysis.getAnalysisId())
                .projectId(analysis.getProjectId())
                .prNumber(analysis.getPrNumber())
                .analyzedHeadSha(analysis.getAnalyzedHeadSha())
                .analysisStatus(analysis.getAnalysisStatus())
                .modelName(analysis.getModelName())
                .analysisResult(analysisResult)
                .errorMessage(analysis.getErrorMessage())
                .requestedAt(analysis.getCreatedAt())
                .startedAt(analysis.getStartedAt())
                .completedAt(analysis.getCompletedAt())
                .build();
    }
}
