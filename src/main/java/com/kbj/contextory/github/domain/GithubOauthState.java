package com.kbj.contextory.github.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@Table(name = "github_oauth_states")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubOauthState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "github_oauth_state_id")
    private Long githubOauthStateId;

    @Column(name = "state_hash", nullable = false, unique = true, length = 64)
    private String stateHash;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Builder
    private GithubOauthState(
            String stateHash,
            Long userId,
            Instant expiresAt
    ) {
        this.stateHash = stateHash;
        this.userId = userId;
        this.expiresAt = expiresAt;
    }
}
