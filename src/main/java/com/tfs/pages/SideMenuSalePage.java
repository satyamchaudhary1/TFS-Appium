package com.tfs.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Page object for TC026 - Side Menu -> "Sale" category -> expand/explore its 3
 * sub-categories (Sale, Offers, Discounts), visiting the "Shop All Sale" PLP
 * from the first one -> close the app.
 * <p>
 * Built on the helpers of SideMenuMenPage.java (tap-and-verify with a
 * bottom-edge exception, scroll-into-view before tapping, page-title
 * scroll-to-top before the back arrow, the app's own back arrow by coordinate
 * - no Android system back).
 * <p>
 * FLAGGED ASSUMPTIONS:
 *  - After "Sale" is expanded, TWO elements have content-desc="Sale": the main
 *    category row and the sub-category. A plain accessibility id would hit the
 *    wrong one, so they are told apart structurally: the main row contains an
 *    ImageView (the red SALE circle), the sub-category row contains only text
 *    and an arrow.
 *  - "Offers" and "Discounts" have real content-desc + clickable=true in the
 *    captured JSON, so they use accessibility ids.
 *  - "Shop All Sale" was captured as a bare container; it is found by its text
 *    and tapped through its clickable ancestor.
 *  - The back arrow (step 7) was captured as the usual mis-captured top-bar
 *    spacer; the confirmed real arrow coordinate is reused instead.
 */
public class SideMenuSalePage {

    private final AndroidDriver driver;
    private final Wait<AndroidDriver> wait;

    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);

    private static final int REF_SCREEN_WIDTH = 1220;
    private static final int REF_SCREEN_HEIGHT = 2712;

    // ---------------------------------------------------------------
    // Locators
    // ---------------------------------------------------------------

    private static final int SEARCH_NAV_ICON_X1 = 337;
    private static final int SEARCH_NAV_ICON_Y1 = 2380;
    private static final int SEARCH_NAV_ICON_X2 = 500;
    private static final int SEARCH_NAV_ICON_Y2 = 2543;

    private static final By SEARCH_FIELD =
            AppiumBy.xpath("//android.widget.EditText[@hint='Search']");

    // Step 2 - main "Sale" category row: clickable ViewGroup with
    // content-desc="Sale" that CONTAINS an ImageView (the red SALE circle).
    private static final By SALE_CATEGORY = AppiumBy.xpath(
            "//android.view.ViewGroup[@content-desc='Sale' and @clickable='true']"
                    + "[.//android.widget.ImageView]");

    // Step 4 - sub-category "Sale": same content-desc, but text + arrow only
    // (no ImageView). CONFIRMED content-desc + clickable=true in the JSON.
    private static final By SALE_SUB = AppiumBy.xpath(
            "//android.view.ViewGroup[@content-desc='Sale' and @clickable='true']"
                    + "[not(.//android.widget.ImageView)]");

    // Steps 9-12 - CONFIRMED real content-desc + clickable=true.
    private static final By OFFERS = AppiumBy.accessibilityId("Offers");
    private static final By DISCOUNTS = AppiumBy.accessibilityId("Discounts");

    // Step 5 - bare container in the JSON; found by its text instead.
    private static final By SHOP_ALL_SALE = textAncestorLocator("Shop All Sale");

    // PLP top-of-page landmark ("Sort By" sits right under the header).
    private static final By PLP_SORT_BY = AppiumBy.xpath("//android.widget.TextView[@text='Sort By']");

    // PLP page title - "Sale" (screenshot: back arrow, "Sale", product count).
    // The header scrolls away with the content, so this being visible means
    // the back arrow row is on screen too.
    private static final By PLP_HEADER_TITLE = AppiumBy.xpath("//android.widget.TextView[@text='Sale']");

    // Step 7 - TODO NOT CONFIRMED: see class javadoc. Reused from
    // BrandsCTAPage.java's confirmed real coordinate for this same element.
    private static final int GO_BACK_ARROW_X1 = 42;
    private static final int GO_BACK_ARROW_Y1 = 213;
    private static final int GO_BACK_ARROW_X2 = 160;
    private static final int GO_BACK_ARROW_Y2 = 332;

    private static final String APP_PACKAGE = "com.thefragranceshop";

    private static By textAncestorLocator(String exactText) {
        if (exactText.contains("'")) {
            return AppiumBy.xpath("//android.widget.TextView[@text=\"" + exactText
                    + "\"]/ancestor::*[@clickable='true'][1]");
        }
        return AppiumBy.xpath("//android.widget.TextView[@text='" + exactText
                + "']/ancestor::*[@clickable='true'][1]");
    }

    public SideMenuSalePage() {
        this(com.tfs.driver.DriverManager.getDriver());
    }

    public SideMenuSalePage(AndroidDriver driver) {
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
    // Step 2 - Tap the main "Sale" category to expand it
    // ---------------------------------------------------------------
    public void selectSaleCategory() {
        System.out.println("[STEP 02] Select 'Sale' category");
        tapToggleHeaderWithVerification(SALE_CATEGORY, "Sale (main category)");
    }

    // ---------------------------------------------------------------
    // Step 3 - Count and print the sub-categories
    // ---------------------------------------------------------------
    public void countAndPrintSubCategories() {
        System.out.println("[STEP 03] Count the sub-categories under 'Sale'");
        java.util.LinkedHashMap<String, By> expected = new java.util.LinkedHashMap<>();
        expected.put("Sale", SALE_SUB);
        expected.put("Offers", OFFERS);
        expected.put("Discounts", DISCOUNTS);

        java.util.LinkedHashSet<String> found = new java.util.LinkedHashSet<>();
        for (int pass = 0; pass < 3 && found.size() < expected.size(); pass++) {
            for (Map.Entry<String, By> e : expected.entrySet()) {
                if (isDisplayedSafely(e.getValue())) {
                    found.add(e.getKey());
                }
            }
            if (found.size() < expected.size()) {
                // The last sub-category can sit under the bottom nav bar - scroll once and re-check.
                scrollDown();
            }
        }
        System.out.println("[RESULT] Sub-categories found: " + found.size() + " -> " + found);
    }

    // ---------------------------------------------------------------
    // Step 4-5 - Expand the "Sale" sub-category, scroll its list, select "Shop All Sale"
    // ---------------------------------------------------------------
    public void expandSaleSubCategoryAndSelectShopAllSale() {
        System.out.println("[STEP 04] Select sub-category 'Sale'");
        tapToggleHeaderWithVerification(SALE_SUB, "Sale (sub-category)");

        System.out.println("[STEP 05] Scroll the sub-list, scroll back up, then select 'Shop All Sale'");
        for (int i = 0; i < 4; i++) {
            scrollDown();
        }
        scrollToTop();
        tapTextAncestorWithVerification(SHOP_ALL_SALE, "Shop All Sale");
    }

    // ---------------------------------------------------------------
    // Step 6 - Read the Shop All Sale PLP fully
    // ---------------------------------------------------------------
    public void readShopAllSalePlpFully() {
        System.out.println("[STEP 06] Read the Shop All Sale PLP, scrolling to capture all content");
        List<String> seen = new ArrayList<>(new java.util.LinkedHashSet<>(collectVisibleText()));
        for (int i = 0; i < 10; i++) {
            int before = seen.size();
            scrollDown();
            java.util.LinkedHashSet<String> merged = new java.util.LinkedHashSet<>(seen);
            merged.addAll(collectVisibleText());
            seen = new ArrayList<>(merged);
            if (seen.size() == before) {
                System.out.println("[INFO] No new content after scroll " + (i + 1) + " - reached the bottom.");
                break;
            }
        }
        printCollected("Shop All Sale PLP (full)", seen);

        // Back to the top (page title visible) so the back-arrow tap lands on
        // the real header, not on product content. Stops on the title landmark,
        // not on scrollGesture's unreliable boolean result.
        System.out.println("[ACTION] Scrolling the Shop All Sale PLP back to the top.");
        scrollToTopUntilVisible(PLP_HEADER_TITLE, 14);
        scrollUpOnce();
        scrollUpOnce();
    }

    // ---------------------------------------------------------------
    // Step 7 - Tap the app's own back arrow to return
    // ---------------------------------------------------------------
    public void tapBackArrowToReturn(String contextLabel) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            if (attempt > 1 && !isDisplayedSafely(PLP_HEADER_TITLE)) {
                System.out.println("[INFO] Page header not visible before retry - scrolling back to the top.");
                scrollToTopUntilVisible(PLP_HEADER_TITLE, 10);
            }
            List<String> before = collectVisibleText();
            System.out.println("[ACTION] Tapping the app's back arrow (" + contextLabel
                    + ", attempt " + attempt + "/3).");
            tapAtScaledCoordinates(GO_BACK_ARROW_X1, GO_BACK_ARROW_Y1, GO_BACK_ARROW_X2, GO_BACK_ARROW_Y2,
                    REF_SCREEN_WIDTH, REF_SCREEN_HEIGHT);

            if (waitForContentChangeFromSnapshot(before, 6)) {
                System.out.println("[PASS] Screen changed after the back arrow tap (" + contextLabel + ").");
                return;
            }
            System.out.println("[WARN] Screen unchanged after tapping the back arrow (" + contextLabel
                    + ", attempt " + attempt + "/3) - retrying.");
        }
        printAllVisibleTextAndDescs("Screen state after 3 failed back-arrow tap attempts (" + contextLabel + ")");
        throw new org.openqa.selenium.TimeoutException(
                "The app's back arrow did not change the screen (" + contextLabel + ") after 3 attempts.");
    }

    // ---------------------------------------------------------------
    // Step 8 - Collapse the "Sale" sub-category
    // ---------------------------------------------------------------
    public void collapseSaleSubCategory() {
        System.out.println("[STEP 08] Collapse sub-category 'Sale'");
        tapToggleHeaderWithVerification(SALE_SUB, "Sale (sub-category, collapse)");
    }

    // ---------------------------------------------------------------
    // Step 9-10 - Expand "Offers", read and print its content, collapse
    // ---------------------------------------------------------------
    public void expandReadAndCollapseOffers() {
        System.out.println("[STEP 09] Select 'Offers'");
        tapToggleHeaderWithVerification(OFFERS, "Offers");
        readExpandedListAndPrint("Offers list");

        System.out.println("[STEP 10] Collapse 'Offers'");
        tapToggleHeaderWithVerification(OFFERS, "Offers (collapse)");
    }

    // ---------------------------------------------------------------
    // Step 11-12 - Expand "Discounts", read and print its content, collapse
    // ---------------------------------------------------------------
    public void expandReadAndCollapseDiscounts() {
        System.out.println("[STEP 11] Select 'Discounts'");
        tapToggleHeaderWithVerification(DISCOUNTS, "Discounts");
        readExpandedListAndPrint("Discounts list");

        System.out.println("[STEP 12] Collapse 'Discounts'");
        tapToggleHeaderWithVerification(DISCOUNTS, "Discounts (collapse)");
    }

    /**
     * Prints what the expanded list shows. Scrolls down up to twice to pick up
     * rows that opened below the visible area, then scrolls back to the top.
     */
    private void readExpandedListAndPrint(String label) {
        java.util.LinkedHashSet<String> seen = new java.util.LinkedHashSet<>(collectVisibleText());
        for (int i = 0; i < 2; i++) {
            int before = seen.size();
            scrollDown();
            seen.addAll(collectVisibleText());
            if (seen.size() == before) {
                break;
            }
        }
        printCollected(label, seen);
        scrollToTop();
    }

    // ---------------------------------------------------------------
    // Step 14 - Close the app
    // ---------------------------------------------------------------
    public void closeApp() {
        System.out.println("[STEP 14] Close the app");
        driver.terminateApp(APP_PACKAGE);
        System.out.println("[PASS] App terminated: " + APP_PACKAGE);
    }

    // =================================================================
    // Helpers
    // =================================================================

    /**
     * Makes sure the target is actually on screen before trying to tap it.
     * React Native lists only expose currently-visible rows in the
     * accessibility tree, and a real run showed that returning from the
     * Shop All PLP restores the Menu at a different scroll offset than
     * before - the target header was scrolled out of view above, so it
     * simply wasn't in the tree and the tap helper timed out waiting for
     * it. Scrolls up first (headers are above their sub-lists), then down
     * as a fallback, stopping as soon as the target is visible.
     */
    private void ensureVisible(By locator, String label) {
        if (isDisplayedSafely(locator)) {
            return;
        }
        System.out.println("[INFO] '" + label + "' not currently visible - scrolling to find it.");
        for (int i = 0; i < 8 && !isDisplayedSafely(locator); i++) {
            scrollUpOnce();
        }
        for (int i = 0; i < 8 && !isDisplayedSafely(locator); i++) {
            scrollDown();
        }
        if (isDisplayedSafely(locator)) {
            System.out.println("[PASS] '" + label + "' is now visible.");
        } else {
            System.out.println("[WARN] '" + label + "' still not visible after scrolling up and down.");
            // Dump what is actually on screen so the next failure shows which
            // screen the app is on (Menu, Home, PLP, ...) instead of a bare timeout.
            printCollected("Screen state - '" + label + "' not found after scrolling", collectVisibleText());
        }
    }

    private void scrollUpOnce() {
        Map<String, Object> params = new HashMap<>();
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        params.put("left", (int) (size.width * 0.1));
        params.put("top", (int) (size.height * 0.2));
        params.put("width", (int) (size.width * 0.8));
        params.put("height", (int) (size.height * 0.6));
        params.put("direction", "up");
        params.put("percent", 0.6);
        Object result = driver.executeScript("mobile: scrollGesture", params);
        System.out.println("[SCROLL] scroll-up result: " + result);
    }

    /**
     * Tap helper for expand/collapse headers (works with either locator
     * type). Same tap-and-verify as the helpers that passed steps 1-12 in the
     * earlier TC025 run, with ONE exception: a header in the bottom fifth of
     * the screen (that run: "Discover Your New Fragrance" at y~2260). When
     * such a header expands or collapses, the rows that change are below the
     * visible area, so the visible text does not change and the normal check
     * wrongly reports failure - then the retries flip the list back and
     * forth. For those headers the helper taps ONCE and does not treat
     * "no visible change" as a failure.
     *
     * An earlier attempt scrolled every header into the upper half first;
     * that broke Step 8 (the header scrolled out of the tree), so it was
     * removed.
     */
    private void tapToggleHeaderWithVerification(By locator, String label) {
        ensureVisible(locator, label);
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        int bottomZone = (int) (size.height * 0.8);

        for (int attempt = 1; attempt <= 3; attempt++) {
            List<String> before = collectVisibleText();
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
            org.openqa.selenium.Rectangle rect = el.getRect();
            int centerY = rect.getY() + rect.getHeight() / 2;
            System.out.println("[ACTION] Tapping '" + label + "' at its live bounds "
                    + "(attempt " + attempt + "/3).");
            tapAtRealCoordinates(rect.getX() + rect.getWidth() / 2, centerY);

            if (centerY > bottomZone) {
                System.out.println("[INFO] '" + label + "' is at the bottom edge (y=" + centerY
                        + ") - its sub-list opens or closes below the visible area, so a visible-text "
                        + "change cannot be used to verify this tap. Tapped once; not retrying.");
                try {
                    Thread.sleep(800);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return;
            }

            if (waitForContentChangeFromSnapshot(before, 5)) {
                System.out.println("[PASS] Screen changed after tapping '" + label + "'.");
                return;
            }
            System.out.println("[WARN] Screen unchanged after tapping '" + label + "' (attempt "
                    + attempt + "/3) - retrying.");
        }
        throw new org.openqa.selenium.TimeoutException(
                "'" + label + "' tap did not change the screen after 3 attempts.");
    }

    private void tapTextAncestorWithVerification(By locator, String label) {
        ensureVisible(locator, label);
        for (int attempt = 1; attempt <= 3; attempt++) {
            List<String> before = collectVisibleText();
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
            org.openqa.selenium.Rectangle rect = el.getRect();
            System.out.println("[ACTION] Tapping '" + label + "' at its live bounds "
                    + "(attempt " + attempt + "/3).");
            tapAtRealCoordinates(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);

            if (waitForContentChangeFromSnapshot(before, 5)) {
                System.out.println("[PASS] Screen changed after tapping '" + label + "'.");
                return;
            }
            System.out.println("[WARN] Screen unchanged after tapping '" + label + "' (attempt "
                    + attempt + "/3) - retrying.");
        }
        throw new org.openqa.selenium.TimeoutException(
                "'" + label + "' tap did not change the screen after 3 attempts.");
    }

    private void tapConfirmedButtonWithVerification(By locator, String label) {
        ensureVisible(locator, label);
        for (int attempt = 1; attempt <= 3; attempt++) {
            List<String> before = collectVisibleText();
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
            org.openqa.selenium.Rectangle rect = el.getRect();
            System.out.println("[ACTION] Tapping '" + label + "' at its live bounds "
                    + "(attempt " + attempt + "/3).");
            tapAtRealCoordinates(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);

            if (waitForContentChangeFromSnapshot(before, 5)) {
                System.out.println("[PASS] Screen changed after tapping '" + label + "'.");
                return;
            }
            System.out.println("[WARN] Screen unchanged after tapping '" + label + "' (attempt "
                    + attempt + "/3) - retrying.");
        }
        throw new org.openqa.selenium.TimeoutException(
                "'" + label + "' tap did not change the screen after 3 attempts.");
    }

    private boolean waitForContentChangeFromSnapshot(List<String> before, int timeoutSeconds) {
        try {
            new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofMillis(400))
                    .until(d -> !collectVisibleText().equals(before));
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    private void tapAtRealCoordinates(int x, int y) {
        org.openqa.selenium.interactions.PointerInput finger =
                new org.openqa.selenium.interactions.PointerInput(
                        org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
        org.openqa.selenium.interactions.Sequence tap =
                new org.openqa.selenium.interactions.Sequence(finger, 0);
        tap.addAction(finger.createPointerMove(Duration.ZERO,
                org.openqa.selenium.interactions.PointerInput.Origin.viewport(), x, y));
        tap.addAction(finger.createPointerDown(
                org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(finger.createPointerUp(
                org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(java.util.Collections.singletonList(tap));
        System.out.println("[ACTION] Tapped real coordinate (" + x + "," + y + ").");
    }

    private void tapAtScaledCoordinates(int refX1, int refY1, int refX2, int refY2,
                                        int refWidth, int refHeight) {
        org.openqa.selenium.Dimension screenSize = driver.manage().window().getSize();
        double xRatio = (double) screenSize.width / refWidth;
        double yRatio = (double) screenSize.height / refHeight;

        int centerX = (int) Math.round(((refX1 + refX2) / 2.0) * xRatio);
        int centerY = (int) Math.round(((refY1 + refY2) / 2.0) * yRatio);
        tapAtRealCoordinates(centerX, centerY);
        System.out.println("[INFO] [reference was (" + ((refX1 + refX2) / 2) + "," + ((refY1 + refY2) / 2)
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

    private void scrollDown() {
        Map<String, Object> params = new HashMap<>();
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        params.put("left", (int) (size.width * 0.1));
        params.put("top", (int) (size.height * 0.2));
        params.put("width", (int) (size.width * 0.8));
        params.put("height", (int) (size.height * 0.6));
        params.put("direction", "down");
        params.put("percent", 0.6);
        Object result = driver.executeScript("mobile: scrollGesture", params);
        System.out.println("[SCROLL] element-based scrollGesture result: " + result);
    }

    private void scrollToTop() {
        for (int i = 0; i < 8; i++) {
            Map<String, Object> params = new HashMap<>();
            org.openqa.selenium.Dimension size = driver.manage().window().getSize();
            params.put("left", (int) (size.width * 0.1));
            params.put("top", (int) (size.height * 0.2));
            params.put("width", (int) (size.width * 0.8));
            params.put("height", (int) (size.height * 0.6));
            params.put("direction", "up");
            params.put("percent", 0.75);
            Object result = driver.executeScript("mobile: scrollGesture", params);
            if (Boolean.FALSE.equals(result)) {
                break;
            }
        }
    }

    /**
     * Scrolls up repeatedly until a given top-of-page landmark becomes
     * visible, rather than trusting mobile:scrollGesture's own boolean
     * result as the stopping signal - this app has repeatedly shown that
     * result isn't fully reliable (returning false even while further
     * scrolling genuinely still works). Keeps scrolling the full attempt
     * budget regardless of individual gesture results.
     */
    private void scrollToTopUntilVisible(By landmark, int maxScrolls) {
        for (int i = 0; i < maxScrolls && !isDisplayedSafely(landmark); i++) {
            Map<String, Object> params = new HashMap<>();
            org.openqa.selenium.Dimension size = driver.manage().window().getSize();
            params.put("left", (int) (size.width * 0.1));
            params.put("top", (int) (size.height * 0.2));
            params.put("width", (int) (size.width * 0.8));
            params.put("height", (int) (size.height * 0.6));
            params.put("direction", "up");
            params.put("percent", 0.75);
            Object result = driver.executeScript("mobile: scrollGesture", params);
            System.out.println("[SCROLL] scroll-to-top-until-visible attempt " + (i + 1) + "/" + maxScrolls
                    + " result: " + result);
        }
        if (!isDisplayedSafely(landmark)) {
            System.out.println("[WARN] Top-of-page landmark still not visible after " + maxScrolls
                    + " scroll-up attempts.");
        }
    }

    private List<String> collectVisibleText() {
        List<String> lines = new ArrayList<>();
        for (WebElement tv : driver.findElements(AppiumBy.className("android.widget.TextView"))) {
            try {
                String text = tv.getText();
                if (text != null && !text.trim().isEmpty()) {
                    lines.add(text.trim());
                }
            } catch (Exception ignored) {
                // stale element - skip
            }
        }
        return lines;
    }

    private void printCollected(String label, java.util.Collection<String> lines) {
        System.out.println("----- [" + label + "] visible text (" + lines.size() + " line(s)) -----");
        for (String line : lines) {
            System.out.println("    " + line);
        }
        System.out.println("----- end of [" + label + "] -----");
    }

    private void printAllVisibleTextAndDescs(String label) {
        List<String> lines = new ArrayList<>();
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