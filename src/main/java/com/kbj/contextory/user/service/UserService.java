package com.kbj.contextory.user.service;

import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.exception.GeneralException;
import com.kbj.contextory.user.domain.User;
import com.kbj.contextory.user.dto.request.UpdateMyInfoRequest;
import com.kbj.contextory.user.dto.response.AdminUserListResponse;
import com.kbj.contextory.user.dto.response.AdminUserResponse;
import com.kbj.contextory.user.dto.response.UpdateMyInfoResponse;
import com.kbj.contextory.user.dto.response.UserInfoResponse;
import com.kbj.contextory.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserInfoResponse getMyInfo(Long userId) {
        return UserInfoResponse.from(getUserEntity(userId));
    }

    @Transactional
    public UpdateMyInfoResponse updateMyInfo(Long userId, UpdateMyInfoRequest request) {
        User user = getUserEntity(userId);
        user.updateProfile(normalizeUsername(request.getUsername()), request.getIntroduction());
        return UpdateMyInfoResponse.from(user);
    }

    @Transactional(readOnly = true)
    public AdminUserListResponse getUsers() {
        return AdminUserListResponse.from(userRepository.findAll());
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUser(Long userId) {
        return AdminUserResponse.from(getUserEntity(userId));
    }

    private User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> GeneralException.of(ErrorCode.USER_NOT_FOUND));
    }

    private String normalizeUsername(String username) {
        return username == null ? null : username.trim();
    }
}
