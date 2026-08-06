package com.kbj.contextory.github.service;

import com.kbj.contextory.github.client.GithubApiClient;
import com.kbj.contextory.github.client.GithubApiRepository;
import com.kbj.contextory.github.domain.ProjectGithubRepository;
import com.kbj.contextory.github.dto.request.ConnectProjectRepositoryRequest;
import com.kbj.contextory.github.dto.response.GithubRepositoryPageResponse;
import com.kbj.contextory.github.dto.response.GithubRepositorySummaryResponse;
import com.kbj.contextory.github.dto.response.ProjectRepositoryConnectionResponse;
import com.kbj.contextory.github.dto.response.ProjectRepositoryDetailResponse;
import com.kbj.contextory.github.repository.ProjectGithubRepositoryJpaRepository;
import com.kbj.contextory.github.support.GithubUserAccessTokenProvider;
import com.kbj.contextory.github.support.ProjectAccessChecker;
import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GithubIntegrationService {

    private final GithubApiClient githubApiClient;
    private final GithubUserAccessTokenProvider accessTokenProvider;
    private final ProjectAccessChecker projectAccessChecker;
    private final ProjectGithubRepositoryJpaRepository repositoryJpaRepository;

    public GithubRepositoryPageResponse getAccessibleRepositories(
            Long userId,
            int page,
            int size
    ) {
        String accessToken = accessTokenProvider.getAccessToken(userId);
        GithubApiClient.RepositoryPage githubPage =
                githubApiClient.listRepositories(accessToken, page, size);

        List<GithubRepositorySummaryResponse> content = githubPage.repositories()
                .stream()
                .map(GithubRepositorySummaryResponse::from)
                .toList();

        return GithubRepositoryPageResponse.builder()
                .content(content)
                .page(page)
                .size(size)
                .hasNext(githubPage.hasNext())
                .build();
    }

    @Transactional
    public ProjectRepositoryConnectionResponse connectRepository(
            Long projectId,
            Long userId,
            ConnectProjectRepositoryRequest request
    ) {
        projectAccessChecker.requireAdmin(projectId, userId);

        String accessToken = accessTokenProvider.getAccessToken(userId);
        GithubApiRepository githubRepository = githubApiClient.getRepository(
                accessToken,
                request.getRepositoryFullName()
        );

        if (!githubRepository.getId().equals(request.getGithubRepositoryId())) {
            throw GeneralException.of(ErrorCode.GITHUB_REPOSITORY_ID_MISMATCH);
        }

        Instant now = Instant.now();
        ProjectGithubRepository connection = repositoryJpaRepository
                .findByProjectId(projectId)
                .orElseGet(() -> ProjectGithubRepository.builder()
                        .projectId(projectId)
                        .connectedBy(userId)
                        .githubRepositoryId(githubRepository.getId())
                        .repositoryFullName(githubRepository.getFullName())
                        .lastSyncedAt(now)
                        .build());

        connection.reconnect(
                userId,
                githubRepository.getId(),
                githubRepository.getFullName(),
                now
        );

        ProjectGithubRepository saved = repositoryJpaRepository.save(connection);
        return ProjectRepositoryConnectionResponse.from(saved);
    }

    @Transactional
    public ProjectRepositoryDetailResponse getConnectedRepository(
            Long projectId,
            Long userId
    ) {
        projectAccessChecker.requireMember(projectId, userId);

        ProjectGithubRepository connection = repositoryJpaRepository
                .findByProjectId(projectId)
                .orElseThrow(() -> GeneralException.of(
                        ErrorCode.PROJECT_REPOSITORY_NOT_CONNECTED
                ));

        String accessToken = accessTokenProvider.getAccessToken(userId);
        GithubApiRepository githubRepository = githubApiClient.getRepository(
                accessToken,
                connection.getRepositoryFullName()
        );

        connection.synchronize(
                githubRepository.getId(),
                githubRepository.getFullName(),
                Instant.now()
        );

        return ProjectRepositoryDetailResponse.of(connection, githubRepository);
    }

    @Transactional
    public void disconnectRepository(Long projectId, Long userId) {
        projectAccessChecker.requireAdmin(projectId, userId);

        ProjectGithubRepository connection = repositoryJpaRepository
                .findByProjectId(projectId)
                .orElseThrow(() -> GeneralException.of(
                        ErrorCode.PROJECT_REPOSITORY_NOT_CONNECTED
                ));

        repositoryJpaRepository.delete(connection);
    }
}
