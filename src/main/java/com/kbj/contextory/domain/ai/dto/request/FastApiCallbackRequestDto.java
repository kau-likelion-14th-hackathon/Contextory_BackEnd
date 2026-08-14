package com.kbj.contextory.domain.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FastApiCallbackRequestDto {

    @JsonProperty("job_id")
    private String jobId;

    private String status; // COMPLETED 또는 FAILED

    @JsonProperty("result_summary")
    private String resultSummary; // 또는 JSON 문자열 / 객체

    @JsonProperty("error_message")
    private String errorMessage;
}