package com.kbj.contextory.github.pullrequest.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PullRequestPageResponse {

    private List<PullRequestSummaryResponse> content;
    private int page;
    private int size;
    private boolean hasNext;
}
