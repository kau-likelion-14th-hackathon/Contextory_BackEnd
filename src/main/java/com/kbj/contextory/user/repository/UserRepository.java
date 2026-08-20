package com.kbj.contextory.user.repository;

import com.kbj.contextory.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderId(String providerId);
    Optional<User> findByLoginId(String loginId);
    Optional<User> findByLoginIdIgnoreCase(String loginId);
}
