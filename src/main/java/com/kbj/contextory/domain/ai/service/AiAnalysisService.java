package com.kbj.contextory.domain.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbj.contextory.domain.ai.dto.request.AiAnalysisCreateRequest;
import com.kbj.contextory.domain.ai.dto.request.FastApiAnalysisRequestDto;
import com.kbj.contextory.domain.ai.dto.response.AiAnalysisCancelResponse;
import com.kbj.contextory.domain.ai.dto.response.AiAnalysisDetailResponse;
import com.kbj.contextory.domain.ai.dto.response.AiAnalysisListItemResponse;
import com.kbj.contextory.domain.ai.dto.response.AiAnalysisListResponse;
import com.kbj.contextory.domain.ai.dto.response.AiAnalysisRequestResponse;
import com.kbj.contextory.domain.ai.dto.response.AiAnalysisRetryResponse;
import com.kbj.contextory.domain.ai.entity.AiAnalysis;
import com.kbj.contextory.domain.ai.entity.AnalysisStatus;
import com.kbj.contextory.domain.ai.record.entity.ProjectRecord;
import com.kbj.contextory.domain.ai.record.repository.ProjectRecordRepository;
import com.kbj.contextory.domain.ai.repository.AiAnalysisRepository;
import com.kbj.contextory.github.domain.ProjectGithubRepository;
import com.kbj.contextory.github.pullrequest.client.GithubApiPullRequest;
import com.kbj.contextory.github.pullrequest.client.GithubApiPullRequestFile;
import com.kbj.contextory.github.pullrequest.client.GithubPullRequestApiClient;
import com.kbj.contextory.github.repository.ProjectGithubRepositoryJpaRepository;
import com.kbj.contextory.github.support.GithubUserAccessTokenProvider;
import com.kbj.contextory.github.support.ProjectAccessChecker;
import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.exception.GeneralException;
import com.kbj.contextory.project.domain.Project;
import com.kbj.contextory.project.domain.ProjectPermissionRole;
import com.kbj.contextory.project.repository.ProjectJpaRepository;
import com.kbj.contextory.user.domain.User;
import com.kbj.contextory.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisService {

    private static final String DEFAULT_LANGUAGE = "ko";
    private static final String UNKNOWN_USERNAME = "알 수 없는 사용자";

    private final AiAnalysisRepository aiAnalysisRepository;
    private final AiInternalService aiInternalService;
    private final ProjectJpaRepository projectJpaRepository;
    private final ProjectGithubRepositoryJpaRepository projectGithubRepositoryJpaRepository;
    private final ProjectAccessChecker projectAccessChecker;
    private final GithubPullRequestApiClient githubPullRequestApiClient;
    private final GithubUserAccessTokenProvider githubUserAccessTokenProvider;
    private final UserRepository userRepository;
    private final ProjectRecordRepository projectRecordRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiAnalysisRequestResponse requestAnalysis(
            Long projectId,
            Long userId,
            AiAnalysisCreateRequest request
    ) {
        projectAccessChecker.requireMemberOrAbove(projectId, userId);

        AiAnalysis analysis = createAndDispatchAnalysis(
                projectId,
                userId,
                request.getPrNumber()
        );

        return AiAnalysisRequestResponse.from(analysis);
    }

    @Transactional(readOnly = true)
    public AiAnalysisListResponse getAnalyses(
            Long projectId,
            Long userId,
            String status,
            Integer prNumber,
            int page,
            int size
    ) {
        projectAccessChecker.requireMemberOrAbove(projectId, userId);

        AnalysisStatus analysisStatus =
                parseAnalysisStatus(status);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        Page<AiAnalysis> analysisPage;

        if (prNumber != null && analysisStatus != null) {
            analysisPage = aiAnalysisRepository
                    .findAllByProjectIdAndPrNumberAndAnalysisStatus(
                            projectId,
                            prNumber,
                            analysisStatus,
                            pageable
                    );
        } else if (prNumber != null) {
            analysisPage = aiAnalysisRepository
                    .findAllByProjectIdAndPrNumber(
                            projectId,
                            prNumber,
                            pageable
                    );
        } else if (analysisStatus != null) {
            analysisPage = aiAnalysisRepository
                    .findAllByProjectIdAndAnalysisStatus(
                            projectId,
                            analysisStatus,
                            pageable
                    );
        } else {
            analysisPage = aiAnalysisRepository
                    .findAllByProjectId(
                            projectId,
                            pageable
                    );
        }

        List<Long> requesterIds =
                analysisPage.getContent()
                        .stream()
                        .map(AiAnalysis::getRequestedBy)
                        .distinct()
                        .toList();

        Map<Long, User> usersById =
                userRepository.findAllById(requesterIds)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        User::getId,
                                        Function.identity()
                                )
                        );

        List<AiAnalysisListItemResponse> content =
                analysisPage.getContent()
                        .stream()
                        .map(analysis -> {
                            User requester =
                                    usersById.get(
                                            analysis.getRequestedBy()
                                    );

                            String username =
                                    requester == null
                                            ? UNKNOWN_USERNAME
                                            : requester.getUsername();

                            return AiAnalysisListItemResponse.of(
                                    analysis,
                                    username
                            );
                        })
                        .toList();

        return AiAnalysisListResponse.builder()
                .content(content)
                .page(analysisPage.getNumber())
                .size(analysisPage.getSize())
                .totalElements(
                        analysisPage.getTotalElements()
                )
                .totalPages(
                        analysisPage.getTotalPages()
                )
                .hasNext(
                        analysisPage.hasNext()
                )
                .build();
    }

    @Transactional(readOnly = true)
    public AiAnalysisDetailResponse getAnalysis(
            Long projectId,
            Long analysisId,
            Long userId
    ) {
        projectAccessChecker.requireMemberOrAbove(
                projectId,
                userId
        );

        AiAnalysis analysis =
                getProjectAnalysis(
                        projectId,
                        analysisId
                );

        // 사람이 수정한 DRAFT 또는 승인본이 있으면
        // AI 원본 result_json 대신 해당 내용을 상세 조회에 보여준다.
        ProjectRecord record =
                projectRecordRepository
                        .findByProjectIdAndAnalysisId(
                                projectId,
                                analysisId
                        )
                        .orElse(null);

        String effectiveResultJson =
                record == null
                        ? analysis.getResultJson()
                        : record.getContentJson();

        return AiAnalysisDetailResponse.of(
                analysis,
                parseResultJson(
                        effectiveResultJson
                ),
                record
        );
    }

    public AiAnalysisRetryResponse retryAnalysis(
            Long projectId,
            Long analysisId,
            Long userId
    ) {
        projectAccessChecker.requireMemberOrAbove(
                projectId,
                userId
        );

        AiAnalysis previousAnalysis =
                getProjectAnalysis(
                        projectId,
                        analysisId
                );

        requireAnalysisActionPermission(
                projectId,
                userId,
                previousAnalysis
        );

        if (previousAnalysis.getAnalysisStatus()
                != AnalysisStatus.FAILED) {
            throw GeneralException.of(
                    ErrorCode.AI_ANALYSIS_INVALID_STATUS
            );
        }

        AiAnalysis newAnalysis =
                createAndDispatchAnalysis(
                        projectId,
                        userId,
                        previousAnalysis.getPrNumber()
                );

        return AiAnalysisRetryResponse.builder()
                .previousAnalysisId(
                        previousAnalysis.getAnalysisId()
                )
                .newAnalysisId(
                        newAnalysis.getAnalysisId()
                )
                .analysisStatus(
                        newAnalysis.getAnalysisStatus()
                )
                .build();
    }

    @Transactional
    public AiAnalysisCancelResponse cancelAnalysis(
            Long projectId,
            Long analysisId,
            Long userId
    ) {
        projectAccessChecker.requireMemberOrAbove(
                projectId,
                userId
        );

        AiAnalysis analysis =
                getProjectAnalysisForUpdate(
                        projectId,
                        analysisId
                );

        requireAnalysisActionPermission(
                projectId,
                userId,
                analysis
        );

        if (!analysis.isCancelable()) {
            throw GeneralException.of(
                    ErrorCode.AI_ANALYSIS_INVALID_STATUS
            );
        }

        analysis.cancel();

        return AiAnalysisCancelResponse.from(
                analysis
        );
    }

    private AiAnalysis createAndDispatchAnalysis(
            Long projectId,
            Long userId,
            Integer prNumber
    ) {
        Project project =
                projectJpaRepository
                        .findById(projectId)
                        .orElseThrow(() ->
                                GeneralException.of(
                                        ErrorCode.PROJECT_NOT_FOUND
                                )
                        );

        ProjectGithubRepository repository =
                projectGithubRepositoryJpaRepository
                        .findByProjectId(projectId)
                        .orElseThrow(() ->
                                GeneralException.of(
                                        ErrorCode.PROJECT_REPOSITORY_NOT_CONNECTED
                                )
                        );

        String accessToken =
                githubUserAccessTokenProvider
                        .getAccessToken(userId);

        GithubApiPullRequest pullRequest =
                githubPullRequestApiClient
                        .getPullRequest(
                                accessToken,
                                repository.getRepositoryFullName(),
                                prNumber
                        );

        List<GithubApiPullRequestFile> files =
                githubPullRequestApiClient
                        .listPullRequestFiles(
                                accessToken,
                                repository.getRepositoryFullName(),
                                prNumber
                        );

        String headSha =
                pullRequest.getHead() == null
                        ? null
                        : pullRequest
                                .getHead()
                                .getSha();

        if (headSha == null
                || headSha.isBlank()) {
            throw GeneralException.of(
                    ErrorCode.BAD_REQUEST
            );
        }

        AiAnalysis analysis =
                aiAnalysisRepository.save(
                        AiAnalysis.builder()
                                .projectId(projectId)
                                .repositoryId(
                                        repository.getRepositoryId()
                                )
                                .requestedBy(userId)
                                .githubPrId(
                                        pullRequest.getId()
                                )
                                .prNumber(
                                        pullRequest.getNumber()
                                )
                                .analyzedHeadSha(
                                        headSha
                                )
                                .build()
                );

        FastApiAnalysisRequestDto fastApiRequest =
                buildFastApiRequest(
                        project,
                        repository,
                        analysis,
                        pullRequest,
                        files
                );

        try {
            aiInternalService.requestAnalysis(
                    fastApiRequest
            );
        } catch (RuntimeException exception) {
            markDispatchFailed(
                    analysis.getAnalysisId(),
                    exception.getMessage()
            );

            throw GeneralException.of(
                    ErrorCode.AI_ANALYSIS_SERVER_UNAVAILABLE
            );
        }

        return analysis;
    }

    private FastApiAnalysisRequestDto buildFastApiRequest(
            Project project,
            ProjectGithubRepository repository,
            AiAnalysis analysis,
            GithubApiPullRequest pullRequest,
            List<GithubApiPullRequestFile> files
    ) {
        List<FastApiAnalysisRequestDto.FileDto> fileDtos =
                files.stream()
                        .map(file ->
                                FastApiAnalysisRequestDto
                                        .FileDto
                                        .builder()
                                        .filePath(
                                                file.getFilename()
                                        )
                                        .changeType(
                                                normalizeChangeType(
                                                        file.getStatus()
                                                )
                                        )
                                        .patch(
                                                file.getPatch()
                                        )
                                        .additions(
                                                file.getAdditions()
                                        )
                                        .deletions(
                                                file.getDeletions()
                                        )
                                        .build()
                        )
                        .toList();

        FastApiAnalysisRequestDto.PullRequestDto pullRequestDto =
                FastApiAnalysisRequestDto
                        .PullRequestDto
                        .builder()
                        .githubPrId(
                                pullRequest.getId()
                        )
                        .prNumber(
                                pullRequest.getNumber()
                        )
                        .title(
                                pullRequest.getTitle()
                        )
                        .body(
                                pullRequest.getBody()
                        )
                        .headSha(
                                analysis.getAnalyzedHeadSha()
                        )
                        .sourceBranch(
                                pullRequest.getHead() == null
                                        ? null
                                        : pullRequest
                                                .getHead()
                                                .getRef()
                        )
                        .targetBranch(
                                pullRequest.getBase() == null
                                        ? null
                                        : pullRequest
                                                .getBase()
                                                .getRef()
                        )
                        .files(
                                fileDtos
                        )
                        .build();

        return FastApiAnalysisRequestDto.builder()
                .analysisId(
                        analysis.getAnalysisId()
                )
                .projectId(
                        project.getProjectId()
                )
                .repositoryId(
                        repository.getRepositoryId()
                )
                .repositoryFullName(
                        repository.getRepositoryFullName()
                )
                .pullRequest(
                        pullRequestDto
                )
                .language(
                        normalizeLanguage(
                                project.getDefaultLanguage()
                        )
                )
                .build();
    }

    private AiAnalysis getProjectAnalysis(
            Long projectId,
            Long analysisId
    ) {
        return aiAnalysisRepository
                .findByAnalysisIdAndProjectId(
                        analysisId,
                        projectId
                )
                .orElseThrow(() ->
                        GeneralException.of(
                                ErrorCode.AI_ANALYSIS_NOT_FOUND
                        )
                );
    }

    private AiAnalysis getProjectAnalysisForUpdate(
            Long projectId,
            Long analysisId
    ) {
        return aiAnalysisRepository
                .findByAnalysisIdAndProjectIdForUpdate(
                        analysisId,
                        projectId
                )
                .orElseThrow(() ->
                        GeneralException.of(
                                ErrorCode.AI_ANALYSIS_NOT_FOUND
                        )
                );
    }

    private void requireAnalysisActionPermission(
            Long projectId,
            Long userId,
            AiAnalysis analysis
    ) {
        if (Objects.equals(
                analysis.getRequestedBy(),
                userId
        )) {
            return;
        }

        ProjectPermissionRole role =
                projectAccessChecker
                        .getRole(
                                projectId,
                                userId
                        );

        if (role != ProjectPermissionRole.OWNER
                && role != ProjectPermissionRole.ADMIN) {
            throw GeneralException.of(
                    ErrorCode.AI_ANALYSIS_ACTION_FORBIDDEN
            );
        }
    }

    private AnalysisStatus parseAnalysisStatus(
            String status
    ) {
        if (status == null
                || status.isBlank()) {
            return null;
        }

        try {
            return AnalysisStatus.valueOf(
                    status
                            .trim()
                            .toUpperCase(
                                    Locale.ROOT
                            )
            );
        } catch (IllegalArgumentException exception) {
            throw GeneralException.of(
                    ErrorCode.BAD_REQUEST
            );
        }
    }

    private Object parseResultJson(
            String resultJson
    ) {
        if (resultJson == null
                || resultJson.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(
                    resultJson,
                    Object.class
            );
        } catch (JsonProcessingException exception) {
            log.warn(
                    "AI 분석 result_json 파싱 실패 - 원문 문자열로 반환합니다. error={}",
                    exception.getMessage()
            );

            return resultJson;
        }
    }

    private String normalizeLanguage(
            String language
    ) {
        if (language == null
                || language.isBlank()) {
            return DEFAULT_LANGUAGE;
        }

        return language
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private String normalizeChangeType(
            String githubStatus
    ) {
        if (githubStatus == null
                || githubStatus.isBlank()) {
            return null;
        }

        return githubStatus
                .trim()
                .toUpperCase(
                        Locale.ROOT
                );
    }

    private void markDispatchFailed(
            Long analysisId,
            String errorMessage
    ) {
        aiAnalysisRepository
                .findById(analysisId)
                .ifPresent(analysis -> {
                    analysis.fail(
                            null,
                            null,
                            errorMessage == null
                                    ? "FastAPI 분석 서버 요청에 실패했습니다."
                                    : errorMessage
                    );

                    aiAnalysisRepository.save(
                            analysis
                    );
                });
    }
}
