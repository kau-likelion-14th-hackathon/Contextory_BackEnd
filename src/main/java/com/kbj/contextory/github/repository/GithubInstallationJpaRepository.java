package com.kbj.contextory.github.repository;

import com.kbj.contextory.github.domain.GithubInstallation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GithubInstallationJpaRepository
        extends JpaRepository<GithubInstallation, Long> {

    List<GithubInstallation> findAllByUserIdOrderByAccountLoginAsc(Long userId);
    void deleteAllByUserId(Long userId);
}
