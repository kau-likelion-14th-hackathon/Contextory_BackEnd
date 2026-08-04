package com.kbj.contextory.user.controller;

import com.kbj.contextory.global.api.ApiResponse;
import com.kbj.contextory.global.api.SuccessCode;
import com.kbj.contextory.user.dto.request.CreateTestUserRequest;
import com.kbj.contextory.user.dto.response.UserProfileResponse;
import com.kbj.contextory.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/profile")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileController {
    public final UserProfileService userProfileService;

    @GetMapping
    @Operation(summary = "유저 프로필 조회", description = "유저아이디를 받아 유저 프로필을 반환하는 api 입니다")
    public ApiResponse<UserProfileResponse> getUserProfile(
            @RequestParam Long userId
    ){
        UserProfileResponse userProfileResponse = userProfileService.getUserProfile(userId);

        return ApiResponse.onSuccess(SuccessCode.OK, userProfileResponse);
    }

    @PostMapping
    @Operation(summary = "테스트 유저를 생성", description = "이름, 한줄소개를 받아 유저를 생성")
    public ApiResponse<UserProfileResponse> createTestUserProfile(
            @RequestBody CreateTestUserRequest createTestUserRequest
    ){
        UserProfileResponse response = userProfileService.createTestUser(createTestUserRequest);
        return ApiResponse.onSuccess(SuccessCode.CREATED, response);
    }
}
