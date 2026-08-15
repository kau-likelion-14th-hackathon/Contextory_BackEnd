package com.kbj.contextory.github.pullrequest.service;

import com.kbj.contextory.github.domain.ProjectGithubRepository;
import com.kbj.contextory.github.pullrequest.client.GithubApiPullRequest;
import com.kbj.contextory.github.pullrequest.client.GithubApiPullRequestFile;
import com.kbj.contextory.github.pullrequest.client.GithubPullRequestApiClient;
import com.kbj.contextory.github.pullrequest.dto.response.PullRequestDetailResponse;
import com.kbj.contextory.github.pullrequest.dto.response.PullRequestFileResponse;
import com.kbj.contextory.github.pullrequest.dto.response.PullRequestFilesResponse;
import com.kbj.contextory.github.pullrequest.dto.response.PullRequestPageResponse;
import com.kbj.contextory.github.pullrequest.dto.response.PullRequestSummaryResponse;
import com.kbj.contextory.github.pullrequest.support.PullRequestState;
import com.kbj.contextory.github.repository.ProjectGithubRepositoryJpaRepository;
import com.kbj.contextory.github.support.GithubUserAccessTokenProvider;
import com.kbj.contextory.github.support.ProjectAccessChecker;
import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProjectPullRequestService {

    private static final int MAX_PAGE_SIZE = 100;

    private final GithubPullRequestApiClient githubPullRequestApiClient;
    private final GithubUserAccessTokenProvider accessTokenProvider;
    private final ProjectAccessChecker projectAccessChecker;
    private final ProjectGithubRepositoryJpaRepository projectGithubRepositoryJpaRepository;

    @Transactional
    public PullRequestPageResponse getPullRequests(
            Long projectId,
            Long userId,
            String state,
            int page,
            int size
    ) {
        validatePagination(page, size);
        PullRequestState pullRequestState = PullRequestState.valueOf(state);

        ProjectGithubRepository repository = getConnectedRepository(projectId, userId);
        String accessToken = accessTokenProvider.getAccessToken(userId);

        GithubPullRequestApiClient.PullRequestPage githubPage =
                githubPullRequestApiClient.listPullRequests(
                        accessToken,
                        repository.getRepositoryFullName(),
                        pullRequestState.githubValue(),
                        page,
                        size
                );

        List<PullRequestSummaryResponse> content = githubPage.pullRequests()
                .stream()
                .map(PullRequestSummaryResponse::from)
                .toList();

        return PullRequestPageResponse.builder()
                .content(content)
                .page(page)
                .size(size)
                .hasNext(githubPage.hasNext())
                .build();
    }

    @Transactional
    public PullRequestDetailResponse getPullRequest(
            Long projectId,
            Long userId,
            int prNumber
    ) {
        validatePrNumber(prNumber);

        ProjectGithubRepository repository = getConnectedRepository(projectId, userId);
        String accessToken = accessTokenProvider.getAccessToken(userId);

        GithubApiPullRequest pullRequest = githubPullRequestApiClient.getPullRequest(
                accessToken,
                repository.getRepositoryFullName(),
                prNumber
        );

        return PullRequestDetailResponse.from(pullRequest);
    }

    @Transactional
    public PullRequestFilesResponse getPullRequestFiles(
            Long projectId,
            Long userId,
            int prNumber
    ) {
        validatePrNumber(prNumber);

        ProjectGithubRepository repository = getConnectedRepository(projectId, userId);
        String accessToken = accessTokenProvider.getAccessToken(userId);

        List<GithubApiPullRequestFile> githubFiles =
                githubPullRequestApiClient.listPullRequestFiles(
                        accessToken,
                        repository.getRepositoryFullName(),
                        prNumber
                );

        List<PullRequestFileResponse> files = githubFiles.stream()
                .map(PullRequestFileResponse::from)
                .toList();

        int totalAdditions = githubFiles.stream()
                .map(GithubApiPullRequestFile::getAdditions)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        int totalDeletions = githubFiles.stream()
                .map(GithubApiPullRequestFile::getDeletions)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        return PullRequestFilesResponse.builder()
                .prNumber(prNumber)
                .totalFiles(files.size())
                .totalAdditions(totalAdditions)
                .totalDeletions(totalDeletions)
                .files(files)
                .build();
    }

    private ProjectGithubRepository getConnectedRepository(Long projectId, Long userId) {
        projectAccessChecker.requireMember(projectId, userId);

        return projectGithubRepositoryJpaRepository
                .findByProjectId(projectId)
                .orElseThrow(() -> GeneralException.of(
                        ErrorCode.PROJECT_REPOSITORY_NOT_CONNECTED
                ));
    }

    private void validatePagination(int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw GeneralException.of(ErrorCode.BAD_REQUEST);
        }
    }

    private void validatePrNumber(int prNumber) {
        if (prNumber < 1) {
            throw GeneralException.of(ErrorCode.BAD_REQUEST);
        }
    }
}
