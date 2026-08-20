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

    /**
     * 사람이 승인하고 프로젝트 메모리에 등록한 과거 AI 분석 기록.
     * FastAPI는 이 값을 다음 PR 분석의 공식 프로젝트 컨텍스트로 사용할 수 있다.
     */
    @Builder.Default
    @JsonProperty("project_memories")
    @Schema(description = "프로젝트의 승인된 과거 AI 분석 기록")
    private List<ProjectMemoryDto> projectMemories = List.of();

    // Spring Boot 서버가 analysisId 기준으로 직접 생성하는 값
    @JsonProperty(value = "callback_url", access = JsonProperty.Access.READ_ONLY)
    @Schema(
            description = "분석 완료 콜백 URL (서버에서 자동 생성)",
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
                .projectMemories(
                        this.projectMemories == null
                                ? List.of()
                                : this.projectMemories
                )
                .callbackUrl(callbackUrl)
                .build();
    }

    public FastApiAnalysisRequestDto withProjectMemories(
            List<ProjectMemoryDto> projectMemories
    ) {
        return FastApiAnalysisRequestDto.builder()
                .analysisId(this.analysisId)
                .projectId(this.projectId)
                .repositoryId(this.repositoryId)
                .repositoryFullName(this.repositoryFullName)
                .pullRequest(this.pullRequest)
                .language(this.language)
                .projectMemories(
                        projectMemories == null
                                ? List.of()
                                : projectMemories
                )
                .callbackUrl(this.callbackUrl)
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "승인된 프로젝트 메모리 DTO")
    public static class ProjectMemoryDto {

        @JsonProperty("record_id")
        private Long recordId;

        @JsonProperty("pr_number")
        private Integer prNumber;

        @JsonProperty("content")
        private Object content;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "PR 메타데이터 및 파일 DTO")
    public static class PullRequestDto {

        @JsonProperty("github_pr_id")
        private Long githubPrId;

        @JsonProperty("pr_number")
        private Integer prNumber;

        @JsonProperty("title")
        private String title;

        @JsonProperty("body")
        private String body;

        @JsonProperty("head_sha")
        private String headSha;

        @JsonProperty("source_branch")
        private String sourceBranch;

        @JsonProperty("target_branch")
        private String targetBranch;

        @JsonProperty("files")
        private List<FileDto> files;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "PR 변경 파일 DTO")
    public static class FileDto {

        @JsonProperty("file_path")
        private String filePath;

        @JsonProperty("change_type")
        private String changeType;

        @JsonProperty("patch")
        private String patch;

        @JsonProperty("additions")
        private Integer additions;

        @JsonProperty("deletions")
        private Integer deletions;
    }
}
