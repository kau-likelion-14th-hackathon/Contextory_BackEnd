package com.kbj.contextory.github.pullrequest.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubApiPullRequestFile {

    private String sha;
    private String filename;
    private String status;
    private Integer additions;
    private Integer deletions;
    private Integer changes;
    private String patch;

    @JsonProperty("previous_filename")
    private String previousFilename;

    @JsonProperty("blob_url")
    private String blobUrl;

    @JsonProperty("raw_url")
    private String rawUrl;

    @JsonProperty("contents_url")
    private String contentsUrl;
}
