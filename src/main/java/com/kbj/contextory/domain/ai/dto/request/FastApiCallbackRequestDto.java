package com.kbj.contextory.domain.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FastApiCallbackRequestDto {

    @JsonProperty("job_id")
    @JsonAlias({"jobId"})
    private String jobId;

    private String status;

    @JsonProperty("model_name")
    @JsonAlias({"modelName"})
    private String modelName;

    // JsonNode -> Object로 변경 (Jackson 역직렬화 에러 해결)
    @JsonProperty("result")
    @JsonAlias({"result_summary", "resultSummary"})
    private Object result;

    @JsonProperty("error_message")
    @JsonAlias({"errorMessage"})
    private String errorMessage;
}