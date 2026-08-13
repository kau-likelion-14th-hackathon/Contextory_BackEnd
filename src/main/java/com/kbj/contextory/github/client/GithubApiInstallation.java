package com.kbj.contextory.github.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubApiInstallation {

    private Long id;
    private Account account;

    @JsonProperty("repository_selection")
    private String repositorySelection;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Account {
        private Long id;
        private String login;
        private String type;
    }
}
