package com.kbj.contextory.project.repository;

import com.kbj.contextory.project.domain.Project;
import com.kbj.contextory.project.domain.ProjectMemberStatus;
import com.kbj.contextory.project.domain.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectJpaRepository extends JpaRepository<Project, Long> {

    boolean existsBySlug(String slug);

    @Query("""
            select p
            from Project p
            where p.status = :projectStatus
              and (
                    p.ownerId = :userId
                    or exists (
                        select pm.projectMemberId
                        from ProjectMember pm
                        where pm.projectId = p.projectId
                          and pm.userId = :userId
                          and pm.status = :memberStatus
                    )
              )
            """)
    Page<Project> findMyProjects(
            @Param("userId") Long userId,
            @Param("projectStatus") ProjectStatus projectStatus,
            @Param("memberStatus") ProjectMemberStatus memberStatus,
            Pageable pageable
    );
}
