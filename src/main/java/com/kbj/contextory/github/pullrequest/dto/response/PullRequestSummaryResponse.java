package com.kbj.contextory.github.pullrequest.dto.response;

import com.kbj.contextory.github.pullrequest.client.GithubApiPullRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class PullRequestSummaryResponse {

    private Integer prNumber;
    private String title;
    private String state;
    private Boolean draft;
    private String authorLogin;
    private String authorAvatarUrl;
    private String htmlUrl;
    private String sourceBranch;
    private String targetBranch;
    private Instant createdAt;
    private Instant updatedAt;

    public static PullRequestSummaryResponse from(GithubApiPullRequest pullRequest) {
        return PullRequestSummaryResponse.builder()
                .prNumber(pullRequest.getNumber())
                .title(pullRequest.getTitle())
                .state(pullRequest.getState())
                .draft(pullRequest.getDraft())
                .authorLogin(pullRequest.getUser() == null ? null : pullRequest.getUser().getLogin())
                .authorAvatarUrl(pullRequest.getUser() == null ? null : pullRequest.getUser().getAvatarUrl())
                .htmlUrl(pullRequest.getHtmlUrl())
                .sourceBranch(pullRequest.getHead() == null ? null : pullRequest.getHead().getRef())
                .targetBranch(pullRequest.getBase() == null ? null : pullRequest.getBase().getRef())
                .createdAt(pullRequest.getCreatedAt())
                .updatedAt(pullRequest.getUpdatedAt())
                .build();
    }
}
