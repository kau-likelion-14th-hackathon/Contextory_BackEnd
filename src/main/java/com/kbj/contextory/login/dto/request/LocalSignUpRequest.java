package com.kbj.contextory.login.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class LocalSignUpRequest {

    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    private String loginId;

    @NotBlank(message = "사용자 이름은 필수 입력 항목입니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    private String password;

    private String introduction;
}
