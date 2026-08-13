package com.tfs.listeners;

import com.tfs.annotations.TestInfo;
import com.tfs.config.ConfigManager;
import com.tfs.model.*;
import com.tfs.reporting.*;
import com.tfs.utils.ScreenshotUtils;
import org.testng.*;
import java.lang.reflect.Method;
import java.time.Instant;

public class TestResultListener implements ITestListener, IExecutionListener {
    @Override public void onExecutionStart() { ReportManager.initialize(); }
    @Override public void onExecutionFinish() { ReportManager.finish(); }

    @Override public void onTestStart(ITestResult result) {
        TestInfo i = info(result);
        TestExecutionContext.start(i.id(), i.title(), i.module());
    }

    @Override public void onTestSuccess(ITestResult result) { record(result, TestStatus.PASS, ""); }
    @Override public void onTestFailure(ITestResult result) {
        record(result, TestStatus.FAIL, result.getThrowable()==null ? "" : result.getThrowable().toString());
    }
    @Override public void onTestSkipped(ITestResult result) {
        record(result, TestStatus.SKIPPED, result.getThrowable()==null ? "" : result.getThrowable().toString());
    }

    private void record(ITestResult r, TestStatus status, String reason) {
        TestInfo i = info(r);
        Instant end = Instant.now();
        Instant start = TestExecutionContext.current().start;
        String screenshot = "", source = "";
        if (status == TestStatus.FAIL) {
            if (ConfigManager.getBoolean("capture.screenshot.on.failure", true)) screenshot = ScreenshotUtils.capture(i.id());
            if (ConfigManager.getBoolean("capture.page.source.on.failure", true)) source = ScreenshotUtils.pageSource(i.id());
        }
        ReportManager.add(new TestCaseResult(i.id(), i.title(), i.module(), RunContext.suite(), status,
                start, end, end.toEpochMilli()-start.toEpochMilli(), reason, screenshot, source));
        TestExecutionContext.clear();
    }

    private TestInfo info(ITestResult r) {
        Method m = r.getMethod().getConstructorOrMethod().getMethod();
        TestInfo i = m.getAnnotation(TestInfo.class);
        if (i == null) throw new IllegalStateException("Every product test must have @TestInfo: " + m.getName());
        return i;
    }
}
