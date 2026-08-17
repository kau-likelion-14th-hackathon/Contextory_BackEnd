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

import java.nio.charset.StandardCharsets;

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

        // 표준 SimpleClientHttpRequestFactory 적용 (BufferRequestBody 기본 활성화)
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

    /**
     * FastAPI 분석 작업 상태 조회 (GET /internal/v1/analyses/{jobId})
     */
    public FastApiJobStatusResponseDto getJobStatus(String jobId) {
        return restClient.get()
                .uri(fastApiBaseUrl + "/internal/v1/analyses/{jobId}", jobId)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(FastApiJobStatusResponseDto.class);
    }

    /**
     * FastAPI 분석 요청 (POST /internal/v1/analyses)
     */
    public FastApiAnalysisResponseDto requestAnalysis(FastApiAnalysisRequestDto requestDto) {
        try {
            // DTO를 byte[]로 직접 변환하여 Content-Length와 함께 확실하게 전송
            byte[] jsonBytes = objectMapper.writeValueAsBytes(requestDto);
            log.info("FastAPI 분석 요청 전송 Body: {}", new String(jsonBytes, StandardCharsets.UTF_8));

            return restClient.post()
                    .uri(fastApiBaseUrl + "/internal/v1/analyses")
                    .header("X-Internal-Api-Key", internalApiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .contentLength(jsonBytes.length)
                    .body(jsonBytes)
                    .retrieve()
                    .body(FastApiAnalysisResponseDto.class);

        } catch (Exception e) {
            log.error("FastAPI 분석 요청 실패: {}", e.getMessage(), e);
            throw new RuntimeException("FastAPI 서버 통신 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}