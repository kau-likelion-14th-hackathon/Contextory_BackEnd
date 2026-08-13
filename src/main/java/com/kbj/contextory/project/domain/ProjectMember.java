package com.kbj.contextory.project.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@Table(name = "project_members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_member_id")
    private Long projectMemberId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_role", nullable = false, length = 30)
    private ProjectPermissionRole permissionRole;

    @Column(name = "project_role", length = 50)
    private String projectRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProjectMemberStatus status;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Builder
    private ProjectMember(
            Long projectId,
            Long userId,
            ProjectPermissionRole permissionRole,
            String projectRole,
            ProjectMemberStatus status
    ) {
        this.projectId = projectId;
        this.userId = userId;
        this.permissionRole = permissionRole;
        this.projectRole = projectRole;
        this.status = status == null ? ProjectMemberStatus.ACTIVE : status;
    }

    public static ProjectMember owner(Long projectId, Long userId) {
        return ProjectMember.builder()
                .projectId(projectId)
                .userId(userId)
                .permissionRole(ProjectPermissionRole.OWNER)
                .status(ProjectMemberStatus.ACTIVE)
                .build();
    }

    @PrePersist
    private void onCreate() {
        Instant now = Instant.now();
        if (joinedAt == null) {
            joinedAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = Instant.now();
    }
}
