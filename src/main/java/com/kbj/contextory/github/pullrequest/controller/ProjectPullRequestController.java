package com.kbj.contextory.github.pullrequest.controller;

import com.kbj.contextory.github.pullrequest.dto.response.PullRequestDetailResponse;
import com.kbj.contextory.github.pullrequest.dto.response.PullRequestFilesResponse;
import com.kbj.contextory.github.pullrequest.dto.response.PullRequestPageResponse;
import com.kbj.contextory.github.pullrequest.service.ProjectPullRequestService;
import com.kbj.contextory.global.api.ApiResponse;
import com.kbj.contextory.global.api.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Pull Request", description = "프로젝트에 연결된 GitHub 저장소의 Pull Request 조회 API")
@RestController
@RequestMapping("/api/projects/{projectId}/pull-requests")
@RequiredArgsConstructor
public class ProjectPullRequestController {

    private final ProjectPullRequestService projectPullRequestService;

    @GetMapping
    @Operation(summary = "Pull Request 목록 조회")
    public ApiResponse<PullRequestPageResponse> getPullRequests(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "OPEN") String state,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        PullRequestPageResponse response = projectPullRequestService.getPullRequests(
                projectId,
                userId,
                state,
                page,
                size
        );

        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @GetMapping("/{prNumber}")
    @Operation(summary = "Pull Request 상세 조회")
    public ApiResponse<PullRequestDetailResponse> getPullRequest(
            @PathVariable Long projectId,
            @PathVariable int prNumber,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        PullRequestDetailResponse response = projectPullRequestService.getPullRequest(
                projectId,
                userId,
                prNumber
        );

        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @GetMapping("/{prNumber}/files")
    @Operation(summary = "Pull Request 변경 파일 조회")
    public ApiResponse<PullRequestFilesResponse> getPullRequestFiles(
            @PathVariable Long projectId,
            @PathVariable int prNumber,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        PullRequestFilesResponse response = projectPullRequestService.getPullRequestFiles(
                projectId,
                userId,
                prNumber
        );

        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }
}
