package com.tfs.utils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {
    private static final DateTimeFormatter RUN =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter DISPLAY =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private DateTimeUtils() {}

    public static String runTimestamp() { return RUN.format(Instant.now()); }
    public static String display(Instant i) { return i == null ? "" : DISPLAY.format(i); }

    public static String duration(long millis) {
        Duration d = Duration.ofMillis(Math.max(0, millis));
        return String.format("%02d:%02d:%02d.%03d",
                d.toHours(), d.toMinutesPart(), d.toSecondsPart(), d.toMillisPart());
    }
}
