package com.kbj.contextory.login.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoCodeRequest {
    private String code;
    private Boolean isDevelop;
}
