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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Page object for TC023 - Side Menu -> Brands tab -> All Brands list ->
 * scroll to and select "Zadig & Voltaire" -> Brand Details page (read full
 * content + Bestsellers carousel) -> Discover Gifting -> Gifting PLP ->
 * back -> Hero Banner -> full PLP -> back x3 -> Mega Menu -> close app.
 * <p>
 * Follows the conventions established across ScentaddictCTAPage.java /
 * FragrancematchCTAPage.java / EveryHumanCTAPage.java / DiscoverMorePage.java:
 * no-arg constructor pulling the driver from DriverManager, [STEP]/[ACTION]/
 * [PASS]/[WARN] logging, raw coordinate taps (not WebElement.click()) with
 * content-change verification and retry for every interactive tap - this
 * project has repeatedly shown that even a confirmed content-desc/locator
 * doesn't guarantee .click() actually registers, so every tap here follows
 * that pattern rather than trusting a single click.
 * <p>
 * FLAGGED ASSUMPTIONS:
 *  - The "Z" alphabet-index-letter jump is best-effort (its own TextView is
 *    clickable=false, likely part of a custom touch-handling index strip) -
 *    tried first as a shortcut, but the real stopping condition is always
 *    "Zadig & Voltaire" becoming visible via normal vertical scrolling.
 *  - "Zadig & Voltaire" and the Hero Banner image are both clickable=false
 *    on the elements given - tapped by coordinate with verification.
 *  - All four "Go back" taps (steps 8-11) were given the exact same
 *    mis-captured generic top-bar-spacer locator (bounds [0,0][1220,308],
 *    clickable=false) - same recurring pattern seen throughout this
 *    project. Uses the proven "Go back" accessibility id + verify +
 *    Android-back/reactivate fallback, applied consistently.
 *  - The Bestsellers product carousel (on both the Brand Details page and
 *    the Gifting PLP) has no confirmed anchor/locator - best-effort
 *    generic region drag, same technique used for the Fragrance Match
 *    results screen.
 */
public class BrandsCTAPage {

    private final AndroidDriver driver;
    private final Wait<AndroidDriver> wait;

    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);

    private static final int REF_SCREEN_WIDTH = 1220;
    private static final int REF_SCREEN_HEIGHT = 2712;

    // ---------------------------------------------------------------
    // Locators
    // ---------------------------------------------------------------

    // Step 1 - Side Menu / Search icon (2nd bottom-nav icon).
    private static final int SEARCH_NAV_ICON_X1 = 337;
    private static final int SEARCH_NAV_ICON_Y1 = 2380;
    private static final int SEARCH_NAV_ICON_X2 = 500;
    private static final int SEARCH_NAV_ICON_Y2 = 2543;

    private static final By SEARCH_FIELD =
            AppiumBy.xpath("//android.widget.EditText[@hint='Search']");

    // Step 2 - CONFIRMED.
    private static final By BRANDS_TAB = AppiumBy.accessibilityId("Brands");

    // Step 3/4 - TODO NOT CONFIRMED (clickable=false): best-effort jump to
    // the "Z" section of the alphabet index strip.
    private static final int Z_INDEX_X1 = 1060;
    private static final int Z_INDEX_Y1 = 2460;
    private static final int Z_INDEX_X2 = 1172;
    private static final int Z_INDEX_Y2 = 2537;

    // Step 4 - CONFIRMED exact text, but clickable=false - tapped via its
    // clickable ancestor.
    private static final By ZADIG_VOLTAIRE_CLICKABLE = AppiumBy.xpath(
            "//android.widget.TextView[@text='Zadig & Voltaire']/ancestor::*[@clickable='true'][1]");
    private static final By ZADIG_VOLTAIRE_TEXT =
            AppiumBy.xpath("//android.widget.TextView[@text='Zadig & Voltaire']");

    // Step 6 - CONFIRMED.
    private static final By DISCOVER_GIFTING = AppiumBy.accessibilityId("Discover Gifting");

    // Step 8 - TODO NOT CONFIRMED (clickable=false): Hero Banner image,
    // bounds from the Inspector capture.
    private static final int HERO_BANNER_X1 = 0;
    private static final int HERO_BANNER_Y1 = 296;
    private static final int HERO_BANNER_X2 = 1220;
    private static final int HERO_BANNER_Y2 = 1013;

    // Steps 8-11 - the "Go back" accessibility id (confirmed working on
    // PDP-style screens in DiscoverMorePage.java) is NOT present on these
    // screens (confirmed via a real log - "not found" every time here).
    // Using the app's own visible top-left back arrow by coordinate
    // instead of the Android system back button, per explicit requirement.
    // Bounds reused from the one confirmed capture of this element
    // elsewhere in the app: content-desc="Go back", bounds [42,213][160,332].
    private static final By GO_BACK = AppiumBy.accessibilityId("Go back");
    private static final int GO_BACK_ARROW_X1 = 42;
    private static final int GO_BACK_ARROW_Y1 = 213;
    private static final int GO_BACK_ARROW_X2 = 160;
    private static final int GO_BACK_ARROW_Y2 = 332;

    private static final String APP_PACKAGE = "com.thefragranceshop";

    public BrandsCTAPage() {
        this(com.tfs.driver.DriverManager.getDriver());
    }

    public BrandsCTAPage(AndroidDriver driver) {
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
    // Step 2 - Scroll to and tap "Brands"
    // ---------------------------------------------------------------
    public void scrollToAndTapBrandsTab() {
        System.out.println("[STEP 02] Scroll horizontally to reveal 'Brands', then tap it");
        for (int i = 0; i < 3 && !isDisplayedSafely(BRANDS_TAB); i++) {
            dragRowNearSearchField("left");
            System.out.println("[SCROLL] Menu tabs row swipe " + (i + 1) + "/3");
        }
        tapConfirmedButtonWithVerification(BRANDS_TAB, "Brands");
    }

    // ---------------------------------------------------------------
    // Step 3-4 - Scroll the All Brands list to "Zadig & Voltaire" and tap it
    // ---------------------------------------------------------------
    public void scrollAndSelectZadigVoltaire() {
        System.out.println("[STEP 03] All Brands list displayed - attempting quick jump via 'Z' index "
                + "(best-effort, not confirmed)");
        tapAtScaledCoordinates(Z_INDEX_X1, Z_INDEX_Y1, Z_INDEX_X2, Z_INDEX_Y2, REF_SCREEN_WIDTH, REF_SCREEN_HEIGHT);

        // FIXED: a real run showed 10 scrolls only reached brands under "H"
        // out of a list that runs 0-9 through Z (hundreds of brands) - the
        // scrolling itself was genuinely working (gesture results were
        // "true"), the cap was just far too low for how long this list
        // actually is. The "Z" index-letter tap above also evidently did
        // nothing useful (if it had, far fewer than 10 further scrolls
        // would have been needed to get anywhere near "H", let alone "Z").
        // Raised the cap substantially and added a "content genuinely
        // stopped changing" stopping condition as a safety net, since
        // "Zadig & Voltaire" is confirmed (via screenshot) to be the very
        // last item in the entire list.
        System.out.println("[STEP 04] Scroll to 'Zadig & Voltaire' and select it");
        Set<String> lastSeen = new LinkedHashSet<>(collectVisibleText());
        int stableCount = 0;
        int maxScrolls = 60;
        for (int i = 0; i < maxScrolls && !isDisplayedSafely(ZADIG_VOLTAIRE_TEXT); i++) {
            scrollDown();
            System.out.println("[SCROLL] All Brands list scroll " + (i + 1) + "/" + maxScrolls);

            Set<String> nowSeen = new LinkedHashSet<>(collectVisibleText());
            if (nowSeen.equals(lastSeen)) {
                stableCount++;
                if (stableCount >= 3) {
                    System.out.println("[WARN] Content stopped changing after " + (i + 1)
                            + " scrolls - likely reached the true bottom of the list.");
                    break;
                }
            } else {
                stableCount = 0;
            }
            lastSeen = nowSeen;
        }
        if (!isDisplayedSafely(ZADIG_VOLTAIRE_TEXT)) {
            printAllVisibleTextAndDescs("Screen state - 'Zadig & Voltaire' never found while scrolling");
            throw new org.openqa.selenium.TimeoutException(
                    "Could not find 'Zadig & Voltaire' after scrolling the All Brands list.");
        }

        for (int attempt = 1; attempt <= 3; attempt++) {
            WebElement target = wait.until(ExpectedConditions.elementToBeClickable(ZADIG_VOLTAIRE_CLICKABLE));
            org.openqa.selenium.Rectangle rect = target.getRect();
            System.out.println("[ACTION] Tapping 'Zadig & Voltaire' at its live bounds (attempt "
                    + attempt + "/3).");
            tapAtRealCoordinates(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);

            if (waitForContentChangeFrom(ZADIG_VOLTAIRE_TEXT, 5)) {
                System.out.println("[PASS] Brand Details page opened.");
                return;
            }
            System.out.println("[WARN] Still on the All Brands list after tapping (attempt "
                    + attempt + "/3) - retrying.");
        }
        printAllVisibleTextAndDescs("Screen state after 3 failed 'Zadig & Voltaire' tap attempts");
        throw new org.openqa.selenium.TimeoutException(
                "'Zadig & Voltaire' tap did not navigate to the Brand Details page after 3 attempts.");
    }

    // ---------------------------------------------------------------
    // Step 5 - Read the Brand Details page fully (vertical scroll) and
    // scroll its Bestsellers carousel.
    // ---------------------------------------------------------------
    public void readBrandDetailsPageFully() {
        System.out.println("[STEP 05] Read Brand Details page, scrolling to capture full content");
        Set<String> seen = new LinkedHashSet<>(collectVisibleText());
        for (int i = 0; i < 10; i++) {
            int before = seen.size();
            scrollDown();
            seen.addAll(collectVisibleText());
            if (seen.size() == before) {
                System.out.println("[INFO] No new content after scroll " + (i + 1) + " - reached the bottom.");
                break;
            }
        }
        printCollected("Brand Details page (full)", seen);

        System.out.println("[ACTION] Attempting to scroll the Bestsellers carousel both directions "
                + "(best-effort - no confirmed anchor/locator for this section).");
        for (int i = 0; i < 3; i++) {
            dragGenericCarouselRegion("left");
        }
        for (int i = 0; i < 3; i++) {
            dragGenericCarouselRegion("right");
        }

        scrollToTop();
    }

    // ---------------------------------------------------------------
    // Step 6 - Tap "Discover Gifting"
    // ---------------------------------------------------------------
    public void tapDiscoverGifting() {
        System.out.println("[STEP 06] Select 'Discover Gifting'");
        tapConfirmedButtonWithVerification(DISCOVER_GIFTING, "Discover Gifting");
    }

    // ---------------------------------------------------------------
    // Step 7 - Check the Gifting PLP's Bestsellers carousel section
    // ---------------------------------------------------------------
    public void checkGiftingPlpBestsellersCarousel() {
        System.out.println("[STEP 07] Check the Gifting PLP's Bestsellers carousel section");
        printCollected("Gifting PLP (initial)", collectVisibleText());
        System.out.println("[ACTION] Attempting to scroll the Bestsellers carousel both directions "
                + "(best-effort - no confirmed anchor/locator for this section).");
        for (int i = 0; i < 3; i++) {
            dragGenericCarouselRegion("left");
        }
        for (int i = 0; i < 3; i++) {
            dragGenericCarouselRegion("right");
        }
    }

    // ---------------------------------------------------------------
    // Step 8 - Go back (to Brand Details), scroll to top, tap Hero Banner
    // ---------------------------------------------------------------
    public void goBackScrollToTopAndTapHeroBanner() {
        tapGoBackArrow("returning from Gifting PLP to Brand Details");

        System.out.println("[ACTION] Scrolling Brand Details page back to the top.");
        scrollToTop();

        System.out.println("[STEP 08] Select the Hero Banner image");
        for (int attempt = 1; attempt <= 3; attempt++) {
            System.out.println("[ACTION] Tapping Hero Banner image (attempt " + attempt + "/3).");
            tapAtScaledCoordinates(HERO_BANNER_X1, HERO_BANNER_Y1, HERO_BANNER_X2, HERO_BANNER_Y2,
                    REF_SCREEN_WIDTH, REF_SCREEN_HEIGHT);

            List<String> before = collectVisibleText();
            if (waitForContentChangeFromSnapshot(before, 5)) {
                System.out.println("[PASS] Hero Banner tap navigated to the PLP.");
                return;
            }
            System.out.println("[WARN] Still on Brand Details after tapping Hero Banner (attempt "
                    + attempt + "/3) - retrying.");
        }
        printAllVisibleTextAndDescs("Screen state after 3 failed Hero Banner tap attempts");
        throw new org.openqa.selenium.TimeoutException(
                "Hero Banner tap did not navigate to the PLP after 3 attempts.");
    }

    // ---------------------------------------------------------------
    // Step 9 - Read the full PLP, then go back (to Brand Details)
    // ---------------------------------------------------------------
    public void readFullPlpThenGoBack() {
        System.out.println("[STEP 09] Read the full PLP, scrolling to capture all content");
        Set<String> seen = new LinkedHashSet<>(collectVisibleText());
        for (int i = 0; i < 10; i++) {
            int before = seen.size();
            scrollDown();
            seen.addAll(collectVisibleText());
            if (seen.size() == before) {
                System.out.println("[INFO] No new content after scroll " + (i + 1) + " - reached the bottom.");
                break;
            }
        }
        printCollected("Hero Banner PLP (full)", seen);

        tapGoBackArrow("returning from the Hero Banner PLP to Brand Details");
    }

    // ---------------------------------------------------------------
    // Step 10 - Go back again (Brand Details -> All Brands list)
    // ---------------------------------------------------------------
    public void goBackToAllBrandsList() {
        tapGoBackArrow("returning from Brand Details to the All Brands list");
    }

    // ---------------------------------------------------------------
    // Step 11 - Go back again (All Brands list -> Mega Menu)
    // ---------------------------------------------------------------
    public void goBackToMegaMenu() {
        tapGoBackArrow("returning from the All Brands list to the Mega Menu");
    }

    // ---------------------------------------------------------------
    // Step 12 - Close the app
    // ---------------------------------------------------------------
    public void closeApp() {
        System.out.println("[STEP 12] Close the app");
        driver.terminateApp(APP_PACKAGE);
        System.out.println("[PASS] App terminated: " + APP_PACKAGE);
    }

    // =================================================================
    // Helpers
    // =================================================================

    /**
     * Taps a button located by a confirmed locator, but BY COORDINATE (its
     * live bounds), not WebElement.click() - and verifies the screen's
     * visible content actually changed, retrying up to 3 times. This
     * project has repeatedly shown that even a confirmed content-desc
     * doesn't guarantee .click() registers (NEXT/DONE/VIEW MATCHES in
     * FragrancematchCTAPage all needed this same fix).
     */
    private void tapConfirmedButtonWithVerification(By locator, String label) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            List<String> before = collectVisibleText();
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
            org.openqa.selenium.Rectangle rect = el.getRect();
            System.out.println("[ACTION] Tapping '" + label + "' at its live bounds "
                    + "(attempt " + attempt + "/3).");
            tapAtRealCoordinates(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);

            if (waitForContentChangeFromSnapshot(before, 5)) {
                System.out.println("[PASS] Screen advanced after tapping '" + label + "'.");
                return;
            }
            System.out.println("[WARN] Screen content unchanged after tapping '" + label + "' (attempt "
                    + attempt + "/3) - retrying.");
        }
        throw new org.openqa.selenium.TimeoutException(
                "'" + label + "' tap did not advance the screen after 3 attempts.");
    }

    /**
     * Taps the app's own top-left back arrow, by coordinate (since the
     * "Go back" accessibility id is confirmed NOT present on these
     * screens - a real log showed "not found" every time here, even
     * though it IS the confirmed working id for this same element
     * elsewhere in the app, e.g. DiscoverMorePage's PDP). Verified by the
     * screen's content actually changing, retried up to 3 times. Does NOT
     * fall back to the Android system back button, per explicit
     * requirement - this must be the app's own in-app back navigation.
     */
    private void tapGoBackArrow(String contextLabel) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            List<String> before = collectVisibleText();

            WebElement goBackEl = null;
            if (isDisplayedSafely(GO_BACK)) {
                try {
                    goBackEl = wait.until(ExpectedConditions.elementToBeClickable(GO_BACK));
                } catch (Exception ignored) {
                    // fall through to coordinate tap
                }
            }

            if (goBackEl != null) {
                org.openqa.selenium.Rectangle rect = goBackEl.getRect();
                System.out.println("[ACTION] Tapping 'Go back' via confirmed accessibility id ("
                        + contextLabel + ", attempt " + attempt + "/3).");
                tapAtRealCoordinates(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);
            } else {
                System.out.println("[ACTION] Tapping the app's own back arrow by coordinate "
                        + "('Go back' id not present on this screen) (" + contextLabel
                        + ", attempt " + attempt + "/3).");
                tapAtScaledCoordinates(GO_BACK_ARROW_X1, GO_BACK_ARROW_Y1, GO_BACK_ARROW_X2, GO_BACK_ARROW_Y2,
                        REF_SCREEN_WIDTH, REF_SCREEN_HEIGHT);
            }

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

    private boolean waitForContentChangeFrom(By disappearingLocator, int timeoutSeconds) {
        try {
            new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofMillis(400))
                    .until(d -> d.findElements(disappearingLocator).isEmpty());
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
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

    private void dragRowNearSearchField(String direction) {
        org.openqa.selenium.Dimension screenSize = driver.manage().window().getSize();
        List<WebElement> fields = driver.findElements(SEARCH_FIELD);
        int centerY;
        if (!fields.isEmpty()) {
            org.openqa.selenium.Rectangle r = fields.get(0).getRect();
            centerY = r.getY() + r.getHeight() + 250;
        } else {
            centerY = (int) (screenSize.height * 0.22);
        }
        dragHorizontal(direction, centerY);
    }

    private void dragGenericCarouselRegion(String direction) {
        org.openqa.selenium.Dimension screenSize = driver.manage().window().getSize();
        int centerY = (int) (screenSize.height * 0.55);
        dragHorizontal(direction, centerY);
    }

    private void dragHorizontal(String direction, int centerY) {
        org.openqa.selenium.Dimension screenSize = driver.manage().window().getSize();
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