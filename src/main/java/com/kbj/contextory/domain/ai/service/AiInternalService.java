package com.kbj.contextory.domain.ai.service;

import com.kbj.contextory.domain.ai.dto.request.FastApiCallbackRequestDto;
import com.kbj.contextory.domain.ai.dto.response.FastApiJobStatusResponseDto;
import com.kbj.contextory.domain.ai.entity.AiAnalysis;
import com.kbj.contextory.domain.ai.repository.AiAnalysisRepository;
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
    private final AiAnalysisRepository aiAnalysisRepository;

    /**
     * FastAPI 분석 작업 상태 보조 조회
     */
    public FastApiJobStatusResponseDto getAnalysisStatus(String jobId) {
        log.info("FastAPI 분석 작업 상태 조회 서비스 호출 - jobId: {}", jobId);
        return fastApiClient.getJobStatus(jobId);
    }

    /**
     * FastAPI 분석 완료/실패 Callback 처리 (DB 상태 갱신)
     */
    @Transactional
    public void processCallback(Long analysisId, FastApiCallbackRequestDto callbackDto) {
        log.info("FastAPI Callback 수신 - analysisId: {}, jobId: {}, status: {}",
                analysisId, callbackDto.getJobId(), callbackDto.getStatus());

        AiAnalysis analysis = aiAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("해당 분석 요청 레코드를 찾을 수 없습니다. id=" + analysisId));

        if ("FAILED".equalsIgnoreCase(callbackDto.getStatus())) {
            analysis.fail(callbackDto.getJobId(), callbackDto.getErrorMessage());
            refundCredit(analysis, callbackDto.getErrorMessage());
            return;
        }

        // 성공 처리: status, fastapi_job_id, result_json 한 번에 갱신
        analysis.complete(callbackDto.getJobId(), callbackDto.getResultSummary());
    }

    private void refundCredit(AiAnalysis analysis, String errorMessage) {
        // TODO: credit_transactions 테이블 연동하여 analysis.getCreditUsed() 만큼 환불 INSERT
        log.info("Credit 환불 처리 대상 - userId: {}, refundAmount: {}", analysis.getRequestedBy(), analysis.getCreditUsed());
    }
}