package com.kbj.contextory.user.controller;

import com.kbj.contextory.global.api.ApiResponse;
import com.kbj.contextory.global.api.SuccessCode;
import com.kbj.contextory.user.dto.response.AdminUserListResponse;
import com.kbj.contextory.user.dto.response.AdminUserResponse;
import com.kbj.contextory.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin User", description = "관리자 사용자 조회 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "모든 유저 목록")
    public ApiResponse<AdminUserListResponse> getUsers() {
        return ApiResponse.onSuccess(SuccessCode.OK, userService.getUsers());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "유저 조회")
    public ApiResponse<AdminUserResponse> getUser(@PathVariable Long userId) {
        return ApiResponse.onSuccess(SuccessCode.OK, userService.getUser(userId));
    }
}
