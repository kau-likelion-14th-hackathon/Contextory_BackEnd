package com.kbj.contextory.user.dto.response;

import com.kbj.contextory.user.domain.User;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class AdminUserListResponse {
    private List<AdminUserResponse> content;

    public static AdminUserListResponse from(List<User> users) {
        return AdminUserListResponse.builder()
                .content(users.stream().map(AdminUserResponse::from).toList())
                .build();
    }
}
