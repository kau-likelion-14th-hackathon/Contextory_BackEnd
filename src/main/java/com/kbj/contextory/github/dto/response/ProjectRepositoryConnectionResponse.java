package com.kbj.contextory.github.dto.response;

import com.kbj.contextory.github.domain.ProjectGithubRepository;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectRepositoryConnectionResponse {

    private Long repositoryId;
    private Long projectId;
    private Long githubRepositoryId;
    private String repositoryFullName;
    private Long connectedBy;

    public static ProjectRepositoryConnectionResponse from(
            ProjectGithubRepository repository
    ) {
        return ProjectRepositoryConnectionResponse.builder()
                .repositoryId(repository.getRepositoryId())
                .projectId(repository.getProjectId())
                .githubRepositoryId(repository.getGithubRepositoryId())
                .repositoryFullName(repository.getRepositoryFullName())
                .connectedBy(repository.getConnectedBy())
                .build();
    }
}
