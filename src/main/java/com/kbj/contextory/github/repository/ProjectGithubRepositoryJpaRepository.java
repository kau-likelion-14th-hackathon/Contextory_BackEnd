package com.kbj.contextory.github.repository;

import com.kbj.contextory.github.domain.ProjectGithubRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectGithubRepositoryJpaRepository
        extends JpaRepository<ProjectGithubRepository, Long> {

    Optional<ProjectGithubRepository> findByProjectId(Long projectId);
}
