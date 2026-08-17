package com.kbj.contextory.domain.ai.dto.response;

import com.kbj.contextory.domain.ai.entity.AiAnalysis;
import com.kbj.contextory.domain.ai.entity.AnalysisStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class AiAnalysisRequestResponse {
    private Long analysisId;
    private Long projectId;
    private Long repositoryId;
    private Integer prNumber;
    private String analyzedHeadSha;
    private AnalysisStatus analysisStatus;
    private LocalDateTime requestedAt;

    public static AiAnalysisRequestResponse from(AiAnalysis analysis) {
        return AiAnalysisRequestResponse.builder()
                .analysisId(analysis.getAnalysisId())
                .projectId(analysis.getProjectId())
                .repositoryId(analysis.getRepositoryId())
                .prNumber(analysis.getPrNumber())
                .analyzedHeadSha(analysis.getAnalyzedHeadSha())
                .analysisStatus(analysis.getAnalysisStatus())
                .requestedAt(analysis.getCreatedAt())
                .build();
    }
}
