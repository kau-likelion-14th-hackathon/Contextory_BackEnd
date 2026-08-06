package com.kbj.contextory.github.service;

import com.kbj.contextory.github.client.GithubApiInstallation;
import com.kbj.contextory.github.client.GithubApiUser;
import com.kbj.contextory.github.client.GithubOauthClient;
import com.kbj.contextory.github.client.GithubOauthTokenResponse;
import com.kbj.contextory.github.dto.response.GithubConnectResponse;
import com.kbj.contextory.github.dto.response.GithubConnectionResponse;
import com.kbj.contextory.github.support.GithubOauthStateService;
import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GithubConnectionService {

    private final GithubOauthStateService stateService;
    private final GithubOauthClient githubOauthClient;
    private final GithubConnectionPersistenceService persistenceService;

    @Value("${github.app.install-base-url:https://github.com/apps}")
    private String installBaseUrl;

    @Value("${github.app.slug}")
    private String appSlug;

    public GithubConnectResponse createConnectUrl(Long userId) {
        GithubOauthStateService.IssuedState issuedState = stateService.issue(userId);

        String installUrl = UriComponentsBuilder
                .fromUriString(installBaseUrl)
                .pathSegment(appSlug, "installations", "new")
                .queryParam("state", issuedState.value())
                .build()
                .encode()
                .toUriString();

        long expiresIn = Math.max(
                0,
                Duration.between(Instant.now(), issuedState.expiresAt()).toSeconds()
        );

        return GithubConnectResponse.builder()
                .installUrl(installUrl)
                .stateExpiresInSeconds(expiresIn)
                .build();
    }

    public GithubConnectionResponse completeConnection(
            String code,
            String state,
            Long callbackInstallationId
    ) {
        Long userId = stateService.consume(state);

        if (code == null || code.isBlank()) {
            throw GeneralException.of(ErrorCode.GITHUB_OAUTH_CODE_MISSING);
        }

        GithubOauthTokenResponse tokenResponse = githubOauthClient
                .exchangeCode(code);
        GithubApiUser githubUser = githubOauthClient
                .getAuthenticatedUser(tokenResponse.getAccessToken());
        List<GithubApiInstallation> installations = githubOauthClient
                .listInstallations(tokenResponse.getAccessToken());

        if (installations.isEmpty()) {
            throw GeneralException.of(ErrorCode.GITHUB_INSTALLATION_NOT_FOUND);
        }

        if (callbackInstallationId != null) {
            boolean callbackInstallationExists = installations.stream()
                    .anyMatch(installation -> callbackInstallationId.equals(
                            installation.getId()
                    ));

            if (!callbackInstallationExists) {
                throw GeneralException.of(
                        ErrorCode.GITHUB_INSTALLATION_NOT_ACCESSIBLE
                );
            }
        }

        return persistenceService.saveConnection(
                userId,
                githubUser,
                tokenResponse,
                installations
        );
    }
}
