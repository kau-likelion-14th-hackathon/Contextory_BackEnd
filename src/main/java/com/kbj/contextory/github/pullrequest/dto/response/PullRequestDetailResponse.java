package com.kbj.contextory.github.pullrequest.dto.response;

import com.kbj.contextory.github.pullrequest.client.GithubApiPullRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class PullRequestDetailResponse {

    private Integer prNumber;
    private String title;
    private String body;
    private String state;
    private Boolean draft;
    private Boolean merged;
    private Boolean mergeable;
    private String mergeableState;

    private String authorLogin;
    private String authorAvatarUrl;
    private String htmlUrl;

    private String sourceBranch;
    private String sourceSha;
    private String targetBranch;
    private String targetSha;

    private Integer additions;
    private Integer deletions;
    private Integer changedFiles;
    private Integer commits;
    private Integer comments;
    private Integer reviewComments;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant closedAt;
    private Instant mergedAt;

    public static PullRequestDetailResponse from(GithubApiPullRequest pullRequest) {
        return PullRequestDetailResponse.builder()
                .prNumber(pullRequest.getNumber())
                .title(pullRequest.getTitle())
                .body(pullRequest.getBody())
                .state(pullRequest.getState())
                .draft(pullRequest.getDraft())
                .merged(pullRequest.getMerged())
                .mergeable(pullRequest.getMergeable())
                .mergeableState(pullRequest.getMergeableState())
                .authorLogin(pullRequest.getUser() == null ? null : pullRequest.getUser().getLogin())
                .authorAvatarUrl(pullRequest.getUser() == null ? null : pullRequest.getUser().getAvatarUrl())
                .htmlUrl(pullRequest.getHtmlUrl())
                .sourceBranch(pullRequest.getHead() == null ? null : pullRequest.getHead().getRef())
                .sourceSha(pullRequest.getHead() == null ? null : pullRequest.getHead().getSha())
                .targetBranch(pullRequest.getBase() == null ? null : pullRequest.getBase().getRef())
                .targetSha(pullRequest.getBase() == null ? null : pullRequest.getBase().getSha())
                .additions(pullRequest.getAdditions())
                .deletions(pullRequest.getDeletions())
                .changedFiles(pullRequest.getChangedFiles())
                .commits(pullRequest.getCommits())
                .comments(pullRequest.getComments())
                .reviewComments(pullRequest.getReviewComments())
                .createdAt(pullRequest.getCreatedAt())
                .updatedAt(pullRequest.getUpdatedAt())
                .closedAt(pullRequest.getClosedAt())
                .mergedAt(pullRequest.getMergedAt())
                .build();
    }
}
