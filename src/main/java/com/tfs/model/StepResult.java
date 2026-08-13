package com.tfs.model;

import java.time.Instant;

public record StepResult(
        String testCaseId, int stepNumber, String action, String expected, String actual,
        TestStatus status, Instant startTime, Instant endTime, long durationMs) {}
