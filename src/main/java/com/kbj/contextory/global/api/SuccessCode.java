package com.kbj.contextory.global.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode implements BaseCode { // 성공
    OK(HttpStatus.OK, "COMMON_200", "Success"),
    CREATED(HttpStatus.CREATED, "COMMON_201", "Created"),
    ACCEPTED(HttpStatus.ACCEPTED, "COMMON_202", "Accepted"),

    USER_SIGNIN_SUCCESS(HttpStatus.CREATED, "USER_2011", "회원가입이 완료되었습니다."),
    USER_LOGOUT_SUCCESS(HttpStatus.OK, "USER_2001", "로그아웃 되었습니다."),
    USER_REISSUE_SUCCESS(HttpStatus.OK, "USER_2002", "토큰 재발급이 완료되었습니다."),
    USER_DELETE_SUCCESS(HttpStatus.OK, "USER_2003", "회원탈퇴가 완료되었습니다."),
    USER_PROFILE_UPDATE_SUCCESS(HttpStatus.OK, "USER_2006", "프로필 저장이 완료되었습니다."),
    USER_INFO_GET_SUCCESS(HttpStatus.OK, "USER_2007", "유저 정보 조회가 완료되었습니다."),
    USER_LOGIN_SUCCESS(HttpStatus.OK, "USER_2008", "로그인이 완료되었습니다."),

    PROFILE_PUT_SUCCESS(HttpStatus.OK, "PROFILE_2001", "프로필 수정(추가)이 완료되었습니다."),
    PROFILE_DELETE_SUCCESS(HttpStatus.OK, "PROFILE_2002", "프로필 삭제가 완료되었습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDTO getReason() {
        return ReasonDTO.builder()
                .httpStatus(this.httpStatus)
                .code(this.code)
                .message(this.message)
                .build();
    }
}
