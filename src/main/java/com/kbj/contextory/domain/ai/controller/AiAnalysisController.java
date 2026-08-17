package com.kbj.contextory.domain.ai.controller;

import com.kbj.contextory.domain.ai.dto.request.AiAnalysisCreateRequest;
import com.kbj.contextory.domain.ai.dto.response.AiAnalysisCancelResponse;
import com.kbj.contextory.domain.ai.dto.response.AiAnalysisDetailResponse;
import com.kbj.contextory.domain.ai.dto.response.AiAnalysisListResponse;
import com.kbj.contextory.domain.ai.dto.response.AiAnalysisRequestResponse;
import com.kbj.contextory.domain.ai.dto.response.AiAnalysisRetryResponse;
import com.kbj.contextory.domain.ai.service.AiAnalysisService;
import com.kbj.contextory.global.api.ApiResponse;
import com.kbj.contextory.global.api.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI 분석", description = "프론트엔드에서 사용하는 PR AI 분석 API")
@Validated
@RestController
@RequestMapping("/api/projects/{projectId}/analyses")
@RequiredArgsConstructor
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;

    @PostMapping
    @Operation(summary = "PR AI 분석 요청")
    public ApiResponse<AiAnalysisRequestResponse> requestAnalysis(
            @PathVariable Long projectId,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AiAnalysisCreateRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ApiResponse.onSuccess(
                SuccessCode.ACCEPTED,
                aiAnalysisService.requestAnalysis(projectId, userId, request)
        );
    }

    @GetMapping
    @Operation(summary = "AI 분석 목록 조회")
    public ApiResponse<AiAnalysisListResponse> getAnalyses(
            @PathVariable Long projectId,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ApiResponse.onSuccess(
                SuccessCode.OK,
                aiAnalysisService.getAnalyses(projectId, userId, status, page, size)
        );
    }

    @GetMapping("/{analysisId}")
    @Operation(summary = "AI 분석 상세 조회")
    public ApiResponse<AiAnalysisDetailResponse> getAnalysis(
            @PathVariable Long projectId,
            @PathVariable Long analysisId,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ApiResponse.onSuccess(
                SuccessCode.OK,
                aiAnalysisService.getAnalysis(projectId, analysisId, userId)
        );
    }

    @PostMapping("/{analysisId}/retry")
    @Operation(summary = "AI 분석 재시도")
    public ApiResponse<AiAnalysisRetryResponse> retryAnalysis(
            @PathVariable Long projectId,
            @PathVariable Long analysisId,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ApiResponse.onSuccess(
                SuccessCode.ACCEPTED,
                aiAnalysisService.retryAnalysis(projectId, analysisId, userId)
        );
    }

    @PostMapping("/{analysisId}/cancel")
    @Operation(summary = "AI 분석 취소")
    public ApiResponse<AiAnalysisCancelResponse> cancelAnalysis(
            @PathVariable Long projectId,
            @PathVariable Long analysisId,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ApiResponse.onSuccess(
                SuccessCode.OK,
                aiAnalysisService.cancelAnalysis(projectId, analysisId, userId)
        );
    }
}
