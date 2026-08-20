package com.kbj.contextory.login.repository;

import com.kbj.contextory.login.domain.RefreshToken;
import com.kbj.contextory.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken> findByUser(User user);
}