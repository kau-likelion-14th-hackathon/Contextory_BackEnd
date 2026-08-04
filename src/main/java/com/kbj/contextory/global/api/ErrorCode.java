package com.kbj.contextory.global.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode implements BaseCode { // 실패
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 에러"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_4041", "존재하지 않는 회원입니다."),
    USER_NOT_FOUND_BY_EMAIL(HttpStatus.NOT_FOUND, "USER_4042", "EMAIL이 존재하지 않는 회원입니다."),
    USER_NOT_FOUND_BY_USERNAME(HttpStatus.NOT_FOUND, "USER_4043", "USERNAME이 존재하지 않는 회원입니다."),
    PASSWORD_NOT_MATCHED(HttpStatus.BAD_REQUEST, "USER_4002", "비밀번호가 일치하지 않습니다."),
    LOGIN_ID_DUPLICATED(HttpStatus.CONFLICT, "USER_4093", "이미 존재하는 아이디입니다."),

    // Login
    WRONG_REFRESH_TOKEN(HttpStatus.NOT_FOUND, "JWT_4041", "일치하는 refresh token이 없습니다."),
    IP_NOT_MATCHED(HttpStatus.FORBIDDEN, "JWT_4031", "refresh token의 IP주소가 일치하지 않습니다."),
    TOKEN_INVALID(HttpStatus.FORBIDDEN, "JWT_4032", "유효하지 않은 token입니다."),
    TOKEN_NO_AUTH(HttpStatus.FORBIDDEN, "JWT_4033", "권한 정보가 없는 token입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "JWT_4011", "token 유효기간이 만료되었습니다."),

    // Kakao Login
    KAKAO_AUTH_FAILED(HttpStatus.UNAUTHORIZED, "KAKAO_4011", "카카오 인증에 실패했습니다."),
    KAKAO_API_FAILED(HttpStatus.BAD_GATEWAY, "KAKAO_5021", "카카오 서버 응답에 실패했습니다."),

    // S3 Upload
    S3_UPLOAD_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "S3_5031", "파일 저장에 실패했습니다."),
    S3_KEY_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_5001", "파일 키 생성에 실패했습니다."),
    S3_DELETE_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "S3_5032", "파일 삭제에 실패했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    // 응답 코드 상세 정보 return
    @Override
    public ReasonDTO getReason() {
        return ReasonDTO.builder()
                .httpStatus(this.httpStatus)
                .code(this.code)
                .message(this.message)
                .build();
    }
}
