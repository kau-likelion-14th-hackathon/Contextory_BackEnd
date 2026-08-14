package com.kbj.contextory.domain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    // 스프링 빈 대신 직접 인스턴스화하여 Bean 주입 에러 방지
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FastApiJobStatusResponseDto getAnalysisStatus(String jobId) {
        log.info("FastAPI 분석 작업 상태 조회 서비스 호출 - jobId: {}", jobId);
        return fastApiClient.getJobStatus(jobId);
    }

    @Transactional
    public void processCallback(Long analysisId, FastApiCallbackRequestDto callbackDto) {
        log.info("FastAPI Callback 수신 - analysisId: {}, jobId: {}, status: {}, model: {}",
                analysisId, callbackDto.getJobId(), callbackDto.getStatus(), callbackDto.getModelName());

        AiAnalysis analysis = aiAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("해당 분석 요청 레코드를 찾을 수 없습니다. id=" + analysisId));

        // 🛡️ [핵심 검증] DB에 이미 등록된 fastapi_job_id가 있다면, 콜백으로 들어온 jobId와 일치하는지 확인
        if (analysis.getFastapiJobId() != null && !analysis.getFastapiJobId().equals(callbackDto.getJobId())) {
            log.error("JobId 불일치 위변조/타이밍 오류 감지! DB JobId: {}, Callback JobId: {}",
                    analysis.getFastapiJobId(), callbackDto.getJobId());
            throw new IllegalStateException("분석 작업 식별자(Job ID)가 일치하지 않습니다.");
        }

        // 1. 실패 처리 분기
        if ("FAILED".equalsIgnoreCase(callbackDto.getStatus())) {
            analysis.fail(callbackDto.getJobId(), callbackDto.getModelName(), callbackDto.getErrorMessage());
            refundCredit(analysis, callbackDto.getErrorMessage());
            return;
        }

        // 2. 분석 결과 직렬화
        String resultJson = null;
        if (callbackDto.getResult() != null) {
            try {
                if (callbackDto.getResult() instanceof String stringResult) {
                    resultJson = stringResult;
                } else {
                    resultJson = objectMapper.writeValueAsString(callbackDto.getResult());
                }
            } catch (Exception e) {
                log.error("분석 결과 JSON 직렬화 실패", e);
                resultJson = callbackDto.getResult().toString();
            }
        }

        // 3. 정상 성공 처리
        analysis.complete(callbackDto.getJobId(), callbackDto.getModelName(), resultJson);
        log.info("분석 완료 데이터 DB 갱신 성공 - analysisId: {}, jobId: {}", analysisId, callbackDto.getJobId());
    }

    private void refundCredit(AiAnalysis analysis, String errorMessage) {
        log.info("Credit 환불 처리 대상 - userId: {}, refundAmount: {}, error: {}",
                analysis.getRequestedBy(), analysis.getCreditUsed(), errorMessage);
    }
}