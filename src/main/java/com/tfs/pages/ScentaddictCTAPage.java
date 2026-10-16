package com.tfs.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Page object for TC020 - Side Menu (Search screen) -> "scentaddict" CTA ->
 * opens an external Chrome Custom Tab (documented current behaviour, not a
 * bug to work around) -> close that tab -> close the app.
 * <p>
 * Follows the same conventions as SearchFieldPage.java / DiscoverMorePage.java:
 * no-arg constructor pulling the driver from DriverManager, [STEP]/[ACTION]/
 * [PASS]/[WARN] logging, tapAtScaledCoordinates() for icon-only elements
 * with no reliable locator.
 * <p>
 * FLAGGED: the "scentaddict" CTA (step 2) has an empty content-desc in the
 * Inspector capture - no reliable accessibility id exists for it, so it's
 * tapped by scaled coordinate using the confirmed bounds. Step 3's "Close
 * tab" button lives in a DIFFERENT app package entirely (com.android.chrome,
 * confirmed by the package field in the captured JSON) - this is an
 * external Chrome Custom Tab, not part of the TFS app. UiAutomator2 can
 * still interact with it directly since it's on-screen; no context switch
 * is needed the way a WebView embedded inside the app would require.
 */
public class ScentaddictCTAPage {

    private final AndroidDriver driver;
    private final Wait<AndroidDriver> wait;

    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);

    private static final int REF_SCREEN_WIDTH = 1220;
    private static final int REF_SCREEN_HEIGHT = 2712;

    // ---------------------------------------------------------------
    // Locators
    // ---------------------------------------------------------------

    // Step 1 - Side Menu / Search icon (2nd bottom-nav icon). Same element
    // used in TC019 (SearchFieldPage) - confirmed bounds from Inspector.
    private static final int SEARCH_NAV_ICON_X1 = 337;
    private static final int SEARCH_NAV_ICON_Y1 = 2380;
    private static final int SEARCH_NAV_ICON_X2 = 500;
    private static final int SEARCH_NAV_ICON_Y2 = 2543;

    // Step 2 - TODO NOT CONFIRMED (no content-desc captured): the
    // "scentaddict" CTA pill button. Tapped by scaled coordinate.
    private static final int SCENTADDICT_CTA_X1 = 132;
    private static final int SCENTADDICT_CTA_Y1 = 485;
    private static final int SCENTADDICT_CTA_X2 = 588;
    private static final int SCENTADDICT_CTA_Y2 = 629;

    // Step 3 - CONFIRMED. Lives in com.android.chrome, NOT com.thefragranceshop -
    // this is the external Chrome Custom Tab's own close button.
    private static final By CHROME_CLOSE_TAB = AppiumBy.accessibilityId("Close tab");
    private static final By CHROME_CLOSE_TAB_BY_ID = By.id("com.android.chrome:id/close_button");

    private static final String APP_PACKAGE = "com.thefragranceshop";
    private static final String CHROME_PACKAGE = "com.android.chrome";

    public ScentaddictCTAPage() {
        this(com.tfs.driver.DriverManager.getDriver());
    }

    public ScentaddictCTAPage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new FluentWait<>(driver)
                .withTimeout(TIMEOUT)
                .pollingEvery(POLL_INTERVAL)
                .ignoring(org.openqa.selenium.NoSuchElementException.class);
    }

    // ---------------------------------------------------------------
    // Step 1 - Tap the Side Menu / Search icon (2nd bottom-nav icon)
    // ---------------------------------------------------------------
    public void openSideMenu() {
        System.out.println("[STEP 01] Tap the Side Menu icon on the bottom navigation bar");
        tapAtScaledCoordinates(SEARCH_NAV_ICON_X1, SEARCH_NAV_ICON_Y1, SEARCH_NAV_ICON_X2, SEARCH_NAV_ICON_Y2,
                REF_SCREEN_WIDTH, REF_SCREEN_HEIGHT);
        System.out.println("[ACTION] Tapped the Side Menu icon.");
    }

    // ---------------------------------------------------------------
    // Step 2 - Tap the "scentaddict" CTA button.
    //
    // FIXED: the inner SvgView bounds you captured ([233,486]-[487,626])
    // sit almost dead-center inside the outer clickable region this method
    // already targets ([132,485]-[588,629], center 360,557) - so the tap
    // coordinate itself wasn't imprecise. The more likely cause, matching a
    // pattern seen elsewhere in this app (the VISA-row selection in
    // TC017): a timing race where the tap lands before the button is
    // actually interactive (entrance animation/transition not yet
    // settled), landing on the screen without triggering navigation. Now
    // verifies the tap actually worked (Chrome became the foreground
    // package) and retries up to 3 times if not, instead of trusting a
    // single blind tap.
    // ---------------------------------------------------------------
    public void tapScentaddictCta() {
        System.out.println("[STEP 02] Select the 'scentaddict' button");

        for (int attempt = 1; attempt <= 3; attempt++) {
            tapAtScaledCoordinates(SCENTADDICT_CTA_X1, SCENTADDICT_CTA_Y1, SCENTADDICT_CTA_X2, SCENTADDICT_CTA_Y2,
                    REF_SCREEN_WIDTH, REF_SCREEN_HEIGHT);
            System.out.println("[ACTION] Tapped 'scentaddict' (attempt " + attempt + "/3).");

            boolean chromeOpened = waitForChromeForeground(5);
            if (chromeOpened) {
                System.out.println("[PASS] Chrome tab opened - 'scentaddict' tap registered.");
                return;
            }

            System.out.println("[WARN] Chrome did not come to the foreground within 5s after the tap "
                    + "(attempt " + attempt + "/3) - the tap likely landed before the button was "
                    + "interactive yet.");
        }

        throw new org.openqa.selenium.TimeoutException(
                "'scentaddict' tap did not open the Chrome tab after 3 attempts.");
    }

    private boolean waitForChromeForeground(int timeoutSeconds) {
        try {
            new FluentWait<AndroidDriver>(driver)
                    .withTimeout(Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofMillis(500))
                    .until(d -> {
                        try {
                            return CHROME_PACKAGE.equals(d.getCurrentPackage());
                        } catch (Exception e) {
                            return false;
                        }
                    });
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    // ---------------------------------------------------------------
    // Step 3 - "scentaddict" opens an external Chrome Custom Tab (documented
    // current behaviour). Close it via Chrome's own "Close tab" button.
    // ---------------------------------------------------------------
    public void closeScentaddictChromeTab() {
        System.out.println("[STEP 03] 'scentaddict' opened an external Chrome tab (expected "
                + "current behaviour) - closing it");

        WebElement closeTab;
        try {
            closeTab = wait.until(ExpectedConditions.elementToBeClickable(CHROME_CLOSE_TAB));
        } catch (org.openqa.selenium.TimeoutException e1) {
            System.out.println("[WARN] 'Close tab' not found by accessibility id - trying by resource-id.");
            try {
                closeTab = wait.until(ExpectedConditions.elementToBeClickable(CHROME_CLOSE_TAB_BY_ID));
            } catch (org.openqa.selenium.TimeoutException e2) {
                // Neither locator found anything in 40s total - don't guess
                // again, dump exactly what's on screen so the next run is
                // diagnosable instead of another blind retry.
                String currentPackage;
                try {
                    currentPackage = driver.getCurrentPackage();
                } catch (Exception ex) {
                    currentPackage = "<unable to read current package: " + ex.getMessage() + ">";
                }
                System.out.println("[FAIL] Neither 'Close tab' locator found after 40s total.");
                System.out.println("[INFO] Current foreground package: " + currentPackage);
                printAllVisibleText("Screen state when 'Close tab' was not found");
                throw e2;
            }
        }
        System.out.println("[ACTION] Tapping Chrome's 'Close tab' button.");
        closeTab.click();
    }

    /**
     * Dumps every non-empty text/content-desc value currently in the
     * accessibility tree, regardless of app package - useful here since
     * the screen in question may belong to Chrome, not the TFS app.
     */
    private void printAllVisibleText(String sectionLabel) {
        List<WebElement> allElements = driver.findElements(AppiumBy.xpath("//*"));
        List<String> lines = new java.util.ArrayList<>();
        for (WebElement el : allElements) {
            try {
                String text = el.getText();
                if (text != null && !text.trim().isEmpty()) {
                    lines.add("text: " + text.trim());
                }
                String desc = el.getAttribute("content-desc");
                if (desc != null && !desc.trim().isEmpty()) {
                    lines.add("content-desc: " + desc.trim());
                }
            } catch (Exception ignored) {
                // stale element - skip
            }
        }
        System.out.println("----- [" + sectionLabel + "] visible text/content-desc (" + lines.size()
                + " entrie(s)) -----");
        for (String line : lines) {
            System.out.println("    " + line);
        }
        System.out.println("----- end of [" + sectionLabel + "] -----");
    }

    // ---------------------------------------------------------------
    // Step 4 - Close the app
    // ---------------------------------------------------------------
    public void closeApp() {
        System.out.println("[STEP 04] Close the app");
        driver.terminateApp(APP_PACKAGE);
        System.out.println("[PASS] App terminated: " + APP_PACKAGE);
    }

    // =================================================================
    // Helpers (mirroring SearchFieldPage.java / DiscoverMorePage.java)
    // =================================================================

    private void tapAtScaledCoordinates(int refX1, int refY1, int refX2, int refY2,
                                        int refWidth, int refHeight) {
        org.openqa.selenium.Dimension screenSize = driver.manage().window().getSize();
        double xRatio = (double) screenSize.width / refWidth;
        double yRatio = (double) screenSize.height / refHeight;

        int centerX = (int) Math.round(((refX1 + refX2) / 2.0) * xRatio);
        int centerY = (int) Math.round(((refY1 + refY2) / 2.0) * yRatio);

        org.openqa.selenium.interactions.PointerInput finger =
                new org.openqa.selenium.interactions.PointerInput(
                        org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
        org.openqa.selenium.interactions.Sequence tap =
                new org.openqa.selenium.interactions.Sequence(finger, 0);
        tap.addAction(finger.createPointerMove(Duration.ZERO,
                org.openqa.selenium.interactions.PointerInput.Origin.viewport(), centerX, centerY));
        tap.addAction(finger.createPointerDown(
                org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(finger.createPointerUp(
                org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(java.util.Collections.singletonList(tap));
        System.out.println("[ACTION] Tapped scaled coordinate (" + centerX + "," + centerY + ") "
                + "[reference was (" + ((refX1 + refX2) / 2) + "," + ((refY1 + refY2) / 2)
                + ") on a " + refWidth + "x" + refHeight + " screen].");
    }
}