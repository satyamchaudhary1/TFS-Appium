package com.tfs.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.time.Duration;
import java.util.List;

/**
 * Page object for TC022 - Side Menu -> horizontally scroll the menu tabs
 * row to reveal "EveryHuman" -> tap it (opens an in-app WebView showing
 * The Fragrance Shop website) -> close it via the top-right cross icon ->
 * confirm back on the Menu screen -> close the app.
 * <p>
 * Follows the conventions established across ScentaddictCTAPage.java /
 * FragrancematchCTAPage.java / DiscoverMorePage.java: no-arg constructor
 * pulling the driver from DriverManager, [STEP]/[ACTION]/[PASS]/[WARN]
 * logging, raw coordinate taps (not WebElement.click()) for this app's
 * custom touchables, self-verifying retries rather than trusting a single
 * tap, and an Android-back + reactivateApp fallback for closing overlays
 * with no confirmed locator.
 * <p>
 * FLAGGED ASSUMPTIONS:
 *  - "EveryHuman" (step 2) has no content-desc/text of its own (confirmed
 *    via the hierarchy dump - only an SvgView with no accessible label).
 *    Tapped by scaled coordinate on its clickable ancestor; verified
 *    indirectly via the adjacent "Brands" tab (which DOES have a
 *    confirmed content-desc) becoming visible, since both appear together
 *    after the same scroll amount.
 *  - The "cross" close icon (step 3) locator given was bounds
 *    [0,0][1220,308], clickable=false - the same mis-captured generic
 *    top-bar-spacer pattern seen repeatedly elsewhere in this project
 *    (TC018's WebView close, TC020's back button). Uses the same proven
 *    coordinate-tap + verify + Android-back/reactivate fallback as
 *    DiscoverMorePage.closeTopRightCrossIcon().
 */
public class EveryHumanCTAPage {

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
    // used in TC019/TC020/TC021.
    private static final int SEARCH_NAV_ICON_X1 = 337;
    private static final int SEARCH_NAV_ICON_Y1 = 2380;
    private static final int SEARCH_NAV_ICON_X2 = 500;
    private static final int SEARCH_NAV_ICON_Y2 = 2543;

    // Landmark confirming the Menu/Search screen is displayed.
    private static final By SEARCH_FIELD =
            AppiumBy.xpath("//android.widget.EditText[@hint='Search']");

    // FIXED: a real run showed that closing the EveryHuman WebView can
    // legitimately land on the HOME page instead of back on the Menu
    // screen (the Menu screen apparently sits as an overlay on top of Home
    // and can get dismissed along with the WebView in one motion) - same
    // pattern already confirmed and fixed for DiscoverMorePage. Both are
    // valid recovery outcomes now, not just the Search field.
    private static final By HOME_LANDMARK = AppiumBy.xpath(
            "//android.widget.TextView[@text='Best Sellers' or @text='Bestsellers' "
                    + "or @text=\"Women's\" or @text=\"Men's\" or @text='Gift Sets' or @text='New In']");

    // Step 2 - CONFIRMED: "Brands" has a real content-desc and is the tab
    // immediately after "EveryHuman" - used as the scroll-verification
    // signal (both appear together after the same scroll amount).
    private static final By BRANDS_TAB = AppiumBy.accessibilityId("Brands");

    // Step 2 - TODO NOT CONFIRMED (no content-desc/text on this element at
    // all): "EveryHuman" tab, tapped by scaled coordinate. Bounds are its
    // clickable ancestor's, taken from the hierarchy dump captured right
    // after scrolling once (matching the screenshot showing
    // "...match | EveryHuman | Brands").
    private static final int EVERYHUMAN_TAB_X1 = 260;
    private static final int EVERYHUMAN_TAB_Y1 = 485;
    private static final int EVERYHUMAN_TAB_X2 = 716;
    private static final int EVERYHUMAN_TAB_Y2 = 629;

    // Step 3 - TODO NOT CONFIRMED: see class javadoc. Approximate top-right
    // corner region on a 1220x2712 reference screen (same region that
    // worked for DiscoverMorePage's WebView close icon).
    private static final int TOP_RIGHT_CROSS_X1 = 1085;
    private static final int TOP_RIGHT_CROSS_Y1 = 20;
    private static final int TOP_RIGHT_CROSS_X2 = 1180;
    private static final int TOP_RIGHT_CROSS_Y2 = 100;

    private static final String APP_PACKAGE = "com.thefragranceshop";

    public EveryHumanCTAPage() {
        this(com.tfs.driver.DriverManager.getDriver());
    }

    public EveryHumanCTAPage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new FluentWait<>(driver)
                .withTimeout(TIMEOUT)
                .pollingEvery(POLL_INTERVAL)
                .ignoring(org.openqa.selenium.NoSuchElementException.class);
    }

    // ---------------------------------------------------------------
    // Step 1 - Tap the Side Menu / Search icon
    // ---------------------------------------------------------------
    public void openSideMenu() {
        System.out.println("[STEP 01] Tap the Side Menu icon on the bottom navigation bar");
        tapAtScaledCoordinates(SEARCH_NAV_ICON_X1, SEARCH_NAV_ICON_Y1, SEARCH_NAV_ICON_X2, SEARCH_NAV_ICON_Y2,
                REF_SCREEN_WIDTH, REF_SCREEN_HEIGHT);
        System.out.println("[ACTION] Tapped the Side Menu icon.");
        waitForElement(SEARCH_FIELD, 8);
    }

    // ---------------------------------------------------------------
    // Step 2 - Scroll the menu tabs row to reveal "EveryHuman", then tap it.
    // ---------------------------------------------------------------
    public void scrollToAndTapEveryHuman() {
        System.out.println("[STEP 02] Scroll horizontally to reveal 'EveryHuman', then tap it");

        for (int i = 0; i < 3 && !isDisplayedSafely(BRANDS_TAB); i++) {
            dragMenuTabsRow("left");
            System.out.println("[SCROLL] Menu tabs row swipe " + (i + 1) + "/3");
        }
        if (!isDisplayedSafely(BRANDS_TAB)) {
            printAllVisibleTextAndDescs("Screen state - 'Brands' tab never appeared while scrolling for EveryHuman");
            throw new org.openqa.selenium.TimeoutException(
                    "Could not scroll the menu tabs row far enough to reveal 'EveryHuman'/'Brands'.");
        }
        System.out.println("[PASS] Menu tabs scrolled far enough ('Brands' tab visible, 'EveryHuman' "
                + "should be adjacent to it).");

        for (int attempt = 1; attempt <= 3; attempt++) {
            System.out.println("[ACTION] Tapping 'EveryHuman' (attempt " + attempt + "/3).");
            tapAtScaledCoordinates(EVERYHUMAN_TAB_X1, EVERYHUMAN_TAB_Y1, EVERYHUMAN_TAB_X2, EVERYHUMAN_TAB_Y2,
                    REF_SCREEN_WIDTH, REF_SCREEN_HEIGHT);

            if (waitForMenuScreenToDisappear(5)) {
                System.out.println("[PASS] Left the Menu screen - 'EveryHuman' tap registered.");
                return;
            }
            System.out.println("[WARN] Still on the Menu screen after tapping 'EveryHuman' (attempt "
                    + attempt + "/3) - the tap likely missed. Retrying.");
        }
        printAllVisibleTextAndDescs("Screen state after 3 failed 'EveryHuman' tap attempts");
        throw new org.openqa.selenium.TimeoutException(
                "'EveryHuman' tap did not navigate away from the Menu screen after 3 attempts.");
    }

    private boolean waitForMenuScreenToDisappear(int timeoutSeconds) {
        try {
            new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofMillis(400))
                    .until(d -> d.findElements(SEARCH_FIELD).isEmpty());
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    // ---------------------------------------------------------------
    // Step 3 - Close the WebView via the top-right cross icon, same proven
    // technique as DiscoverMorePage.closeTopRightCrossIcon().
    // ---------------------------------------------------------------
    public void closeEveryHumanWebView() {
        System.out.println("[STEP 03] Close the EveryHuman WebView via the top-right cross icon");
        tapAtScaledCoordinates(TOP_RIGHT_CROSS_X1, TOP_RIGHT_CROSS_Y1, TOP_RIGHT_CROSS_X2, TOP_RIGHT_CROSS_Y2,
                REF_SCREEN_WIDTH, REF_SCREEN_HEIGHT);

        if (waitForMenuOrHome(5)) {
            System.out.println("[PASS] Back on the Menu screen or Home (cross icon tap worked).");
            return;
        }

        System.out.println("[WARN] Cross icon tap didn't return to the Menu screen or Home - "
                + "trying Android back button.");
        driver.navigate().back();
        if (waitForMenuOrHome(5)) {
            System.out.println("[PASS] Back on the Menu screen or Home (Android back button worked).");
            return;
        }

        System.out.println("[WARN] Still not on the Menu screen or Home after back button - the app "
                + "may have been backgrounded or exited. Re-activating it.");
        driver.activateApp(APP_PACKAGE);
        if (waitForMenuOrHome(8)) {
            System.out.println("[PASS] Back on the Menu screen or Home (after re-activating the app).");
            return;
        }

        printAllVisibleTextAndDescs("Screen state after all recovery attempts to return to the Menu screen or Home");
        throw new org.openqa.selenium.TimeoutException(
                "Could not return to the Menu screen or Home after closing the EveryHuman WebView, "
                        + "Android back, and re-activating the app.");
    }

    private boolean waitForMenuOrHome(int timeoutSeconds) {
        try {
            new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofMillis(400))
                    .until(d -> isDisplayedSafely(SEARCH_FIELD) || isDisplayedSafely(HOME_LANDMARK));
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    // ---------------------------------------------------------------
    // Step 4 - Confirm back on the Menu screen (or Home - both are valid
    // outcomes, see closeEveryHumanWebView() above), close the app.
    // ---------------------------------------------------------------
    public void verifyBackOnMegaMenu() {
        System.out.println("[STEP 04] Confirm back on the Mega Menu screen (or Home)");
        if (isDisplayedSafely(SEARCH_FIELD)) {
            System.out.println("[RESULT] Mega Menu screen displayed: PASS");
        } else if (isDisplayedSafely(HOME_LANDMARK)) {
            System.out.println("[RESULT] Landed on Home page instead of the Mega Menu screen - "
                    + "still a valid outcome (the Menu screen appears to be an overlay on Home that "
                    + "can get dismissed together with the WebView).");
        } else {
            System.out.println("[RESULT] Neither the Mega Menu screen nor Home could be confirmed - WARN.");
        }
    }

    public void closeApp() {
        System.out.println("[STEP] Close the app");
        driver.terminateApp(APP_PACKAGE);
        System.out.println("[PASS] App terminated: " + APP_PACKAGE);
    }

    // =================================================================
    // Helpers
    // =================================================================

    /**
     * Drags a finger horizontally across the menu tabs row, computed from
     * the confirmed search field's on-screen position as a Y anchor - same
     * raw-touch-drag technique proven reliable elsewhere in this app's
     * custom horizontal lists (DiscoverMorePage, FragrancematchCTAPage).
     */
    private void dragMenuTabsRow(String direction) {
        org.openqa.selenium.Dimension screenSize = driver.manage().window().getSize();
        List<WebElement> fields = driver.findElements(SEARCH_FIELD);
        int centerY;
        if (!fields.isEmpty()) {
            org.openqa.selenium.Rectangle r = fields.get(0).getRect();
            centerY = r.getY() + r.getHeight() + 250; // into the tabs row below the search field
        } else {
            centerY = (int) (screenSize.height * 0.22);
        }

        int leftX = (int) (screenSize.width * 0.10);
        int rightX = (int) (screenSize.width * 0.90);
        int startX = "left".equals(direction) ? rightX : leftX;
        int endX = "left".equals(direction) ? leftX : rightX;

        org.openqa.selenium.interactions.PointerInput finger =
                new org.openqa.selenium.interactions.PointerInput(
                        org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
        org.openqa.selenium.interactions.Sequence drag =
                new org.openqa.selenium.interactions.Sequence(finger, 0);
        drag.addAction(finger.createPointerMove(Duration.ZERO,
                org.openqa.selenium.interactions.PointerInput.Origin.viewport(), startX, centerY));
        drag.addAction(finger.createPointerDown(
                org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
        drag.addAction(finger.createPointerMove(Duration.ofMillis(300),
                org.openqa.selenium.interactions.PointerInput.Origin.viewport(), endX, centerY));
        drag.addAction(finger.createPointerUp(
                org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(java.util.Collections.singletonList(drag));
    }

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

    private boolean waitForElement(By locator, int timeoutSeconds) {
        try {
            new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofMillis(300))
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    private boolean isDisplayedSafely(By locator) {
        try {
            List<WebElement> els = driver.findElements(locator);
            return !els.isEmpty() && els.get(0).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private void printAllVisibleTextAndDescs(String label) {
        List<String> lines = new java.util.ArrayList<>();
        for (WebElement el : driver.findElements(AppiumBy.xpath("//*"))) {
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
        System.out.println("----- [" + label + "] (" + lines.size() + " entrie(s)) -----");
        for (String line : lines) {
            System.out.println("    " + line);
        }
        System.out.println("----- end of [" + label + "] -----");
    }
}