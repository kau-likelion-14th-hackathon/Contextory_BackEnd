package com.kbj.contextory.domain.ai.record.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbj.contextory.domain.ai.entity.AiAnalysis;
import com.kbj.contextory.domain.ai.entity.AnalysisStatus;
import com.kbj.contextory.domain.ai.record.api.ProjectRecordErrorCode;
import com.kbj.contextory.domain.ai.record.dto.request.AiAnalysisEditRequest;
import com.kbj.contextory.domain.ai.record.dto.response.AiAnalysisRecordResponse;
import com.kbj.contextory.domain.ai.record.entity.ProjectRecord;
import com.kbj.contextory.domain.ai.record.entity.ProjectRecordStatus;
import com.kbj.contextory.domain.ai.record.repository.ProjectRecordRepository;
import com.kbj.contextory.domain.ai.repository.AiAnalysisRepository;
import com.kbj.contextory.github.support.ProjectAccessChecker;
import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.exception.GeneralException;
import com.kbj.contextory.project.domain.ProjectPermissionRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AiAnalysisRecordService {

    private final AiAnalysisRepository aiAnalysisRepository;
    private final ProjectRecordRepository projectRecordRepository;
    private final ProjectAccessChecker projectAccessChecker;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 1) AI 분석 수정
     *
     * AiAnalysis.resultJson 원본은 건드리지 않는다.
     * 사람이 수정한 내용은 project_record DRAFT에 별도로 저장한다.
     */
    @Transactional
    public AiAnalysisRecordResponse editAnalysis(
            Long projectId,
            Long analysisId,
            Long userId,
            AiAnalysisEditRequest request
    ) {
        projectAccessChecker.requireMemberOrAbove(projectId, userId);

        AiAnalysis analysis = getCompletedAnalysisForUpdate(projectId, analysisId);
        requireEditPermission(projectId, userId, analysis);

        String contentJson = serialize(request.getAnalysisResult());

        ProjectRecord record = projectRecordRepository
                .findByProjectIdAndAnalysisIdForUpdate(projectId, analysisId)
                .orElse(null);

        if (record == null) {
            record = ProjectRecord.createDraft(
                    projectId,
                    analysisId,
                    analysis.getPrNumber(),
                    contentJson,
                    userId
            );
            record = projectRecordRepository.save(record);
        } else {
            if (record.getStatus() == ProjectRecordStatus.APPROVED) {
                throw GeneralException.of(
                        ProjectRecordErrorCode.PROJECT_RECORD_ALREADY_APPROVED
                );
            }

            record.updateDraft(contentJson, userId);
        }

        return AiAnalysisRecordResponse.of(
                record,
                deserialize(record.getContentJson())
        );
    }

    /**
     * 2) AI 분석 승인
     *
     * 기존 DRAFT가 있으면 수정본을 승인하고,
     * DRAFT가 없으면 AI 원본 result_json을 그대로 승인한다.
     */
    @Transactional
    public AiAnalysisRecordResponse approveAnalysis(
            Long projectId,
            Long analysisId,
            Long userId
    ) {
        projectAccessChecker.requireMemberOrAbove(projectId, userId);

        AiAnalysis analysis = getCompletedAnalysisForUpdate(projectId, analysisId);

        ProjectRecord record = projectRecordRepository
                .findByProjectIdAndAnalysisIdForUpdate(projectId, analysisId)
                .orElse(null);

        if (record == null) {
            String originalResultJson = analysis.getResultJson();

            if (originalResultJson == null || originalResultJson.isBlank()) {
                throw GeneralException.of(
                        ProjectRecordErrorCode.PROJECT_RECORD_CONTENT_REQUIRED
                );
            }

            record = ProjectRecord.createDraft(
                    projectId,
                    analysisId,
                    analysis.getPrNumber(),
                    originalResultJson,
                    null
            );
            record = projectRecordRepository.save(record);
        }

        // 승인 API는 재호출되어도 같은 승인본을 반환하도록 멱등 처리
        if (record.getStatus() != ProjectRecordStatus.APPROVED) {
            record.approve(userId);
        }

        return AiAnalysisRecordResponse.of(
                record,
                deserialize(record.getContentJson())
        );
    }

    /**
     * 3) 승인된 분석 기록을 프로젝트 메모리로 등록
     *
     * 여기서는 memoryEnabled=true로 설정한다.
     * 이후 새로운 분석을 FastAPI에 보낼 때 AiInternalService가
     * memoryEnabled=true인 최근 승인 기록을 project_memories로 자동 첨부한다.
     */
    @Transactional
    public AiAnalysisRecordResponse registerMemory(
            Long projectId,
            Long analysisId,
            Long userId
    ) {
        projectAccessChecker.requireAdmin(projectId, userId);

        ProjectRecord record = projectRecordRepository
                .findByProjectIdAndAnalysisIdForUpdate(projectId, analysisId)
                .orElseThrow(() -> GeneralException.of(
                        ProjectRecordErrorCode.PROJECT_RECORD_NOT_FOUND
                ));

        if (record.getStatus() != ProjectRecordStatus.APPROVED) {
            throw GeneralException.of(
                    ProjectRecordErrorCode.PROJECT_RECORD_NOT_APPROVED
            );
        }

        // 재호출되어도 동일 상태 유지
        record.enableMemory(userId);

        return AiAnalysisRecordResponse.of(
                record,
                deserialize(record.getContentJson())
        );
    }

    private AiAnalysis getCompletedAnalysisForUpdate(
            Long projectId,
            Long analysisId
    ) {
        AiAnalysis analysis = aiAnalysisRepository
                .findByAnalysisIdAndProjectIdForUpdate(analysisId, projectId)
                .orElseThrow(() -> GeneralException.of(
                        ErrorCode.AI_ANALYSIS_NOT_FOUND
                ));

        if (analysis.getAnalysisStatus() != AnalysisStatus.COMPLETED) {
            throw GeneralException.of(
                    ProjectRecordErrorCode.PROJECT_RECORD_ANALYSIS_NOT_COMPLETED
            );
        }

        return analysis;
    }

    private void requireEditPermission(
            Long projectId,
            Long userId,
            AiAnalysis analysis
    ) {
        ProjectPermissionRole role = projectAccessChecker.getRole(projectId, userId);

        if (Objects.equals(analysis.getRequestedBy(), userId)) {
            return;
        }

        if (role == ProjectPermissionRole.OWNER
                || role == ProjectPermissionRole.ADMIN) {
            return;
        }

        throw GeneralException.of(
                ProjectRecordErrorCode.PROJECT_RECORD_EDIT_FORBIDDEN
        );
    }

    private String serialize(Object value) {
        if (value == null) {
            throw GeneralException.of(
                    ProjectRecordErrorCode.PROJECT_RECORD_CONTENT_REQUIRED
            );
        }

        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw GeneralException.of(
                    ProjectRecordErrorCode.PROJECT_RECORD_CONTENT_REQUIRED
            );
        }
    }

    private Object deserialize(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException exception) {
            return json;
        }
    }
}
