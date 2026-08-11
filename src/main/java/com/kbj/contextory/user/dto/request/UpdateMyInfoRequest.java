package com.kbj.contextory.user.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateMyInfoRequest {

    @Size(max = 255, message = "사용자명은 255자 이하여야 합니다.")
    @Pattern(regexp = ".*\\S.*", message = "사용자명은 공백일 수 없습니다.")
    private String username;

    private String introduction;
}
