package com.kbj.contextory.github.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubApiInstallationPage {

    @JsonProperty("total_count")
    private Long totalCount;

    private List<GithubApiInstallation> installations = new ArrayList<>();
}
