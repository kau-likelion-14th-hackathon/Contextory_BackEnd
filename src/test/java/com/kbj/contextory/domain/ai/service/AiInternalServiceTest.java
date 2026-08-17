package com.kbj.contextory.domain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbj.contextory.domain.ai.dto.request.FastApiCallbackRequestDto;
import com.kbj.contextory.domain.ai.entity.AiAnalysis;
import com.kbj.contextory.domain.ai.entity.AnalysisStatus;
import com.kbj.contextory.domain.ai.repository.AiAnalysisRepository;
import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.client.FastApiClient;
import com.kbj.contextory.global.exception.GeneralException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiInternalServiceTest {

    @Mock
    private FastApiClient fastApiClient;

    @Mock
    private AiAnalysisRepository aiAnalysisRepository;

    @InjectMocks
    private AiInternalService aiInternalService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AiAnalysis newAnalysis() {
        return AiAnalysis.builder()
                .projectId(1L)
                .repositoryId(1L)
                .requestedBy(1L)
                .githubPrId(1L)
                .prNumber(1)
                .analyzedHeadSha("abc123")
                .creditUsed(1)
                .modelName("gpt-test")
                .build();
    }

    private FastApiCallbackRequestDto callbackOf(String jobId, String status) throws Exception {
        String json = """
                {"job_id": "%s", "status": "%s"}
                """.formatted(jobId, status);
        return objectMapper.readValue(json, FastApiCallbackRequestDto.class);
    }

    @Test
    void DB에_저장된_jobId와_콜백_jobId가_다르면_예외를_던지고_상태를_변경하지_않는다() throws Exception {
        AiAnalysis analysis = newAnalysis();
        analysis.updateFastApiJobId("original-job-id");
        when(aiAnalysisRepository.findById(1L)).thenReturn(Optional.of(analysis));

        FastApiCallbackRequestDto callback = callbackOf("tampered-job-id", "COMPLETED");

        assertThatThrownBy(() -> aiInternalService.processCallback(1L, callback))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(ErrorCode.AI_ANALYSIS_JOB_ID_MISMATCH);

        assertThat(analysis.getAnalysisStatus()).isEqualTo(AnalysisStatus.PENDING);
        assertThat(analysis.getFastapiJobId()).isEqualTo("original-job-id");
    }

    @Test
    void DB에_저장된_jobId와_콜백_jobId가_같으면_정상적으로_완료_처리된다() throws Exception {
        AiAnalysis analysis = newAnalysis();
        analysis.updateFastApiJobId("matching-job-id");
        when(aiAnalysisRepository.findById(1L)).thenReturn(Optional.of(analysis));

        FastApiCallbackRequestDto callback = callbackOf("matching-job-id", "COMPLETED");

        aiInternalService.processCallback(1L, callback);

        assertThat(analysis.getAnalysisStatus()).isEqualTo(AnalysisStatus.COMPLETED);
    }

    @Test
    void DB에_기록된_jobId가_아직_없으면_불일치_검증을_건너뛰고_최초_jobId를_기록한다() throws Exception {
        AiAnalysis analysis = newAnalysis();
        when(aiAnalysisRepository.findById(1L)).thenReturn(Optional.of(analysis));

        FastApiCallbackRequestDto callback = callbackOf("first-job-id", "COMPLETED");

        aiInternalService.processCallback(1L, callback);

        assertThat(analysis.getFastapiJobId()).isEqualTo("first-job-id");
        assertThat(analysis.getAnalysisStatus()).isEqualTo(AnalysisStatus.COMPLETED);
    }
}
