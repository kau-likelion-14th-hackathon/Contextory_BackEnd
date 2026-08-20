package com.kbj.contextory.domain.ai.record.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "AI 분석 수정 요청")
public class AiAnalysisEditRequest {

    @NotNull
    @Schema(
            description = "사람이 검토·수정한 AI 분석 결과 전체 JSON 객체",
            example = """
                    {
                      "summary": "수정된 작업 요약",
                      "changes": [],
                      "impacts": [],
                      "risks": [],
                      "recommendations": []
                    }
                    """
    )
    private Object analysisResult;
}
