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

    // Project
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_4041", "프로젝트를 찾을 수 없습니다."),
    PROJECT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PROJECT_4031", "프로젝트 접근 권한이 없습니다."),
    PROJECT_ADMIN_REQUIRED(HttpStatus.FORBIDDEN, "PROJECT_4032", "프로젝트 OWNER 또는 ADMIN 권한이 필요합니다."),
    PROJECT_REPOSITORY_NOT_CONNECTED(HttpStatus.NOT_FOUND, "PROJECT_REPOSITORY_4041", "프로젝트에 연결된 GitHub 저장소가 없습니다."),
    PROJECT_OWNER_REQUIRED(HttpStatus.FORBIDDEN, "PROJECT_4033", "프로젝트 OWNER 권한이 필요합니다."),
    PROJECT_SLUG_DUPLICATED(HttpStatus.CONFLICT, "PROJECT_4091", "이미 사용 중인 프로젝트 slug입니다."),
    PROJECT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_MEMBER_4041", "프로젝트 멤버를 찾을 수 없습니다."),
    PROJECT_OWNER_MEMBER_UPDATE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PROJECT_MEMBER_4001", "OWNER 멤버의 권한과 역할은 수정할 수 없습니다."),
    PROJECT_OWNER_MEMBER_REMOVE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PROJECT_MEMBER_4002", "OWNER 멤버는 프로젝트에서 제거할 수 없습니다."),
    PROJECT_MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "PROJECT_MEMBER_4091", "이미 프로젝트에 참여 중인 사용자입니다."),
    PROJECT_INVITATION_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_INVITATION_4041", "프로젝트 초대를 찾을 수 없습니다."),
    PROJECT_INVITATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "PROJECT_INVITATION_4091", "해당 이메일로 대기 중인 초대가 이미 존재합니다."),
    PROJECT_INVITATION_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "PROJECT_INVITATION_4001", "이미 처리되었거나 취소할 수 없는 초대입니다."),
    PROJECT_INVITATION_EXPIRED(HttpStatus.BAD_REQUEST, "PROJECT_INVITATION_4002", "만료된 초대입니다."),
    PROJECT_INVITATION_EMAIL_MISMATCH(HttpStatus.FORBIDDEN, "PROJECT_INVITATION_4031", "초대 이메일과 로그인한 사용자 이메일이 일치하지 않습니다."),
    PROJECT_INVITATION_EMAIL_SEND_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "PROJECT_INVITATION_5031", "초대 이메일 발송에 실패했습니다."),

    // GitHub
    GITHUB_CONNECTION_REQUIRED(HttpStatus.UNAUTHORIZED, "GITHUB_4011", "GitHub 연결 또는 Access Token 설정이 필요합니다."),
    GITHUB_REPOSITORY_NOT_FOUND(HttpStatus.NOT_FOUND, "GITHUB_4041", "GitHub 저장소 또는 Pull Request를 찾을 수 없습니다."),
    GITHUB_REPOSITORY_ID_MISMATCH(HttpStatus.BAD_REQUEST, "GITHUB_4001", "GitHub 저장소 ID와 저장소 이름이 일치하지 않습니다."),
    GITHUB_API_FAILED(HttpStatus.BAD_GATEWAY, "GITHUB_5021", "GitHub API 호출에 실패했습니다."),
    GITHUB_REAUTHORIZATION_REQUIRED(HttpStatus.UNAUTHORIZED, "GITHUB_4012", "GitHub 연결이 만료되었습니다. 다시 연결해주세요."),
    GITHUB_OAUTH_STATE_INVALID(HttpStatus.BAD_REQUEST, "GITHUB_4001", "유효하지 않은 GitHub OAuth state입니다."),
    GITHUB_OAUTH_STATE_EXPIRED(HttpStatus.BAD_REQUEST, "GITHUB_4002", "GitHub OAuth state가 만료되었습니다. 다시 연결해주세요."),
    GITHUB_OAUTH_CODE_MISSING(HttpStatus.BAD_REQUEST, "GITHUB_4003", "GitHub OAuth code가 없습니다."),
    GITHUB_OAUTH_CODE_EXCHANGE_FAILED(HttpStatus.BAD_GATEWAY, "GITHUB_5021", "GitHub OAuth code를 Access Token으로 교환하지 못했습니다."),
    GITHUB_INSTALLATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "GITHUB_4004", "접근 가능한 GitHub App 설치 정보가 없습니다."),
    GITHUB_INSTALLATION_NOT_ACCESSIBLE(HttpStatus.FORBIDDEN, "GITHUB_4031", "해당 GitHub App 설치 정보에 접근할 수 없습니다."),
    GITHUB_ACCOUNT_ALREADY_CONNECTED(HttpStatus.CONFLICT, "GITHUB_4091", "해당 GitHub 계정은 다른 사용자에게 이미 연결되어 있습니다."),
    GITHUB_TOKEN_REFRESH_FAILED(HttpStatus.UNAUTHORIZED, "GITHUB_4013", "GitHub Access Token 갱신에 실패했습니다. 다시 연결해주세요."),
    GITHUB_TOKEN_ENCRYPT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "GITHUB_5001", "GitHub Token 암호화에 실패했습니다."),
    GITHUB_TOKEN_DECRYPT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "GITHUB_5002", "GitHub Token 복호화에 실패했습니다."),

    // Internal API
    INTERNAL_API_KEY_INVALID(HttpStatus.UNAUTHORIZED, "INTERNAL_4011", "유효하지 않은 Internal API Key입니다."),

    // AI Analysis
    AI_ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_ANALYSIS_4041", "분석 요청을 찾을 수 없습니다."),
    AI_ANALYSIS_INVALID_STATUS(HttpStatus.BAD_REQUEST, "AI_ANALYSIS_4001", "현재 상태에서는 해당 분석 작업을 수행할 수 없습니다."),
    AI_ANALYSIS_REPOSITORY_NOT_CONNECTED(HttpStatus.BAD_REQUEST, "AI_ANALYSIS_4002", "프로젝트에 GitHub 저장소가 연결되어 있지 않습니다."),
    AI_ANALYSIS_JOB_ID_MISMATCH(HttpStatus.CONFLICT, "AI_ANALYSIS_4091", "분석 작업 식별자(Job ID)가 일치하지 않습니다."),
    AI_ANALYSIS_INVALID_CALLBACK_STATUS(HttpStatus.BAD_REQUEST, "AI_ANALYSIS_4003", "허용되지 않는 콜백 상태입니다."),
    AI_ANALYSIS_SERVER_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI_ANALYSIS_5031", "FastAPI 분석 서버를 사용할 수 없습니다."),
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
