package com.kbj.contextory.login.controller;

import com.kbj.contextory.login.dto.request.KakaoCodeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import com.kbj.contextory.login.dto.request.LocalLoginRequest;
import com.kbj.contextory.login.dto.request.LocalSignUpRequest;
import com.kbj.contextory.login.dto.response.AuthResponse;
import com.kbj.contextory.login.service.AuthService;
import com.kbj.contextory.global.api.ApiResponse;
import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.api.SuccessCode;
import com.kbj.contextory.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "자체 로그인 및 회원가입 API 입니다")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/kakao")
    @Operation(
            summary = "카카오 로그인 처리",
            description = "인가 코드를 이용하여 카카오 로그인 후 JWT를 발급합니다."
    )
    public ApiResponse<AuthResponse> kakaoLogin(
            @RequestBody KakaoCodeRequest request,
            HttpServletResponse httpResponse
    ) {
        AuthResponse response = authService.handleKakaoCode(request.getCode(), request.getIsDevelop());
        ResponseCookie cookie = createRefreshTokenCookie(response.getRefreshToken());
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.onSuccess(SuccessCode.USER_LOGIN_SUCCESS, response);
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "솔트해시 기반 자체 회원가입 API")
    public ApiResponse<AuthResponse> signUp(
            @Valid @RequestBody LocalSignUpRequest request,
            HttpServletResponse httpResponse
    ) {
        AuthResponse response = authService.signUp(request);

        // Refresh Token은 쿠키로 만듬
        ResponseCookie cookie = createRefreshTokenCookie(response.getRefreshToken());

        // 직접 받아온 httpResponse 객체의 헤더에 쿠키를 꽂아 넣음
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.onSuccess(SuccessCode.USER_SIGNIN_SUCCESS, response);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "솔트해시 기반 자체 로그인 API")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LocalLoginRequest request,
            HttpServletResponse httpResponse
    ) {
        AuthResponse response = authService.login(request);

        ResponseCookie cookie = createRefreshTokenCookie(response.getRefreshToken());
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.onSuccess(SuccessCode.USER_LOGIN_SUCCESS, response);
    }

    @PostMapping("/reissue")
    @Operation(summary = "accessToken 재발급", description = "HttpOnly 쿠키에 담긴 Refresh Token을 통해 재발급합니다.")
    public ApiResponse<String> reissue(
            @Parameter(hidden = true)
            @CookieValue(value = "refresh_token", required = false) String refreshToken
    ) {
        if (refreshToken == null) {
            throw new GeneralException(ErrorCode.TOKEN_INVALID);
        }

        String newAccessToken = authService.reissueAccessToken(refreshToken);

        return ApiResponse.onSuccess(SuccessCode.USER_REISSUE_SUCCESS, newAccessToken);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "DB의 Refresh Token을 지우고, 브라우저 쿠키도 비웁니다.")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletResponse httpResponse
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        authService.logout(userId);

        // 브라우저에 남아있는 쿠키를 강제로 덮어씌워서 삭제(수명 0초)
        ResponseCookie expiredCookie = ResponseCookie.from("refresh_token", "")
                .maxAge(0)
                .secure(true)
                .sameSite("None")
                .path("/")
                .build();
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());

        return ApiResponse.onSuccess(SuccessCode.USER_LOGOUT_SUCCESS, null);
    }

    @DeleteMapping("/withdraw")
    @Operation(summary = "회원탈퇴")
    public ApiResponse<Void> withdraw(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        authService.withdraw(userId);
        return ApiResponse.onSuccess(SuccessCode.USER_DELETE_SUCCESS, null);
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)    // 자바스크립트 접근 불가 (XSS 방어)
                .secure(true)     // HTTPS 적용 전에는 false로 둬야 로컬에서 쿠키가 구워짐 (나중에 배포 시 true로 변경)
                .sameSite("None")   // CSRF 방어용 (프론트/백 도메인이 다르면 환경에 따라 None 설정 필요)
                .maxAge(14 * 24 * 60 * 60) // 14일 (초 단위)
                .path("/")
                .build();
    }
}