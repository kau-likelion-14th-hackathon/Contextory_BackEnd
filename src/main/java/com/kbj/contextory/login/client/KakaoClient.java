package com.kbj.contextory.login.client;

import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

@Component
@RequiredArgsConstructor
public class KakaoClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String userInfoUri;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    public String getAccessToken(String code, Boolean develop) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", develop ? redirectUri : "http://localhost:3000/login/oauth2/code/kakao");
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        try {
            JsonNode response = restTemplate.postForObject(
                    tokenUri, request, JsonNode.class
            );

            if (response == null || !response.has("access_token")) {
                throw new GeneralException(ErrorCode.KAKAO_AUTH_FAILED);
            }

            return response.get("access_token").asText();

        } catch (Exception e) {
            System.out.println(e);
            throw new GeneralException(ErrorCode.KAKAO_AUTH_FAILED);
        }
    }

    public JsonNode getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    userInfoUri,
                    HttpMethod.GET,
                    request,
                    JsonNode.class
            );

            JsonNode body = response.getBody();
            if (body == null || !body.has("id")) {
                throw new GeneralException(ErrorCode.KAKAO_AUTH_FAILED);
            }

            return body;

        } catch (Exception e) {
            throw new GeneralException(ErrorCode.KAKAO_AUTH_FAILED);
        }
    }
}
