package com.kbj.contextory.project.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProjectListResponse {

    private List<ProjectSummaryResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private boolean hasNext;
}
