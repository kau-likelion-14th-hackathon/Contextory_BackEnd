package com.kbj.contextory.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateProjectRequest {

    @NotBlank(message = "프로젝트 이름은 필수입니다.")
    @Size(max = 150, message = "프로젝트 이름은 150자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "프로젝트 slug는 필수입니다.")
    @Size(max = 150, message = "프로젝트 slug는 150자 이하여야 합니다.")
    private String slug;

    @Size(max = 500, message = "프로젝트 요약은 500자 이하여야 합니다.")
    private String summary;

    private String purpose;

    @Pattern(
            regexp = "^(ko|en)$",
            message = "기본 언어는 ko 또는 en이어야 합니다."
    )
    private String defaultLanguage;
}
