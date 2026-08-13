package com.kbj.contextory.project.dto.response;

import com.kbj.contextory.project.domain.ProjectMember;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateProjectMemberResponse {

    private Long projectMemberId;
    private String permissionRole;
    private String projectRole;

    public static UpdateProjectMemberResponse from(ProjectMember member) {
        return UpdateProjectMemberResponse.builder()
                .projectMemberId(member.getProjectMemberId())
                .permissionRole(member.getPermissionRole().name())
                .projectRole(member.getProjectRole())
                .build();
    }
}
