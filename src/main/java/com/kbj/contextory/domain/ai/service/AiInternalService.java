package com.kbj.contextory.domain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbj.contextory.domain.ai.dto.request.FastApiAnalysisRequestDto;
import com.kbj.contextory.domain.ai.dto.request.FastApiCallbackRequestDto;
import com.kbj.contextory.domain.ai.dto.response.FastApiAnalysisResponseDto;
import com.kbj.contextory.domain.ai.dto.response.FastApiJobStatusResponseDto;
import com.kbj.contextory.domain.ai.entity.AiAnalysis;
import com.kbj.contextory.domain.ai.entity.AnalysisStatus;
import com.kbj.contextory.domain.ai.record.entity.ProjectRecordStatus;
import com.kbj.contextory.domain.ai.record.repository.ProjectRecordRepository;
import com.kbj.contextory.domain.ai.repository.AiAnalysisRepository;
import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.client.FastApiClient;
import com.kbj.contextory.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiInternalService {

    private static final String CALLBACK_PATH_TEMPLATE =
            "/internal/v1/analyses/%d/callback";

    private final FastApiClient fastApiClient;
    private final AiAnalysisRepository aiAnalysisRepository;
    private final ProjectRecordRepository projectRecordRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${contextory.server.base-url}")
    private String serverBaseUrl;

    public FastApiJobStatusResponseDto getAnalysisStatus(String jobId) {
        log.info("FastAPI 분석 작업 상태 조회 - jobId: {}", jobId);
        return fastApiClient.getJobStatus(jobId);
    }

    @Transactional
    public void processCallback(
            Long analysisId,
            FastApiCallbackRequestDto callbackDto
    ) {
        log.info(
                "FastAPI Callback 수신 - analysisId: {}, jobId: {}, status: {}, model: {}",
                analysisId,
                callbackDto.getJobId(),
                callbackDto.getStatus(),
                callbackDto.getModelName()
        );

        AiAnalysis analysis = aiAnalysisRepository
                .findByIdForUpdate(analysisId)
                .orElseThrow(() -> GeneralException.of(
                        ErrorCode.AI_ANALYSIS_NOT_FOUND
                ));

        if (analysis.getFastapiJobId() != null
                && !analysis.getFastapiJobId().equals(callbackDto.getJobId())) {
            log.error(
                    "FastAPI Callback JobId 불일치 - dbJobId: {}, callbackJobId: {}",
                    analysis.getFastapiJobId(),
                    callbackDto.getJobId()
            );
            throw GeneralException.of(
                    ErrorCode.AI_ANALYSIS_JOB_ID_MISMATCH
            );
        }

        if (analysis.isTerminal()) {
            log.info(
                    "이미 종료된 분석 Callback 무시 - analysisId: {}, status: {}",
                    analysisId,
                    analysis.getAnalysisStatus()
            );
            return;
        }

        AnalysisStatus status = parseCallbackStatus(
                callbackDto.getStatus()
        );

        switch (status) {
            case FAILED -> analysis.fail(
                    callbackDto.getJobId(),
                    callbackDto.getModelName(),
                    callbackDto.getErrorMessage()
            );

            case COMPLETED -> {
                String resultJson = serializeResult(
                        callbackDto.getResult()
                );

                analysis.complete(
                        callbackDto.getJobId(),
                        callbackDto.getModelName(),
                        resultJson
                );

                log.info(
                        "AI 분석 결과 DB 반영 완료 - analysisId: {}, jobId: {}",
                        analysisId,
                        callbackDto.getJobId()
                );
            }

            default -> throw GeneralException.of(
                    ErrorCode.AI_ANALYSIS_INVALID_CALLBACK_STATUS
            );
        }
    }

    private AnalysisStatus parseCallbackStatus(String status) {
        if (status == null || status.isBlank()) {
            throw GeneralException.of(
                    ErrorCode.AI_ANALYSIS_INVALID_CALLBACK_STATUS
            );
        }

        try {
            return AnalysisStatus.valueOf(
                    status.trim().toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            throw GeneralException.of(
                    ErrorCode.AI_ANALYSIS_INVALID_CALLBACK_STATUS
            );
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
    public FastApiAnalysisResponseDto requestAnalysis(
            FastApiAnalysisRequestDto requestDto
    ) {
        AiAnalysis analysis = aiAnalysisRepository
                .findById(requestDto.getAnalysisId())
                .orElseThrow(() -> GeneralException.of(
                        ErrorCode.AI_ANALYSIS_NOT_FOUND
                ));

        List<FastApiAnalysisRequestDto.ProjectMemoryDto> memories =
                loadProjectMemories(requestDto.getProjectId());

        FastApiAnalysisRequestDto requestWithMemories =
                requestDto.withProjectMemories(memories);

        String callbackUrl = buildCallbackUrl(
                requestDto.getAnalysisId()
        );

        FastApiAnalysisRequestDto requestWithCallback =
                requestWithMemories.withCallbackUrl(callbackUrl);

        FastApiAnalysisResponseDto response =
                fastApiClient.requestAnalysis(requestWithCallback);

        // FastAPI 202 + PROCESSING 접수 시점 기록
        // startedAt이 여기서 채워진다.
        analysis.markProcessing(response.getJobId());

        log.info(
                "FastAPI 분석 요청 접수 - analysisId: {}, jobId: {}, status: {}, memoryCount: {}",
                requestDto.getAnalysisId(),
                response.getJobId(),
                response.getStatus(),
                memories.size()
        );

        return response;
    }

    private List<FastApiAnalysisRequestDto.ProjectMemoryDto>
    loadProjectMemories(Long projectId) {

        return projectRecordRepository
                .findTop20ByProjectIdAndStatusAndMemoryEnabledTrueOrderByApprovedAtDesc(
                        projectId,
                        ProjectRecordStatus.APPROVED
                )
                .stream()
                .map(record ->
                        FastApiAnalysisRequestDto.ProjectMemoryDto.builder()
                                .recordId(record.getRecordId())
                                .prNumber(record.getPrNumber())
                                .content(
                                        deserializeMemory(
                                                record.getContentJson()
                                        )
                                )
                                .build()
                )
                .toList();
    }

    private Object deserializeMemory(String contentJson) {
        if (contentJson == null || contentJson.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(
                    contentJson,
                    Object.class
            );
        } catch (Exception exception) {
            log.warn(
                    "프로젝트 메모리 JSON 파싱 실패 - 원문 전달. error={}",
                    exception.getMessage()
            );
            return contentJson;
        }
    }

    private String buildCallbackUrl(Long analysisId) {
        String normalizedBaseUrl =
                serverBaseUrl.endsWith("/")
                        ? serverBaseUrl.substring(
                                0,
                                serverBaseUrl.length() - 1
                        )
                        : serverBaseUrl;

        return normalizedBaseUrl
                + String.format(
                        CALLBACK_PATH_TEMPLATE,
                        analysisId
                );
    }
}
