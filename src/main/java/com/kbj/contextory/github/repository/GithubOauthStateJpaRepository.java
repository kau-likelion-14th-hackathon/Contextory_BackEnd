package com.kbj.contextory.github.repository;

import com.kbj.contextory.github.domain.GithubOauthState;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GithubOauthStateJpaRepository
        extends JpaRepository<GithubOauthState, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from GithubOauthState s where s.stateHash = :stateHash")
    Optional<GithubOauthState> findByStateHashForUpdate(
            @Param("stateHash") String stateHash
    );
}
