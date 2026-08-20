package com.kbj.contextory.github.support;

import com.kbj.contextory.github.client.GithubOauthClient;
import com.kbj.contextory.github.client.GithubOauthTokenResponse;
import com.kbj.contextory.github.domain.GithubConnection;
import com.kbj.contextory.github.repository.GithubConnectionJpaRepository;
import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class GithubUserAccessTokenProvider {

    private static final long EXPIRATION_SKEW_SECONDS = 60;

    private final GithubConnectionJpaRepository connectionRepository;
    private final GithubOauthClient githubOauthClient;
    private final GithubTokenCrypto tokenCrypto;

    @Transactional
    public String getAccessToken(Long userId) {
        GithubConnection connection = connectionRepository
                .findByUserId(userId)
                .orElseThrow(() -> GeneralException.of(
                        ErrorCode.GITHUB_CONNECTION_REQUIRED
                ));

        if (isAccessTokenUsable(connection)) {
            return tokenCrypto.decrypt(connection.getAccessTokenEncrypted());
        }

        if (!isRefreshTokenUsable(connection)) {
            throw GeneralException.of(ErrorCode.GITHUB_REAUTHORIZATION_REQUIRED);
        }

        String refreshToken = tokenCrypto.decrypt(
                connection.getRefreshTokenEncrypted()
        );
        GithubOauthTokenResponse refreshed = githubOauthClient
                .refreshAccessToken(refreshToken);

        Instant now = Instant.now();
        String nextRefreshToken = refreshed.getRefreshToken() == null
                ? refreshToken
                : refreshed.getRefreshToken();

        Instant accessExpiresAt = expirationFrom(
                now,
                refreshed.getExpiresIn()
        );
        Instant refreshExpiresAt = expirationFrom(
                now,
                refreshed.getRefreshTokenExpiresIn()
        );

        if (refreshExpiresAt == null) {
            refreshExpiresAt = connection.getRefreshTokenExpiresAt();
        }

        connection.rotateTokens(
                tokenCrypto.encrypt(refreshed.getAccessToken()),
                tokenCrypto.encrypt(nextRefreshToken),
                accessExpiresAt,
                refreshExpiresAt
        );

        return refreshed.getAccessToken();
    }

    private boolean isAccessTokenUsable(GithubConnection connection) {
        Instant expiresAt = connection.getAccessTokenExpiresAt();
        return expiresAt == null
                || expiresAt.isAfter(
                        Instant.now().plusSeconds(EXPIRATION_SKEW_SECONDS)
                );
    }

    private boolean isRefreshTokenUsable(GithubConnection connection) {
        if (connection.getRefreshTokenEncrypted() == null) {
            return false;
        }

        Instant expiresAt = connection.getRefreshTokenExpiresAt();
        return expiresAt == null || expiresAt.isAfter(Instant.now());
    }

    private Instant expirationFrom(Instant now, Long expiresInSeconds) {
        if (expiresInSeconds == null) {
            return null;
        }
        return now.plusSeconds(expiresInSeconds);
    }
}
