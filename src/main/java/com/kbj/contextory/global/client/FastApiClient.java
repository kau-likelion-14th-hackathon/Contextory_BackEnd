package com.kbj.contextory.global.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbj.contextory.domain.ai.dto.request.FastApiAnalysisRequestDto;
import com.kbj.contextory.domain.ai.dto.response.FastApiAnalysisResponseDto;
import com.kbj.contextory.domain.ai.dto.response.FastApiJobStatusResponseDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class FastApiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.fastapi.base-url:http://localhost:8000}")
    private String fastApiBaseUrl;

    @Value("${ai.fastapi.internal-api-key}")
    private String internalApiKey;

    public FastApiClient() {
        this.objectMapper = new ObjectMapper();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(10000);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @PostConstruct
    public void init() {
        log.info("[FastApiClient Init] BaseURL: {}", fastApiBaseUrl);
    }

    public FastApiJobStatusResponseDto getJobStatus(String jobId) {
        return restClient.get()
                .uri(fastApiBaseUrl + "/internal/v1/analyses/{jobId}", jobId)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(FastApiJobStatusResponseDto.class);
    }

    public FastApiAnalysisResponseDto requestAnalysis(FastApiAnalysisRequestDto requestDto) {
        try {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(requestDto);

            // PR patch 전체 대신 식별용 metadata만 로그에 남긴다.
            log.info(
                    "FastAPI 분석 요청 전송 - analysisId: {}, projectId: {}, repositoryId: {}, repository: {}",
                    requestDto.getAnalysisId(),
                    requestDto.getProjectId(),
                    requestDto.getRepositoryId(),
                    requestDto.getRepositoryFullName()
            );

            return restClient.post()
                    .uri(fastApiBaseUrl + "/internal/v1/analyses")
                    .header("X-Internal-Api-Key", internalApiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .contentLength(jsonBytes.length)
                    .body(jsonBytes)
                    .retrieve()
                    .body(FastApiAnalysisResponseDto.class);
        } catch (Exception exception) {
            log.error("FastAPI 분석 요청 실패 - analysisId: {}, error: {}",
                    requestDto.getAnalysisId(),
                    exception.getMessage(),
                    exception);
            throw new RuntimeException(
                    "FastAPI 서버 통신 중 오류가 발생했습니다: " + exception.getMessage(),
                    exception
            );
        }
    }
}
