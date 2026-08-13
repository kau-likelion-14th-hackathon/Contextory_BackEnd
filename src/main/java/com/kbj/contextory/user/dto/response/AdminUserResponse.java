package com.kbj.contextory.user.dto.response;

import com.kbj.contextory.user.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminUserResponse {
    private Long userId;
    private String loginId;
    private String username;
    private String introduction;
    private String profileImage;

    public static AdminUserResponse from(User user) {
        return AdminUserResponse.builder()
                .userId(user.getId())
                .loginId(user.getLoginId())
                .username(user.getUsername())
                .introduction(user.getIntroduction())
                .profileImage(user.getProfileImage())
                .build();
    }
}
