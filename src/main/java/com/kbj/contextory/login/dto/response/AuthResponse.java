package com.kbj.contextory.login.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kbj.contextory.user.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private Long id;
    private String loginId;
    private String username;
    private String introduction;
    private String profileImage;
    private String accessToken;

    // 프론트에 안넘겨줌
    @JsonIgnore
    private String refreshToken;

    public static AuthResponse from(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .id(user.getId())
                .loginId(user.getLoginId())
                .username(user.getUsername())
                .introduction(user.getIntroduction())
                .profileImage(user.getProfileImage())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
