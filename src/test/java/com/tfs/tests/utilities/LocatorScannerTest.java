package com.tfs.tests.utilities;

import com.tfs.annotations.TestInfo;
import com.tfs.tests.BaseTest;
import com.tfs.utils.LocatorScanner;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;

/** Runs the locator scanner against the screen currently displayed by Appium. */
public class LocatorScannerTest extends BaseTest {

    @Test(groups = {"utility", "locator-scanner"})
    @TestInfo(id = "UTILITY_LOCATOR_SCANNER",
            title = "Scan current Android screen and generate locators",
            module = "Automation Utilities")
    public void scanCurrentAndroidScreen() throws Exception {
        String screenName = System.getProperty("locator.scan.screen", "current-screen");
        int delaySeconds = Integer.parseInt(
                System.getProperty("locator.scan.delay", "30"));

        System.out.println("==================================================");
        System.out.println("[LOCATOR SCANNER] Screen : " + screenName);
        System.out.println("[LOCATOR SCANNER] Delay  : " + delaySeconds + " second(s)");
        System.out.println("[LOCATOR SCANNER] Navigate manually to the required screen now.");
        System.out.println("==================================================");

        if (delaySeconds > 0) {
            Thread.sleep(delaySeconds * 1_000L);
        }

        Path report = LocatorScanner.scanCurrentScreen(screenName);
        Assert.assertTrue(Files.exists(report),
                "Locator scanner report was not created: " + report);
        Assert.assertTrue(Files.size(report) > 0,
                "Locator scanner report is empty: " + report);

        System.out.println("[PASS] Locator scan completed: " + report);
    }
}
