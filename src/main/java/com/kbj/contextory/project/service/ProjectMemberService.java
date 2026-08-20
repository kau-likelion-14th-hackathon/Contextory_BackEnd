package com.kbj.contextory.project.service;

import com.kbj.contextory.github.support.ProjectAccessChecker;
import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.exception.GeneralException;
import com.kbj.contextory.project.domain.ProjectMember;
import com.kbj.contextory.project.domain.ProjectMemberStatus;
import com.kbj.contextory.project.domain.ProjectPermissionRole;
import com.kbj.contextory.project.dto.request.UpdateProjectMemberRequest;
import com.kbj.contextory.project.dto.response.ProjectMemberResponse;
import com.kbj.contextory.project.dto.response.UpdateProjectMemberResponse;
import com.kbj.contextory.project.repository.ProjectMemberJpaRepository;
import com.kbj.contextory.user.domain.User;
import com.kbj.contextory.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {

    private final ProjectMemberJpaRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectAccessChecker projectAccessChecker;

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getProjectMembers(Long projectId, Long userId) {
        projectAccessChecker.requireMember(projectId, userId);

        List<ProjectMember> members =
                projectMemberRepository.findAllByProjectIdAndStatusOrderByProjectMemberIdAsc(
                        projectId,
                        ProjectMemberStatus.ACTIVE
                );

        List<Long> userIds = members.stream()
                .map(ProjectMember::getUserId)
                .distinct()
                .toList();

        Map<Long, User> userById = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return members.stream()
                .map(member -> {
                    User memberUser = userById.get(member.getUserId());

                    if (memberUser == null) {
                        throw GeneralException.of(ErrorCode.USER_NOT_FOUND);
                    }

                    return ProjectMemberResponse.of(member, memberUser);
                })
                .toList();
    }

    @Transactional
    public UpdateProjectMemberResponse updateProjectMember(
            Long projectId,
            Long projectMemberId,
            Long userId,
            UpdateProjectMemberRequest request
    ) {
        projectAccessChecker.requireAdmin(projectId, userId);

        ProjectMember member = getActiveProjectMember(projectId, projectMemberId);

        if (member.getPermissionRole() == ProjectPermissionRole.OWNER) {
            throw GeneralException.of(ErrorCode.PROJECT_OWNER_MEMBER_UPDATE_NOT_ALLOWED);
        }

        ProjectPermissionRole permissionRole =
                request.getPermissionRole() == null ? null
                        : ProjectPermissionRole.valueOf(request.getPermissionRole());

        member.update(permissionRole, normalizeProjectRole(request.getProjectRole()));

        return UpdateProjectMemberResponse.from(member);
    }

    @Transactional
    public void removeProjectMember(Long projectId, Long projectMemberId, Long userId) {
        projectAccessChecker.requireAdmin(projectId, userId);

        ProjectMember member = getActiveProjectMember(projectId, projectMemberId);

        if (member.getPermissionRole() == ProjectPermissionRole.OWNER) {
            throw GeneralException.of(ErrorCode.PROJECT_OWNER_MEMBER_REMOVE_NOT_ALLOWED);
        }

        member.leave();
    }

    private ProjectMember getActiveProjectMember(Long projectId, Long projectMemberId) {
        return projectMemberRepository.findByProjectIdAndProjectMemberIdAndStatus(
                        projectId,
                        projectMemberId,
                        ProjectMemberStatus.ACTIVE
                )
                .orElseThrow(() -> GeneralException.of(ErrorCode.PROJECT_MEMBER_NOT_FOUND));
    }

    private String normalizeProjectRole(String projectRole) {
        return projectRole == null ? null : projectRole.trim();
    }
}
