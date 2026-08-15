package com.kbj.contextory.github.pullrequest.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubApiPullRequest {

    private Integer number;
    private String state;
    private String title;
    private String body;
    private Boolean draft;
    private Boolean merged;
    private Boolean mergeable;

    @JsonProperty("mergeable_state")
    private String mergeableState;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    @JsonProperty("closed_at")
    private Instant closedAt;

    @JsonProperty("merged_at")
    private Instant mergedAt;

    private Integer additions;
    private Integer deletions;

    @JsonProperty("changed_files")
    private Integer changedFiles;

    private Integer commits;
    private Integer comments;

    @JsonProperty("review_comments")
    private Integer reviewComments;

    private GithubApiPullRequestUser user;
    private GithubApiPullRequestRef head;
    private GithubApiPullRequestRef base;
}
