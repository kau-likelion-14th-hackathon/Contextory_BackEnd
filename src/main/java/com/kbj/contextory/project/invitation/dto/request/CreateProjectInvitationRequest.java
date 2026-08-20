package com.kbj.contextory.project.invitation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateProjectInvitationRequest {

    @NotBlank(message = "초대 이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    private String inviteEmail;

    @NotBlank(message = "permissionRole은 필수입니다.")
    @Pattern(
            regexp = "^(MEMBER|VIEWER)$",
            message = "permissionRole은 MEMBER 또는 VIEWER여야 합니다."
    )
    private String permissionRole;

    @Size(max = 50, message = "projectRole은 50자 이하여야 합니다.")
    @Pattern(
            regexp = ".*\\S.*",
            message = "projectRole은 공백일 수 없습니다."
    )
    private String projectRole;
}
