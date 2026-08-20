package com.kbj.contextory.domain.ai.controller;

import com.kbj.contextory.domain.ai.dto.request.FastApiAnalysisRequestDto;
import com.kbj.contextory.domain.ai.dto.request.FastApiCallbackRequestDto;
import com.kbj.contextory.domain.ai.dto.response.FastApiAnalysisResponseDto;
import com.kbj.contextory.domain.ai.dto.response.FastApiJobStatusResponseDto;
import com.kbj.contextory.domain.ai.service.AiInternalService;
import com.kbj.contextory.global.api.ApiResponse;
import com.kbj.contextory.global.api.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// JWT(BearerToken)가 아닌 X-Internal-Api-Key로 인증되는 서버 간 내부 API (InternalApiKeyFilter 참고)
@Tag(name = "FastAPI 연동 (내부 API)", description = "FastAPI RAG 서버 연동 및 상태 조회 API")
@SecurityRequirement(name = "InternalApiKey")
@RestController
@RequestMapping("/internal/v1/analyses")
@RequiredArgsConstructor
public class AiInternalController {

    private final AiInternalService aiInternalService;

    @Operation(
            summary = "FastAPI 분석 요청",
            description = "Spring Boot가 PR 데이터와 분석 ID를 FastAPI RAG 서버에 전달하여 비동기 분석을 요청합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<FastApiAnalysisResponseDto>> requestAnalysis(
            @Valid @RequestBody FastApiAnalysisRequestDto requestDto
    ) {
        FastApiAnalysisResponseDto response = aiInternalService.requestAnalysis(requestDto);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.onSuccess(SuccessCode.OK, response));
    }

    @Operation(
            summary = "FastAPI 분석 상태 조회",
            description = "Spring Boot가 FastAPI의 작업 상태를 조회합니다 (Callback 실패 시 보조 수단)."
    )
    @GetMapping("/{jobId}")
    public ApiResponse<FastApiJobStatusResponseDto> getJobStatus(@PathVariable String jobId) {
        FastApiJobStatusResponseDto response = aiInternalService.getAnalysisStatus(jobId);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @Operation(
            summary = "FastAPI 분석 완료 Callback",
            description = "FastAPI가 분석 완료 또는 실패 결과를 Spring Boot에 전송합니다."
    )
    @PostMapping("/{analysisId}/callback")
    public ApiResponse<Void> handleAnalysisCallback(
            @PathVariable Long analysisId,
            @RequestBody FastApiCallbackRequestDto requestDto
    ) {
        aiInternalService.processCallback(analysisId, requestDto);
        return ApiResponse.onSuccess(SuccessCode.OK, null);
    }
}