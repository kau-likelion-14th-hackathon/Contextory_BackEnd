package com.kbj.contextory.domain.ai.record.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbj.contextory.domain.ai.record.dto.response.ProjectMemoryListItemResponse;
import com.kbj.contextory.domain.ai.record.dto.response.ProjectMemoryListResponse;
import com.kbj.contextory.domain.ai.record.entity.ProjectRecord;
import com.kbj.contextory.domain.ai.record.entity.ProjectRecordStatus;
import com.kbj.contextory.domain.ai.record.repository.ProjectRecordRepository;
import com.kbj.contextory.github.support.ProjectAccessChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectMemoryService {

    private final ProjectRecordRepository projectRecordRepository;
    private final ProjectAccessChecker projectAccessChecker;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public ProjectMemoryListResponse getMemories(
            Long projectId,
            Long userId,
            int page,
            int size
    ) {
        projectAccessChecker.requireMemberOrAbove(projectId, userId);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "approvedAt")
        );

        Page<ProjectRecord> memoryPage = projectRecordRepository
                .findAllByProjectIdAndStatusAndMemoryEnabledTrue(
                        projectId,
                        ProjectRecordStatus.APPROVED,
                        pageable
                );

        List<ProjectMemoryListItemResponse> content =
                memoryPage.getContent()
                        .stream()
                        .map(record ->
                                ProjectMemoryListItemResponse.of(
                                        record,
                                        parseContentJson(record.getContentJson())
                                )
                        )
                        .toList();

        return ProjectMemoryListResponse.builder()
                .content(content)
                .page(memoryPage.getNumber())
                .size(memoryPage.getSize())
                .totalElements(memoryPage.getTotalElements())
                .totalPages(memoryPage.getTotalPages())
                .hasNext(memoryPage.hasNext())
                .build();
    }

    private Object parseContentJson(String contentJson) {
        if (contentJson == null || contentJson.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(contentJson, Object.class);
        } catch (JsonProcessingException exception) {
            return contentJson;
        }
    }
}
