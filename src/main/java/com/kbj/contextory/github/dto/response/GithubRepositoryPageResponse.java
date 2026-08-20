package com.kbj.contextory.github.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GithubRepositoryPageResponse {

    private List<GithubRepositorySummaryResponse> content;
    private int page;
    private int size;
    private boolean hasNext;
}
