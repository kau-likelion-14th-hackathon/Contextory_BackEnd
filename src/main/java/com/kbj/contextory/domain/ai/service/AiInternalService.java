package com.kbj.contextory.domain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbj.contextory.domain.ai.dto.request.FastApiAnalysisRequestDto;
import com.kbj.contextory.domain.ai.dto.request.FastApiCallbackRequestDto;
import com.kbj.contextory.domain.ai.dto.response.FastApiAnalysisResponseDto;
import com.kbj.contextory.domain.ai.dto.response.FastApiJobStatusResponseDto;
import com.kbj.contextory.domain.ai.entity.AiAnalysis;
import com.kbj.contextory.domain.ai.entity.AnalysisStatus;
import com.kbj.contextory.domain.ai.repository.AiAnalysisRepository;
import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.client.FastApiClient;
import com.kbj.contextory.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiInternalService {

    private static final String CALLBACK_PATH_TEMPLATE = "/internal/v1/analyses/%d/callback";

    private final FastApiClient fastApiClient;
    private final AiAnalysisRepository aiAnalysisRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${contextory.server.base-url}")
    private String serverBaseUrl;

    public FastApiJobStatusResponseDto getAnalysisStatus(String jobId) {
        log.info("FastAPI 분석 작업 상태 조회 - jobId: {}", jobId);
        return fastApiClient.getJobStatus(jobId);
    }

    @Transactional
    public void processCallback(Long analysisId, FastApiCallbackRequestDto callbackDto) {
        log.info("FastAPI Callback 수신 - analysisId: {}, jobId: {}, status: {}, model: {}",
                analysisId,
                callbackDto.getJobId(),
                callbackDto.getStatus(),
                callbackDto.getModelName());

        // cancel 요청과 callback이 동시에 들어와도 같은 분석 Row를 순차 처리하도록 잠근다.
        AiAnalysis analysis = aiAnalysisRepository.findByIdForUpdate(analysisId)
                .orElseThrow(() -> GeneralException.of(ErrorCode.AI_ANALYSIS_NOT_FOUND));

        if (analysis.getFastapiJobId() != null
                && !analysis.getFastapiJobId().equals(callbackDto.getJobId())) {
            log.error("FastAPI Callback JobId 불일치 - dbJobId: {}, callbackJobId: {}",
                    analysis.getFastapiJobId(),
                    callbackDto.getJobId());
            throw GeneralException.of(ErrorCode.AI_ANALYSIS_JOB_ID_MISMATCH);
        }

        // 중복 callback 및 CANCELED 이후 늦게 도착한 callback은 무시한다.
        if (analysis.isTerminal()) {
            log.info("이미 종료된 분석 Callback 무시 - analysisId: {}, status: {}",
                    analysisId,
                    analysis.getAnalysisStatus());
            return;
        }

        AnalysisStatus status = parseCallbackStatus(callbackDto.getStatus());

        switch (status) {
            case FAILED -> analysis.fail(
                    callbackDto.getJobId(),
                    callbackDto.getModelName(),
                    callbackDto.getErrorMessage()
            );
            case COMPLETED -> {
                String resultJson = serializeResult(callbackDto.getResult());
                analysis.complete(
                        callbackDto.getJobId(),
                        callbackDto.getModelName(),
                        resultJson
                );
                log.info("AI 분석 결과 DB 반영 완료 - analysisId: {}, jobId: {}",
                        analysisId,
                        callbackDto.getJobId());
            }
            default -> throw GeneralException.of(
                    ErrorCode.AI_ANALYSIS_INVALID_CALLBACK_STATUS
            );
        }
    }

    private AnalysisStatus parseCallbackStatus(String status) {
        if (status == null || status.isBlank()) {
            throw GeneralException.of(ErrorCode.AI_ANALYSIS_INVALID_CALLBACK_STATUS);
        }
        try {
            return AnalysisStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw GeneralException.of(ErrorCode.AI_ANALYSIS_INVALID_CALLBACK_STATUS);
        }
    }

    private String serializeResult(Object result) {
        if (result == null) {
            return null;
        }
        if (result instanceof String stringResult) {
            return stringResult;
        }
        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception exception) {
            log.error("AI 분석 결과 JSON 직렬화 실패", exception);
            return result.toString();
        }
    }

    @Transactional
    public FastApiAnalysisResponseDto requestAnalysis(FastApiAnalysisRequestDto requestDto) {
        AiAnalysis analysis = aiAnalysisRepository.findById(requestDto.getAnalysisId())
                .orElseThrow(() -> GeneralException.of(ErrorCode.AI_ANALYSIS_NOT_FOUND));

        String callbackUrl = buildCallbackUrl(requestDto.getAnalysisId());
        FastApiAnalysisRequestDto requestWithCallback = requestDto.withCallbackUrl(callbackUrl);

        FastApiAnalysisResponseDto response = fastApiClient.requestAnalysis(requestWithCallback);

        // PROCESSING 전이 기준은 FastAPI 담당자와 API 계약 확인 후 별도 반영한다.
        analysis.updateFastApiJobId(response.getJobId());

        log.info("FastAPI 분석 요청 접수 - analysisId: {}, jobId: {}, status: {}",
                requestDto.getAnalysisId(),
                response.getJobId(),
                response.getStatus());

        return response;
    }

    private String buildCallbackUrl(Long analysisId) {
        String normalizedBaseUrl = serverBaseUrl.endsWith("/")
                ? serverBaseUrl.substring(0, serverBaseUrl.length() - 1)
                : serverBaseUrl;

        return normalizedBaseUrl + String.format(CALLBACK_PATH_TEMPLATE, analysisId);
    }
}
