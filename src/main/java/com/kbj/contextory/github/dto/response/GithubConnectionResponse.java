package com.kbj.contextory.github.dto.response;

import com.kbj.contextory.github.domain.GithubConnection;
import com.kbj.contextory.github.domain.GithubInstallation;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class GithubConnectionResponse {

    private Long githubUserId;
    private String githubLogin;
    private String githubAvatarUrl;
    private Instant connectedAt;
    private Integer installationCount;
    private List<GithubInstallationResponse> installations;

    public static GithubConnectionResponse of(
            GithubConnection connection,
            List<GithubInstallation> installations
    ) {
        List<GithubInstallationResponse> responses = installations.stream()
                .map(GithubInstallationResponse::from)
                .toList();

        return GithubConnectionResponse.builder()
                .githubUserId(connection.getGithubUserId())
                .githubLogin(connection.getGithubLogin())
                .githubAvatarUrl(connection.getGithubAvatarUrl())
                .connectedAt(connection.getConnectedAt())
                .installationCount(responses.size())
                .installations(responses)
                .build();
    }
}
