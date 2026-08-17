package com.kbj.contextory.github.controller;

import com.kbj.contextory.github.dto.response.GithubConnectResponse;
import com.kbj.contextory.github.service.GithubConnectionService;
import com.kbj.contextory.global.api.ApiResponse;
import com.kbj.contextory.global.api.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Tag(name = "GitHub Connection", description = "GitHub App 연결 및 OAuth Callback API")
@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GithubConnectionController {

    private final GithubConnectionService githubConnectionService;

    @Value("${contextory.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @GetMapping("/connect")
    @Operation(summary = "GitHub App 연결 URL 생성")
    public ApiResponse<GithubConnectResponse> connect(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long projectId
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        GithubConnectResponse response = githubConnectionService
                .createConnectUrl(userId, projectId);

        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @GetMapping("/callback")
    @Operation(
            summary = "GitHub App OAuth Callback",
            description = "GitHub 인증/설치 완료 후 사용자의 브라우저가 Redirect되는 공개 Callback 엔드포인트입니다."
    )
    public ResponseEntity<Void> callback(
            @RequestParam String code,
            @RequestParam String state,
            @RequestParam(name = "installation_id", required = false)
            Long installationId
    ) {
        GithubConnectionService.GithubConnectionResult result =
                githubConnectionService.completeConnection(
                        code,
                        state,
                        installationId
                );

        URI redirectUri = UriComponentsBuilder
                .fromUriString(frontendBaseUrl)
                .pathSegment(
                        "projects",
                        String.valueOf(result.projectId()),
                        "settings"
                )
                .build()
                .encode()
                .toUri();

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(redirectUri)
                .build();
    }
}
