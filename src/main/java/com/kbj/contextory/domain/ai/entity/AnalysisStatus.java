package com.kbj.contextory.domain.ai.entity;

import lombok.Getter;

@Getter
public enum AnalysisStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELED
}