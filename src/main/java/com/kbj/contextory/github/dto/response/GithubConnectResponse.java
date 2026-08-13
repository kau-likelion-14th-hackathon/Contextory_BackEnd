package com.kbj.contextory.github.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GithubConnectResponse {

    private String installUrl;
    private Long stateExpiresInSeconds;
}
