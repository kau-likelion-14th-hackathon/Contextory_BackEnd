package com.kbj.contextory.project.invitation.repository;

import com.kbj.contextory.project.invitation.domain.ProjectInvitation;
import com.kbj.contextory.project.invitation.domain.ProjectInvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ProjectInvitationJpaRepository
        extends JpaRepository<ProjectInvitation, Long> {

    Optional<ProjectInvitation> findByInviteToken(String inviteToken);

    Optional<ProjectInvitation> findByProjectIdAndInviteEmailIgnoreCaseAndStatus(
            Long projectId,
            String inviteEmail,
            ProjectInvitationStatus status
    );

    Optional<ProjectInvitation> findByProjectIdAndInvitationId(
            Long projectId,
            Long invitationId
    );

    List<ProjectInvitation>
    findAllByProjectIdAndStatusAndExpiresAtAfterOrderByInvitationIdDesc(
            Long projectId,
            ProjectInvitationStatus status,
            Instant now
    );

    List<ProjectInvitation> findAllByProjectIdAndStatusOrderByInvitationIdDesc(
            Long projectId,
            ProjectInvitationStatus status
    );
}
