package com.kbj.contextory.github.controller;

import com.kbj.contextory.github.dto.response.GithubConnectResponse;
import com.kbj.contextory.github.dto.response.GithubConnectionResponse;
import com.kbj.contextory.github.service.GithubConnectionService;
import com.kbj.contextory.global.api.ApiResponse;
import com.kbj.contextory.global.api.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "GitHub Connection", description = "GitHub App 연결 및 OAuth Callback API")
@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GithubConnectionController {

    private final GithubConnectionService githubConnectionService;

    @GetMapping("/connect")
    @Operation(summary = "GitHub App 연결 URL 생성")
    public ApiResponse<GithubConnectResponse> connect(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        GithubConnectResponse response = githubConnectionService
                .createConnectUrl(userId);

        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @GetMapping("/callback")
    @Operation(
            summary = "GitHub App OAuth Callback",
            description = "GitHub에서 직접 호출하는 공개 Callback 엔드포인트입니다."
    )
    public ApiResponse<GithubConnectionResponse> callback(
            @RequestParam String code,
            @RequestParam String state,
            @RequestParam(name = "installation_id", required = false)
            Long installationId
    ) {
        GithubConnectionResponse response = githubConnectionService
                .completeConnection(code, state, installationId);

        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }
}
