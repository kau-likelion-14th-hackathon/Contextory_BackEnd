package com.kbj.contextory.project.repository;

import com.kbj.contextory.project.domain.ProjectMember;
import com.kbj.contextory.project.domain.ProjectMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberJpaRepository
        extends JpaRepository<ProjectMember, Long> {

    Optional<ProjectMember> findByProjectIdAndUserIdAndStatus(
            Long projectId,
            Long userId,
            ProjectMemberStatus status
    );

    List<ProjectMember> findAllByUserIdAndStatus(
            Long userId,
            ProjectMemberStatus status
    );

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
}
