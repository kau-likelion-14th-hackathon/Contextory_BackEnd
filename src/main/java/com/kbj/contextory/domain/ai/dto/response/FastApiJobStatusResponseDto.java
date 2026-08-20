package com.kbj.contextory.domain.ai.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FastApiJobStatusResponseDto {

    private String jobId;
    private Long analysisId;
    private String status;
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;
}