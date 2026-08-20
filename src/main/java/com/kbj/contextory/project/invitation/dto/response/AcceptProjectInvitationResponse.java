package com.kbj.contextory.project.invitation.dto.response;

import com.kbj.contextory.project.domain.Project;
import com.kbj.contextory.project.domain.ProjectMember;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class AcceptProjectInvitationResponse {

    private Long projectId;
    private String projectName;
    private Long projectMemberId;
    private String permissionRole;
    private String projectRole;
    private Instant joinedAt;

    public static AcceptProjectInvitationResponse of(
            Project project,
            ProjectMember member
    ) {
        return AcceptProjectInvitationResponse.builder()
                .projectId(project.getProjectId())
                .projectName(project.getName())
                .projectMemberId(member.getProjectMemberId())
                .permissionRole(member.getPermissionRole().name())
                .projectRole(member.getProjectRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
