package com.kbj.contextory.github.pullrequest.dto.response;

import com.kbj.contextory.github.pullrequest.client.GithubApiPullRequestFile;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PullRequestFileResponse {

    private String sha;
    private String filename;
    private String previousFilename;
    private String status;
    private Integer additions;
    private Integer deletions;
    private Integer changes;
    private String patch;
    private String blobUrl;
    private String rawUrl;
    private String contentsUrl;

    public static PullRequestFileResponse from(GithubApiPullRequestFile file) {
        return PullRequestFileResponse.builder()
                .sha(file.getSha())
                .filename(file.getFilename())
                .previousFilename(file.getPreviousFilename())
                .status(file.getStatus())
                .additions(file.getAdditions())
                .deletions(file.getDeletions())
                .changes(file.getChanges())
                .patch(file.getPatch())
                .blobUrl(file.getBlobUrl())
                .rawUrl(file.getRawUrl())
                .contentsUrl(file.getContentsUrl())
                .build();
    }
}
