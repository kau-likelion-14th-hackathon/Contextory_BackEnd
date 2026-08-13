package com.kbj.contextory.github.support;

import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProjectAccessChecker {

    private final JdbcClient jdbcClient;

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

    private ProjectPermissionRole getRole(Long projectId, Long userId) {
        boolean projectExists = jdbcClient.sql("""
                        SELECT EXISTS (
                            SELECT 1
                            FROM projects
                            WHERE project_id = :projectId
                              AND status <> 'DELETED'
                        )
                        """)
                .param("projectId", projectId)
                .query(Boolean.class)
                .single();

        if (!projectExists) {
            throw GeneralException.of(ErrorCode.PROJECT_NOT_FOUND);
        }

        boolean owner = jdbcClient.sql("""
                        SELECT EXISTS (
                            SELECT 1
                            FROM projects
                            WHERE project_id = :projectId
                              AND owner_id = :userId
                              AND status <> 'DELETED'
                        )
                        """)
                .param("projectId", projectId)
                .param("userId", userId)
                .query(Boolean.class)
                .single();

        if (owner) {
            return ProjectPermissionRole.OWNER;
        }

        Optional<String> role = jdbcClient.sql("""
                        SELECT permission_role
                        FROM project_members
                        WHERE project_id = :projectId
                          AND user_id = :userId
                          AND status = 'ACTIVE'
                        """)
                .param("projectId", projectId)
                .param("userId", userId)
                .query(String.class)
                .optional();

        return role.map(ProjectPermissionRole::valueOf).orElse(null);
    }

    private enum ProjectPermissionRole {
        OWNER,
        ADMIN,
        MEMBER,
        VIEWER
    }
}
