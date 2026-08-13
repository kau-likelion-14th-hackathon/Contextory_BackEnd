    package com.kbj.contextory.login.service;

    import com.kbj.contextory.login.client.KakaoClient;
    import com.kbj.contextory.login.dto.request.LocalLoginRequest;
    import com.kbj.contextory.login.dto.request.LocalSignUpRequest;
    import com.kbj.contextory.login.dto.response.AuthResponse;
    import com.kbj.contextory.login.domain.RefreshToken;
    import com.kbj.contextory.login.jwt.JwtProvider;
    import com.kbj.contextory.login.repository.RefreshTokenRepository;
    import com.kbj.contextory.global.api.ErrorCode;
    import com.kbj.contextory.global.exception.GeneralException;
    import com.kbj.contextory.global.util.SHA256Util;
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

            // code -> 카카오 access_token
            String kakaoAccessToken = kakaoClient.getAccessToken(code,isDevelop);

            // access token -> kakao user info
            JsonNode kakaoUserInfo = kakaoClient.getUserInfo(kakaoAccessToken);

            String providerId = kakaoUserInfo.get("id").asText();
            String username = kakaoUserInfo
                    .path("kakao_account")
                    .path("profile")
                    .path("nickname")
                    .asText("유저");

            // 유저 조회 or 생성
            User user = userRepository.findByProviderId(providerId)
                    .orElseGet(() -> User.builder()
                            .providerId(providerId)
                            .username(username)
                            .build());

            User savedUser = userRepository.save(user);

            String accessToken = jwtProvider.createAccessToken(savedUser.getId());
            String refreshToken = jwtProvider.createRefreshToken(savedUser.getId());

            Long refreshTokenExpiration = jwtProvider.getRefreshTokenExpiration();

            // RefreshToken 저장/업데이트
            saveOrUpdateRefreshToken(savedUser, refreshToken, refreshTokenExpiration);

            // 응답 (유저 정보 + accessToken)
            return AuthResponse.from(savedUser, accessToken, refreshToken);
        }
        // 로컬 회원가입
        @Transactional
        public AuthResponse signUp(LocalSignUpRequest request) {
            // 1. 아이디 중복 체크
            if (userRepository.findByLoginId(request.getLoginId()).isPresent()) {
                throw new GeneralException(ErrorCode.LOGIN_ID_DUPLICATED);
            }

            // 2. 무작위 Salt 생성
            String salt = SHA256Util.generateSalt();

            // 3. Salt와 비밀번호를 조합하여 SHA-256 해시 및 키 스트레칭 적용
            String encryptedPassword = SHA256Util.getEncrypt(request.getPassword(), salt);

            // 4. 유저 생성 및 저장
            User user = User.builder()
                    .username(request.getUsername())
                    .loginId(request.getLoginId())
                    .introduction(request.getIntroduction())
                    .password(encryptedPassword)
                    .salt(salt)
                    .build();

            User savedUser = userRepository.save(user);

            // JWT 발급
            return issueTokens(savedUser);
        }

        // 로컬 로그인
        @Transactional
        public AuthResponse login(LocalLoginRequest request) {
            // 1. loginId로 유저 조회
            User user = userRepository.findByLoginId(request.getLoginId())
                    .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

            // 2. 해당 유저가 회원가입 시 생성된 Salt를 가져옴
            String salt = user.getSalt();
            if (salt == null || user.getPassword() == null) {
                throw new GeneralException(ErrorCode.BAD_REQUEST);
            }

            // 3. 입력받은 비밀번호를 유저의 Salt로 암호화
            String encryptedInputPassword = SHA256Util.getEncrypt(request.getPassword(), salt);

            // 4. 암호화된 값끼리 비교 검증
            if (!user.getPassword().equals(encryptedInputPassword)) {
                throw new GeneralException(ErrorCode.PASSWORD_NOT_MATCHED);
            }

            // JWT 발급
            return issueTokens(user);
        }

        // JWT 발급 및 RefreshToken 저장/업데이트
        private AuthResponse issueTokens(User user) {
            String accessToken = jwtProvider.createAccessToken(user.getId());
            String refreshToken = jwtProvider.createRefreshToken(user.getId());
            Long refreshTokenExpiration = jwtProvider.getRefreshTokenExpiration();

            saveOrUpdateRefreshToken(user, refreshToken, refreshTokenExpiration);

            return AuthResponse.from(user, accessToken, refreshToken);
        }

        @Transactional
        public String reissueAccessToken(String refreshToken) {
            // 1. Refresh Token 검증 및 userId 추출 (만료되었거나 위조되었으면 여기서 알아서 에러)
            Long userId;
            try {
                userId = jwtProvider.validateRefreshToken(refreshToken);
            } catch (Exception e) {
                throw new GeneralException(ErrorCode.TOKEN_INVALID); // 또는 TOKEN_EXPIRED
            }

            // 2. 유저 확인
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

            // 3. DB에 저장된 실제 Refresh Token과 방금 유저가 보낸 토큰이 똑같은지 비교
            RefreshToken savedToken = refreshTokenRepository.findByUser(user)
                    .orElseThrow(() -> new GeneralException(ErrorCode.WRONG_REFRESH_TOKEN));

            if (!savedToken.getRefreshToken().equals(refreshToken)) {
                throw new GeneralException(ErrorCode.TOKEN_INVALID);
            }

            // 4. 모든 검증을 통과했으니 새로운 Access Token 발급
            return jwtProvider.createAccessToken(userId);
        }

        private void saveOrUpdateRefreshToken(User user, String token, Long expiration) {
            refreshTokenRepository.findByUser(user)
                    .ifPresentOrElse(
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

        // userId로 refreshToken 삭제
        @Transactional
        public void logout(Long userId) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));
            RefreshToken saved = refreshTokenRepository.findByUser(user)
                    .orElseThrow(() -> new GeneralException(ErrorCode.WRONG_REFRESH_TOKEN));
            refreshTokenRepository.delete(saved);
        }

        // 유효한 accessToken 으로 인증 후 회원탈퇴
        @Transactional
        public void withdraw(Long userId) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));
            userRepository.delete(user);
        }
    }
