package com.kbj.contextory.domain.ai.service;

import com.kbj.contextory.domain.ai.dto.request.FastApiCallbackRequestDto;
import com.kbj.contextory.domain.ai.dto.response.FastApiJobStatusResponseDto;
import com.kbj.contextory.global.client.FastApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiInternalService {

    private final FastApiClient fastApiClient;

    /**
     * FastAPI 분석 작업 상태 보조 조회
     */
    public FastApiJobStatusResponseDto getAnalysisStatus(String jobId) {
        log.info("FastAPI 분석 작업 상태 조회 서비스 호출 - jobId: {}", jobId);
        return fastApiClient.getJobStatus(jobId);
    }

    /**
     * FastAPI 분석 완료/실패 Callback 처리
     */
    @Transactional
    public void processCallback(Long analysisId, FastApiCallbackRequestDto callbackDto) {
        log.info("FastAPI Callback 수신 - analysisId: {}, status: {}", analysisId, callbackDto.getStatus());

        if ("FAILED".equalsIgnoreCase(callbackDto.getStatus())) {
            log.warn("분석 실패 수신. 크레딧 환불(REFUND)을 진행합니다. 사유: {}", callbackDto.getErrorMessage());
            refundCredit(analysisId, callbackDto.getErrorMessage());
            return;
        }

        // 성공 처리 및 결과 저장
        saveAnalysisResult(analysisId, callbackDto);
    }

    private void refundCredit(Long analysisId, String errorMessage) {
        // TODO: analysisId로 유저/프로젝트를 조회하여 차감했던 크레딧 REFUND 처리
    }

    private void saveAnalysisResult(Long analysisId, FastApiCallbackRequestDto callbackDto) {
        // TODO: DB의 분석 결과(Analysis Entity) 상태를 COMPLETED로 변경하고 결과 요약 저장
    }
}