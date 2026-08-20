package com.kbj.contextory.domain.ai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "PR AI 분석 요청")
public class AiAnalysisCreateRequest {
    @NotNull(message = "PR 번호는 필수입니다.")
    @Positive(message = "PR 번호는 1 이상이어야 합니다.")
    @Schema(description = "분석할 GitHub PR 번호", example = "18")
    private Integer prNumber;
}
