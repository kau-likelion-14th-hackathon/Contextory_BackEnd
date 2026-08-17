package com.kbj.contextory.github.support;

import com.kbj.contextory.github.domain.GithubOauthState;
import com.kbj.contextory.github.repository.GithubOauthStateJpaRepository;
import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class GithubOauthStateService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final GithubOauthStateJpaRepository stateRepository;

    @Value("${github.oauth.state-expiration:10m}")
    private Duration stateExpiration;

    @Transactional
    public IssuedState issue(
            Long userId,
            Long projectId
    ) {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);

        String rawState = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        Instant expiresAt = Instant.now().plus(stateExpiration);
        stateRepository.save(
                GithubOauthState.builder()
                        .stateHash(hash(rawState))
                        .userId(userId)
                        .projectId(projectId)
                        .expiresAt(expiresAt)
                        .build()
        );

        return new IssuedState(rawState, expiresAt);
    }

    @Transactional
    public ConsumedState consume(String rawState) {
        if (rawState == null || rawState.isBlank()) {
            throw GeneralException.of(ErrorCode.GITHUB_OAUTH_STATE_INVALID);
        }

        GithubOauthState state = stateRepository
                .findByStateHashForUpdate(hash(rawState))
                .orElseThrow(() -> GeneralException.of(
                        ErrorCode.GITHUB_OAUTH_STATE_INVALID
                ));

        if (!state.getExpiresAt().isAfter(Instant.now())) {
            stateRepository.delete(state);
            throw GeneralException.of(ErrorCode.GITHUB_OAUTH_STATE_EXPIRED);
        }

        if (state.getProjectId() == null) {
            stateRepository.delete(state);
            throw GeneralException.of(ErrorCode.GITHUB_OAUTH_STATE_INVALID);
        }

        ConsumedState consumedState = new ConsumedState(
                state.getUserId(),
                state.getProjectId()
        );

        stateRepository.delete(state);
        return consumedState;
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", exception);
        }
    }

    public record IssuedState(String value, Instant expiresAt) {
    }

    public record ConsumedState(Long userId, Long projectId) {
    }
}
