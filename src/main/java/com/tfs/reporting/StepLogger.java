package com.tfs.reporting;

import com.tfs.model.*;
import java.time.Instant;
import java.util.function.Supplier;

public final class StepLogger {
    private StepLogger() {}

    public static void action(String action, String expected, Runnable code, String successActual) {
        Instant start = Instant.now();
        int step = TestExecutionContext.current().step.incrementAndGet();
        try {
            code.run();
            Instant end = Instant.now();
            ReportManager.add(new StepResult(TestExecutionContext.current().id, step, action, expected,
                    successActual, TestStatus.PASS, start, end, end.toEpochMilli() - start.toEpochMilli()));
        } catch (RuntimeException | AssertionError e) {
            Instant end = Instant.now();
            ReportManager.add(new StepResult(TestExecutionContext.current().id, step, action, expected,
                    e.getMessage(), TestStatus.FAIL, start, end, end.toEpochMilli() - start.toEpochMilli()));
            throw e;
        }
    }

    public static void verify(String action, String expected, Supplier<Boolean> check, String successActual) {
        action(action, expected, () -> {
            if (!check.get()) throw new AssertionError(expected);
        }, successActual);
    }
}
