package com.kbj.contextory.github.client;

import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.exception.GeneralException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Component
public class GithubOauthClient {

    private static final String GITHUB_ACCEPT = "application/vnd.github+json";
    private static final String GITHUB_API_VERSION_HEADER = "X-GitHub-Api-Version";

    private final RestClient oauthRestClient;
    private final RestClient apiRestClient;
    private final String clientId;
    private final String clientSecret;
    private final String callbackUrl;

    public GithubOauthClient(
            RestClient.Builder restClientBuilder,
            @Value("${github.oauth.base-url:https://github.com}") String oauthBaseUrl,
            @Value("${github.api.base-url:https://api.github.com}") String apiBaseUrl,
            @Value("${github.api.version:2026-03-10}") String apiVersion,
            @Value("${github.app.client-id}") String clientId,
            @Value("${github.app.client-secret}") String clientSecret,
            @Value("${github.app.callback-url}") String callbackUrl
    ) {
        this.oauthRestClient = restClientBuilder
                .clone()
                .baseUrl(oauthBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        this.apiRestClient = restClientBuilder
                .clone()
                .baseUrl(apiBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, GITHUB_ACCEPT)
                .defaultHeader(GITHUB_API_VERSION_HEADER, apiVersion)
                .build();

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.callbackUrl = callbackUrl;
    }

    public GithubOauthTokenResponse exchangeCode(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("code", code);
        form.add("redirect_uri", callbackUrl);

        return requestToken(form, ErrorCode.GITHUB_OAUTH_CODE_EXCHANGE_FAILED);
    }

    public GithubOauthTokenResponse refreshAccessToken(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);

        return requestToken(form, ErrorCode.GITHUB_TOKEN_REFRESH_FAILED);
    }

    public GithubApiUser getAuthenticatedUser(String accessToken) {
        try {
            GithubApiUser user = apiRestClient.get()
                    .uri("/user")
                    .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                    .retrieve()
                    .body(GithubApiUser.class);

            if (user == null || user.getId() == null || user.getLogin() == null) {
                throw GeneralException.of(ErrorCode.GITHUB_API_FAILED);
            }
            return user;
        } catch (RestClientResponseException exception) {
            throw mapApiException(exception);
        } catch (RestClientException exception) {
            throw GeneralException.of(ErrorCode.GITHUB_API_FAILED);
        }
    }

    public List<GithubApiInstallation> listInstallations(String accessToken) {
        try {
            GithubApiInstallationPage page = apiRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/user/installations")
                            .queryParam("page", 1)
                            .queryParam("per_page", 100)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                    .retrieve()
                    .body(GithubApiInstallationPage.class);

            if (page == null || page.getInstallations() == null) {
                return List.of();
            }
            return page.getInstallations();
        } catch (RestClientResponseException exception) {
            throw mapApiException(exception);
        } catch (RestClientException exception) {
            throw GeneralException.of(ErrorCode.GITHUB_API_FAILED);
        }
    }

    private GithubOauthTokenResponse requestToken(
            MultiValueMap<String, String> form,
            ErrorCode failureCode
    ) {
        try {
            GithubOauthTokenResponse response = oauthRestClient.post()
                    .uri("/login/oauth/access_token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(GithubOauthTokenResponse.class);

            if (response == null
                    || response.getAccessToken() == null
                    || response.getAccessToken().isBlank()) {
                throw GeneralException.of(failureCode);
            }
            return response;
        } catch (RestClientResponseException exception) {
            throw GeneralException.of(failureCode);
        } catch (RestClientException exception) {
            throw GeneralException.of(failureCode);
        }
    }

    private GeneralException mapApiException(RestClientResponseException exception) {
        int status = exception.getStatusCode().value();
        if (status == 401 || status == 403) {
            return GeneralException.of(ErrorCode.GITHUB_CONNECTION_REQUIRED);
        }
        return GeneralException.of(ErrorCode.GITHUB_API_FAILED);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
