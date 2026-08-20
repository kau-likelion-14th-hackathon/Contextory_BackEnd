package com.kbj.contextory.user.dto.response;

import com.kbj.contextory.user.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateMyInfoResponse {
    private Long userId;
    private String username;
    private String introduction;

    public static UpdateMyInfoResponse from(User user) {
        return UpdateMyInfoResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .introduction(user.getIntroduction())
                .build();
    }
}
