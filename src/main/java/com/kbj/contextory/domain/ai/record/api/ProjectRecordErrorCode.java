package com.kbj.contextory.domain.ai.record.api;

import com.kbj.contextory.global.api.BaseCode;
import com.kbj.contextory.global.api.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProjectRecordErrorCode implements BaseCode {

    PROJECT_RECORD_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PROJECT_RECORD_4041",
            "AI 분석 승인 기록을 찾을 수 없습니다."
    ),
    PROJECT_RECORD_ANALYSIS_NOT_COMPLETED(
            HttpStatus.BAD_REQUEST,
            "PROJECT_RECORD_4001",
            "완료된 AI 분석만 수정하거나 승인할 수 있습니다."
    ),
    PROJECT_RECORD_CONTENT_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "PROJECT_RECORD_4002",
            "저장할 AI 분석 결과가 없습니다."
    ),
    PROJECT_RECORD_NOT_APPROVED(
            HttpStatus.BAD_REQUEST,
            "PROJECT_RECORD_4003",
            "승인된 AI 분석 기록만 프로젝트 메모리에 등록할 수 있습니다."
    ),
    PROJECT_RECORD_EDIT_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "PROJECT_RECORD_4031",
            "해당 AI 분석 결과를 수정할 권한이 없습니다."
    ),
    PROJECT_RECORD_ALREADY_APPROVED(
            HttpStatus.CONFLICT,
            "PROJECT_RECORD_4091",
            "이미 승인된 AI 분석 기록은 수정할 수 없습니다."
    );

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
