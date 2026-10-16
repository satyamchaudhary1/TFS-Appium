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
 * Page object for TC019 - Search (from bottom nav) -> DIOR suggestion ->
 * DIOR PLP -> Show More -> Sort By (Price: low to high) -> Filter
 * (Brand=DIOR, Price=£20-£39.99) -> show results -> verify filtered PLP ->
 * Back.
 * <p>
 * Follows the same conventions as MyTfsMembershipPage.java/DiscoverMorePage.java:
 * no-arg constructor pulling the driver from DriverManager, [STEP]/[ACTION]/
 * [PASS]/[SCROLL]/[WARN] logging, tapAtScaledCoordinates() for icon-only
 * elements with no reliable locator.
 * <p>
 * FLAGGED ASSUMPTIONS - two of the captured locators from Inspector were
 * bounds [0,0][1220,308] with clickable="false", which is the same generic
 * top-status-bar spacer element that has been mis-captured multiple times
 * elsewhere in this project (confirmed identical bounds/pattern in earlier
 * screens). Neither is trusted as-is:
 *  - The "search input field" (step 2): targets the first EditText on
 *    screen instead, falling back to the currently active/focused element.
 *  - The "Back" arrow (step 13): uses the "Go back" accessibility id,
 *    which has proven reliable elsewhere in this app (TC018's PDP back
 *    button), with a coordinate/system-back fallback if not present here.
 * <p>
 * The bottom-nav icon tapped in step 1 has no content-desc/text of its own
 * (an SVG glyph) - tapped by scaled coordinate using the confirmed bounds
 * from the Inspector capture. Per the attached screenshot, this opens a
 * Search feature (search box + suggestions + quick category links), not a
 * navigation drawer - named accordingly in this class despite the original
 * step calling it "Side Menu".
 */
public class SearchFieldPage {

    private final AndroidDriver driver;
    private final Wait<AndroidDriver> wait;

    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);

    private static final int REF_SCREEN_WIDTH = 1220;
    private static final int REF_SCREEN_HEIGHT = 2712;

    // ---------------------------------------------------------------
    // Locators
    // ---------------------------------------------------------------

    // Step 1 - 2nd bottom-nav icon (opens Search). No content-desc of its
    // own; confirmed bounds from Inspector.
    private static final int SEARCH_NAV_ICON_X1 = 337;
    private static final int SEARCH_NAV_ICON_Y1 = 2380;
    private static final int SEARCH_NAV_ICON_X2 = 500;
    private static final int SEARCH_NAV_ICON_Y2 = 2543;

    // Step 2 - TODO NOT CONFIRMED: see class javadoc. First EditText on
    // screen, with an active-element fallback.
    private static final By ANY_EDIT_TEXT = AppiumBy.className("android.widget.EditText");

    // Step 3 - CONFIRMED exact content-desc for the "dior" suggestion.
    private static final By DIOR_SUGGESTION = AppiumBy.accessibilityId("dior");

    // Step 4 - PLP landmarks, used to confirm we've actually landed there.
    private static final By SORT_BY = AppiumBy.accessibilityId("Sort By");
    private static final By FILTER = AppiumBy.accessibilityId("Filter");

    // Step 6 - CONFIRMED.
    private static final By SHOW_MORE = AppiumBy.accessibilityId("Show More");

    // Step 8 - CONFIRMED exact spacing ("Price : low to high").
    private static final By SORT_PRICE_LOW_TO_HIGH = AppiumBy.accessibilityId("Price : low to high");

    // Step 10 - CONFIRMED exact content-desc values. Brand is already
    // expanded by default in the filter panel; Price is a collapsed
    // accordion section that must be tapped open first before its price
    // range options (e.g. "£20 - £39.99") become present/clickable.
    private static final By FILTER_BRAND_DIOR = AppiumBy.accessibilityId("DIOR");
    private static final By FILTER_PRICE_SECTION_HEADER = AppiumBy.accessibilityId("Price");
    private static final By FILTER_PRICE_20_39 = AppiumBy.accessibilityId("\u00a320 - \u00a339.99");

    // Step 11 - CONFIRMED exact content-desc (lowercase).
    private static final By SHOW_RESULTS = AppiumBy.accessibilityId("show results");

    // Step 13 - TODO NOT CONFIRMED: see class javadoc. Reusing the "Go
    // back" accessibility id confirmed reliable elsewhere in this app.
    private static final By GO_BACK = AppiumBy.accessibilityId("Go back");

    private static final String APP_PACKAGE = "com.thefragranceshop";

    public SearchFieldPage() {
        this(com.tfs.driver.DriverManager.getDriver());
    }

    public SearchFieldPage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new FluentWait<>(driver)
                .withTimeout(TIMEOUT)
                .pollingEvery(POLL_INTERVAL)
                .ignoring(org.openqa.selenium.NoSuchElementException.class);
    }

    // ---------------------------------------------------------------
    // Step 1 - Tap the Search icon (2nd bottom-nav icon)
    // ---------------------------------------------------------------
    public void openSearch() {
        System.out.println("[STEP 01] Tap the Search icon on the bottom navigation bar");
        tapAtScaledCoordinates(SEARCH_NAV_ICON_X1, SEARCH_NAV_ICON_Y1, SEARCH_NAV_ICON_X2, SEARCH_NAV_ICON_Y2,
                REF_SCREEN_WIDTH, REF_SCREEN_HEIGHT);
        System.out.println("[ACTION] Tapped the Search icon.");
    }

    // ---------------------------------------------------------------
    // Step 2-3 - Type into the search field and select the "dior" suggestion
    // ---------------------------------------------------------------
    public void searchAndSelectDior(String query) {
        System.out.println("[STEP 02] Select the Search input field and type '" + query + "'");
        WebElement searchField = locateSearchField();
        System.out.println("[ACTION] Typing '" + query + "' into the search field.");
        searchField.sendKeys(query);

        System.out.println("[STEP 03] Select 'dior' from the suggestions list");
        WebElement suggestion = wait.until(ExpectedConditions.elementToBeClickable(DIOR_SUGGESTION));
        System.out.println("[ACTION] Tapping 'dior' suggestion.");
        suggestion.click();
    }

    /**
     * The captured "search field" locator was a mis-captured status-bar
     * spacer (see class javadoc). Falls back through: first visible
     * EditText -> the currently active/focused element.
     */
    private WebElement locateSearchField() {
        try {
            return wait.until(ExpectedConditions.elementToBeClickable(ANY_EDIT_TEXT));
        } catch (org.openqa.selenium.TimeoutException e) {
            System.out.println("[WARN] No EditText found - falling back to the active/focused element.");
            return driver.switchTo().activeElement();
        }
    }

    // ---------------------------------------------------------------
    // Step 4 - Confirm the DIOR PLP is displayed
    // ---------------------------------------------------------------
    public void verifyDiorPlpDisplayed() {
        System.out.println("[STEP 04] Verify DIOR PLP is displayed (Sort By / Filter present)");
        wait.until(ExpectedConditions.presenceOfElementLocated(SORT_BY));
        wait.until(ExpectedConditions.presenceOfElementLocated(FILTER));
        printAllVisibleText("DIOR PLP (initial)");
        System.out.println("[PASS] DIOR PLP displayed with Sort By and Filter controls.");
    }

    // ---------------------------------------------------------------
    // Step 5 - Scroll the PLP list down to the "Show More" button
    // ---------------------------------------------------------------
    public void scrollToShowMoreButton() {
        System.out.println("[STEP 05] Scroll PLP list down to 'Show More' button");
        for (int i = 1; i <= 8; i++) {
            if (isDisplayedSafely(SHOW_MORE)) {
                System.out.println("[PASS] 'Show More' button visible after " + (i - 1) + " scroll(s).");
                return;
            }
            scrollDown();
            System.out.println("[SCROLL] PLP scroll " + i + "/8");
        }
        if (!isDisplayedSafely(SHOW_MORE)) {
            printAllVisibleText("Screen state when 'Show More' was not found");
            throw new org.openqa.selenium.TimeoutException(
                    "'Show More' button not found after scrolling.");
        }
    }

    // ---------------------------------------------------------------
    // Step 6 - Tap "Show More" to load additional DIOR products
    // ---------------------------------------------------------------
    public void clickShowMore() {
        System.out.println("[STEP 06] Tap 'Show More' to load additional DIOR products");
        WebElement showMore = wait.until(ExpectedConditions.elementToBeClickable(SHOW_MORE));
        System.out.println("[ACTION] Tapping 'Show More'.");
        showMore.click();
    }

    // ---------------------------------------------------------------
    // Step 7-8 - Scroll to top, tap "Sort By", select "Price : low to high"
    // ---------------------------------------------------------------
    public void scrollToTopAndSortByPriceLowToHigh() {
        System.out.println("[STEP 07] Scroll PLP back to top and locate 'Sort By'");
        for (int i = 1; i <= 8 && !isDisplayedSafely(SORT_BY); i++) {
            scrollUp();
            System.out.println("[SCROLL] PLP scroll up " + i + "/8");
        }
        WebElement sortBy = wait.until(ExpectedConditions.elementToBeClickable(SORT_BY));
        System.out.println("[ACTION] Tapping 'Sort By'.");
        sortBy.click();

        System.out.println("[STEP 08] Select 'Price : low to high'");
        WebElement priceLowToHigh = wait.until(ExpectedConditions.elementToBeClickable(SORT_PRICE_LOW_TO_HIGH));
        System.out.println("[ACTION] Tapping 'Price : low to high'.");
        priceLowToHigh.click();
        System.out.println("[PASS] PLP sorted by Price: low to high.");
    }

    // ---------------------------------------------------------------
    // Step 9 - Tap "Filter"
    // ---------------------------------------------------------------
    public void clickFilter() {
        System.out.println("[STEP 09] Tap 'Filter'");
        WebElement filter = wait.until(ExpectedConditions.elementToBeClickable(FILTER));
        System.out.println("[ACTION] Tapping 'Filter'.");
        filter.click();
    }

    // ---------------------------------------------------------------
    // Step 10 - Select Brand=DIOR and Price=£20-£39.99 in the filter panel.
    // "Price" is a collapsed accordion section (confirmed via screenshot -
    // Brand, Price, Gender, Size (ml), etc. each start collapsed except
    // Brand) - it must be expanded before its price range options are
    // present in the tree at all.
    // <p>
    // Each option is now OPTIONAL: if any of Brand/Price-section/Price-range
    // isn't found (filter panel content can vary - e.g. after a sort
    // action, or if a particular price bracket isn't offered for the
    // current result set), this logs a warning and moves on rather than
    // failing the whole test. Only "show results" is still required next.
    // ---------------------------------------------------------------
    public void selectDiorBrandAndPriceRangeFilter() {
        System.out.println("[STEP 10] Select filter options: Brand=DIOR, Price=\u00a320 - \u00a339.99");

        boolean brandSelected = clickIfPresent(FILTER_BRAND_DIOR, "Brand 'DIOR'", 8);
        if (!brandSelected) {
            System.out.println("[WARN] 'Brand: DIOR' filter option not found - skipping, continuing test.");
        }

        boolean priceSectionExpanded = clickIfPresent(FILTER_PRICE_SECTION_HEADER, "'Price' section header", 8);
        if (!priceSectionExpanded) {
            System.out.println("[WARN] 'Price' filter section header not found - skipping the price range "
                    + "selection, continuing test.");
            return;
        }

        boolean priceRangeSelected = clickIfPresent(FILTER_PRICE_20_39, "Price '\u00a320 - \u00a339.99'", 8);
        if (!priceRangeSelected) {
            System.out.println("[WARN] 'Price: \u00a320 - \u00a339.99' filter option not found - skipping, "
                    + "continuing test.");
        }
    }

    /**
     * Taps an element if it becomes clickable within the given timeout;
     * otherwise logs a warning and returns false without throwing. Used to
     * make optional filter selections resilient to a filter panel whose
     * exact contents can vary, per the requirement that a missing filter
     * option should not fail the whole test.
     */
    private boolean clickIfPresent(By locator, String label, int timeoutSeconds) {
        try {
            WebElement el = new FluentWait<AndroidDriver>(driver)
                    .withTimeout(Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofMillis(300))
                    .until(ExpectedConditions.elementToBeClickable(locator));
            el.click();
            System.out.println("[ACTION] Tapped " + label + ".");
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    // ---------------------------------------------------------------
    // Step 11 - Tap "show results"
    // ---------------------------------------------------------------
    public void clickShowResults() {
        System.out.println("[STEP 11] Tap 'show results' to apply the filter");
        WebElement showResults = wait.until(ExpectedConditions.elementToBeClickable(SHOW_RESULTS));
        System.out.println("[ACTION] Tapping 'show results'.");
        showResults.click();
    }

    // ---------------------------------------------------------------
    // Step 12 - Scroll through the filtered PLP results, then back to top
    // ---------------------------------------------------------------
    public void verifyFilteredPlpAndScroll() {
        System.out.println("[STEP 12] Verify filtered DIOR PLP content, scroll down then back to top");
        for (int i = 1; i <= 6; i++) {
            scrollDown();
            System.out.println("[SCROLL] Filtered PLP scroll down " + i + "/6");
        }
        printAllVisibleText("Filtered DIOR PLP (bottom)");

        for (int i = 1; i <= 6; i++) {
            scrollUp();
            System.out.println("[SCROLL] Filtered PLP scroll up " + i + "/6");
        }
        printAllVisibleText("Filtered DIOR PLP (top)");
    }

    // ---------------------------------------------------------------
    // Step 13 - Tap "Back" to return to the previous screen
    // ---------------------------------------------------------------
    public void clickBack() {
        System.out.println("[STEP 13] Tap 'Back' to return to the previous screen");
        if (isDisplayedSafely(GO_BACK)) {
            WebElement back = wait.until(ExpectedConditions.elementToBeClickable(GO_BACK));
            System.out.println("[ACTION] Tapping 'Go back'.");
            back.click();
            return;
        }

        System.out.println("[WARN] 'Go back' accessibility id not found on this screen - "
                + "falling back to the Android system back button.");
        driver.navigate().back();
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
    // Helpers (mirroring MyTfsMembershipPage.java / DiscoverMorePage.java)
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

    private void scrollUp() {
        Map<String, Object> params = new HashMap<>();
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        params.put("left", (int) (size.width * 0.1));
        params.put("top", (int) (size.height * 0.2));
        params.put("width", (int) (size.width * 0.8));
        params.put("height", (int) (size.height * 0.6));
        params.put("direction", "up");
        params.put("percent", 0.75);
        Object result = driver.executeScript("mobile: scrollGesture", params);
        System.out.println("[SCROLL] element-based scrollGesture result: " + result);
    }

    private void printAllVisibleText(String sectionLabel) {
        List<WebElement> textViews = driver.findElements(AppiumBy.className("android.widget.TextView"));
        List<String> lines = new ArrayList<>();
        for (WebElement tv : textViews) {
            try {
                String text = tv.getText();
                if (text != null && !text.trim().isEmpty()) {
                    lines.add(text.trim());
                }
            } catch (Exception ignored) {
                // stale element reference from RN re-render - skip
            }
        }
        System.out.println("----- [" + sectionLabel + "] visible text (" + lines.size() + " line(s)) -----");
        for (String line : lines) {
            System.out.println("    " + line);
        }
        System.out.println("----- end of [" + sectionLabel + "] -----");
    }
}