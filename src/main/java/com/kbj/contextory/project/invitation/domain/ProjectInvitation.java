package com.kbj.contextory.project.invitation.domain;

import com.kbj.contextory.project.domain.ProjectPermissionRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@Table(
        name = "project_invitations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_project_invitation_token",
                        columnNames = "invite_token"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitation_id")
    private Long invitationId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "invite_email", nullable = false, length = 320)
    private String inviteEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_role", nullable = false, length = 30)
    private ProjectPermissionRole permissionRole;

    @Column(name = "project_role", length = 50)
    private String projectRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProjectInvitationStatus status;

    @Column(name = "invite_token", nullable = false, unique = true, length = 100)
    private String inviteToken;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Builder
    private ProjectInvitation(
            Long projectId,
            String inviteEmail,
            ProjectPermissionRole permissionRole,
            String projectRole,
            String inviteToken,
            Instant expiresAt
    ) {
        this.projectId = projectId;
        this.inviteEmail = inviteEmail;
        this.permissionRole = permissionRole;
        this.projectRole = projectRole;
        this.inviteToken = inviteToken;
        this.expiresAt = expiresAt;
        this.status = ProjectInvitationStatus.PENDING;
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    public void accept() {
        this.status = ProjectInvitationStatus.ACCEPTED;
    }

    public void cancel() {
        this.status = ProjectInvitationStatus.CANCELED;
    }

    public void expire() {
        this.status = ProjectInvitationStatus.EXPIRED;
    }

    @PrePersist
    private void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = Instant.now();
    }
}
