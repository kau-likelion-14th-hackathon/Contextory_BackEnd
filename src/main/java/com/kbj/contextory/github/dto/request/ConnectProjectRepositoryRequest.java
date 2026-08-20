package com.kbj.contextory.github.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ConnectProjectRepositoryRequest {

    @NotNull(message = "GitHub 저장소 ID는 필수입니다.")
    private Long githubRepositoryId;

    @NotBlank(message = "저장소 전체 이름은 필수입니다.")
    @Pattern(
            regexp = "^[^/\\s]+/[^/\\s]+$",
            message = "저장소 이름은 owner/repository 형식이어야 합니다."
    )
    private String repositoryFullName;
}
