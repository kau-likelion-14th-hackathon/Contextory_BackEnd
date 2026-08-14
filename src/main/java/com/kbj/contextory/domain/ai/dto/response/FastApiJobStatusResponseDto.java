package com.kbj.contextory.domain.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("job_id")
    private String jobId;

    @JsonProperty("analysis_id")
    private Long analysisId;

    private String status; // PENDING, PROCESSING, COMPLETED, FAILED, CANCELED

    @JsonProperty("started_at")
    private OffsetDateTime startedAt;

    @JsonProperty("completed_at")
    private OffsetDateTime completedAt;
}