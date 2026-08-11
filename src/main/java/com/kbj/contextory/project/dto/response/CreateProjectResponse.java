package com.kbj.contextory.project.dto.response;

import com.kbj.contextory.project.domain.Project;
import com.kbj.contextory.project.domain.ProjectPermissionRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateProjectResponse {

    private Long projectId;
    private Long ownerId;
    private String name;
    private String slug;
    private String status;
    private String myPermissionRole;

    public static CreateProjectResponse from(Project project) {
        return CreateProjectResponse.builder()
                .projectId(project.getProjectId())
                .ownerId(project.getOwnerId())
                .name(project.getName())
                .slug(project.getSlug())
                .status(project.getStatus().name())
                .myPermissionRole(ProjectPermissionRole.OWNER.name())
                .build();
    }
}
