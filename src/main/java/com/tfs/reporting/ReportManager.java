package com.tfs.reporting;

import com.tfs.config.ConfigManager;
import com.tfs.model.*;
import com.tfs.utils.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

public final class ReportManager {
    private static final List<TestCaseResult> TESTS = Collections.synchronizedList(new ArrayList<>());
    private ReportManager() {}

    public static synchronized void initialize() {
        try {
            Files.createDirectories(RunContext.reportDir().resolve("screenshots"));
            Files.createDirectories(RunContext.reportDir().resolve("page-sources"));
            write("execution_summary.csv","Suite,Environment,Start_Time,End_Time,Duration,Total,Pass,Fail,Blocked,Skipped,Infrastructure_Failed\n");
            write("test_case_results.csv","Test_Case_ID,Title,Module,Suite,Status,Start_Time,End_Time,Duration,Failure_Reason,Screenshot,Page_Source\n");
            write("step_results.csv","Test_Case_ID,Step,Action,Expected,Actual,Status,Start_Time,End_Time,Duration\n");
            write("failed_test_cases.csv","Test_Case_ID,Title,Module,Status,Failure_Reason,Screenshot,Page_Source\n");
            write("environment_details.csv","Key,Value\n");
            append("environment_details.csv", csv("Run_ID", RunContext.runId()));
            append("environment_details.csv", csv("Suite", RunContext.suite()));
            append("environment_details.csv", csv("Environment", ConfigManager.get("environment")));
            append("environment_details.csv", csv("Isolation_Mode", ConfigManager.get("isolation.mode")));
            append("environment_details.csv", csv("App_Package", ConfigManager.get("app.package")));
            try {
                append("environment_details.csv", csv("Device_UDID", AdbUtils.resolveDeviceId()));
                append("environment_details.csv", csv("Android_Version", AdbUtils.androidVersion()));
            } catch (RuntimeException e) {
                append("environment_details.csv", csv("Device", "Unavailable: " + e.getMessage()));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize reports.", e);
        }
    }

    public static void add(TestCaseResult r) { TESTS.add(r); append("test_case_results.csv", csv(
            r.testCaseId(), r.title(), r.module(), r.suite(), r.status().name(),
            DateTimeUtils.display(r.startTime()), DateTimeUtils.display(r.endTime()),
            DateTimeUtils.duration(r.durationMs()), r.failureReason(), r.screenshotPath(), r.pageSourcePath())); }

    public static void add(StepResult r) { append("step_results.csv", csv(
            r.testCaseId(), String.valueOf(r.stepNumber()), r.action(), r.expected(), r.actual(),
            r.status().name(), DateTimeUtils.display(r.startTime()), DateTimeUtils.display(r.endTime()),
            DateTimeUtils.duration(r.durationMs()))); }

    public static synchronized void finish() {
        Instant end = Instant.now();
        long pass = TESTS.stream().filter(r -> r.status()==TestStatus.PASS).count();
        long fail = TESTS.stream().filter(r -> r.status()==TestStatus.FAIL).count();
        long blocked = TESTS.stream().filter(r -> r.status()==TestStatus.BLOCKED).count();
        long skipped = TESTS.stream().filter(r -> r.status()==TestStatus.SKIPPED).count();
        long infra = TESTS.stream().filter(r -> r.status()==TestStatus.INFRASTRUCTURE_FAILED).count();
        append("execution_summary.csv", csv(RunContext.suite(), ConfigManager.get("environment"),
                DateTimeUtils.display(RunContext.startTime()), DateTimeUtils.display(end),
                DateTimeUtils.duration(end.toEpochMilli()-RunContext.startTime().toEpochMilli()),
                String.valueOf(TESTS.size()), String.valueOf(pass), String.valueOf(fail),
                String.valueOf(blocked), String.valueOf(skipped), String.valueOf(infra)));

        TESTS.stream().filter(r -> r.status()!=TestStatus.PASS).forEach(r ->
                append("failed_test_cases.csv", csv(r.testCaseId(), r.title(), r.module(), r.status().name(),
                        r.failureReason(), r.screenshotPath(), r.pageSourcePath())));
    }

    private static void write(String name, String content) throws IOException {
        Files.writeString(RunContext.reportDir().resolve(name), content, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static synchronized void append(String name, String row) {
        try {
            Files.writeString(RunContext.reportDir().resolve(name), row + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write report " + name, e);
        }
    }

    private static String csv(String... values) {
        StringJoiner j = new StringJoiner(",");
        for (String v : values) {
            String value = v == null ? "" : v;
            j.add("\"" + value.replace("\"","\"\"") + "\"");
        }
        return j.toString();
    }
}
