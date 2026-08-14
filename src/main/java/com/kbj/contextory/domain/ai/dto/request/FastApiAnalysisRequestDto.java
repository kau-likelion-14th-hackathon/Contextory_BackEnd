package com.kbj.contextory.domain.ai.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FastApiAnalysisRequestDto {

    private Long analysisId;
    private Long projectId;
    private Integer prNumber;
    private String repositoryName;
    // FastAPI RAG 분석에 필요한 추가 필드가 있다면 여기에 추가
}