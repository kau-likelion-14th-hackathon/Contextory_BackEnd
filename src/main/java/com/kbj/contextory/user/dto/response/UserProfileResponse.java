package com.kbj.contextory.user.dto.response;


import com.kbj.contextory.user.domain.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileResponse {
    private String username;
    private String profileImageUrl;
    private String introduction;

    public static UserProfileResponse from (User user) {
        return new UserProfileResponse(
                user.getUsername(),
                user.getProfileImage(),
                user.getIntroduction()
        );
    }
}
