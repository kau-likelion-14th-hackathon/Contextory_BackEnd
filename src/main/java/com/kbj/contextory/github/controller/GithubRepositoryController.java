package com.kbj.contextory.github.controller;

import com.kbj.contextory.github.dto.response.GithubRepositoryPageResponse;
import com.kbj.contextory.github.service.GithubIntegrationService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "GitHub", description = "GitHub 저장소 조회 API")
@Validated
@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GithubRepositoryController {

    private final GithubIntegrationService githubIntegrationService;

    @GetMapping("/repositories")
    @Operation(summary = "연결 가능한 GitHub 저장소 목록 조회")
    public ApiResponse<GithubRepositoryPageResponse> getRepositories(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "page는 1 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "30")
            @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.") int size
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        GithubRepositoryPageResponse response =
                githubIntegrationService.getAccessibleRepositories(
                        userId,
                        page,
                        size
                );

        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }
}
