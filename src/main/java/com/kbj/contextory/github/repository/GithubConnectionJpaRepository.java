package com.kbj.contextory.github.repository;

import com.kbj.contextory.github.domain.GithubConnection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GithubConnectionJpaRepository
        extends JpaRepository<GithubConnection, Long> {

    Optional<GithubConnection> findByUserId(Long userId);
    boolean existsByGithubUserIdAndUserIdNot(Long githubUserId, Long userId);
}
