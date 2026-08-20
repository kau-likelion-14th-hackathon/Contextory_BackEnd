package com.kbj.contextory.project.dto.response;

import com.kbj.contextory.github.domain.ProjectGithubRepository;
import com.kbj.contextory.project.domain.Project;
import com.kbj.contextory.project.domain.ProjectPermissionRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectDetailResponse {

    private Long projectId;
    private String name;
    private String slug;
    private String summary;
    private String purpose;
    private String defaultLanguage;
    private String status;
    private String myPermissionRole;
    private RepositoryInfo repository;

    // Billing 도메인 구현 전까지 null
    private SubscriptionInfo subscription;

    public static ProjectDetailResponse of(
            Project project,
            ProjectPermissionRole permissionRole,
            ProjectGithubRepository githubRepository
    ) {
        RepositoryInfo repositoryInfo = RepositoryInfo.builder()
                .connected(githubRepository != null)
                .repositoryFullName(
                        githubRepository == null
                                ? null
                                : githubRepository.getRepositoryFullName()
                )
                .build();

        return ProjectDetailResponse.builder()
                .projectId(project.getProjectId())
                .name(project.getName())
                .slug(project.getSlug())
                .summary(project.getSummary())
                .purpose(project.getPurpose())
                .defaultLanguage(project.getDefaultLanguage())
                .status(project.getStatus().name())
                .myPermissionRole(permissionRole.name())
                .repository(repositoryInfo)
                .subscription(null)
                .build();
    }

    @Getter
    @Builder
    public static class RepositoryInfo {
        private boolean connected;
        private String repositoryFullName;
    }

    @Getter
    @Builder
    public static class SubscriptionInfo {
        private String planName;
        private Integer creditBalance;
    }
}
