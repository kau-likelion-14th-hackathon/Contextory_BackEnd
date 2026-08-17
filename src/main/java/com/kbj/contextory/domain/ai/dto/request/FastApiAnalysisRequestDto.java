package com.kbj.contextory.domain.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "FastAPI 분석 요청 DTO")
public class FastApiAnalysisRequestDto {

    @NotNull
    @JsonProperty("analysis_id")
    @Schema(description = "Spring Boot 분석 ID", example = "1")
    private Long analysisId;

    @NotNull
    @JsonProperty("project_id")
    @Schema(description = "프로젝트 ID", example = "1")
    private Long projectId;

    @NotNull
    @JsonProperty("repository_id")
    @Schema(description = "저장소 연결 ID", example = "1")
    private Long repositoryId;

    @NotNull
    @JsonProperty("repository_full_name")
    @Schema(description = "GitHub 저장소 전체 이름", example = "org/contextory")
    private String repositoryFullName;

    @NotNull
    @JsonProperty("pull_request")
    @Schema(description = "PR 정보 및 변경 파일 목록")
    private PullRequestDto pullRequest;

    @NotNull
    @JsonProperty("language")
    @Schema(description = "분석 결과 언어 (ko/en)", example = "ko")
    private String language;

    // Spring Boot 서버가 analysisId 기준으로 직접 생성하는 값 — 클라이언트가 보낸 값은 역직렬화 시 무시됨
    @JsonProperty(value = "callback_url", access = JsonProperty.Access.READ_ONLY)
    @Schema(
            description = "분석 완료 콜백 URL (서버에서 자동 생성되며, 요청 값은 무시됩니다)",
            example = "http://localhost:8080/internal/v1/analyses/1/callback",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String callbackUrl;

    public FastApiAnalysisRequestDto withCallbackUrl(String callbackUrl) {
        return FastApiAnalysisRequestDto.builder()
                .analysisId(this.analysisId)
                .projectId(this.projectId)
                .repositoryId(this.repositoryId)
                .repositoryFullName(this.repositoryFullName)
                .pullRequest(this.pullRequest)
                .language(this.language)
                .callbackUrl(callbackUrl)
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "PR 메타데이터 및 파일 DTO")
    public static class PullRequestDto {
        @JsonProperty("github_pr_id")
        @Schema(description = "GitHub PR ID", example = "987654321")
        private Long githubPrId;

        @JsonProperty("pr_number")
        @Schema(description = "PR 번호", example = "18")
        private Integer prNumber;

        @JsonProperty("title")
        @Schema(description = "PR 제목", example = "JWT 로그인 기능 추가")
        private String title;

        @JsonProperty("body")
        @Schema(description = "PR 본문", example = "JWT 기반 인증 기능을 구현했습니다.")
        private String body;

        @JsonProperty("head_sha")
        @Schema(description = "분석 대상 커밋 SHA", example = "a123bc456def789")
        private String headSha;

        @JsonProperty("source_branch")
        @Schema(description = "소스 브랜치", example = "feature/login")
        private String sourceBranch;

        @JsonProperty("target_branch")
        @Schema(description = "대상 브랜치", example = "develop")
        private String targetBranch;

        @JsonProperty("files")
        @Schema(description = "변경된 파일 목록")
        private List<FileDto> files;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "PR 변경 파일 DTO")
    public static class FileDto {
        @JsonProperty("file_path")
        @Schema(description = "파일 경로", example = "src/main/java/auth/AuthService.java")
        private String filePath;

        @JsonProperty("change_type")
        @Schema(description = "변경 타입", example = "MODIFIED")
        private String changeType;

        @JsonProperty("patch")
        @Schema(description = "Diff Patch 문자열", example = "@@ -21,7 +21,18 @@ ...")
        private String patch;

        @JsonProperty("additions")
        @Schema(description = "추가된 라인 수", example = "50")
        private Integer additions;

        @JsonProperty("deletions")
        @Schema(description = "삭제된 라인 수", example = "10")
        private Integer deletions;
    }
}