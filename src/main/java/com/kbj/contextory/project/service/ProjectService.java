package com.kbj.contextory.project.service;

import com.kbj.contextory.github.domain.ProjectGithubRepository;
import com.kbj.contextory.github.repository.ProjectGithubRepositoryJpaRepository;
import com.kbj.contextory.github.support.ProjectAccessChecker;
import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.exception.GeneralException;
import com.kbj.contextory.project.domain.Project;
import com.kbj.contextory.project.domain.ProjectMember;
import com.kbj.contextory.project.domain.ProjectMemberStatus;
import com.kbj.contextory.project.domain.ProjectPermissionRole;
import com.kbj.contextory.project.domain.ProjectStatus;
import com.kbj.contextory.project.dto.request.CreateProjectRequest;
import com.kbj.contextory.project.dto.request.UpdateProjectRequest;
import com.kbj.contextory.project.dto.response.CreateProjectResponse;
import com.kbj.contextory.project.dto.response.ProjectDetailResponse;
import com.kbj.contextory.project.dto.response.ProjectListResponse;
import com.kbj.contextory.project.dto.response.ProjectSummaryResponse;
import com.kbj.contextory.project.dto.response.UpdateProjectResponse;
import com.kbj.contextory.project.repository.ProjectJpaRepository;
import com.kbj.contextory.project.repository.ProjectMemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectJpaRepository projectRepository;
    private final ProjectMemberJpaRepository projectMemberJpaRepository;
    private final ProjectGithubRepositoryJpaRepository githubRepositoryJpaRepository;
    private final ProjectAccessChecker projectAccessChecker;

    @Transactional
    public CreateProjectResponse createProject(
            Long userId,
            CreateProjectRequest request
    ) {
        String slug = request.getSlug().trim();

        if (projectRepository.existsBySlug(slug)) {
            throw GeneralException.of(ErrorCode.PROJECT_SLUG_DUPLICATED);
        }

        Project project = projectRepository.save(
                Project.builder()
                        .ownerId(userId)
                        .name(request.getName().trim())
                        .slug(slug)
                        .summary(request.getSummary())
                        .purpose(request.getPurpose())
                        .defaultLanguage(request.getDefaultLanguage())
                        .build()
        );

        projectMemberJpaRepository.save(ProjectMember.owner(project.getProjectId(), userId));

        /*
         * TODO Billing:
         * Notion 명세에는 프로젝트 생성 시 FREE 구독과 기본 크레딧을 같이 생성하도록
         * 되어 있지만 현재 MVP DB에서는 plans/subscriptions/credits를 제외했습니다.
         */

        return CreateProjectResponse.from(project);
    }

    @Transactional(readOnly = true)
    public ProjectListResponse getMyProjects(Long userId, int page, int size, String status) {
        ProjectStatus projectStatus = ProjectStatus.valueOf(status);

        Page<Project> projectPage = projectRepository.findMyProjects(
                userId,
                projectStatus,
                ProjectMemberStatus.ACTIVE,
                PageRequest.of(page, size)
        );

        Map<Long, ProjectPermissionRole> memberRoleByProjectId = getMemberRoleMap(userId);

        List<ProjectSummaryResponse> content = projectPage.getContent()
                .stream()
                .map(project -> ProjectSummaryResponse.of(
                        project,
                        resolveRole(project, userId, memberRoleByProjectId),
                        githubRepositoryJpaRepository
                                .findByProjectId(project.getProjectId())
                                .isPresent()
                ))
                .toList();

        return ProjectListResponse.builder()
                .content(content)
                .page(projectPage.getNumber())
                .size(projectPage.getSize())
                .totalElements(projectPage.getTotalElements())
                .hasNext(projectPage.hasNext())
                .build();
    }

    @Transactional(readOnly = true)
    public ProjectDetailResponse getProject(Long projectId, Long userId) {
        ProjectPermissionRole permissionRole = projectAccessChecker.getRole(projectId, userId);

        if (permissionRole == null) {
            throw GeneralException.of(ErrorCode.PROJECT_ACCESS_DENIED);
        }

        Project project = getActiveProject(projectId);

        ProjectGithubRepository githubRepository =
                githubRepositoryJpaRepository.findByProjectId(projectId).orElse(null);

        return ProjectDetailResponse.of(project, permissionRole, githubRepository);
    }

    @Transactional
    public UpdateProjectResponse updateProject(Long projectId, Long userId, UpdateProjectRequest request) {
        projectAccessChecker.requireAdmin(projectId, userId);

        Project project = getActiveProject(projectId);

        ProjectStatus nextStatus = request.getStatus() == null
                ? null
                : ProjectStatus.valueOf(request.getStatus());

        project.update(
                request.getName() == null ? null : request.getName().trim(),
                request.getSummary(),
                request.getPurpose(),
                request.getDefaultLanguage(),
                nextStatus
        );

        return UpdateProjectResponse.from(project);
    }

    @Transactional
    public void deleteProject(Long projectId, Long userId) {
        projectAccessChecker.requireOwner(projectId, userId);

        Project project = getActiveProject(projectId);
        project.delete();
    }

    private Project getActiveProject(Long projectId) {
        return projectRepository.findById(projectId)
                .filter(project -> project.getStatus() != ProjectStatus.DELETED)
                .orElseThrow(() -> GeneralException.of(ErrorCode.PROJECT_NOT_FOUND));
    }

    private Map<Long, ProjectPermissionRole> getMemberRoleMap(Long userId) {
        return projectMemberJpaRepository.findAllByUserIdAndStatus(userId,ProjectMemberStatus.ACTIVE)
                .stream()
                .collect(Collectors.toMap(
                        member -> member.getProjectId(),
                        member -> member.getPermissionRole(),
                        (first, second) -> first
                ));
    }

    private ProjectPermissionRole resolveRole(
            Project project,
            Long userId,
            Map<Long, ProjectPermissionRole> memberRoleByProjectId
    ) {
        if (project.getOwnerId().equals(userId)) {
            return ProjectPermissionRole.OWNER;
        }

        ProjectPermissionRole role = memberRoleByProjectId.get(project.getProjectId());

        if (role == null) {
            throw GeneralException.of(ErrorCode.PROJECT_ACCESS_DENIED);
        }

        return role;
    }
}
