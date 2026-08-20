package com.kbj.contextory.github.pullrequest.support;

import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.exception.GeneralException;

import java.util.Locale;

public enum PullRequestState {
    OPEN("open"),
    CLOSED("closed"),
    ALL("all");

    private final String githubValue;

    PullRequestState(String githubValue) {
        this.githubValue = githubValue;
    }

    public String githubValue() {
        return githubValue;
    }

    public static PullRequestState from(String value) {
        if (value == null || value.isBlank()) {
            return OPEN;
        }

        try {
            return PullRequestState.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw GeneralException.of(ErrorCode.BAD_REQUEST);
        }
    }
}
