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
 * Page object for TC030 - Side Menu -> "Brands" row -> All Brands list (read,
 * count) -> index letters D and R -> "Rabanne" -> brand page (carousel, top
 * Women's / Men's tabs, bottom Women's / Men's toggle, video, About) ->
 * "Discover Gifting" -> Rabanne PLP (brand check) -> Sort by "Price : low to
 * high" -> Filter (Category: Body Lotion, ItemGroup: Single) -> show results ->
 * three back-arrow taps -> Home icon -> close app.
 * <p>
 * Built on the helpers of SideMenuChristmasPage.java / SideMenuGiftingPage.java
 * (tap-and-verify with a bottom-edge exception, scroll-into-view before
 * tapping, the bottom-nav guard, the sticky-header PLP handling).
 * <p>
 * FLAGGED ASSUMPTIONS:
 *  - The right-hand index letters (0-9, A-Z) are bare TextViews on a custom
 *    strip. The same letter also appears as a SECTION HEADER on the left, so the
 *    one on the right edge (x >= 80% of the width) is picked, and a jump is
 *    verified by that letter's header appearing on the LEFT half of the screen.
 *  - The menu has two "Brands" elements (the horizontal tab at the top and the
 *    bottom row); the LAST match in document order is used.
 *  - The brand page has TWO Women's / Men's control sets: the top pills use
 *    straight apostrophes ("Women's", "Men's") and the bottom toggle uses a
 *    curly one ("Men\u2019s", per the step 11 JSON).
 *  - The step 12 JSON (Armani) is left over from another test and is ignored.
 *  - The video container is a bare TextView; the play state cannot be read, so
 *    the player is tapped about 500px above the "About Rabanne" heading and the
 *    test waits VIDEO_WATCH_SECONDS (the video length is unknown).
 *  - The carousel's vertical position (about 60% of the screen height) is an
 *    estimate from the screenshots.
 *  - Body Lotion and Single are radio-style (only a dot changes), so they are
 *    tapped ONCE with no retry; the filter chips shown afterwards are the check.
 *  - The brand check reads each product card's content-desc. If cards do not
 *    expose one, it logs a warning instead of failing.
 *  - The back arrow sits at a different spot on the PLP, the brand page and the
 *    All Brands list, so each has its own tap point (later retries nudge it).
 */
public class SideMenuBrandsPage {

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

    // Video: seconds to let the brand video play (its length is unknown).
    private static final int VIDEO_WATCH_SECONDS = 45;

    // Step 2 - the "Brands" row at the bottom of the menu. The horizontal tab row
    // at the top ALSO has a "Brands" element, so the LAST match is used.
    private static final By BRANDS_MENU_ROW = AppiumBy.xpath(
            "(//android.widget.TextView[@text='Brands']/ancestor::*[@clickable='true'][1])[last()]");

    // Step 3 - All Brands list landmark (grey heading under the back arrow).
    private static final By ALL_BRANDS_HEADING = AppiumBy.xpath("//android.widget.TextView[@text='All Brands']");

    // Step 8 - brand row (TextView spans the full row width in the JSON).
    private static final By BRAND_RABANNE_TEXT = AppiumBy.xpath("//android.widget.TextView[@text='Rabanne']");

    // Steps 9-11 - brand page. Top pills: straight apostrophe. Bottom toggle: curly.
    private static final By PILL_WOMENS = AppiumBy.accessibilityId("Women's");
    private static final By PILL_MENS = AppiumBy.accessibilityId("Men's");
    private static final By TOGGLE_MENS = AppiumBy.accessibilityId("Men\u2019s");
    private static final By TOGGLE_MENS_TEXT = textAncestorLocator("Men\u2019s");
    private static final By TOGGLE_WOMENS_ID = AppiumBy.accessibilityId("Women\u2019s");
    private static final By TOGGLE_WOMENS_TEXT = textAncestorLocator("Women\u2019s");

    // Step 13 - heading under the video.
    private static final By ABOUT_HEADING = AppiumBy.xpath("//android.widget.TextView[@text='About Rabanne']");

    // Step 14 - CONFIRMED content-desc + clickable=true (earlier Brands test).
    private static final By DISCOVER_GIFTING = AppiumBy.accessibilityId("Discover Gifting");

    // Steps 16-18 - "Sort by" / "Filter" CONFIRMED content-desc. "Price : low to
    // high" was captured as a bare TextView; the id is kept as a fallback.
    private static final By SORT_BY = AppiumBy.accessibilityId("Sort by");
    private static final By FILTER = AppiumBy.accessibilityId("Filter");
    // The Filter window is proven open by its own buttons - never by "some text changed".
    private static final By FILTER_WINDOW_SHOW_RESULTS = AppiumBy.xpath("//android.widget.TextView[@text='show results']");
    private static final By FILTER_WINDOW_CLEAR_ALL = AppiumBy.xpath("//android.widget.TextView[@text='Clear all']");
    private static final By SORT_LOW_TO_HIGH_TEXT = textAncestorLocator("Price : low to high");
    private static final By SORT_LOW_TO_HIGH_ID = AppiumBy.accessibilityId("Price : low to high");

    // Steps 19-23 - Filter window. Single and show results are CONFIRMED
    // content-desc; Category, Body Lotion and ItemGroup were bare TextViews.
    private static final By CATEGORY_HEADER_TEXT = textAncestorLocator("Category");
    private static final By CATEGORY_HEADER_ID = AppiumBy.accessibilityId("Category");
    private static final By BODY_LOTION_TEXT = textAncestorLocator("Body Lotion");
    private static final By BODY_LOTION_ID = AppiumBy.accessibilityId("Body Lotion");
    private static final By ITEMGROUP_HEADER_TEXT = textAncestorLocator("ItemGroup");
    private static final By ITEMGROUP_HEADER_ID = AppiumBy.accessibilityId("ItemGroup");
    private static final By SINGLE_ID = AppiumBy.accessibilityId("Single");
    private static final By SINGLE_TEXT = textAncestorLocator("Single");
    private static final By SHOW_RESULTS_ID = AppiumBy.accessibilityId("show results");
    private static final By SHOW_RESULTS_TEXT = textAncestorLocator("show results");

    // Step 28 - Home page landmark ("Women's" tile; the menu uses "Women") and the
    // bottom-navigation Home item bounds.
    private static final By HOME_PAGE_WOMENS_TILE = AppiumBy.xpath("//android.widget.TextView[@text=\"Women's\"]");
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

    public SideMenuBrandsPage() {
        this(com.tfs.driver.DriverManager.getDriver());
    }

    public SideMenuBrandsPage(AndroidDriver driver) {
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
    // Step 2 - Print all menu categories, scroll down to the bottom, select "Brands"
    // ---------------------------------------------------------------
    public void printCategoriesAndSelectBrands() {
        System.out.println("[STEP 02] Check all menu categories, scroll down, then select 'Brands'");
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

        tapConfirmedButtonWithVerification(BRANDS_MENU_ROW, "Brands (menu row)");
        if (waitForElement(ALL_BRANDS_HEADING, 8)) {
            System.out.println("[PASS] The All Brands list is displayed.");
        } else {
            System.out.println("[WARN] The 'All Brands' heading was not found after tapping Brands.");
        }
    }

    // ---------------------------------------------------------------
    // Step 3 - Read and scroll the whole All Brands list, count the brands
    // ---------------------------------------------------------------
    public void readAllBrandsAndCount() {
        System.out.println("[STEP 03] Read and scroll the whole All Brands list, then count the brands");
        waitForElement(ALL_BRANDS_HEADING, 8);
        java.util.LinkedHashSet<String> brands = new java.util.LinkedHashSet<>();
        int quietScrolls = 0;
        for (int i = 0; i < 130; i++) {
            int before = brands.size();
            for (String t : collectVisibleText()) {
                if (isBrandName(t)) {
                    brands.add(t);
                }
            }
            if (brands.size() == before) {
                quietScrolls++;
                if (quietScrolls >= 3) {
                    System.out.println("[INFO] No new brands for 3 scrolls - reached the end of the list.");
                    break;
                }
            } else {
                quietScrolls = 0;
            }
            scrollDown();
        }
        printCollected("All Brands (full list)", brands);
        System.out.println("[RESULT] Total brands counted: " + brands.size());
    }

    /** A brand row: not the heading, not a section letter ("A", "0-9") and not an index letter. */
    private boolean isBrandName(String t) {
        return t != null && t.length() > 1 && !t.equals("All Brands") && !t.equals("0-9");
    }

    // ---------------------------------------------------------------
    // Steps 4-7 - Tap a letter on the right-hand index, confirm the list jumped to it
    // ---------------------------------------------------------------
    public void selectIndexLetter(String letter, String stepLabel) {
        System.out.println("[" + stepLabel + "] Tap the '" + letter + "' letter on the right-hand index");
        for (int attempt = 1; attempt <= 3; attempt++) {
            WebElement el = findIndexLetter(letter);
            if (el == null) {
                System.out.println("[WARN] The '" + letter + "' index letter was not found (attempt "
                        + attempt + "/3).");
                continue;
            }
            org.openqa.selenium.Rectangle r = el.getRect();
            System.out.println("[ACTION] Tapping the '" + letter + "' index letter (attempt " + attempt + "/3).");
            tapAtRealCoordinates(r.getX() + r.getWidth() / 2, r.getY() + r.getHeight() / 2);

            if (waitForSectionHeader(letter, 5)) {
                System.out.println("[PASS] The list jumped to the '" + letter + "' section.");
                printCollected("Brands list after tapping '" + letter + "'",
                        new java.util.LinkedHashSet<>(collectVisibleText()));
                return;
            }
            System.out.println("[WARN] The '" + letter + "' section header did not appear (attempt "
                    + attempt + "/3) - retrying.");
        }
        printAllVisibleTextAndDescs("Screen state after 3 failed '" + letter + "' index taps");
        throw new org.openqa.selenium.TimeoutException(
                "The list did not jump to the '" + letter + "' section after 3 attempts.");
    }

    /** The index letter lives on the right edge; the same letter as a section header sits on the left. */
    private WebElement findIndexLetter(String letter) {
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        for (WebElement el : driver.findElements(
                AppiumBy.xpath("//android.widget.TextView[@text='" + letter + "']"))) {
            try {
                if (el.getRect().getX() >= size.width * 0.8) {
                    return el;
                }
            } catch (Exception ignored) {
                // stale element - skip
            }
        }
        return null;
    }

    private boolean waitForSectionHeader(String letter, int timeoutSeconds) {
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        try {
            new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofMillis(400))
                    .until(d -> {
                        for (WebElement el : d.findElements(
                                AppiumBy.xpath("//android.widget.TextView[@text='" + letter + "']"))) {
                            try {
                                if (el.getRect().getX() < size.width * 0.5) {
                                    return true;
                                }
                            } catch (Exception ignored) {
                                // stale element - skip
                            }
                        }
                        return false;
                    });
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    // ---------------------------------------------------------------
    // Step 8 - Select the "Rabanne" brand
    // ---------------------------------------------------------------
    public void selectRabanneBrand() {
        System.out.println("[STEP 08] Select the 'Rabanne' brand");
        tapConfirmedButtonWithVerification(BRAND_RABANNE_TEXT, "Rabanne");
    }

    // ---------------------------------------------------------------
    // Step 9 - Read the Rabanne page and its carousel, select the top "Women's" tab
    // ---------------------------------------------------------------
    public void readBrandPageAndSelectWomensTab() {
        System.out.println("[STEP 09] Read the Rabanne brand page and its carousel, then select the Women's tab");
        waitForElement(PILL_WOMENS, 8);
        java.util.LinkedHashSet<String> seen = new java.util.LinkedHashSet<>(collectVisibleText());
        printCollected("Rabanne brand page (top of the page)", seen);

        int carouselY = (int) (driver.manage().window().getSize().height * 0.60);
        java.util.LinkedHashSet<String> carousel = new java.util.LinkedHashSet<>(collectVisibleText());
        for (int i = 0; i < 3; i++) {
            dragHorizontalAt("left", carouselY);
            carousel.addAll(collectVisibleText());
        }
        for (int i = 0; i < 3; i++) {
            dragHorizontalAt("right", carouselY);
            carousel.addAll(collectVisibleText());
        }
        printCollected("Bestsellers carousel (everything seen while swiping)", carousel);

        tapConfirmedButtonWithVerification(PILL_WOMENS, "Women's (top tab)");
    }

    // ---------------------------------------------------------------
    // Step 10 - Select the top "Men's" tab
    // ---------------------------------------------------------------
    public void selectMensTab() {
        System.out.println("[STEP 10] Select the Men's tab");
        tapConfirmedButtonWithVerification(PILL_MENS, "Men's (top tab)");
        printCollected("Rabanne brand page after the Men's tab", new java.util.LinkedHashSet<>(collectVisibleText()));
    }

    // ---------------------------------------------------------------
    // Steps 11-12 - Check the Women's list, switch the bottom toggle to Men's, check that list
    // ---------------------------------------------------------------
    public void checkWomensListThenSwitchToMensToggle() {
        System.out.println("[STEP 11] Scroll so the Women's / Men's section is fully on screen, check the "
                + "Women's list, then switch to Men's");

        // FIXED: a real run left the page unscrolled, so this toggle sat at y~2506 on the very bottom
        // edge of the screen and the tap there did nothing. Scroll gently until the toggle is in the
        // upper half, which also brings the whole product section into view.
        scrollIntoUpperPart(firstDisplayed(TOGGLE_MENS, TOGGLE_MENS_TEXT), 0.55, 8);

        // The top Men's tab (step 10) may already have switched this toggle to Men's. Force Women's first
        // so the list checked here really is the Women's one. No visible change just means it was already
        // on Women's, so this tap is allowed to produce none.
        tapSoft(firstDisplayed(TOGGLE_WOMENS_ID, TOGGLE_WOMENS_TEXT), "Women\u2019s (bottom toggle)", 1);
        printCollected("Women's list (bottom toggle)", new java.util.LinkedHashSet<>(collectVisibleText()));

        tapSoft(firstDisplayed(TOGGLE_MENS, TOGGLE_MENS_TEXT), "Men\u2019s (bottom toggle)", 2);

        System.out.println("[STEP 12] Check the Men's list");
        printCollected("Men's list (bottom toggle)", new java.util.LinkedHashSet<>(collectVisibleText()));
    }

    /**
     * Scrolls the page in small steps until the element's centre is at or above the given fraction of the
     * screen height. Presence alone is not enough: an element on the very bottom edge is "displayed" but
     * can be half hidden, and taps there do not register.
     */
    private void scrollIntoUpperPart(By locator, double maxCenterRatio, int maxScrolls) {
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        int limit = (int) (size.height * maxCenterRatio);
        for (int i = 0; i < maxScrolls; i++) {
            java.util.List<WebElement> els = driver.findElements(locator);
            if (!els.isEmpty()) {
                org.openqa.selenium.Rectangle r = els.get(0).getRect();
                int centerY = r.getY() + r.getHeight() / 2;
                if (centerY <= limit) {
                    System.out.println("[INFO] Target is now at y=" + centerY + " - section is on screen.");
                    return;
                }
            }
            scrollDownGently();
        }
        System.out.println("[WARN] Target did not reach the upper part of the screen after " + maxScrolls
                + " gentle scrolls (the page may have reached its end).");
    }

    /** A shorter scroll (about 35% of the area) for placing an element precisely. */
    private void scrollDownGently() {
        Map<String, Object> params = new HashMap<>();
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        params.put("left", (int) (size.width * 0.1));
        params.put("top", (int) (size.height * 0.2));
        params.put("width", (int) (size.width * 0.8));
        params.put("height", (int) (size.height * 0.6));
        params.put("direction", "down");
        params.put("percent", 0.35);
        Object result = driver.executeScript("mobile: scrollGesture", params);
        System.out.println("[SCROLL] gentle scroll result: " + result);
    }

    /**
     * Taps at the element's live bounds and waits briefly for a visible change, but does NOT fail when
     * there is none. For a tab/toggle that may already be selected, "no change" is a valid outcome.
     * Retries up to the given number of attempts, then continues.
     */
    private boolean tapSoft(By locator, String label, int attempts) {
        for (int a = 1; a <= attempts; a++) {
            List<String> before = collectVisibleText();
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
            org.openqa.selenium.Rectangle r = el.getRect();
            System.out.println("[ACTION] Tapping '" + label + "' at its live bounds (attempt " + a + "/" + attempts + ").");
            tapAtRealCoordinates(r.getX() + r.getWidth() / 2, r.getY() + r.getHeight() / 2);
            if (waitForContentChangeFromSnapshot(before, 4)) {
                System.out.println("[PASS] Screen changed after tapping '" + label + "'.");
                return true;
            }
            System.out.println("[WARN] No visible change after tapping '" + label + "' (attempt " + a + "/" + attempts + ").");
        }
        System.out.println("[INFO] '" + label + "' produced no visible change - it may already be selected; continuing.");
        return false;
    }

    // ---------------------------------------------------------------
    // Step 13 - Scroll to the brand video, play it, read the "About Rabanne" text
    // ---------------------------------------------------------------
    public void playBrandVideoAndReadAbout() {
        System.out.println("[STEP 13] Scroll to the brand video, play it, then read 'About Rabanne'");
        for (int i = 0; i < 15 && !isDisplayedSafely(ABOUT_HEADING); i++) {
            scrollDown();
        }
        if (!isDisplayedSafely(ABOUT_HEADING)) {
            printAllVisibleTextAndDescs("Screen state - 'About Rabanne' never became visible");
            throw new org.openqa.selenium.TimeoutException("The 'About Rabanne' heading was not found.");
        }

        org.openqa.selenium.Rectangle heading = driver.findElements(ABOUT_HEADING).get(0).getRect();
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        int playX = size.width / 2;
        int playY = Math.max(heading.getY() - (int) (size.height * 0.185), (int) (size.height * 0.12));
        System.out.println("[ACTION] Tapping the video play button at (" + playX + "," + playY
                + ") - about 500 reference px above the 'About Rabanne' heading.");
        tapAtRealCoordinates(playX, playY);

        System.out.println("[ACTION] Letting the video play for " + VIDEO_WATCH_SECONDS + " seconds.");
        for (int s = 0; s < VIDEO_WATCH_SECONDS; s += 5) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        scrollDown();
        printCollected("About Rabanne (visible text)", new java.util.LinkedHashSet<>(collectVisibleText()));
    }

    // ---------------------------------------------------------------
    // Step 14 - Scroll up and tap "Discover Gifting"
    // ---------------------------------------------------------------
    public void tapDiscoverGifting() {
        System.out.println("[STEP 14] Scroll up and select 'Discover Gifting'");
        tapConfirmedButtonWithVerification(DISCOVER_GIFTING, "Discover Gifting");
    }

    // ---------------------------------------------------------------
    // Step 15 - Read the Rabanne PLP and verify only Rabanne products are listed
    // ---------------------------------------------------------------
    public void readRabannePlpAndVerifyBrand() {
        System.out.println("[STEP 15] Read the Rabanne PLP and verify that only Rabanne products are listed");
        waitForElement(SORT_BY, 8);

        java.util.LinkedHashSet<String> texts = new java.util.LinkedHashSet<>();
        java.util.LinkedHashSet<String> cards = new java.util.LinkedHashSet<>();
        int quiet = 0;
        for (int i = 0; i < 40; i++) {
            int before = texts.size() + cards.size();
            texts.addAll(collectVisibleText());
            cards.addAll(collectProductCardDescriptions());
            if (texts.size() + cards.size() == before) {
                quiet++;
                if (quiet >= 2) {
                    System.out.println("[INFO] No new content after scroll " + (i + 1) + " - reached the bottom.");
                    break;
                }
            } else {
                quiet = 0;
            }
            scrollDown();
        }
        printCollected("Rabanne PLP (full)", texts);

        if (cards.isEmpty()) {
            System.out.println("[WARN] No product card descriptions were exposed, so the 'only Rabanne "
                    + "products' check could not be done automatically - review the printed PLP above.");
            return;
        }
        java.util.List<String> offenders = new ArrayList<>();
        for (String card : cards) {
            if (!card.toLowerCase().contains("rabanne")) {
                offenders.add(card);
            }
        }
        System.out.println("[RESULT] Product cards found: " + cards.size()
                + "; cards that do not mention Rabanne: " + offenders.size());
        if (!offenders.isEmpty()) {
            printCollected("Cards that do NOT mention Rabanne", offenders);
            throw new AssertionError("The Rabanne PLP lists " + offenders.size()
                    + " product(s) that do not mention Rabanne.");
        }
        System.out.println("[PASS] Every product card on the PLP is a Rabanne product.");
    }

    /** Product cards expose their text as one content-desc containing the price. */
    private java.util.List<String> collectProductCardDescriptions() {
        java.util.List<String> out = new ArrayList<>();
        for (WebElement el : driver.findElements(AppiumBy.xpath("//*[contains(@content-desc,'\u00a3')]"))) {
            try {
                String d = el.getAttribute("content-desc");
                if (d != null && d.contains("\u00a3") && d.contains(",")) {
                    out.add(d.trim());
                }
            } catch (Exception ignored) {
                // stale element - skip
            }
        }
        return out;
    }

    // ---------------------------------------------------------------
    // Steps 16-17 - Back to the top, Sort by -> "Price : low to high"
    // ---------------------------------------------------------------
    public void scrollToTopAndSortByPriceLowToHigh() {
        System.out.println("[STEP 16] Scroll the PLP back to the top and open 'Sort by'");
        scrollPlpToTop();
        tapConfirmedButtonWithVerification(SORT_BY, "Sort by");

        System.out.println("[STEP 17] Select 'Price : low to high'");
        By option = firstDisplayed(SORT_LOW_TO_HIGH_TEXT, SORT_LOW_TO_HIGH_ID);
        tapConfirmedButtonWithVerification(option, "Price : low to high");
        settleAfterSort();
    }

    /**
     * FIXED: a real run tapped Filter while the PLP was still reloading after the sort. The reload itself
     * changed the visible text, so the tap was wrongly judged a success although the Filter window never
     * opened. Wait for the "Price : low to high" chip and for the list to stop changing first.
     */
    private void settleAfterSort() {
        By chip = AppiumBy.xpath("//android.widget.TextView[@text='Price : low to high']");
        if (waitForElement(chip, 8)) {
            System.out.println("[PASS] The 'Price : low to high' chip is shown.");
        } else {
            System.out.println("[WARN] The 'Price : low to high' chip was not found after sorting.");
        }
        List<String> last = collectVisibleText();
        int stable = 0;
        long deadline = System.currentTimeMillis() + 8000;
        while (System.currentTimeMillis() < deadline && stable < 3) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
            List<String> current = collectVisibleText();
            if (current.equals(last)) {
                stable++;
            } else {
                stable = 0;
                last = current;
            }
        }
        System.out.println("[INFO] The PLP has settled after sorting.");
    }

    /**
     * Scrolls the PLP back to its very top. The header is sticky, so it cannot be
     * the landmark; the "Sort by" chip is (it rests just under the header, about
     * 14% down the screen, on this PLP which has no results-count row).
     */
    private void scrollPlpToTop() {
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        int restingY = (int) (size.height * 0.13);
        for (int i = 0; i < 60; i++) {
            java.util.List<WebElement> els = driver.findElements(SORT_BY);
            if (!els.isEmpty() && els.get(0).getRect().getY() >= restingY) {
                break;
            }
            scrollUpOnce();
        }
        scrollUpOnce();
        System.out.println("[INFO] PLP scrolled back to the top.");
    }

    // ---------------------------------------------------------------
    // Steps 18-23 - Filter: Category Body Lotion + ItemGroup Single, show results
    // ---------------------------------------------------------------
    public void applyBodyLotionAndSingleFilter() {
        System.out.println("[STEP 18] Open the 'Filter' window");
        openFilterWindow();

        System.out.println("[STEP 19] Open the 'Category' drop-down");
        if (!isDisplayedSafely(BODY_LOTION_ID) && !isDisplayedSafely(BODY_LOTION_TEXT)) {
            tapToggleHeaderWithVerification(firstDisplayed(CATEGORY_HEADER_ID, CATEGORY_HEADER_TEXT), "Category");
        } else {
            System.out.println("[INFO] Category options already visible - not tapping the header.");
        }

        System.out.println("[STEP 20] Select 'Body Lotion'");
        tapOnceWithoutVerification(firstDisplayed(BODY_LOTION_ID, BODY_LOTION_TEXT), "Body Lotion");

        System.out.println("[STEP 21] Open the 'ItemGroup' drop-down");
        if (!isDisplayedSafely(SINGLE_ID) && !isDisplayedSafely(SINGLE_TEXT)) {
            tapToggleHeaderWithVerification(firstDisplayed(ITEMGROUP_HEADER_ID, ITEMGROUP_HEADER_TEXT), "ItemGroup");
        } else {
            System.out.println("[INFO] ItemGroup options already visible - not tapping the header.");
        }

        System.out.println("[STEP 22] Select 'Single'");
        tapOnceWithoutVerification(firstDisplayed(SINGLE_ID, SINGLE_TEXT), "Single");

        System.out.println("[STEP 23] Tap 'show results'");
        tapConfirmedButtonWithVerification(firstDisplayed(SHOW_RESULTS_ID, SHOW_RESULTS_TEXT), "show results");
    }

    /**
     * Taps "Filter" and only counts it as opened when the Filter window's own buttons are on screen
     * ("show results" / "Clear all"). Does NOT scroll the PLP: the Filter chip sits in the header area
     * right under the sticky bar, and scrolling after the sort changes nothing useful.
     */
    private void openFilterWindow() {
        for (int attempt = 1; attempt <= 3; attempt++) {
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(FILTER));
            org.openqa.selenium.Rectangle r = el.getRect();
            System.out.println("[ACTION] Tapping 'Filter' at its live bounds (attempt " + attempt + "/3).");
            tapAtRealCoordinates(r.getX() + r.getWidth() / 2, r.getY() + r.getHeight() / 2);

            if (waitForFilterWindow(6)) {
                System.out.println("[PASS] The Filter window is open.");
                return;
            }
            System.out.println("[WARN] The Filter window did not open (attempt " + attempt + "/3) - retrying.");
        }
        printAllVisibleTextAndDescs("Screen state after 3 failed attempts to open the Filter window");
        throw new org.openqa.selenium.TimeoutException("The Filter window did not open after 3 attempts.");
    }

    private boolean waitForFilterWindow(int timeoutSeconds) {
        try {
            new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofMillis(400))
                    .until(d -> isDisplayedSafely(FILTER_WINDOW_SHOW_RESULTS) || isDisplayedSafely(FILTER_WINDOW_CLEAR_ALL));
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    private By firstDisplayed(By preferred, By fallback) {
        return isDisplayedSafely(preferred) ? preferred : fallback;
    }

    // ---------------------------------------------------------------
    // Step 24 - Read the sorted and filtered PLP (the filter chips confirm the selections)
    // ---------------------------------------------------------------
    public void readFilteredPlpFully() {
        System.out.println("[STEP 24] Read the sorted and filtered PLP");
        waitForElement(SORT_BY, 8);
        String[] chips = {"Price : low to high", "Body Lotion", "Single"};
        for (String chip : chips) {
            boolean shown = isDisplayedSafely(AppiumBy.xpath("//android.widget.TextView[@text='" + chip + "']"));
            System.out.println((shown ? "[PASS] Filter chip shown: " : "[WARN] Filter chip NOT shown: ") + chip);
        }
        readPlpFullyKeepPosition("Sorted + filtered PLP (full)", 30);
    }

    // ---------------------------------------------------------------
    // Steps 25-27 - Back arrow taps (each screen has its arrow in a slightly different spot)
    // ---------------------------------------------------------------
    public void backFromFilteredPlp() {
        System.out.println("[STEP 25] Back arrow - filtered PLP to the Rabanne brand page");
        tapBackArrowAt(125, 258, "filtered PLP -> Rabanne brand page");
    }

    public void backFromBrandPage() {
        System.out.println("[STEP 26] Back arrow - Rabanne brand page to the All Brands list");
        tapBackArrowAt(124, 232, "Rabanne brand page -> All Brands list");
    }

    public void backFromBrandsList() {
        System.out.println("[STEP 27] Back arrow - All Brands list to the menu");
        tapBackArrowAt(80, 238, "All Brands list -> menu");
    }

    private void tapBackArrowAt(int refX, int refY, String contextLabel) {
        // FIXED: a single hard-coded point missed the arrow on one of the three screens (it sits at a
        // slightly different spot on each). Attempt 1 now looks for the arrow itself in the header's
        // top-left corner; the later attempts use the reference point with wider nudges.
        int[][] nudges = {{0, 0}, {20, 15}, {-15, 20}, {35, -5}, {0, 35}};
        for (int attempt = 1; attempt <= nudges.length; attempt++) {
            List<String> before = collectVisibleText();

            int[] found = (attempt == 1) ? findHeaderBackArrowCenter(refX, refY) : null;
            if (found != null) {
                System.out.println("[ACTION] Tapping the back arrow found in the header at (" + found[0] + ","
                        + found[1] + ") (" + contextLabel + ", attempt " + attempt + "/" + nudges.length + ").");
                tapAtRealCoordinates(found[0], found[1]);
            } else {
                int dx = nudges[attempt - 1][0];
                int dy = nudges[attempt - 1][1];
                System.out.println("[ACTION] Tapping the back arrow by coordinate (" + contextLabel
                        + ", attempt " + attempt + "/" + nudges.length + ").");
                tapAtScaledCoordinates(refX + dx - 8, refY + dy - 8, refX + dx + 8, refY + dy + 8,
                        REF_SCREEN_WIDTH, REF_SCREEN_HEIGHT);
            }

            if (waitForContentChangeFromSnapshot(before, 6)) {
                System.out.println("[PASS] Screen changed after the back arrow tap (" + contextLabel + ").");
                return;
            }
            System.out.println("[WARN] Screen unchanged after tapping the back arrow (" + contextLabel
                    + ", attempt " + attempt + "/" + nudges.length + ") - retrying.");
        }
        printAllVisibleTextAndDescs("Screen state after failed back-arrow tap attempts (" + contextLabel + ")");
        throw new org.openqa.selenium.TimeoutException(
                "The app's back arrow did not change the screen (" + contextLabel + ") after "
                        + nudges.length + " attempts.");
    }

    /**
     * Looks for the back arrow in the header's top-left corner: a small clickable element whose centre is in
     * the left quarter and top 14% of the screen. Returns its centre (nearest to the expected reference
     * point if several qualify), or null when none is exposed. Only the first 40 clickable elements are
     * inspected (the header comes early in the tree) so this stays quick on long product lists.
     */
    private int[] findHeaderBackArrowCenter(int refX, int refY) {
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        int expectedX = (int) Math.round((double) refX * size.width / REF_SCREEN_WIDTH);
        int expectedY = (int) Math.round((double) refY * size.height / REF_SCREEN_HEIGHT);
        int bestDistance = Integer.MAX_VALUE;
        int[] best = null;
        int inspected = 0;
        for (WebElement el : driver.findElements(AppiumBy.xpath("//*[@clickable='true']"))) {
            if (inspected++ >= 40) {
                break;
            }
            try {
                org.openqa.selenium.Rectangle r = el.getRect();
                int cx = r.getX() + r.getWidth() / 2;
                int cy = r.getY() + r.getHeight() / 2;
                if (cx > size.width * 0.25 || cy > size.height * 0.14 || cy < size.height * 0.02) {
                    continue;
                }
                if (r.getWidth() > size.width * 0.3 || r.getHeight() > size.height * 0.1) {
                    continue;
                }
                int distance = Math.abs(cx - expectedX) + Math.abs(cy - expectedY);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = new int[]{cx, cy};
                }
            } catch (Exception ignored) {
                // stale element - skip
            }
        }
        return best;
    }

    // ---------------------------------------------------------------
    // Step 28 - Home icon in the bottom navigation, confirm the Home page
    // ---------------------------------------------------------------
    public void tapHomeNavIconAndVerifyHomePage() {
        System.out.println("[STEP 28] Tap the Home icon in the bottom navigation");
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

    /**
     * Taps once at the element's live bounds and does NOT check for a visible
     * change or retry. For radio-style options (Body Lotion, Single) only a dot
     * changes, never visible text, so the normal change-check would call a good
     * tap a failure and retry it - which would deselect the option.
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
     * clear first - a tap there would hit Basket / Account instead.
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

    /** Raw horizontal swipe at a given Y (the carousel is not a reliable scrollGesture target). */
    private void dragHorizontalAt(String direction, int y) {
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        int leftX = (int) (size.width * 0.15);
        int rightX = (int) (size.width * 0.85);
        int startX = "left".equals(direction) ? rightX : leftX;
        int endX = "left".equals(direction) ? leftX : rightX;

        org.openqa.selenium.interactions.PointerInput finger =
                new org.openqa.selenium.interactions.PointerInput(
                        org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
        org.openqa.selenium.interactions.Sequence drag =
                new org.openqa.selenium.interactions.Sequence(finger, 0);
        drag.addAction(finger.createPointerMove(Duration.ZERO,
                org.openqa.selenium.interactions.PointerInput.Origin.viewport(), startX, y));
        drag.addAction(finger.createPointerDown(
                org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
        drag.addAction(finger.createPointerMove(Duration.ofMillis(300),
                org.openqa.selenium.interactions.PointerInput.Origin.viewport(), endX, y));
        drag.addAction(finger.createPointerUp(
                org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(java.util.Collections.singletonList(drag));
    }

    // ---------------------------------------------------------------
    // Step 29 - Close the app
    // ---------------------------------------------------------------
    public void closeApp() {
        System.out.println("[STEP 29] Close the app");
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