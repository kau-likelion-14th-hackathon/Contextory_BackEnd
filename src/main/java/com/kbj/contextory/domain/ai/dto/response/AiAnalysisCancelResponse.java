package com.kbj.contextory.domain.ai.dto.response;

import com.kbj.contextory.domain.ai.entity.AiAnalysis;
import com.kbj.contextory.domain.ai.entity.AnalysisStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiAnalysisCancelResponse {
    private Long analysisId;
    private AnalysisStatus analysisStatus;

    public static AiAnalysisCancelResponse from(AiAnalysis analysis) {
        return AiAnalysisCancelResponse.builder()
                .analysisId(analysis.getAnalysisId())
                .analysisStatus(analysis.getAnalysisStatus())
                .build();
    }
}
