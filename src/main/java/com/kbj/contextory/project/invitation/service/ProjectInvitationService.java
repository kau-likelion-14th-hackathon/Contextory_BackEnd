package com.kbj.contextory.project.invitation.service;

import com.kbj.contextory.github.support.ProjectAccessChecker;
import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.exception.GeneralException;
import com.kbj.contextory.project.domain.Project;
import com.kbj.contextory.project.domain.ProjectMember;
import com.kbj.contextory.project.domain.ProjectMemberStatus;
import com.kbj.contextory.project.domain.ProjectPermissionRole;
import com.kbj.contextory.project.domain.ProjectStatus;
import com.kbj.contextory.project.invitation.domain.ProjectInvitation;
import com.kbj.contextory.project.invitation.domain.ProjectInvitationStatus;
import com.kbj.contextory.project.invitation.dto.request.CreateProjectInvitationRequest;
import com.kbj.contextory.project.invitation.dto.response.AcceptProjectInvitationResponse;
import com.kbj.contextory.project.invitation.dto.response.CreateProjectInvitationResponse;
import com.kbj.contextory.project.invitation.dto.response.ProjectInvitationResponse;
import com.kbj.contextory.project.invitation.repository.ProjectInvitationJpaRepository;
import com.kbj.contextory.project.invitation.support.ProjectInvitationTokenGenerator;
import com.kbj.contextory.project.repository.ProjectJpaRepository;
import com.kbj.contextory.project.repository.ProjectMemberJpaRepository;
import com.kbj.contextory.user.domain.User;
import com.kbj.contextory.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ProjectInvitationService {

    private static final Duration INVITATION_EXPIRATION = Duration.ofDays(7);

    private final ProjectInvitationJpaRepository invitationRepository;
    private final ProjectMemberJpaRepository projectMemberRepository;
    private final ProjectJpaRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectAccessChecker projectAccessChecker;
    private final ProjectInvitationTokenGenerator tokenGenerator;
    private final ProjectInvitationMailService invitationMailService;

    @Transactional
    public CreateProjectInvitationResponse createInvitation(
            Long projectId,
            Long userId,
            CreateProjectInvitationRequest request
    ) {
        projectAccessChecker.requireAdmin(projectId, userId);

        Project project = projectRepository.findById(projectId)
                .filter(found -> found.getStatus() != ProjectStatus.DELETED)
                .orElseThrow(() ->
                        GeneralException.of(ErrorCode.PROJECT_NOT_FOUND)
                );

        String inviteEmail = normalizeEmail(request.getInviteEmail());
        Instant now = Instant.now();

        expireOldPendingInvitation(projectId, inviteEmail, now);
        validateNotAlreadyActiveMember(projectId, inviteEmail);

        ProjectPermissionRole permissionRole =
                ProjectPermissionRole.valueOf(request.getPermissionRole());

        ProjectInvitation invitation = invitationRepository.save(
                ProjectInvitation.builder()
                        .projectId(projectId)
                        .inviteEmail(inviteEmail)
                        .permissionRole(permissionRole)
                        .projectRole(normalizeProjectRole(request.getProjectRole()))
                        .inviteToken(tokenGenerator.generate())
                        .expiresAt(now.plus(INVITATION_EXPIRATION))
                        .build()
        );

        // SMTP 발송 실패 시 예외를 발생시켜 초대 생성 Transaction도 롤백한다.
        invitationMailService.sendInvitation(project, invitation);

        // Billing/seat-limit 검사는 현재 MVP에서 제외.
        return CreateProjectInvitationResponse.from(invitation);
    }

    @Transactional(readOnly = true)
    public List<ProjectInvitationResponse> getInvitations(
            Long projectId,
            Long userId,
            String status
    ) {
        projectAccessChecker.requireAdmin(projectId, userId);

        ProjectInvitationStatus invitationStatus = ProjectInvitationStatus.valueOf(status);

        List<ProjectInvitation> invitations;

        if (invitationStatus == ProjectInvitationStatus.PENDING) {
            invitations = invitationRepository
                    .findAllByProjectIdAndStatusAndExpiresAtAfterOrderByInvitationIdDesc(
                            projectId,
                            invitationStatus,
                            Instant.now()
                    );
        } else {
            invitations = invitationRepository
                    .findAllByProjectIdAndStatusOrderByInvitationIdDesc(
                            projectId,
                            invitationStatus
                    );
        }

        return invitations.stream()
                .map(ProjectInvitationResponse::from)
                .toList();
    }

    @Transactional
    public AcceptProjectInvitationResponse acceptInvitation(
            String inviteToken,
            Long userId
    ) {
        ProjectInvitation invitation = invitationRepository.findByInviteToken(inviteToken)
                        .orElseThrow(() -> GeneralException.of(ErrorCode.PROJECT_INVITATION_NOT_FOUND));

        if (invitation.getStatus() != ProjectInvitationStatus.PENDING) {
            throw GeneralException.of(ErrorCode.PROJECT_INVITATION_ALREADY_PROCESSED);
        }

        if (invitation.isExpired(Instant.now())) {
            throw GeneralException.of(ErrorCode.PROJECT_INVITATION_EXPIRED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> GeneralException.of(ErrorCode.USER_NOT_FOUND));

        if (user.getLoginId() == null  || !user.getLoginId().equalsIgnoreCase(invitation.getInviteEmail())) {
            throw GeneralException.of(ErrorCode.PROJECT_INVITATION_EMAIL_MISMATCH);
        }

        Project project = projectRepository.findById(invitation.getProjectId())
                .filter(found -> found.getStatus() != ProjectStatus.DELETED)
                .orElseThrow(() -> GeneralException.of(ErrorCode.PROJECT_NOT_FOUND));

        ProjectMember member = projectMemberRepository
                .findByProjectIdAndUserId(project.getProjectId(), userId)
                .map(existing -> {
                    if (existing.getStatus() == ProjectMemberStatus.ACTIVE) {
                        throw GeneralException.of(ErrorCode.PROJECT_MEMBER_ALREADY_EXISTS);
                    }

                    existing.reactivate(
                            invitation.getPermissionRole(),
                            invitation.getProjectRole()
                    );
                    return existing;
                })
                .orElseGet(() -> projectMemberRepository.save(
                        ProjectMember.builder()
                                .projectId(project.getProjectId())
                                .userId(userId)
                                .permissionRole(invitation.getPermissionRole())
                                .projectRole(invitation.getProjectRole())
                                .status(ProjectMemberStatus.ACTIVE)
                                .build()
                        )
                );
        invitation.accept();

        // Billing/seat-limit 검사는 현재 MVP에서 제외.

        return AcceptProjectInvitationResponse.of(project, member);
    }

    @Transactional
    public void cancelInvitation(Long projectId, Long invitationId, Long userId) {
        projectAccessChecker.requireAdmin(projectId, userId);

        ProjectInvitation invitation = invitationRepository
                        .findByProjectIdAndInvitationId(projectId, invitationId)
                        .orElseThrow(() -> GeneralException.of(ErrorCode.PROJECT_INVITATION_NOT_FOUND)
                        );

        if (invitation.getStatus() != ProjectInvitationStatus.PENDING || invitation.isExpired(Instant.now())) {
            throw GeneralException.of(ErrorCode.PROJECT_INVITATION_ALREADY_PROCESSED);
        }

        invitation.cancel();
    }

    private void expireOldPendingInvitation(Long projectId, String inviteEmail, Instant now) {
        invitationRepository.findByProjectIdAndInviteEmailIgnoreCaseAndStatus(
                        projectId,
                        inviteEmail,
                        ProjectInvitationStatus.PENDING
                ).ifPresent(existing -> {
                    if (existing.isExpired(now)) {
                        existing.expire();
                        return;
                    }

                    throw GeneralException.of(ErrorCode.PROJECT_INVITATION_ALREADY_EXISTS);
                });
    }

    private void validateNotAlreadyActiveMember(Long projectId, String inviteEmail) {
        userRepository.findByLoginIdIgnoreCase(inviteEmail).ifPresent(user -> {
                    if (projectMemberRepository.existsByProjectIdAndUserIdAndStatus(
                                    projectId,
                                    user.getId(),
                                    ProjectMemberStatus.ACTIVE
                            )) {
                        throw GeneralException.of(ErrorCode.PROJECT_MEMBER_ALREADY_EXISTS);
                    }
                });
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeProjectRole(String projectRole) {
        return projectRole == null ? null : projectRole.trim();
    }
}
