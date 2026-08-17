package com.kbj.contextory.domain.ai.dto.response;

import com.kbj.contextory.domain.ai.entity.AiAnalysis;
import com.kbj.contextory.domain.ai.entity.AnalysisStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class AiAnalysisListItemResponse {
    private Long analysisId;
    private Integer prNumber;
    private String analyzedHeadSha;
    private AnalysisStatus analysisStatus;
    private String modelName;
    private RequestedByResponse requestedBy;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;

    public static AiAnalysisListItemResponse of(AiAnalysis analysis, String username) {
        return AiAnalysisListItemResponse.builder()
                .analysisId(analysis.getAnalysisId())
                .prNumber(analysis.getPrNumber())
                .analyzedHeadSha(analysis.getAnalyzedHeadSha())
                .analysisStatus(analysis.getAnalysisStatus())
                .modelName(analysis.getModelName())
                .requestedBy(RequestedByResponse.builder()
                        .userId(analysis.getRequestedBy())
                        .username(username)
                        .build())
                .requestedAt(analysis.getCreatedAt())
                .completedAt(analysis.getCompletedAt())
                .build();
    }

    @Getter
    @Builder
    public static class RequestedByResponse {
        private Long userId;
        private String username;
    }
}
