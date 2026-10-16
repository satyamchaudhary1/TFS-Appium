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
 * Page object for TC018 - "Discover More" home-page carousel.
 * <p>
 * Flow: scroll to the carousel, read/count its image banners, open the
 * first one and close it via the top-right cross icon, scroll back to the
 * carousel, open the second image (a product -> PDP), read the full PDP,
 * tap the wishlist icon, open the product image zoom and close it, tap
 * back to return Home, then close the app.
 * <p>
 * Follows the same conventions as MyTfsMembershipPage.java: no-arg
 * constructor pulling the driver from DriverManager, [STEP]/[ACTION]/
 * [PASS]/[SCROLL]/[WARN] console logging, and tapAtScaledCoordinates() for
 * icon-only elements with no text/content-desc of their own.
 * <p>
 * FLAGGED ASSUMPTIONS - no confirmed Inspector locator exists for either
 * of these, so both use scaled-coordinate tapping:
 *  - The top-right "cross" icon used to close the first banner's screen
 *    and the PDP image zoom overlay.
 *  - The wishlist heart icon on the PDP.
 * If either misfires, capture the real element (or its clickable ancestor)
 * via Appium Inspector and swap in the exact locator/coordinates.
 */
public class DiscoverMorePage {

    private final AndroidDriver driver;
    private final Wait<AndroidDriver> wait;

    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);

    // Reference screen used for all captured bounds in this class.
    private static final int REF_SCREEN_WIDTH = 1220;
    private static final int REF_SCREEN_HEIGHT = 2712;

    // ---------------------------------------------------------------
    // Locators
    // ---------------------------------------------------------------

    private static final By DISCOVER_MORE_HEADING =
            AppiumBy.xpath("//android.widget.TextView[@text='Discover More']");

    // FIXED APPROACH: previously tried to locate a specific
    // HorizontalScrollView WebElement to scroll (via XPath "following::",
    // which crashed the Appium XPath engine; then via Java Y-position
    // comparison, which still picked the wrong/empty scrollview on some
    // runs). Neither actually needed to succeed - mobile:scrollGesture
    // only needs a screen REGION to swipe over, not a correctly-identified
    // container. All carousel interaction is now coordinate-based (see
    // swipeCarouselRegion() and getCarouselItemLabels() below), computed
    // directly from the "Discover More" heading's on-screen position.
    private static final By CAROUSEL_ITEMS_GLOBAL =
            AppiumBy.xpath("//*[@clickable='true' and @content-desc!='']");

    private static final By FIRST_CARD = AppiumBy.accessibilityId("Summer Magazine");
    private static final By SECOND_CARD =
            AppiumBy.accessibilityId("Hugo Boss, BOSS Bottled Citrus Bold");

    // TODO CONFIRM WITH INSPECTOR - see class javadoc. Approximate top-right
    // corner region on a 1220x2712 reference screen.
    private static final int TOP_RIGHT_CROSS_X1 = 1085;
    private static final int TOP_RIGHT_CROSS_Y1 = 20;
    private static final int TOP_RIGHT_CROSS_X2 = 1180;
    private static final int TOP_RIGHT_CROSS_Y2 = 100;

    // Product hero image on the PDP - no text/content-desc captured.
    private static final int PRODUCT_IMAGE_X1 = 0;
    private static final int PRODUCT_IMAGE_Y1 = 134;
    private static final int PRODUCT_IMAGE_X2 = 1220;
    private static final int PRODUCT_IMAGE_Y2 = 1112;

    // Zoom view close "x" - CONFIRMED exact text via Inspector.
    private static final By ZOOM_CLOSE_X =
            AppiumBy.xpath("//android.widget.TextView[@text='\u00d7']");

    // TODO CONFIRM WITH INSPECTOR - wishlist heart icon (com.horcrux.svg.d,
    // no content-desc). Bounds from Inspector: [1085,223]-[1181,319].
    private static final int WISHLIST_ICON_X1 = 1085;
    private static final int WISHLIST_ICON_Y1 = 223;
    private static final int WISHLIST_ICON_X2 = 1181;
    private static final int WISHLIST_ICON_Y2 = 319;

    // CONFIRMED exact accessibility id.
    private static final By GO_BACK = AppiumBy.accessibilityId("Go back");

    private static final String APP_PACKAGE = "com.thefragranceshop";

    public DiscoverMorePage() {
        this(com.tfs.driver.DriverManager.getDriver());
    }

    public DiscoverMorePage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new FluentWait<>(driver)
                .withTimeout(TIMEOUT)
                .pollingEvery(POLL_INTERVAL)
                .ignoring(org.openqa.selenium.NoSuchElementException.class);
    }

    // ---------------------------------------------------------------
    // Step 1 - Scroll Home page down until "Discover More" AND its carousel
    // content are both visible (not just the heading text).
    //
    // FIXED: previously only checked for the heading text, which can become
    // visible before the carousel's actual card content has rendered,
    // causing the very next step (counting banners) to scan an empty
    // carousel. Now also waits for at least one known card
    // (Summer Magazine) to be present before declaring the section ready.
    // ---------------------------------------------------------------
    public void scrollToDiscoverMoreSection() {
        System.out.println("[STEP 01] Scroll Home page down to 'Discover More' section");
        for (int i = 1; i <= 8; i++) {
            if (isDisplayedSafely(DISCOVER_MORE_HEADING)) {
                System.out.println("[INFO] 'Discover More' heading visible after " + (i - 1)
                        + " scroll(s) - waiting for carousel content to render.");
                if (waitForFirstCardToAppear(4)) {
                    System.out.println("[PASS] 'Discover More' section fully visible (heading + carousel content).");
                    return;
                }
                System.out.println("[WARN] Heading visible but carousel content not rendered yet - "
                        + "continuing to scroll a little further.");
            }
            scrollDown();
            System.out.println("[SCROLL] Home page scroll " + i + "/8");
        }
        // One last chance: heading may already be visible from the loop above.
        if (isDisplayedSafely(DISCOVER_MORE_HEADING) && waitForFirstCardToAppear(5)) {
            System.out.println("[PASS] 'Discover More' section fully visible (heading + carousel content).");
            return;
        }
        printAllVisibleText("Screen state when 'Discover More' section could not be fully found");
        throw new org.openqa.selenium.TimeoutException(
                "'Discover More' section (heading and/or its carousel content) not found after scrolling.");
    }

    private boolean waitForFirstCardToAppear(int timeoutSeconds) {
        try {
            new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofMillis(300))
                    .until(d -> isDisplayedSafely(FIRST_CARD));
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    // ---------------------------------------------------------------
    // Step 2 - Read and count every image banner in the carousel, print to
    // console, then leave the carousel scrolled back to its start.
    //
    // FIXED TIMING RACE: a real run showed this scan finding 0 items even
    // though the same code found 2 correctly on a previous run - the
    // carousel's content (cards with their content-desc) can still be
    // lazy-rendering the instant the "Discover More" heading itself
    // becomes visible. Now waits for at least one item to actually appear
    // before scanning, and retries the whole scan once if it still comes
    // up empty (covers a slow-loading first attempt).
    // ---------------------------------------------------------------
    public int countAndPrintCarouselImages() {
        System.out.println("[STEP 02] Read and count image banners in the 'Discover More' carousel");

        Set<String> seen = scanCarouselOnce();
        if (seen.isEmpty()) {
            System.out.println("[WARN] No carousel items found on first scan - likely still rendering. "
                    + "Waiting and retrying once.");
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            seen = scanCarouselOnce();
        }

        System.out.println("[INFO] 'Discover More' carousel contains " + seen.size() + " image(s)/banner(s):");
        int idx = 1;
        for (String label : seen) {
            System.out.println("    " + idx++ + ". " + label);
        }
        return seen.size();
    }

    private Set<String> scanCarouselOnce() {
        try {
            new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(5))
                    .pollingEvery(Duration.ofMillis(300))
                    .until(d -> !getCarouselItemLabels().isEmpty());
        } catch (org.openqa.selenium.TimeoutException e) {
            System.out.println("[WARN] No carousel items appeared within 5s of settling.");
        }

        // FIXED: capture labels BEFORE each swipe, and once more AFTER the
        // loop ends - a swipe that reports "can't scroll further" has
        // already physically moved the content to its final position
        // before reporting that, so the cards only visible at that final
        // position (confirmed missing: Prada, Jean Paul Gaultier) were
        // never being captured.
        Set<String> seen = new LinkedHashSet<>();
        for (int i = 0; i <= 6; i++) {
            seen.addAll(getCarouselItemLabels());
            Object result = swipeCarouselRegion("left");
            System.out.println("[SCROLL] Carousel scan swipe " + (i + 1) + "/7 result: " + result);
            if (Boolean.FALSE.equals(result)) {
                break;
            }
        }
        seen.addAll(getCarouselItemLabels());

        returnCarouselToStart();

        return seen;
    }

    /**
     * Scrolls the carousel back to its start, verified by actually
     * checking for "Summer Magazine" (the first card) to reappear - NOT by
     * trusting the swipe gesture's own reported boolean, which a real run
     * showed to be unreliable (it can report "can't scroll further" while
     * the carousel is still mid-way through, leaving the flow stranded on
     * a later card with no way to find the first one again).
     */
    private void returnCarouselToStart() {
        for (int i = 0; i < 10; i++) {
            if (isDisplayedSafely(FIRST_CARD)) {
                System.out.println("[PASS] Carousel back at the start ('Summer Magazine' visible) "
                        + "after " + i + " swipe(s) right.");
                return;
            }
            swipeCarouselRegion("right");
            System.out.println("[SCROLL] Carousel swipe right (returning to start) " + (i + 1) + "/10");
        }
        if (!isDisplayedSafely(FIRST_CARD)) {
            System.out.println("[WARN] Could not confirm carousel returned to start after 10 swipes - "
                    + "'Summer Magazine' still not visible.");
        }
    }

    /**
     * Returns the content-desc labels of every clickable carousel item
     * currently in the accessibility tree, positioned below the "Discover
     * More" heading and within a generous height band matching the
     * carousel's card height (avoids picking up unrelated clickable
     * elements further down the page, e.g. product carousels).
     * <p>
     * FIXED: "Find your true fragrance Today!" is NOT one of this
     * carousel's cards - it's a separate promotional video section below
     * it (confirmed via an earlier hierarchy dump). A fixed guessed pixel
     * band was accidentally including it as a false "3rd banner" while
     * still missing real cards further along (Prada, Jean Paul Gaultier).
     * Now uses that element's own Y-position as a hard exclusive lower
     * boundary when present, falling back to the previous generous band
     * only if it isn't found (e.g. not yet scrolled into the tree).
     */
    private static final By NEXT_SECTION_LANDMARK =
            AppiumBy.accessibilityId("Find your true fragrance Today!");

    private Set<String> getCarouselItemLabels() {
        Set<String> labels = new LinkedHashSet<>();
        List<WebElement> headings = driver.findElements(DISCOVER_MORE_HEADING);
        if (headings.isEmpty()) {
            return labels;
        }
        int headingBottomY;
        try {
            org.openqa.selenium.Rectangle r = headings.get(0).getRect();
            headingBottomY = r.getY() + r.getHeight();
        } catch (Exception e) {
            return labels;
        }

        int lowerBoundY;
        List<WebElement> nextSection = driver.findElements(NEXT_SECTION_LANDMARK);
        if (!nextSection.isEmpty()) {
            try {
                lowerBoundY = nextSection.get(0).getRect().getY();
            } catch (Exception e) {
                lowerBoundY = headingBottomY + 1700;
            }
        } else {
            lowerBoundY = headingBottomY + 1700;
        }

        for (WebElement item : driver.findElements(CAROUSEL_ITEMS_GLOBAL)) {
            try {
                org.openqa.selenium.Rectangle r = item.getRect();
                if (r.getY() >= headingBottomY && r.getY() < lowerBoundY) {
                    String desc = item.getAttribute("content-desc");
                    if (desc != null && !desc.isBlank() && !desc.equals("Find your true fragrance Today!")) {
                        labels.add(desc);
                    }
                }
            } catch (Exception ignored) {
                // stale element - skip
            }
        }
        return labels;
    }

    /**
     * Swipes a screen region horizontally, computed from the "Discover
     * More" heading's CURRENT on-screen position rather than any located
     * scrollview element - this is what actually fixes the repeated
     * "picked the wrong/empty scrollview" failures: mobile:scrollGesture
     * only needs a region rectangle to gesture over, it doesn't require
     * correctly identifying which container lives there.
     */
    private Object swipeCarouselRegion(String direction) {
        List<WebElement> headings = driver.findElements(DISCOVER_MORE_HEADING);
        org.openqa.selenium.Dimension screenSize = driver.manage().window().getSize();

        int top;
        if (!headings.isEmpty()) {
            org.openqa.selenium.Rectangle r = headings.get(0).getRect();
            top = r.getY() + r.getHeight() + 40; // just below the heading, into the card area
        } else {
            top = (int) (screenSize.height * 0.25);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("left", (int) (screenSize.width * 0.05));
        params.put("top", top);
        params.put("width", (int) (screenSize.width * 0.9));
        params.put("height", Math.min(900, screenSize.height - top - 50));
        params.put("direction", direction);
        params.put("percent", 0.8);
        return driver.executeScript("mobile: scrollGesture", params);
    }

    // ---------------------------------------------------------------
    // Step 3 - Tap the first image ("Summer Magazine")
    // ---------------------------------------------------------------
    public void tapFirstImage() {
        System.out.println("[STEP 03] Tap the first image banner ('Summer Magazine')");
        if (!isDisplayedSafely(FIRST_CARD) && !isDisplayedSafely(DISCOVER_MORE_HEADING)) {
            System.out.println("[WARN] 'Discover More' section not currently visible - re-scrolling "
                    + "before tapping (defensive check).");
            scrollToDiscoverMoreSection();
        }
        WebElement card;
        try {
            card = wait.until(ExpectedConditions.elementToBeClickable(FIRST_CARD));
        } catch (org.openqa.selenium.TimeoutException e) {
            printAllVisibleText("Screen state when 'Summer Magazine' card was not found");
            throw e;
        }
        System.out.println("[ACTION] Tapping first image.");
        card.click();
    }

    // ---------------------------------------------------------------
    // Step 4 - Tap the top-right cross icon to close whatever opened,
    // returning to Home.
    //
    // FIXED: an earlier version fell back to a bare driver.navigate().back()
    // and then unconditionally printed "[PASS] Back on Home page" without
    // ever verifying it actually worked. A real run showed exactly why
    // that's wrong: the "Summer Magazine" screen is apparently the first
    // screen in the app's back-stack, so Android back() exited the app
    // entirely instead of returning to Home - and the false PASS message
    // masked that, so the next 8 scrolls (looking for "Discover More" on a
    // screen the app wasn't even showing anymore) timed out.
    //
    // Now: try the coordinate tap, verify; if that fails, try back(),
    // verify; if that ALSO fails (likely because the app got backgrounded
    // or exited), explicitly re-activate the app and verify one more time.
    // Only reports PASS once actually confirmed, and throws a clear error
    // (with a full screen dump) if none of the three recovery steps work.
    // ---------------------------------------------------------------
    public void closeTopRightCrossIcon() {
        System.out.println("[STEP 04] Tap the top-right cross icon to return to Home");
        tapAtScaledCoordinates(TOP_RIGHT_CROSS_X1, TOP_RIGHT_CROSS_Y1, TOP_RIGHT_CROSS_X2, TOP_RIGHT_CROSS_Y2,
                REF_SCREEN_WIDTH, REF_SCREEN_HEIGHT);

        if (waitForHomeLandmark(5)) {
            System.out.println("[PASS] Back on Home page (cross icon tap worked).");
            return;
        }

        System.out.println("[WARN] Coordinate tap didn't return to Home - trying Android back button.");
        driver.navigate().back();
        if (waitForHomeLandmark(5)) {
            System.out.println("[PASS] Back on Home page (Android back button worked).");
            return;
        }

        System.out.println("[WARN] Still not on Home after back button - the app may have been "
                + "backgrounded or exited. Re-activating it.");
        driver.activateApp(APP_PACKAGE);
        if (waitForHomeLandmark(8)) {
            System.out.println("[PASS] Back on Home page (after re-activating the app).");
            return;
        }

        printAllVisibleText("Screen state after all recovery attempts to return to Home");
        throw new org.openqa.selenium.TimeoutException(
                "Could not return to Home page after tapping the cross icon, Android back, "
                        + "and re-activating the app. See dump above for the actual screen state.");
    }

    private boolean waitForHomeLandmark(int timeoutSeconds) {
        try {
            new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofMillis(500))
                    .until(d -> isDisplayedSafely(DISCOVER_MORE_HEADING) || isHomeLikelyVisible());
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    // FIXED: only checked for "Discover More"/"Best Sellers" text, both of
    // which require the page to be scrolled down. A real run showed the
    // recovery correctly returning to Home, but landing at the TOP of the
    // page (showing the Women's/Men's/Gift Sets/New In category tiles)
    // after the back-button + reactivateApp recovery reset the scroll
    // position - so this check reported false negative and the test
    // failed even though it had actually succeeded. Now also recognizes
    // those top-of-Home tiles.
    private boolean isHomeLikelyVisible() {
        return !driver.findElements(AppiumBy.xpath(
                "//android.widget.TextView[@text='Best Sellers' or @text='Bestsellers' "
                        + "or @text=\"Women's\" or @text=\"Men's\" or @text='Gift Sets' or @text='New In']"
        )).isEmpty();
    }

    // ---------------------------------------------------------------
    // Step 5 - Scroll Home page down to "Discover More" again
    // ---------------------------------------------------------------
    public void scrollToDiscoverMoreSectionAgain() {
        System.out.println("[STEP 05] Scroll Home page down to 'Discover More' section again");
        scrollToDiscoverMoreSection();
    }

    // ---------------------------------------------------------------
    // Step 6 - Reveal and tap the second image (Hugo Boss product)
    // ---------------------------------------------------------------
    public void tapSecondImage() {
        System.out.println("[STEP 06] Tap the second image banner (Hugo Boss product)");
        for (int i = 0; i < 3 && !isDisplayedSafely(SECOND_CARD); i++) {
            swipeCarouselRegion("left");
            System.out.println("[SCROLL] Carousel swipe to reveal second image " + (i + 1) + "/3");
        }
        WebElement card;
        try {
            card = wait.until(ExpectedConditions.elementToBeClickable(SECOND_CARD));
        } catch (org.openqa.selenium.TimeoutException e) {
            printAllVisibleText("Screen state when Hugo Boss card was not found");
            throw e;
        }
        System.out.println("[ACTION] Tapping second image.");
        card.click();
    }

    // ---------------------------------------------------------------
    // Step 7 - Confirm PDP opened, scroll slowly to the bottom reading all
    // content, print to console, then scroll back to the top.
    // ---------------------------------------------------------------
    public void readCompletePdpAndReturnToTop() {
        System.out.println("[STEP 07] PDP should now be open - scroll slowly to the bottom, reading all content");
        for (int i = 1; i <= 8; i++) {
            scrollDown();
            System.out.println("[SCROLL] PDP scroll down " + i + "/8");
        }
        printAllVisibleText("PDP content (bottom)");

        System.out.println("[ACTION] Scrolling PDP back to the top.");
        for (int i = 1; i <= 8; i++) {
            scrollUp();
            System.out.println("[SCROLL] PDP scroll up " + i + "/8");
        }
    }

    // ---------------------------------------------------------------
    // Step 8 - Tap the wishlist icon (top-right corner of the PDP)
    // ---------------------------------------------------------------
    public void tapWishlistIcon() {
        System.out.println("[STEP 08] Tap the wishlist icon");
        tapAtScaledCoordinates(WISHLIST_ICON_X1, WISHLIST_ICON_Y1, WISHLIST_ICON_X2, WISHLIST_ICON_Y2,
                REF_SCREEN_WIDTH, REF_SCREEN_HEIGHT);
        System.out.println("[ACTION] Tapped wishlist icon.");
    }

    // ---------------------------------------------------------------
    // Step 9 - Tap the product image to open the zoom view
    // ---------------------------------------------------------------
    public void tapProductImageForZoom() {
        System.out.println("[STEP 09] Tap the product image to open zoom view");
        tapAtScaledCoordinates(PRODUCT_IMAGE_X1, PRODUCT_IMAGE_Y1, PRODUCT_IMAGE_X2, PRODUCT_IMAGE_Y2,
                REF_SCREEN_WIDTH, REF_SCREEN_HEIGHT);
        System.out.println("[ACTION] Tapped product image.");
    }

    // ---------------------------------------------------------------
    // Step 10 - Tap the cross icon to close the zoom view
    // ---------------------------------------------------------------
    public void closeImageZoomView() {
        System.out.println("[STEP 10] Close the image zoom view");
        WebElement closeX = wait.until(ExpectedConditions.presenceOfElementLocated(ZOOM_CLOSE_X));
        System.out.println("[ACTION] Tapping zoom view close 'x'.");
        closeX.click();
    }

    // ---------------------------------------------------------------
    // Step 11 - Tap "Go back" to return to Home page
    // ---------------------------------------------------------------
    public void clickGoBackToHome() {
        System.out.println("[STEP 11] Click 'Go back' to return to Home page");
        WebElement goBack = wait.until(ExpectedConditions.elementToBeClickable(GO_BACK));
        System.out.println("[ACTION] Tapping 'Go back'.");
        goBack.click();
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
    // Helpers (mirroring MyTfsMembershipPage.java conventions)
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