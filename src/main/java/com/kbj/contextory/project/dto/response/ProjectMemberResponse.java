package com.kbj.contextory.project.dto.response;

import com.kbj.contextory.project.domain.ProjectMember;
import com.kbj.contextory.user.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectMemberResponse {

    private Long projectMemberId;
    private Long userId;
    private String username;
    private String permissionRole;
    private String projectRole;
    private String status;

    public static ProjectMemberResponse of(ProjectMember member, User user) {
        return ProjectMemberResponse.builder()
                .projectMemberId(member.getProjectMemberId())
                .userId(member.getUserId())
                .username(user.getUsername())
                .permissionRole(member.getPermissionRole().name())
                .projectRole(member.getProjectRole())
                .status(member.getStatus().name())
                .build();
    }
}
