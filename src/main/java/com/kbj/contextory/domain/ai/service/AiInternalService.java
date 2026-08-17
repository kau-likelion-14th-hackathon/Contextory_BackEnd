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

    // 스프링 빈 대신 직접 인스턴스화하여 Bean 주입 에러 방지
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${contextory.server.base-url}")
    private String serverBaseUrl;

    public FastApiJobStatusResponseDto getAnalysisStatus(String jobId) {
        log.info("FastAPI 분석 작업 상태 조회 서비스 호출 - jobId: {}", jobId);
        return fastApiClient.getJobStatus(jobId);
    }

    @Transactional
    public void processCallback(Long analysisId, FastApiCallbackRequestDto callbackDto) {
        log.info("FastAPI Callback 수신 - analysisId: {}, jobId: {}, status: {}, model: {}",
                analysisId, callbackDto.getJobId(), callbackDto.getStatus(), callbackDto.getModelName());

        AiAnalysis analysis = aiAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> GeneralException.of(ErrorCode.AI_ANALYSIS_NOT_FOUND));

        // 🛡️ [핵심 검증] DB에 이미 등록된 fastapi_job_id가 있다면, 콜백으로 들어온 jobId와 일치하는지 확인
        if (analysis.getFastapiJobId() != null && !analysis.getFastapiJobId().equals(callbackDto.getJobId())) {
            log.error("JobId 불일치 위변조/타이밍 오류 감지! DB JobId: {}, Callback JobId: {}",
                    analysis.getFastapiJobId(), callbackDto.getJobId());
            throw GeneralException.of(ErrorCode.AI_ANALYSIS_JOB_ID_MISMATCH);
        }

        AnalysisStatus status = parseCallbackStatus(callbackDto.getStatus());

        // COMPLETED/FAILED 이외의 상태(PENDING, PROCESSING, CANCELED, 알 수 없는 값)는 콜백으로 허용하지 않고 명시적으로 거부
        switch (status) {
            case FAILED -> {
                analysis.fail(callbackDto.getJobId(), callbackDto.getModelName(), callbackDto.getErrorMessage());
                refundCredit(analysis, callbackDto.getErrorMessage());
            }
            case COMPLETED -> {
                String resultJson = serializeResult(callbackDto.getResult());
                analysis.complete(callbackDto.getJobId(), callbackDto.getModelName(), resultJson);
                log.info("분석 완료 데이터 DB 갱신 성공 - analysisId: {}, jobId: {}", analysisId, callbackDto.getJobId());
            }
            default -> throw GeneralException.of(ErrorCode.AI_ANALYSIS_INVALID_CALLBACK_STATUS);
        }
    }

    private AnalysisStatus parseCallbackStatus(String status) {
        if (status == null || status.isBlank()) {
            throw GeneralException.of(ErrorCode.AI_ANALYSIS_INVALID_CALLBACK_STATUS);
        }
        try {
            return AnalysisStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
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
        } catch (Exception e) {
            log.error("분석 결과 JSON 직렬화 실패", e);
            return result.toString();
        }
    }

    private void refundCredit(AiAnalysis analysis, String errorMessage) {
        log.info("Credit 환불 처리 대상 - userId: {}, refundAmount: {}, error: {}",
                analysis.getRequestedBy(), analysis.getCreditUsed(), errorMessage);
    }

    @Transactional
    public FastApiAnalysisResponseDto requestAnalysis(FastApiAnalysisRequestDto requestDto) {
        // 1. analysisId가 Spring DB에 존재하는지 먼저 검증 (없으면 FastAPI 호출 자체를 막는다)
        AiAnalysis analysis = aiAnalysisRepository.findById(requestDto.getAnalysisId())
                .orElseThrow(() -> GeneralException.of(ErrorCode.AI_ANALYSIS_NOT_FOUND));

        // 2. callback_url은 클라이언트 입력을 신뢰하지 않고 서버가 analysisId 기준으로 직접 생성
        String callbackUrl = buildCallbackUrl(requestDto.getAnalysisId());
        FastApiAnalysisRequestDto requestWithCallback = requestDto.withCallbackUrl(callbackUrl);

        // 3. FastApiClient를 통해 FastAPI 서버에 비동기 분석 요청 전달
        FastApiAnalysisResponseDto response = fastApiClient.requestAnalysis(requestWithCallback);

        // 4. 응답받은 jobId를 존재가 확인된 분석 레코드에 매핑
        analysis.updateFastApiJobId(response.getJobId());

        log.info("[FastAPI 분석 요청 접수 성공] analysisId: {}, jobId: {}, status: {}",
                requestDto.getAnalysisId(), response.getJobId(), response.getStatus());

        return response;
    }

    private String buildCallbackUrl(Long analysisId) {
        return serverBaseUrl + String.format(CALLBACK_PATH_TEMPLATE, analysisId);
    }
}