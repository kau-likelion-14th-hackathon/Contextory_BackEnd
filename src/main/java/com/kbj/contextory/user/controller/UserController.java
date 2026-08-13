package com.kbj.contextory.user.controller;

import com.kbj.contextory.global.api.ApiResponse;
import com.kbj.contextory.global.api.SuccessCode;
import com.kbj.contextory.user.dto.request.UpdateMyInfoRequest;
import com.kbj.contextory.user.dto.response.UpdateMyInfoResponse;
import com.kbj.contextory.user.dto.response.UserInfoResponse;
import com.kbj.contextory.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 정보 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회")
    public ApiResponse<UserInfoResponse> getMyInfo(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        UserInfoResponse response = userService.getMyInfo(userId);
        return ApiResponse.onSuccess(SuccessCode.USER_INFO_GET_SUCCESS, response);
    }

    @PatchMapping("/me")
    @Operation(summary = "내 정보 수정")
    public ApiResponse<UpdateMyInfoResponse> updateMyInfo(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateMyInfoRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        UpdateMyInfoResponse response = userService.updateMyInfo(userId, request);
        return ApiResponse.onSuccess(SuccessCode.USER_PROFILE_UPDATE_SUCCESS, response);
    }
}
