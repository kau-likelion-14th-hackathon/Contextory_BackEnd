package com.kbj.contextory.github.pullrequest.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubApiPullRequestUser {

    private String login;

    @JsonProperty("avatar_url")
    private String avatarUrl;
}
