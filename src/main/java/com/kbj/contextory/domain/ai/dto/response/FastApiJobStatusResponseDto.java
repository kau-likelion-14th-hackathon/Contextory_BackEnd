package com.kbj.contextory.domain.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FastApiJobStatusResponseDto {

    private String jobId;
    private Long analysisId;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED, CANCELED
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;
}