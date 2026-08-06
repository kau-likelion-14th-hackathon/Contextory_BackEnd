package com.kbj.contextory.github.dto.response;

import com.kbj.contextory.github.domain.GithubInstallation;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GithubInstallationResponse {

    private Long installationId;
    private Long accountId;
    private String accountLogin;
    private String accountType;
    private String repositorySelection;

    public static GithubInstallationResponse from(
            GithubInstallation installation
    ) {
        return GithubInstallationResponse.builder()
                .installationId(installation.getInstallationId())
                .accountId(installation.getAccountId())
                .accountLogin(installation.getAccountLogin())
                .accountType(installation.getAccountType())
                .repositorySelection(installation.getRepositorySelection())
                .build();
    }
}
