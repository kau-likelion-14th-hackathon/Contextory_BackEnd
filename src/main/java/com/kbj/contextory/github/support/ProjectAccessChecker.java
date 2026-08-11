package com.kbj.contextory.github.support;

import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.exception.GeneralException;
import com.kbj.contextory.project.domain.Project;
import com.kbj.contextory.project.domain.ProjectMemberStatus;
import com.kbj.contextory.project.domain.ProjectPermissionRole;
import com.kbj.contextory.project.domain.ProjectStatus;
import com.kbj.contextory.project.repository.ProjectJpaRepository;
import com.kbj.contextory.project.repository.ProjectMemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectAccessChecker {

    private final ProjectJpaRepository projectRepository;
    private final ProjectMemberJpaRepository projectMemberRepository;

    public void requireMember(Long projectId, Long userId) {
        ProjectPermissionRole role = getRole(projectId, userId);
        if (role == null) {
            throw GeneralException.of(ErrorCode.PROJECT_ACCESS_DENIED);
        }
    }

    public void requireAdmin(Long projectId, Long userId) {
        ProjectPermissionRole role = getRole(projectId, userId);
        if (role != ProjectPermissionRole.OWNER
                && role != ProjectPermissionRole.ADMIN) {
            throw GeneralException.of(ErrorCode.PROJECT_ADMIN_REQUIRED);
        }
    }

    public void requireOwner(Long projectId, Long userId) {
        ProjectPermissionRole role = getRole(projectId, userId);
        if (role != ProjectPermissionRole.OWNER) {
            throw GeneralException.of(ErrorCode.PROJECT_OWNER_REQUIRED);
        }
    }

    public ProjectPermissionRole getRole(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .filter(found -> found.getStatus() != ProjectStatus.DELETED)
                .orElseThrow(() -> GeneralException.of(
                        ErrorCode.PROJECT_NOT_FOUND
                ));

        if (project.getOwnerId().equals(userId)) {
            return ProjectPermissionRole.OWNER;
        }

        return projectMemberRepository
                .findByProjectIdAndUserIdAndStatus(
                        projectId,
                        userId,
                        ProjectMemberStatus.ACTIVE
                )
                .map(member -> member.getPermissionRole())
                .orElse(null);
    }
}
