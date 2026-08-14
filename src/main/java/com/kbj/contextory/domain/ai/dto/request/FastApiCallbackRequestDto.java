package com.kbj.contextory.domain.ai.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FastApiCallbackRequestDto {

    private String jobId;
    private String status; // SUCCESS 또는 FAILED
    private String resultSummary;
    private String errorMessage;
}