package com.kbj.contextory.github.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kbj.contextory.github.client.GithubApiRepository;
import com.kbj.contextory.github.domain.ProjectGithubRepository;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ProjectRepositoryDetailResponse {

    private Long repositoryId;
    private Long githubRepositoryId;
    private String repositoryFullName;
    private String repositoryUrl;
    private String defaultBranch;

    @JsonProperty("private")
    private Boolean privateRepository;

    private Long connectedBy;
    private Instant lastSyncedAt;

    public static ProjectRepositoryDetailResponse of(
            ProjectGithubRepository connection,
            GithubApiRepository githubRepository
    ) {
        return ProjectRepositoryDetailResponse.builder()
                .repositoryId(connection.getRepositoryId())
                .githubRepositoryId(connection.getGithubRepositoryId())
                .repositoryFullName(connection.getRepositoryFullName())
                .repositoryUrl(githubRepository.getHtmlUrl())
                .defaultBranch(githubRepository.getDefaultBranch())
                .privateRepository(githubRepository.getPrivateRepository())
                .connectedBy(connection.getConnectedBy())
                .lastSyncedAt(connection.getLastSyncedAt())
                .build();
    }
}
