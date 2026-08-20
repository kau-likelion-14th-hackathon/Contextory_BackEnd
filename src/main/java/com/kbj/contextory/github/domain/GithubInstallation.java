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

@Entity
@Getter
@Table(name = "github_installations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubInstallation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "github_installation_id")
    private Long githubInstallationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "installation_id", nullable = false)
    private Long installationId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "account_login", nullable = false, length = 255)
    private String accountLogin;

    @Column(name = "account_type", nullable = false, length = 50)
    private String accountType;

    @Column(name = "repository_selection", nullable = false, length = 50)
    private String repositorySelection;

    @Builder
    private GithubInstallation(
            Long userId,
            Long installationId,
            Long accountId,
            String accountLogin,
            String accountType,
            String repositorySelection
    ) {
        this.userId = userId;
        this.installationId = installationId;
        this.accountId = accountId;
        this.accountLogin = accountLogin;
        this.accountType = accountType;
        this.repositorySelection = repositorySelection;
    }
}
