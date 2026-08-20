package com.kbj.contextory.domain.ai.record.controller;

import com.kbj.contextory.domain.ai.record.dto.request.AiAnalysisEditRequest;
import com.kbj.contextory.domain.ai.record.dto.response.AiAnalysisRecordResponse;
import com.kbj.contextory.domain.ai.record.service.AiAnalysisRecordService;
import com.kbj.contextory.global.api.ApiResponse;
import com.kbj.contextory.global.api.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "AI 분석 검토/승인",
        description = "AI 분석 수정, 승인, 프로젝트 메모리 등록 API"
)
@RestController
@RequestMapping("/api/projects/{projectId}/analyses")
@RequiredArgsConstructor
public class AiAnalysisRecordController {

    private final AiAnalysisRecordService aiAnalysisRecordService;

    @PatchMapping("/{analysisId}")
    @Operation(summary = "AI 분석 수정")
    public ApiResponse<AiAnalysisRecordResponse> editAnalysis(
            @PathVariable Long projectId,
            @PathVariable Long analysisId,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AiAnalysisEditRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        return ApiResponse.onSuccess(
                SuccessCode.OK,
                aiAnalysisRecordService.editAnalysis(
                        projectId,
                        analysisId,
                        userId,
                        request
                )
        );
    }

    @PostMapping("/{analysisId}/approve")
    @Operation(summary = "AI 분석 승인")
    public ApiResponse<AiAnalysisRecordResponse> approveAnalysis(
            @PathVariable Long projectId,
            @PathVariable Long analysisId,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        return ApiResponse.onSuccess(
                SuccessCode.OK,
                aiAnalysisRecordService.approveAnalysis(
                        projectId,
                        analysisId,
                        userId
                )
        );
    }

    @PostMapping("/{analysisId}/memory")
    @Operation(summary = "승인된 AI 분석 기록을 프로젝트 메모리에 등록")
    public ApiResponse<AiAnalysisRecordResponse> registerMemory(
            @PathVariable Long projectId,
            @PathVariable Long analysisId,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        return ApiResponse.onSuccess(
                SuccessCode.OK,
                aiAnalysisRecordService.registerMemory(
                        projectId,
                        analysisId,
                        userId
                )
        );
    }
}
