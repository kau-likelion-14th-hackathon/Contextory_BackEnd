package com.kbj.contextory.project.dto.response;

import com.kbj.contextory.project.domain.Project;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateProjectResponse {

    private Long projectId;
    private String name;
    private String status;

    public static UpdateProjectResponse from(Project project) {
        return UpdateProjectResponse.builder()
                .projectId(project.getProjectId())
                .name(project.getName())
                .status(project.getStatus().name())
                .build();
    }
}
