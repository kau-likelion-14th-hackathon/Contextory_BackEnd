package com.kbj.contextory.global.client;

import com.kbj.contextory.domain.ai.dto.request.FastApiAnalysisRequestDto;
import com.kbj.contextory.domain.ai.dto.response.FastApiJobStatusResponseDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class FastApiClient {

    private final RestClient restClient;

    @Value("${ai.fastapi.base-url:http://localhost:8000}")
    private String fastApiBaseUrl;

    @Value("${ai.fastapi.internal-api-key}")
    private String internalApiKey;

    public FastApiClient() {
        this.restClient = RestClient.builder().build();
    }

    @PostConstruct
    public void init() {
        // 💡 주입된 internalApiKey 값 디버깅 로그
        log.info("[FastApiClient Init] Loaded BaseURL: {}", fastApiBaseUrl);
        log.info("[FastApiClient Init] Loaded InternalApiKey: '{}'", internalApiKey);
    }

    /**
     * FastAPI 분석 작업 상태 조회 (GET /internal/v1/analyses/{jobId})
     */
    public FastApiJobStatusResponseDto getJobStatus(String jobId) {
        log.info("FastAPI 상태 조회 요청 - jobId: {}, Key: '{}'", jobId, internalApiKey);

        return restClient.get()
                .uri(fastApiBaseUrl + "/internal/v1/analyses/{jobId}", jobId)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(FastApiJobStatusResponseDto.class);
    }

    /**
     * FastAPI 분석 요청 (POST /internal/v1/analyses)
     */
    public void requestAnalysis(FastApiAnalysisRequestDto requestDto) {
        log.info("FastAPI 분석 요청 - analysisId: {}", requestDto.getAnalysisId());

        restClient.post()
                .uri(fastApiBaseUrl + "/internal/v1/analyses")
                .header("X-Internal-Api-Key", internalApiKey)
                .header("Content-Type", "application/json")
                .body(requestDto)
                .retrieve()
                .toBodilessEntity(); // 응답 바디가 없을 경우
    }
}