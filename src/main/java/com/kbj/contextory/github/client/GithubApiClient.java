package com.kbj.contextory.github.client;

import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.exception.GeneralException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Arrays;
import java.util.List;

@Component
public class GithubApiClient {

    private static final String GITHUB_ACCEPT = "application/vnd.github+json";
    private static final String GITHUB_API_VERSION_HEADER = "X-GitHub-Api-Version";

    private final RestClient restClient;

    public GithubApiClient(
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

    public RepositoryPage listRepositories(
            String accessToken,
            int page,
            int size
    ) {
        try {
            ResponseEntity<GithubApiRepository[]> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/user/repos")
                            .queryParam("visibility", "all")
                            .queryParam("affiliation", "owner,collaborator,organization_member")
                            .queryParam("sort", "updated")
                            .queryParam("direction", "desc")
                            .queryParam("page", page)
                            .queryParam("per_page", size)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                    .retrieve()
                    .toEntity(GithubApiRepository[].class);

            GithubApiRepository[] body = response.getBody();
            List<GithubApiRepository> repositories = body == null
                    ? List.of()
                    : Arrays.asList(body);

            String linkHeader = response.getHeaders().getFirst(HttpHeaders.LINK);
            boolean hasNext = linkHeader != null && linkHeader.contains("rel=\"next\"");

            return new RepositoryPage(repositories, hasNext);
        } catch (RestClientResponseException exception) {
            throw mapGithubException(exception, false);
        } catch (RestClientException exception) {
            throw GeneralException.of(ErrorCode.GITHUB_API_FAILED);
        }
    }

    public GithubApiRepository getRepository(
            String accessToken,
            String repositoryFullName
    ) {
        RepositoryName repositoryName = RepositoryName.from(repositoryFullName);

        try {
            GithubApiRepository repository = restClient.get()
                    .uri("/repos/{owner}/{repository}",
                            repositoryName.owner(),
                            repositoryName.repository())
                    .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                    .retrieve()
                    .body(GithubApiRepository.class);

            if (repository == null) {
                throw GeneralException.of(ErrorCode.GITHUB_API_FAILED);
            }
            return repository;
        } catch (RestClientResponseException exception) {
            throw mapGithubException(exception, true);
        } catch (RestClientException exception) {
            throw GeneralException.of(ErrorCode.GITHUB_API_FAILED);
        }
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private GeneralException mapGithubException(
            RestClientResponseException exception,
            boolean repositoryLookup
    ) {
        int status = exception.getStatusCode().value();

        if (status == 401) {
            return GeneralException.of(ErrorCode.GITHUB_CONNECTION_REQUIRED);
        }
        if (status == 404 && repositoryLookup) {
            return GeneralException.of(ErrorCode.GITHUB_REPOSITORY_NOT_FOUND);
        }
        return GeneralException.of(ErrorCode.GITHUB_API_FAILED);
    }

    public record RepositoryPage(
            List<GithubApiRepository> repositories,
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
