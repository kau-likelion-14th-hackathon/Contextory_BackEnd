package com.kbj.contextory.github.pullrequest.client;

import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.exception.GeneralException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class GithubPullRequestApiClient {

    private static final String GITHUB_ACCEPT = "application/vnd.github+json";
    private static final String GITHUB_API_VERSION_HEADER = "X-GitHub-Api-Version";
    private static final int FILES_PER_PAGE = 100;
    private static final int MAX_FILE_PAGES = 30; // GitHub REST API 최대 3,000 files

    private final RestClient restClient;

    public GithubPullRequestApiClient(
            RestClient.Builder restClientBuilder,
            @Value("${github.api.base-url:https://api.github.com}") String baseUrl,
            @Value("${github.api.version:2026-03-10}") String apiVersion
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, GITHUB_ACCEPT)
                .defaultHeader(GITHUB_API_VERSION_HEADER, apiVersion)
                .build();
    }

    public PullRequestPage listPullRequests(
            String accessToken,
            String repositoryFullName,
            String state,
            int page,
            int size
    ) {
        RepositoryName repositoryName = RepositoryName.from(repositoryFullName);

        try {
            ResponseEntity<GithubApiPullRequest[]> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/repos/{owner}/{repository}/pulls")
                            .queryParam("state", state)
                            .queryParam("sort", "updated")
                            .queryParam("direction", "desc")
                            .queryParam("page", page)
                            .queryParam("per_page", size)
                            .build(repositoryName.owner(), repositoryName.repository()))
                    .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                    .retrieve()
                    .toEntity(GithubApiPullRequest[].class);

            GithubApiPullRequest[] body = response.getBody();
            List<GithubApiPullRequest> pullRequests = body == null
                    ? List.of()
                    : Arrays.asList(body);

            String linkHeader = response.getHeaders().getFirst(HttpHeaders.LINK);
            boolean hasNext = linkHeader != null && linkHeader.contains("rel=\"next\"");

            return new PullRequestPage(pullRequests, hasNext);
        } catch (RestClientResponseException exception) {
            throw mapGithubException(exception);
        } catch (RestClientException exception) {
            throw GeneralException.of(ErrorCode.GITHUB_API_FAILED);
        }
    }

    public GithubApiPullRequest getPullRequest(
            String accessToken,
            String repositoryFullName,
            int prNumber
    ) {
        RepositoryName repositoryName = RepositoryName.from(repositoryFullName);

        try {
            GithubApiPullRequest pullRequest = restClient.get()
                    .uri("/repos/{owner}/{repository}/pulls/{pullNumber}",
                            repositoryName.owner(),
                            repositoryName.repository(),
                            prNumber)
                    .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                    .retrieve()
                    .body(GithubApiPullRequest.class);

            if (pullRequest == null) {
                throw GeneralException.of(ErrorCode.GITHUB_API_FAILED);
            }
            return pullRequest;
        } catch (RestClientResponseException exception) {
            throw mapGithubException(exception);
        } catch (RestClientException exception) {
            throw GeneralException.of(ErrorCode.GITHUB_API_FAILED);
        }
    }

    public List<GithubApiPullRequestFile> listPullRequestFiles(
            String accessToken,
            String repositoryFullName,
            int prNumber
    ) {
        RepositoryName repositoryName = RepositoryName.from(repositoryFullName);
        List<GithubApiPullRequestFile> result = new ArrayList<>();

        try {
            for (int page = 1; page <= MAX_FILE_PAGES; page++) {
                int finalPage = page;
                GithubApiPullRequestFile[] files = restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/repos/{owner}/{repository}/pulls/{pullNumber}/files")
                                .queryParam("page", finalPage)
                                .queryParam("per_page", FILES_PER_PAGE)
                                .build(
                                        repositoryName.owner(),
                                        repositoryName.repository(),
                                        prNumber
                                ))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .retrieve()
                        .body(GithubApiPullRequestFile[].class);

                if (files == null || files.length == 0) {
                    break;
                }

                result.addAll(Arrays.asList(files));

                if (files.length < FILES_PER_PAGE) {
                    break;
                }
            }

            return result;
        } catch (RestClientResponseException exception) {
            throw mapGithubException(exception);
        } catch (RestClientException exception) {
            throw GeneralException.of(ErrorCode.GITHUB_API_FAILED);
        }
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private GeneralException mapGithubException(RestClientResponseException exception) {
        int status = exception.getStatusCode().value();

        if (status == 401) {
            return GeneralException.of(ErrorCode.GITHUB_CONNECTION_REQUIRED);
        }
        if (status == 404) {
            // 현 ErrorCode에 PR 전용 404가 없어 기존 GitHub 404를 재사용한다.
            return GeneralException.of(ErrorCode.GITHUB_REPOSITORY_NOT_FOUND);
        }
        if (status == 422) {
            return GeneralException.of(ErrorCode.BAD_REQUEST);
        }
        return GeneralException.of(ErrorCode.GITHUB_API_FAILED);
    }

    public record PullRequestPage(
            List<GithubApiPullRequest> pullRequests,
            boolean hasNext
    ) {}

    private record RepositoryName(String owner, String repository) {

        private static RepositoryName from(String repositoryFullName) {
            if (repositoryFullName == null) {
                throw GeneralException.of(ErrorCode.BAD_REQUEST);
            }

            String[] parts = repositoryFullName.trim().split("/", -1);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                throw GeneralException.of(ErrorCode.BAD_REQUEST);
            }

            return new RepositoryName(parts[0], parts[1]);
        }
    }
}
