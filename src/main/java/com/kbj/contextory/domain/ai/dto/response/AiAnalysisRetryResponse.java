package com.kbj.contextory.domain.ai.dto.response;

import com.kbj.contextory.domain.ai.entity.AnalysisStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiAnalysisRetryResponse {
    private Long previousAnalysisId;
    private Long newAnalysisId;
    private AnalysisStatus analysisStatus;
}
