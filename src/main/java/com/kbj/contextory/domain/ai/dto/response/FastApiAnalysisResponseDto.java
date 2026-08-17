package com.kbj.contextory.domain.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "FastAPI 분석 요청 접수 응답 DTO")
public class FastApiAnalysisResponseDto {

    @JsonProperty("jobId")
    @JsonAlias({"job_id", "jobId"})
    @Schema(description = "FastAPI 작업 ID", example = "rag-job-a12b34c56")
    private String jobId;

    @JsonProperty("status")
    @Schema(description = "작업 상태", example = "PROCESSING")
    private String status;
}