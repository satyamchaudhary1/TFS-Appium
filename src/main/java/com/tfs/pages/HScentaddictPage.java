package com.tfs.pages;

import com.tfs.driver.DriverManager;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Collections;

/**
 * TC018 - Scentaddict Get Started Page Object
 * CODE_VERSION: TC018-SCENTADDICT-STEPS-1-3-V2-H-PREFIX
 */
public class HScentaddictPage extends BasePage {

    public static final String CODE_VERSION = "TC018-SCENTADDICT-STEPS-1-3-V2-H-PREFIX";

    private final AndroidDriver driver;
    private final WebDriverWait wait;

    // ---------- Locators ----------
    // Exact content-desc from the on-device XML.
    private final By getStarted = AppiumBy.xpath(
            "//android.view.ViewGroup[@content-desc='Get Started' and @clickable='true']");

    // Chrome toolbar 'Close tab' button (package com.android.chrome).
    private final By closeTab = AppiumBy.xpath(
            "//android.widget.ImageButton[@resource-id='com.android.chrome:id/close_button']");

    // Markers that only the TFS Home page shows after returning.
    private final By tfsHomeMarker = AppiumBy.xpath(
            "//*[@content-desc='Learn More' or @text=\"Women's\" or @text=\"Men's\"]");

    public HScentaddictPage() {
        super();
        this.driver = DriverManager.getDriver();
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    private static Boolean apply(WebDriver d) {
        String pkg = String.valueOf(
                d.toString());
        return "com.thefragranceshop".equals(pkg);
    
        }

    // ==========================================================
    // STEP 1 — Scroll to Scentaddict card and click Get Started
    // ==========================================================
    public void scrollToAndClickScentaddictGetStarted() {
        System.out.println("[INFO] Step 1 - Scrolling to Scentaddict Get Started card...");

        for (int i = 0; i < 6; i++) {
            if (isDisplayed(getStarted)) {
                System.out.println("[PASS] Step 1: Scentaddict Get Started card is visible.");
                break;
            }
            swipeUp();
            pause(600);
        }

        WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(getStarted));
        Rectangle r = btn.getRect();
        System.out.println("[INFO] Step 1: Scentaddict Get Started bounds = " + r);
        btn.click();
        System.out.println("[PASS] Step 1: Scentaddict Get Started clicked.");
        pause(3000);
    }

    // ==========================================================
    // STEP 2 — Confirm we landed on the Scentaddict WebView
    // ==========================================================
    public void verifyScentaddictWebViewOpened() {
        System.out.println("[INFO] Step 2 - Verifying Scentaddict WebView opened...");
        pause(3000);

        // Appium 10.x: 'getCurrentPackage()' was removed. Use the
        // UiAutomator2 mobile command instead.
        String currentPkg = String.valueOf(
                driver.executeScript("mobile: getCurrentPackage"));
        System.out.println("[INFO] Step 2: Foreground package = " + currentPkg);

        if (!"com.android.chrome".equals(currentPkg)) {
            throw new AssertionError(
                    "[FAIL] Step 2: Expected com.android.chrome, but foreground is '"
                            + currentPkg + "'.");
        }

        long deadline = System.currentTimeMillis() + 15_000;
        boolean found = false;
        while (System.currentTimeMillis() < deadline) {
            try {
                String src = driver.getPageSource();
                if (src.contains("scentaddict")
                        || src.contains("Close tab")
                        || src.contains("com.android.chrome:id/close_button")) {
                    found = true;
                    break;
                }
            } catch (Exception ignored) { }
            pause(500);
        }
        if (!found) {
            System.out.println("[WARN] Step 2: Could not confirm page content, but package is Chrome.");
        }

        System.out.println("[PASS] Step 2: Scentaddict WebView is open (Chrome foreground).");
    }

    // ==========================================================
// STEP 3 — Click 'Close tab' to return to the TFS app
// ==========================================================
    public void closeTabAndReturnToApp() {
        System.out.println("[INFO] Step 3 - Clicking Close tab in Chrome...");

        WebElement close = new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.elementToBeClickable(closeTab));
        close.click();
        System.out.println("[PASS] Step 3: Close tab clicked.");

        // Wait until the foreground package is back to the TFS app.
        // We poll manually so we don't rely on a lambda whose 'd' variable
        // may be inferred as WebDriver (no executeScript method).
        long deadline = System.currentTimeMillis() + 15_000;
        boolean returned = false;
        String lastPkg = "";

        while (System.currentTimeMillis() < deadline) {
            try {
                lastPkg = String.valueOf(
                        driver.executeScript("mobile: getCurrentPackage"));
                if ("com.thefragranceshop".equals(lastPkg)) {
                    returned = true;
                    break;
                }
            } catch (Exception ignored) { }
            pause(500);
        }

        if (!returned) {
            throw new AssertionError(
                    "[FAIL] Step 3: Did not return to TFS app. Foreground is '"
                            + lastPkg + "'.");
        }
        System.out.println("[PASS] Step 3: Returned to TFS app (com.thefragranceshop).");
        pause(1500);

        if (!isDisplayed(tfsHomeMarker)) {
            System.out.println("[WARN] Step 3: TFS app foreground, but Home marker not yet visible.");
        } else {
            System.out.println("[PASS] Step 3: TFS Home screen visible.");
        }
    }
    // ==========================================================
    // PRIVATE HELPERS
    // ==========================================================

    private boolean isDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private void swipeUp() {
        Dimension size = driver.manage().window().getSize();
        int x = size.width / 2;
        int startY = (int) (size.height * 0.75);
        int endY   = (int) (size.height * 0.30);
        performSwipe(x, startY, x, endY);
    }

    private void performSwipe(int startX, int startY, int endX, int endY) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);
        swipe.addAction(finger.createPointerMove(
                Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(
                Duration.ofMillis(500), PointerInput.Origin.viewport(), endX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Collections.singletonList(swipe));
    }

    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted.", e);
        }
    }
}