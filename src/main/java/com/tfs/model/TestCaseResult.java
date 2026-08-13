package com.tfs.model;

import java.time.Instant;

public record TestCaseResult(
        String testCaseId, String title, String module, String suite, TestStatus status,
        Instant startTime, Instant endTime, long durationMs, String failureReason,
        String screenshotPath, String pageSourcePath) {}
