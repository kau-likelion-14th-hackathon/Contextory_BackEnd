package com.kbj.contextory.github.domain;

import com.kbj.contextory.Entity.BaseEntity;
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
@Table(name = "github_connections")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubConnection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "github_connection_id")
    private Long githubConnectionId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "github_user_id", nullable = false, unique = true)
    private Long githubUserId;

    @Column(name = "github_login", nullable = false, length = 255)
    private String githubLogin;

    @Column(name = "github_avatar_url", length = 1024)
    private String githubAvatarUrl;

    @Column(name = "access_token_encrypted", nullable = false, columnDefinition = "TEXT")
    private String accessTokenEncrypted;

    @Column(name = "refresh_token_encrypted", columnDefinition = "TEXT")
    private String refreshTokenEncrypted;

    @Column(name = "access_token_expires_at")
    private Instant accessTokenExpiresAt;

    @Column(name = "refresh_token_expires_at")
    private Instant refreshTokenExpiresAt;

    @Column(name = "connected_at", nullable = false)
    private Instant connectedAt;

    @Builder
    private GithubConnection(
            Long userId,
            Long githubUserId,
            String githubLogin,
            String githubAvatarUrl,
            String accessTokenEncrypted,
            String refreshTokenEncrypted,
            Instant accessTokenExpiresAt,
            Instant refreshTokenExpiresAt,
            Instant connectedAt
    ) {
        this.userId = userId;
        this.githubUserId = githubUserId;
        this.githubLogin = githubLogin;
        this.githubAvatarUrl = githubAvatarUrl;
        this.accessTokenEncrypted = accessTokenEncrypted;
        this.refreshTokenEncrypted = refreshTokenEncrypted;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
        this.connectedAt = connectedAt;
    }

    public void reconnect(
            Long githubUserId,
            String githubLogin,
            String githubAvatarUrl,
            String accessTokenEncrypted,
            String refreshTokenEncrypted,
            Instant accessTokenExpiresAt,
            Instant refreshTokenExpiresAt,
            Instant connectedAt
    ) {
        this.githubUserId = githubUserId;
        this.githubLogin = githubLogin;
        this.githubAvatarUrl = githubAvatarUrl;
        this.accessTokenEncrypted = accessTokenEncrypted;
        this.refreshTokenEncrypted = refreshTokenEncrypted;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
        this.connectedAt = connectedAt;
    }

    public void rotateTokens(
            String accessTokenEncrypted,
            String refreshTokenEncrypted,
            Instant accessTokenExpiresAt,
            Instant refreshTokenExpiresAt
    ) {
        this.accessTokenEncrypted = accessTokenEncrypted;
        this.refreshTokenEncrypted = refreshTokenEncrypted;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    }
}
