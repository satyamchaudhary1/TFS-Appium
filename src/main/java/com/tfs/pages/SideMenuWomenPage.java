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
 * Page object for TC024 - Side Menu -> "Women" category -> expand/explore
 * its 4 sub-categories (Women's Fragrance, Popular Brands, Shop By
 * Fragrance Strength, Discover Your New Fragrance), visiting the "Shop
 * All" PLP from the first one -> close the app.
 * <p>
 * Follows the conventions established across BrandsCTAPage.java /
 * EveryHumanCTAPage.java / FragrancematchCTAPage.java: no-arg constructor
 * pulling the driver from DriverManager, [STEP]/[ACTION]/[PASS]/[WARN]
 * logging, raw coordinate taps with content-change verification and retry
 * for every interactive tap.
 * <p>
 * FLAGGED ASSUMPTIONS:
 *  - Every sub-category TextView ("Women", "Women's Fragrance", "Popular
 *    Brands", "Discover Your New Fragrance", "Shop All") is clickable=false
 *    - tapped via clickable ancestor, anchored by exact text rather than
 *    coordinates, since several were captured at DIFFERENT Y-positions
 *    across the given steps (reflecting scroll-state shifts), confirming
 *    coordinates alone would be unreliable here.
 *  - "Shop By Fragrance Strength" is the one exception: its JSON genuinely
 *    has a real content-desc and clickable=true, so it's tapped via
 *    accessibility id directly.
 *  - The back arrow (step 7) was given the same mis-captured generic
 *    top-bar-spacer locator seen repeatedly throughout this project.
 *    Reuses the confirmed real coordinate for the app's own back arrow
 *    (bounds [42,213][160,332]) established in BrandsCTAPage.java - NOT
 *    the Android system back button, per explicit prior instruction.
 */
public class SideMenuWomenPage {

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

    // Step 2 - CONFIRMED exact text, clickable=false.
    private static final By WOMEN_CATEGORY = textAncestorLocator("Women");

    // Step 3 - the 4 sub-categories, CONFIRMED exact text.
    private static final By WOMENS_FRAGRANCE = textAncestorLocator("Women's Fragrance");
    private static final By POPULAR_BRANDS = textAncestorLocator("Popular Brands");
    private static final By DISCOVER_NEW_FRAGRANCE = textAncestorLocator("Discover Your New Fragrance");

    // Step 11 - CONFIRMED real content-desc + clickable=true.
    private static final By SHOP_BY_FRAGRANCE_STRENGTH = AppiumBy.accessibilityId("Shop By Fragrance Strength");

    // Step 5 - CONFIRMED exact text, clickable=false.
    private static final By SHOP_ALL = textAncestorLocator("Shop All");

    // PLP top-of-page landmark - CONFIRMED present right near the header
    // in a real successful read of this exact PLP ("Sort By" / "Filter"
    // appeared immediately after the category tiles, near the very top).
    private static final By PLP_SORT_BY = AppiumBy.xpath("//android.widget.TextView[@text='Sort By']");

    // PLP page title - CONFIRMED in the real read ("Women's Fragrances" right after
    // "The Fragrance Shop"). The header scrolls away with the content, so this being
    // visible means the back arrow row is on screen too, unlike "Sort By" which sits
    // BELOW the header.
    private static final By PLP_HEADER_TITLE =
            AppiumBy.xpath("//android.widget.TextView[@text=\"Women's Fragrances\"]");

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

    public SideMenuWomenPage() {
        this(com.tfs.driver.DriverManager.getDriver());
    }

    public SideMenuWomenPage(AndroidDriver driver) {
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
    // Step 2 - Tap "Women" to expand it
    // ---------------------------------------------------------------
    public void selectWomenCategory() {
        System.out.println("[STEP 02] Select 'Women' category");
        tapTextAncestorWithVerification(WOMEN_CATEGORY, "Women");
    }

    // ---------------------------------------------------------------
    // Step 3 - Count and print the sub-categories
    // ---------------------------------------------------------------
    public void countAndPrintSubCategories() {
        System.out.println("[STEP 03] Count the sub-categories under 'Women'");
        String[] expectedLabels = {"Women's Fragrance", "Popular Brands",
                "Shop By Fragrance Strength", "Discover Your New Fragrance"};
        List<String> found = new ArrayList<>();
        for (String label : expectedLabels) {
            // FIXED: a real run showed "Women's Fragrance" silently missing
            // from the count - its apostrophe was breaking the XPath string
            // literal built with simple single-quote concatenation. Now
            // uses double quotes for any label containing an apostrophe,
            // same escaping approach already used correctly in
            // textAncestorLocator() below.
            String xpath = label.contains("'")
                    ? "//*[@text=\"" + label + "\" or @content-desc=\"" + label + "\"]"
                    : "//*[@text='" + label + "' or @content-desc='" + label + "']";
            if (isDisplayedSafely(AppiumBy.xpath(xpath))) {
                found.add(label);
            }
        }
        System.out.println("[RESULT] Sub-categories found: " + found.size() + " -> " + found);
    }

    // ---------------------------------------------------------------
    // Step 4-5 - Expand "Women's Fragrance", scroll its list, select "Shop All"
    // ---------------------------------------------------------------
    public void expandWomensFragranceAndSelectShopAll() {
        System.out.println("[STEP 04] Select 'Women's Fragrance'");
        tapTextAncestorWithVerification(WOMENS_FRAGRANCE, "Women's Fragrance");

        System.out.println("[STEP 05] Scroll the sub-list, scroll back up, then select 'Shop All'");
        for (int i = 0; i < 4; i++) {
            scrollDown();
        }
        scrollToTop();
        tapTextAncestorWithVerification(SHOP_ALL, "Shop All");
    }

    // ---------------------------------------------------------------
    // Step 6 - Read the Shop All PLP fully
    // ---------------------------------------------------------------
    public void readShopAllPlpFully() {
        System.out.println("[STEP 06] Read the Shop All PLP, scrolling to capture all content");
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
        printCollected("Shop All PLP (full)", seen);

        // FIXED: a real run showed the back-arrow tap right after this
        // method kept failing - the diagnostic dump showed we were still
        // sitting deep in the middle of the product list (DIOR, Rabanne,
        // etc.), not back at the top where the header/back arrow actually
        // renders. A follow-up report confirmed the original scrollToTop()
        // fix still wasn't reaching the real top - its early-break-on-false
        // logic trusts mobile:scrollGesture's boolean result, which this
        // app has repeatedly shown is not fully reliable. Now scrolls
        // until the "Sort By" landmark (confirmed present near this PLP's
        // header in a real successful read) genuinely becomes visible,
        // not until the gesture claims it can't scroll further.
        System.out.println("[ACTION] Scrolling the Shop All PLP back to the top.");
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
    // Step 8 - Collapse "Women's Fragrance"
    // ---------------------------------------------------------------
    public void collapseWomensFragrance() {
        System.out.println("[STEP 08] Collapse 'Women's Fragrance'");
        tapTextAncestorWithVerification(WOMENS_FRAGRANCE, "Women's Fragrance (collapse)");
    }

    // ---------------------------------------------------------------
    // Step 9-10 - Expand "Popular Brands", scroll, collapse
    // ---------------------------------------------------------------
    public void expandScrollAndCollapsePopularBrands() {
        System.out.println("[STEP 09] Select 'Popular Brands'");
        tapTextAncestorWithVerification(POPULAR_BRANDS, "Popular Brands");

        System.out.println("[ACTION] Scrolling the sub-list, then scrolling back up.");
        for (int i = 0; i < 4; i++) {
            scrollDown();
        }
        scrollToTop();

        System.out.println("[STEP 10] Collapse 'Popular Brands'");
        tapTextAncestorWithVerification(POPULAR_BRANDS, "Popular Brands (collapse)");
    }

    // ---------------------------------------------------------------
    // Step 11-12 - Expand "Shop By Fragrance Strength", collapse
    // ---------------------------------------------------------------
    public void expandAndCollapseFragranceStrength() {
        System.out.println("[STEP 11] Select 'Shop By Fragrance Strength'");
        tapConfirmedButtonWithVerification(SHOP_BY_FRAGRANCE_STRENGTH, "Shop By Fragrance Strength");

        System.out.println("[STEP 12] Collapse 'Shop By Fragrance Strength'");
        tapConfirmedButtonWithVerification(SHOP_BY_FRAGRANCE_STRENGTH, "Shop By Fragrance Strength (collapse)");
    }

    // ---------------------------------------------------------------
    // Step 13-14 - Expand "Discover Your New Fragrance", scroll, collapse
    // ---------------------------------------------------------------
    public void expandScrollAndCollapseDiscoverNewFragrance() {
        System.out.println("[STEP 13] Select 'Discover Your New Fragrance'");
        tapTextAncestorWithVerification(DISCOVER_NEW_FRAGRANCE, "Discover Your New Fragrance");

        System.out.println("[ACTION] Scrolling the sub-list, then scrolling back up.");
        for (int i = 0; i < 4; i++) {
            scrollDown();
        }
        scrollToTop();

        System.out.println("[STEP 14] Collapse 'Discover Your New Fragrance'");
        tapTextAncestorWithVerification(DISCOVER_NEW_FRAGRANCE, "Discover Your New Fragrance (collapse)");
    }

    // ---------------------------------------------------------------
    // Step 15 - Close the app
    // ---------------------------------------------------------------
    public void closeApp() {
        System.out.println("[STEP 15] Close the app");
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
     * before - "Women's Fragrance" was scrolled out of view above, so it
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