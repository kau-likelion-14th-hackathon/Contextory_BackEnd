package com.kbj.contextory.github.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kbj.contextory.github.client.GithubApiRepository;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GithubRepositorySummaryResponse {

    private Long githubRepositoryId;
    private String repositoryFullName;
    private String repositoryUrl;
    private String defaultBranch;

    @JsonProperty("private")
    private Boolean privateRepository;

    public static GithubRepositorySummaryResponse from(
            GithubApiRepository repository
    ) {
        return GithubRepositorySummaryResponse.builder()
                .githubRepositoryId(repository.getId())
                .repositoryFullName(repository.getFullName())
                .repositoryUrl(repository.getHtmlUrl())
                .defaultBranch(repository.getDefaultBranch())
                .privateRepository(repository.getPrivateRepository())
                .build();
    }
}
