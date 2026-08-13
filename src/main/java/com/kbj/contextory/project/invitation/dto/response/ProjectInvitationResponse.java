package com.kbj.contextory.project.invitation.dto.response;

import com.kbj.contextory.project.invitation.domain.ProjectInvitation;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ProjectInvitationResponse {

    private Long invitationId;
    private String inviteEmail;
    private String status;
    private Instant expiresAt;

    public static ProjectInvitationResponse from(ProjectInvitation invitation) {
        return ProjectInvitationResponse.builder()
                .invitationId(invitation.getInvitationId())
                .inviteEmail(invitation.getInviteEmail())
                .status(invitation.getStatus().name())
                .expiresAt(invitation.getExpiresAt())
                .build();
    }
}
