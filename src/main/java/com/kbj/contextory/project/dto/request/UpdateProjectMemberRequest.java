package com.kbj.contextory.project.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateProjectMemberRequest {

    @Pattern(
            regexp = "^(ADMIN|MEMBER|VIEWER)$",
            message = "permissionRole은 ADMIN, MEMBER, VIEWER 중 하나여야 합니다."
    )
    private String permissionRole;

    @Size(max = 50, message = "projectRole은 50자 이하여야 합니다.")
    @Pattern(
            regexp = ".*\\S.*",
            message = "projectRole은 공백일 수 없습니다."
    )
    private String projectRole;
}
