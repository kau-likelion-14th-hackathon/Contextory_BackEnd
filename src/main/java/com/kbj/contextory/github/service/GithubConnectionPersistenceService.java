package com.kbj.contextory.github.service;

import com.kbj.contextory.github.client.GithubApiInstallation;
import com.kbj.contextory.github.client.GithubApiUser;
import com.kbj.contextory.github.client.GithubOauthTokenResponse;
import com.kbj.contextory.github.domain.GithubConnection;
import com.kbj.contextory.github.domain.GithubInstallation;
import com.kbj.contextory.github.dto.response.GithubConnectionResponse;
import com.kbj.contextory.github.repository.GithubConnectionJpaRepository;
import com.kbj.contextory.github.repository.GithubInstallationJpaRepository;
import com.kbj.contextory.github.support.GithubTokenCrypto;
import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GithubConnectionPersistenceService {

    private final GithubTokenCrypto tokenCrypto;
    private final GithubConnectionJpaRepository connectionRepository;
    private final GithubInstallationJpaRepository installationRepository;

    @Transactional
    public GithubConnectionResponse saveConnection(
            Long userId,
            GithubApiUser githubUser,
            GithubOauthTokenResponse tokenResponse,
            List<GithubApiInstallation> installations
    ) {
        if (connectionRepository.existsByGithubUserIdAndUserIdNot(
                githubUser.getId(),
                userId
        )) {
            throw GeneralException.of(ErrorCode.GITHUB_ACCOUNT_ALREADY_CONNECTED);
        }

        Instant now = Instant.now();
        Instant accessExpiresAt = expirationFrom(
                now,
                tokenResponse.getExpiresIn()
        );
        Instant refreshExpiresAt = expirationFrom(
                now,
                tokenResponse.getRefreshTokenExpiresIn()
        );

        String encryptedAccessToken = tokenCrypto.encrypt(
                tokenResponse.getAccessToken()
        );
        String encryptedRefreshToken = tokenCrypto.encrypt(
                tokenResponse.getRefreshToken()
        );

        GithubConnection connection = connectionRepository
                .findByUserId(userId)
                .orElseGet(() -> GithubConnection.builder()
                        .userId(userId)
                        .githubUserId(githubUser.getId())
                        .githubLogin(githubUser.getLogin())
                        .githubAvatarUrl(githubUser.getAvatarUrl())
                        .accessTokenEncrypted(encryptedAccessToken)
                        .refreshTokenEncrypted(encryptedRefreshToken)
                        .accessTokenExpiresAt(accessExpiresAt)
                        .refreshTokenExpiresAt(refreshExpiresAt)
                        .connectedAt(now)
                        .build());

        connection.reconnect(
                githubUser.getId(),
                githubUser.getLogin(),
                githubUser.getAvatarUrl(),
                encryptedAccessToken,
                encryptedRefreshToken,
                accessExpiresAt,
                refreshExpiresAt,
                now
        );
        GithubConnection savedConnection = connectionRepository.save(connection);

        installationRepository.deleteAllByUserId(userId);
        installationRepository.flush();

        List<GithubInstallation> savedInstallations = installationRepository.saveAll(
                installations.stream()
                        .map(installation -> toEntity(userId, installation))
                        .toList()
        );

        return GithubConnectionResponse.of(
                savedConnection,
                savedInstallations
        );
    }

    private GithubInstallation toEntity(
            Long userId,
            GithubApiInstallation installation
    ) {
        GithubApiInstallation.Account account = installation.getAccount();
        if (installation.getId() == null
                || account == null
                || account.getId() == null
                || account.getLogin() == null
                || account.getType() == null) {
            throw GeneralException.of(ErrorCode.GITHUB_API_FAILED);
        }

        return GithubInstallation.builder()
                .userId(userId)
                .installationId(installation.getId())
                .accountId(account.getId())
                .accountLogin(account.getLogin())
                .accountType(account.getType())
                .repositorySelection(
                        installation.getRepositorySelection() == null
                                ? "selected"
                                : installation.getRepositorySelection()
                )
                .build();
    }

    private Instant expirationFrom(Instant now, Long expiresInSeconds) {
        if (expiresInSeconds == null) {
            return null;
        }
        return now.plusSeconds(expiresInSeconds);
    }
}
