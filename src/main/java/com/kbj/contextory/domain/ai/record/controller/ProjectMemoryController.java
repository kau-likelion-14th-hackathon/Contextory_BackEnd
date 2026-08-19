package com.kbj.contextory.domain.ai.record.controller;

import com.kbj.contextory.domain.ai.record.dto.response.ProjectMemoryListResponse;
import com.kbj.contextory.domain.ai.record.service.ProjectMemoryService;
import com.kbj.contextory.global.api.ApiResponse;
import com.kbj.contextory.global.api.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "프로젝트 메모리",
        description = "프로젝트별 승인된 AI 분석 메모리 조회 API"
)
@Validated
@RestController
@RequestMapping("/api/projects/{projectId}/memories")
@RequiredArgsConstructor
public class ProjectMemoryController {

    private final ProjectMemoryService projectMemoryService;

    @GetMapping
    @Operation(summary = "프로젝트 메모리 목록 조회")
    public ApiResponse<ProjectMemoryListResponse> getMemories(
            @PathVariable Long projectId,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        return ApiResponse.onSuccess(
                SuccessCode.OK,
                projectMemoryService.getMemories(projectId, userId, page, size)
        );
    }
}
