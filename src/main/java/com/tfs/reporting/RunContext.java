package com.tfs.reporting;

import com.tfs.config.ConfigManager;
import com.tfs.utils.DateTimeUtils;
import java.nio.file.Path;
import java.time.Instant;

public final class RunContext {
    private static final Instant START = Instant.now();
    private static final String SUITE = System.getProperty("suite.name", "MANUAL").trim().toUpperCase();
    private static final String RUN_ID = SUITE + "_" + DateTimeUtils.runTimestamp();
    private static final Path REPORT_DIR = Path.of(
            ConfigManager.get("report.root").isBlank() ? "reports/CSV" : ConfigManager.get("report.root")).resolve(RUN_ID);

    private RunContext() {}
    public static Instant startTime() { return START; }
    public static String suite() { return SUITE; }
    public static String runId() { return RUN_ID; }
    public static Path reportDir() { return REPORT_DIR; }
}
