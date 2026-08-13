package com.kbj.contextory.github.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubApiUser {

    private Long id;
    private String login;

    @JsonProperty("avatar_url")
    private String avatarUrl;
}
