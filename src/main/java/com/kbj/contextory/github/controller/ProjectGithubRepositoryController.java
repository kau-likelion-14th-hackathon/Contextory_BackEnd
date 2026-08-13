package com.kbj.contextory.github.controller;

import com.kbj.contextory.github.dto.request.ConnectProjectRepositoryRequest;
import com.kbj.contextory.github.dto.response.ProjectRepositoryConnectionResponse;
import com.kbj.contextory.github.dto.response.ProjectRepositoryDetailResponse;
import com.kbj.contextory.github.service.GithubIntegrationService;
import com.kbj.contextory.global.api.ApiResponse;
import com.kbj.contextory.global.api.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Project GitHub", description = "프로젝트와 GitHub 저장소 연결 API")
@RestController
@RequestMapping("/api/projects/{projectId}/repository")
@RequiredArgsConstructor
public class ProjectGithubRepositoryController {

    private final GithubIntegrationService githubIntegrationService;

    @PutMapping
    @Operation(summary = "프로젝트 GitHub 저장소 연결 또는 교체")
    public ApiResponse<ProjectRepositoryConnectionResponse> connectRepository(
            @PathVariable Long projectId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ConnectProjectRepositoryRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        ProjectRepositoryConnectionResponse response =
                githubIntegrationService.connectRepository(
                        projectId,
                        userId,
                        request
                );

        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @GetMapping
    @Operation(summary = "프로젝트 연결 저장소 조회")
    public ApiResponse<ProjectRepositoryDetailResponse> getRepository(
            @PathVariable Long projectId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        ProjectRepositoryDetailResponse response =
                githubIntegrationService.getConnectedRepository(
                        projectId,
                        userId
                );

        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @DeleteMapping
    @Operation(summary = "프로젝트 GitHub 저장소 연결 해제")
    public ApiResponse<Void> disconnectRepository(
            @PathVariable Long projectId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        githubIntegrationService.disconnectRepository(projectId, userId);

        return ApiResponse.onSuccess(SuccessCode.OK, null);
    }
}
