package com.kbj.contextory.login.service;

import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.exception.GeneralException;
import com.kbj.contextory.global.util.SHA256Util;
import com.kbj.contextory.login.client.KakaoClient;
import com.kbj.contextory.login.domain.RefreshToken;
import com.kbj.contextory.login.dto.request.LocalLoginRequest;
import com.kbj.contextory.login.dto.request.LocalSignUpRequest;
import com.kbj.contextory.login.dto.response.AuthResponse;
import com.kbj.contextory.login.jwt.JwtProvider;
import com.kbj.contextory.login.repository.RefreshTokenRepository;
import com.kbj.contextory.user.domain.User;
import com.kbj.contextory.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoClient kakaoClient;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public AuthResponse handleKakaoCode(String code, Boolean isDevelop) {
        String kakaoAccessToken = kakaoClient.getAccessToken(code, isDevelop);
        JsonNode kakaoUserInfo = kakaoClient.getUserInfo(kakaoAccessToken);

        String providerId = kakaoUserInfo.get("id").asText();
        String username = kakaoUserInfo.path("kakao_account")
                .path("profile").path("nickname").asText("유저");

        User user = userRepository.findByProviderId(providerId)
                .orElseGet(() -> User.builder()
                        .providerId(providerId)
                        .username(username)
                        .build());

        User savedUser = userRepository.save(user);
        return issueTokens(savedUser);
    }

    @Transactional
    public AuthResponse signUp(LocalSignUpRequest request) {
        if (userRepository.findByLoginId(request.getLoginId()).isPresent()) {
            throw new GeneralException(ErrorCode.LOGIN_ID_DUPLICATED);
        }

        String salt = SHA256Util.generateSalt();
        String encryptedPassword = SHA256Util.getEncrypt(request.getPassword(), salt);

        User user = User.builder()
                .username(request.getUsername())
                .loginId(request.getLoginId())
                .introduction(request.getIntroduction())
                .password(encryptedPassword)
                .salt(salt)
                .build();

        User savedUser = userRepository.save(user);
        return issueTokens(savedUser);
    }

    @Transactional
    public AuthResponse login(LocalLoginRequest request) {
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        String salt = user.getSalt();
        if (salt == null || user.getPassword() == null) {
            throw new GeneralException(ErrorCode.BAD_REQUEST);
        }

        String encryptedInputPassword = SHA256Util.getEncrypt(request.getPassword(), salt);
        if (!user.getPassword().equals(encryptedInputPassword)) {
            throw new GeneralException(ErrorCode.PASSWORD_NOT_MATCHED);
        }

        return issueTokens(user);
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        Long refreshTokenExpiration = jwtProvider.getRefreshTokenExpiration();

        saveOrUpdateRefreshToken(user, refreshToken, refreshTokenExpiration);
        return AuthResponse.from(user, accessToken, refreshToken);
    }

    @Transactional
    public String reissueAccessToken(String refreshToken) {
        Long userId;
        try {
            userId = jwtProvider.validateRefreshToken(refreshToken);
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.TOKEN_INVALID);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        RefreshToken savedToken = refreshTokenRepository.findByUser(user)
                .orElseThrow(() -> new GeneralException(ErrorCode.WRONG_REFRESH_TOKEN));

        if (!savedToken.getRefreshToken().equals(refreshToken)) {
            throw new GeneralException(ErrorCode.TOKEN_INVALID);
        }

        return jwtProvider.createAccessToken(user.getId(), user.getRole());
    }

    private void saveOrUpdateRefreshToken(User user, String token, Long expiration) {
        refreshTokenRepository.findByUser(user).ifPresentOrElse(
                existing -> existing.updateToken(token, expiration),
                () -> refreshTokenRepository.save(
                        RefreshToken.builder()
                                .user(user)
                                .refreshToken(token)
                                .refreshTokenExpiration(expiration)
                                .build()
                )
        );
    }

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));
        RefreshToken saved = refreshTokenRepository.findByUser(user)
                .orElseThrow(() -> new GeneralException(ErrorCode.WRONG_REFRESH_TOKEN));
        refreshTokenRepository.delete(saved);
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }
}
