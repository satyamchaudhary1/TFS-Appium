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
 * Page object for TC029 - Side Menu -> "Christmas" category -> 3 sub-categories
 * (Women's Christmas, Men's Christmas, Discover The Perfect Gift) -> "Women's
 * Christmas" -> "Gift Sets for Her" PLP -> Show More -> Sort by "Product Name :
 * Z-A" -> Filter (Brand: Armani, Price: GBP 50.0-100.0) -> show results -> back
 * arrow -> expand/collapse every sub-category -> collapse "Christmas" -> Home
 * icon -> close app.
 * <p>
 * Built on the helpers of SideMenuGiftingPage.java (tap-and-verify with a
 * bottom-edge exception, scroll-into-view before tapping, the bottom-nav guard,
 * the sticky-header PLP handling, the app's own back arrow by coordinate - no
 * Android system back).
 * <p>
 * FLAGGED ASSUMPTIONS:
 *  - The three sub-categories have a CONFIRMED content-desc + clickable=true in
 *    the JSON, so they use accessibility ids. The main "Christmas" row was
 *    captured as a bare TextView: found by its exact text, tapped through its
 *    clickable ancestor. Its sub-categories ("Women's Christmas", ...) have
 *    different text, so there is no clash.
 *  - "Gift Sets for Her" was typed with a lowercase "for", but the Men's list
 *    in the screenshots reads "Gift Sets For Him"; it is matched
 *    case-insensitively (and ignoring a trailing space).
 *  - Armani and the price band are radio-style: only a dot changes, never
 *    visible text, so they are tapped ONCE with no retry. The results count is
 *    logged before and after as the check that the filter applied.
 *  - The PLP header is sticky (back arrow + title stay on screen), so the back
 *    arrow needs no scrolling; "go to the top" uses the "Sort by" chip.
 *  - The back arrow was captured as the usual mis-captured spacer; the
 *    confirmed real arrow coordinate is reused.
 */
public class SideMenuChristmasPage {

    private final AndroidDriver driver;
    private final Wait<AndroidDriver> wait;

    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);

    private static final int REF_SCREEN_WIDTH = 1220;
    private static final int REF_SCREEN_HEIGHT = 2712;

    // ---------------------------------------------------------------
    // Locators
    // ---------------------------------------------------------------

    private static final int SEARCH_NAV_ICON_X1 = 276;
    private static final int SEARCH_NAV_ICON_Y1 = 2341;
    private static final int SEARCH_NAV_ICON_X2 = 499;
    private static final int SEARCH_NAV_ICON_Y2 = 2577;

    private static final By SEARCH_FIELD =
            AppiumBy.xpath("//android.widget.EditText[@hint='Search']");

    // Step 2 - main "Christmas" category row (bare TextView in the JSON): found by
    // its exact text, tapped through its clickable ancestor.
    private static final By CHRISTMAS_CATEGORY = textAncestorLocator("Christmas");

    // Steps 3-4, 17-21 - sub-categories. CONFIRMED content-desc + clickable=true.
    private static final By WOMENS_CHRISTMAS = AppiumBy.accessibilityId("Women's Christmas");
    private static final By MENS_CHRISTMAS = AppiumBy.accessibilityId("Men's Christmas");
    private static final By DISCOVER_PERFECT_GIFT = AppiumBy.accessibilityId("Discover The Perfect Gift");

    // Step 5 - typed as "Gift Sets for Her"; the Men's list reads "Gift Sets For
    // Him", so match case-insensitively and ignore any trailing space.
    private static final By GIFT_SETS_FOR_HER = AppiumBy.xpath(
            "//android.widget.TextView[contains(translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                    + "'abcdefghijklmnopqrstuvwxyz'),'gift sets for her')]/ancestor::*[@clickable='true'][1]");

    // PLP - "304 Results" / "2 Results" text under the sticky header.
    private static final By PLP_RESULTS_COUNT = AppiumBy.xpath("//android.widget.TextView[contains(@text,'Result')]");

    // Step 7 - CONFIRMED content-desc + clickable=true.
    private static final By SHOW_MORE = AppiumBy.accessibilityId("Show More");

    // Steps 8-9 - "Sort by" CONFIRMED content-desc (lowercase b). "Product Name :
    // Z-A" was captured as a bare TextView (note: no spaces around the hyphen);
    // the same text as an accessibility id is kept as a fallback.
    private static final By SORT_BY = AppiumBy.accessibilityId("Sort by");
    private static final By SORT_Z_A_TEXT = textAncestorLocator("Product Name : Z-A");
    private static final By SORT_Z_A_ID = AppiumBy.accessibilityId("Product Name : Z-A");

    // Steps 10-14 - Filter window.
    private static final By FILTER = AppiumBy.accessibilityId("Filter");
    private static final By FILTER_ARMANI = AppiumBy.accessibilityId("Armani");
    private static final By FILTER_PRICE_HEADER_TEXT = textAncestorLocator("Price");
    private static final By FILTER_PRICE_HEADER_ID = AppiumBy.accessibilityId("Price");
    private static final By FILTER_PRICE_50_100 = textAncestorLocator("\u00a350.0-\u00a3100.0");
    private static final By SHOW_RESULTS_ID = AppiumBy.accessibilityId("show results");
    private static final By SHOW_RESULTS_TEXT = textAncestorLocator("show results");

    // Home page landmark - the "Women's" tile. The menu uses "Women" (no
    // apostrophe), so this only exists on the Home page.
    private static final By HOME_PAGE_WOMENS_TILE = AppiumBy.xpath("//android.widget.TextView[@text=\"Women's\"]");

    // Bottom-navigation Home item, bounds from the Home-page hierarchy.
    private static final int HOME_NAV_X1 = 53;
    private static final int HOME_NAV_Y1 = 2341;
    private static final int HOME_NAV_X2 = 276;
    private static final int HOME_NAV_Y2 = 2577;

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

    private static By textContainsAncestorLocator(String partialText) {
        return AppiumBy.xpath("//android.widget.TextView[contains(@text,'" + partialText
                + "')]/ancestor::*[@clickable='true'][1]");
    }

    public SideMenuChristmasPage() {
        this(com.tfs.driver.DriverManager.getDriver());
    }

    public SideMenuChristmasPage(AndroidDriver driver) {
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
    // Step 2 - Check and print all menu categories, scroll back up, select "Christmas"
    // ---------------------------------------------------------------
    public void printCategoriesAndSelectChristmas() {
        System.out.println("[STEP 02] Check all menu categories, scroll back up, then select 'Christmas'");
        java.util.LinkedHashSet<String> seen = new java.util.LinkedHashSet<>(collectVisibleText());
        for (int i = 0; i < 10; i++) {
            int before = seen.size();
            scrollDown();
            seen.addAll(collectVisibleText());
            if (seen.size() == before) {
                System.out.println("[INFO] No new categories after scroll " + (i + 1) + " - reached the bottom.");
                break;
            }
        }
        printCollected("Menu categories (full list)", seen);

        System.out.println("[ACTION] Scrolling the menu back up to the top.");
        scrollToTop();
        tapToggleHeaderWithVerification(CHRISTMAS_CATEGORY, "Christmas (main category)");
    }

    // ---------------------------------------------------------------
    // Step 3 - Count and print the sub-categories
    // ---------------------------------------------------------------
    public void countAndPrintSubCategories() {
        System.out.println("[STEP 03] Count the sub-categories under 'Christmas'");
        java.util.LinkedHashMap<String, By> expected = new java.util.LinkedHashMap<>();
        expected.put("Women's Christmas", WOMENS_CHRISTMAS);
        expected.put("Men's Christmas", MENS_CHRISTMAS);
        expected.put("Discover The Perfect Gift", DISCOVER_PERFECT_GIFT);

        java.util.LinkedHashSet<String> found = new java.util.LinkedHashSet<>();
        for (int pass = 0; pass < 3 && found.size() < expected.size(); pass++) {
            for (Map.Entry<String, By> e : expected.entrySet()) {
                if (isDisplayedSafely(e.getValue())) {
                    found.add(e.getKey());
                }
            }
            if (found.size() < expected.size()) {
                scrollDown();
            }
        }
        System.out.println("[RESULT] Sub-categories found: " + found.size() + " -> " + found);
    }

    // ---------------------------------------------------------------
    // Step 4-5 - Expand "Women's Christmas", scroll its list, select "Gift Sets for Her"
    // ---------------------------------------------------------------
    public void expandWomensChristmasAndSelectGiftSetsForHer() {
        System.out.println("[STEP 04] Select sub-category \"Women's Christmas\"");
        tapToggleHeaderWithVerification(WOMENS_CHRISTMAS, "Women's Christmas");

        System.out.println("[STEP 05] Scroll the sub-list, scroll back up, then select 'Gift Sets for Her'");
        for (int i = 0; i < 4; i++) {
            scrollDown();
        }
        scrollToTop();
        tapTextAncestorWithVerification(GIFT_SETS_FOR_HER, "Gift Sets for Her");
    }

    private String resultsCountBeforeFilter = "n/a";

    // ---------------------------------------------------------------
    // Step 6 - Read the Gift Sets for Her PLP fully (stays at the bottom, where Show More is)
    // ---------------------------------------------------------------
    public void readGiftSetsForHerPlpFully() {
        System.out.println("[STEP 06] Read the 'Gift Sets For Her' PLP, scrolling to capture all content");
        waitForElement(PLP_RESULTS_COUNT, 8);
        resultsCountBeforeFilter = readResultsCountText();
        System.out.println("[RESULT] PLP shows: '" + resultsCountBeforeFilter + "'");
        readPlpFullyKeepPosition("Gift Sets For Her PLP (full)", 25);
    }

    // ---------------------------------------------------------------
    // Step 7 - Show More
    // ---------------------------------------------------------------
    public void tapShowMore() {
        System.out.println("[STEP 07] Scroll to 'Show More' and tap it");
        for (int i = 0; i < 15 && !isDisplayedSafely(SHOW_MORE); i++) {
            scrollDown();
        }
        if (!isDisplayedSafely(SHOW_MORE)) {
            printAllVisibleTextAndDescs("Screen state - 'Show More' never became visible");
            throw new org.openqa.selenium.TimeoutException("'Show More' was not found at the bottom of the PLP.");
        }

        for (int attempt = 1; attempt <= 3; attempt++) {
            List<String> before = collectVisibleText();
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(SHOW_MORE));
            org.openqa.selenium.Rectangle rect = el.getRect();
            System.out.println("[ACTION] Tapping 'Show More' at its live bounds (attempt " + attempt + "/3).");
            tapAtRealCoordinates(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);

            // Retry ONLY while the button is still showing: if the tap registered, the content changes
            // (or the button moves away). A blind second tap could land on a product card.
            boolean loaded;
            try {
                new FluentWait<>(driver)
                        .withTimeout(Duration.ofSeconds(8))
                        .pollingEvery(Duration.ofMillis(400))
                        .until(d -> !collectVisibleText().equals(before) || !isDisplayedSafely(SHOW_MORE));
                loaded = true;
            } catch (org.openqa.selenium.TimeoutException e) {
                loaded = false;
            }
            if (loaded) {
                System.out.println("[PASS] More products loaded after 'Show More'.");
                return;
            }
            System.out.println("[WARN] 'Show More' still showing and content unchanged (attempt "
                    + attempt + "/3) - retrying.");
        }
        throw new org.openqa.selenium.TimeoutException("'Show More' did not load more products after 3 attempts.");
    }

    // ---------------------------------------------------------------
    // Step 8-9 - Back to the top, Sort by -> "Product Name : Z-A"
    // ---------------------------------------------------------------
    public void scrollToTopAndSortByProductNameZtoA() {
        System.out.println("[STEP 08] Scroll the PLP back to the top and open 'Sort by'");
        scrollToPlpTop();
        tapConfirmedButtonWithVerification(SORT_BY, "Sort by");

        System.out.println("[STEP 09] Select 'Product Name : Z-A'");
        By option = isDisplayedSafely(SORT_Z_A_TEXT) ? SORT_Z_A_TEXT : SORT_Z_A_ID;
        tapConfirmedButtonWithVerification(option, "Product Name : Z-A");
    }

    // ---------------------------------------------------------------
    // Step 10-14 - Filter: Brand Armani + Price GBP 50.0-100.0, show results
    // ---------------------------------------------------------------
    public void applyArmaniAndPriceFilter() {
        System.out.println("[STEP 10] Open the 'Filter' window");
        tapConfirmedButtonWithVerification(FILTER, "Filter");

        System.out.println("[STEP 11] Select 'Armani' under Brand");
        tapOnceWithoutVerification(FILTER_ARMANI, "Armani");

        System.out.println("[STEP 12] Open the 'Price' filter section");
        expandPriceSectionIfNeeded();

        System.out.println("[STEP 13] Select '\u00a350.0-\u00a3100.0'");
        tapOnceWithoutVerification(FILTER_PRICE_50_100, "\u00a350.0-\u00a3100.0");

        System.out.println("[STEP 14] Tap 'show results'");
        By show = isDisplayedSafely(SHOW_RESULTS_ID) ? SHOW_RESULTS_ID : SHOW_RESULTS_TEXT;
        tapConfirmedButtonWithVerification(show, "show results");
    }

    private void expandPriceSectionIfNeeded() {
        if (isDisplayedSafely(FILTER_PRICE_50_100)) {
            System.out.println("[INFO] Price options already visible - not tapping the header.");
            return;
        }
        By header = isDisplayedSafely(FILTER_PRICE_HEADER_ID) ? FILTER_PRICE_HEADER_ID : FILTER_PRICE_HEADER_TEXT;
        tapToggleHeaderWithVerification(header, "Price");
        if (!waitForElement(FILTER_PRICE_50_100, 5)) {
            System.out.println("[WARN] '\u00a350.0-\u00a3100.0' still not visible after expanding Price.");
        }
    }

    // ---------------------------------------------------------------
    // Step 15 - Read the sorted and filtered PLP
    // ---------------------------------------------------------------
    public void readFilteredPlpFully() {
        System.out.println("[STEP 15] Read the sorted and filtered PLP, scrolling to capture all content");
        waitForElement(PLP_RESULTS_COUNT, 8);
        String after = readResultsCountText();
        System.out.println("[RESULT] PLP shows: '" + after + "' (before the filter: '" + resultsCountBeforeFilter + "')");
        if (after.equals(resultsCountBeforeFilter)) {
            System.out.println("[WARN] The results count did not change - the filter may not have applied.");
        } else {
            System.out.println("[PASS] The results count changed - the filter applied.");
        }
        readPlpFullyKeepPosition("Sorted + filtered PLP (full)", 25);
    }

    private void readPlpFullyKeepPosition(String label, int maxScrolls) {
        List<String> seen = new ArrayList<>(new java.util.LinkedHashSet<>(collectVisibleText()));
        for (int i = 0; i < maxScrolls; i++) {
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
        printCollected(label, seen);
    }

    private String readResultsCountText() {
        for (WebElement el : driver.findElements(PLP_RESULTS_COUNT)) {
            try {
                String t = el.getText();
                if (t != null && t.trim().matches("[0-9]+ Results?")) {
                    return t.trim();
                }
            } catch (Exception ignored) {
                // stale element - skip
            }
        }
        return "n/a";
    }

    private void scrollToPlpTop() {
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        int restingY = (int) (size.height * 0.18);
        for (int i = 0; i < 45; i++) {
            List<WebElement> els = driver.findElements(SORT_BY);
            if (!els.isEmpty() && els.get(0).getRect().getY() >= restingY) {
                break;
            }
            scrollUpOnce();
        }
        scrollUpOnce();
        System.out.println("[INFO] PLP scrolled back to the top.");
    }

    // ---------------------------------------------------------------
    // Step 16 - Tap the app's own back arrow (the header is sticky - no scrolling needed)
    // ---------------------------------------------------------------
    public void tapBackArrowToReturn(String contextLabel) {
        for (int attempt = 1; attempt <= 3; attempt++) {
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
    // Step 17 - Collapse "Women's Christmas"
    // ---------------------------------------------------------------
    public void collapseWomensChristmas() {
        System.out.println("[STEP 17] Collapse \"Women's Christmas\"");
        tapToggleHeaderWithVerification(WOMENS_CHRISTMAS, "Women's Christmas (collapse)");
    }

    // ---------------------------------------------------------------
    // Step 18-19 - "Men's Christmas": expand, scroll the list and back up, collapse
    // ---------------------------------------------------------------
    public void expandScrollAndCollapseMensChristmas() {
        System.out.println("[STEP 18] Select \"Men's Christmas\"");
        tapToggleHeaderWithVerification(MENS_CHRISTMAS, "Men's Christmas");
        readExpandedListAndPrint("Men's Christmas list");

        System.out.println("[STEP 19] Collapse \"Men's Christmas\"");
        tapToggleHeaderWithVerification(MENS_CHRISTMAS, "Men's Christmas (collapse)");
    }

    private void readExpandedListAndPrint(String s) {
    }

    // ---------------------------------------------------------------
    // Step 20-21 - "Discover The Perfect Gift": expand, print, collapse
    // ---------------------------------------------------------------
    public void expandReadAndCollapseDiscoverPerfectGift() {
        System.out.println("[STEP 20] Select 'Discover The Perfect Gift'");
        tapToggleHeaderWithVerification(DISCOVER_PERFECT_GIFT, "Discover The Perfect Gift");
        printCollected("Discover The Perfect Gift list", new java.util.LinkedHashSet<>(collectVisibleText()));

        System.out.println("[STEP 21] Collapse 'Discover The Perfect Gift'");
        tapToggleHeaderWithVerification(DISCOVER_PERFECT_GIFT, "Discover The Perfect Gift (collapse)");
    }

    // ---------------------------------------------------------------
    // Step 22 - Tap the main "Christmas" row to collapse the whole category
    // ---------------------------------------------------------------
    public void collapseChristmasMainCategory() {
        System.out.println("[STEP 22] Select the main category 'Christmas' to collapse its list");
        tapToggleHeaderWithVerification(CHRISTMAS_CATEGORY, "Christmas (main category, collapse)");
    }

    // ---------------------------------------------------------------
    // Step 23 - Home icon in the bottom navigation, confirm the Home page
    // ---------------------------------------------------------------
    public void tapHomeNavIconAndVerifyHomePage() {
        System.out.println("[STEP 23] Tap the Home icon in the bottom navigation");
        for (int attempt = 1; attempt <= 3; attempt++) {
            System.out.println("[ACTION] Tapping the Home nav icon (attempt " + attempt + "/3).");
            tapAtScaledCoordinates(HOME_NAV_X1, HOME_NAV_Y1, HOME_NAV_X2, HOME_NAV_Y2,
                    REF_SCREEN_WIDTH, REF_SCREEN_HEIGHT);
            if (waitForHomePage(6)) {
                System.out.println("[PASS] Home page displayed.");
                return;
            }
            System.out.println("[WARN] Home page not detected (attempt " + attempt + "/3) - retrying.");
        }
        printAllVisibleTextAndDescs("Screen state after 3 failed Home nav icon taps");
        throw new org.openqa.selenium.TimeoutException(
                "The Home page was not displayed after tapping the Home nav icon 3 times.");
    }

    private boolean waitForHomePage(int timeoutSeconds) {
        try {
            new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofMillis(400))
                    .until(d -> !isDisplayedSafely(SEARCH_FIELD) && isDisplayedSafely(HOME_PAGE_WOMENS_TILE));
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    /**
     * Taps once at the element's live bounds and does NOT check for a visible
     * change or retry. For radio-style options (a brand, a price band) the only
     * thing that changes is a dot, never visible text, so the normal
     * change-check would call a good tap a failure and retry it - which would
     * deselect the option.
     */
    private void tapOnceWithoutVerification(By locator, String label) {
        ensureVisible(locator, label);
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        org.openqa.selenium.Rectangle rect = el.getRect();
        System.out.println("[ACTION] Tapping '" + label + "' once at its live bounds (selection - "
                + "no visible-text change to verify, no retry).");
        tapAtRealCoordinates(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);
        try {
            Thread.sleep(700);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * If the target sits under the floating bottom navigation bar, scrolls it
     * clear first - a tap there would hit Basket / Account instead. Only used
     * for menu rows (not for the Filter window, which has no nav bar).
     */
    private void avoidBottomNav(By locator) {
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        int limit = (int) (size.height * 0.84);
        for (int i = 0; i < 2; i++) {
            List<WebElement> els = driver.findElements(locator);
            if (els.isEmpty()) {
                return;
            }
            org.openqa.selenium.Rectangle r = els.get(0).getRect();
            if (r.getY() + r.getHeight() / 2 <= limit) {
                return;
            }
            System.out.println("[INFO] Target is low on screen (near the bottom nav bar) - scrolling it clear first.");
            scrollDown();
        }
    }

    // ---------------------------------------------------------------
    // Step 24 - Close the app
    // ---------------------------------------------------------------
    public void closeApp() {
        System.out.println("[STEP 24] Close the app");
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
        avoidBottomNav(locator);
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
        avoidBottomNav(locator);
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