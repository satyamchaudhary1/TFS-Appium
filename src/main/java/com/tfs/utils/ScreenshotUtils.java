package com.tfs.utils;

import com.tfs.driver.DriverManager;
import com.tfs.reporting.RunContext;
import org.openqa.selenium.*;
import java.nio.file.*;

public final class ScreenshotUtils {
    private ScreenshotUtils() {}

    public static String capture(String testId) {
        if (!DriverManager.hasDriver()) return "";
        try {
            Path p = RunContext.reportDir().resolve("screenshots").resolve(testId+"_"+System.currentTimeMillis()+".png");
            Files.write(p, ((TakesScreenshot)DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES));
            return p.toString();
        } catch (Exception e) { return ""; }
    }

    public static String pageSource(String testId) {
        if (!DriverManager.hasDriver()) return "";
        try {
            Path p = RunContext.reportDir().resolve("page-sources").resolve(testId+"_"+System.currentTimeMillis()+".xml");
            Files.writeString(p, DriverManager.getDriver().getPageSource());
            return p.toString();
        } catch (Exception e) { return ""; }
    }
}
