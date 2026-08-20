package com.kbj.contextory.user.dto.response;

import com.kbj.contextory.user.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponse {
    private Long userId;
    private String loginId;
    private String username;
    private String introduction;
    private String profileImage;

    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
                .userId(user.getId())
                .loginId(user.getLoginId())
                .username(user.getUsername())
                .introduction(user.getIntroduction())
                .profileImage(user.getProfileImage())
                .build();
    }
}
