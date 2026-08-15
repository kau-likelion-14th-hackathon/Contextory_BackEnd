package com.kbj.contextory.github.pullrequest.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PullRequestFilesResponse {

    private Integer prNumber;
    private int totalFiles;
    private int totalAdditions;
    private int totalDeletions;
    private List<PullRequestFileResponse> files;
}
