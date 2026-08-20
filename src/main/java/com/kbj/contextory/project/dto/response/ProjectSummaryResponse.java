package com.kbj.contextory.project.dto.response;

import com.kbj.contextory.project.domain.Project;
import com.kbj.contextory.project.domain.ProjectPermissionRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectSummaryResponse {

    private Long projectId;
    private String name;
    private String myPermissionRole;
    private boolean repositoryConnected;

    // Billing 도메인 구현 전까지 null
    private String planName;
    private Integer creditBalance;

    public static ProjectSummaryResponse of(
            Project project,
            ProjectPermissionRole permissionRole,
            boolean repositoryConnected
    ) {
        return ProjectSummaryResponse.builder()
                .projectId(project.getProjectId())
                .name(project.getName())
                .myPermissionRole(permissionRole.name())
                .repositoryConnected(repositoryConnected)
                .planName(null)
                .creditBalance(null)
                .build();
    }
}
