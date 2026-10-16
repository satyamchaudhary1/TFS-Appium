package com.tfs.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
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
 * Page object for TC021 - Side Menu -> "Fragrancematch" CTA -> Fragrance
 * Match quiz (For Myself -> search + pick a fragrance -> Similar/Different
 * -> Male/Female -> time of day -> age range -> price range slider -> Done
 * -> email + marketing checkbox -> View Matches -> results carousel) ->
 * Back -> close app.
 * <p>
 * Follows the established conventions from ScentaddictCTAPage.java /
 * DiscoverMorePage.java: no-arg constructor pulling the driver from
 * DriverManager, [STEP]/[ACTION]/[PASS]/[WARN] logging,
 * tapAtScaledCoordinates() for icon-only elements with no reliable
 * locator, dragCarouselOnce()-style raw touch drags rather than
 * mobile:scrollGesture (proven more reliable for this app's custom
 * horizontal lists and slider widgets).
 * <p>
 * FLAGGED ASSUMPTIONS - see individual step comments below for detail:
 *  - Step 2 ("Fragrancematch" CTA): same timing-race issue confirmed for
 *    the "scentaddict" CTA in TC020 - self-verifying retry applied.
 *  - Step 4 ("FOR MYSELF"): the TextView itself is clickable=false; its
 *    clickable ancestor is tapped instead.
 *  - Step 17 (price range slider): no target values were specified, so
 *    each handle is dragged a modest arbitrary amount just to exercise the
 *    interaction, not to hit any specific business-required price range.
 *  - Step 21 ("Close the Keyboard"): the captured locator was a
 *    mis-captured full-screen container (same recurring pattern seen
 *    elsewhere in this project) - dismissed via the Android back key
 *    instead.
 *  - Step 24 ("Back icon"): no locator was captured at all - reuses the
 *    "Go back" accessibility id confirmed reliable elsewhere in this app.
 *  - Step 23 (results carousel): no locator captured for the "My
 *    Fragrance Matches" section - anchored to that heading text.
 */
public class FragrancematchCTAPage {

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
    // used in TC019/TC020.
    private static final int SEARCH_NAV_ICON_X1 = 337;
    private static final int SEARCH_NAV_ICON_Y1 = 2380;
    private static final int SEARCH_NAV_ICON_X2 = 500;
    private static final int SEARCH_NAV_ICON_Y2 = 2543;

    // Step 2 - TODO: captured element was the inner SvgView (clickable=false).
    // Its clickable ancestor ViewGroup bounds (confirmed via hierarchy dump).
    private static final int FRAGRANCE_MATCH_CTA_X1 = 627;
    private static final int FRAGRANCE_MATCH_CTA_Y1 = 485;
    private static final int FRAGRANCE_MATCH_CTA_X2 = 1083;
    private static final int FRAGRANCE_MATCH_CTA_Y2 = 629;

    // Fragrance Match screen header, used to verify navigation succeeded.
    private static final By FRAGRANCE_MATCH_HEADER =
            AppiumBy.xpath("//android.widget.TextView[@text='Fragrance Match']");

    // Step 4 - "FOR MYSELF" TextView is clickable=false; tap its clickable
    // ancestor instead (same pattern as the earlier "Edit" button fix).
    private static final By FOR_MYSELF_CLICKABLE =
            AppiumBy.xpath("//android.widget.TextView[@text='FOR MYSELF']/ancestor::*[@clickable='true'][1]");

    // Step 6 - CONFIRMED (hint attribute).
    private static final By SEARCH_FRAGRANCE_FIELD =
            AppiumBy.xpath("//android.widget.EditText[@hint='Search for a fragrance...']");

    // Step 8 - CONFIRMED exact content-desc.
    private static final By RABANNE_SUGGESTION =
            AppiumBy.accessibilityId("Rabanne, 1 MILLION, Shower Gel 150ml");

    // Steps 9/11/13/15/18 - CONFIRMED. Same bounds/content-desc reused
    // across multiple quiz screens; step 18 uses "DONE" instead of "NEXT".
    private static final By NEXT_BUTTON = AppiumBy.accessibilityId("NEXT");
    private static final By DONE_BUTTON = AppiumBy.accessibilityId("DONE");

    // Step 10 - CONFIRMED.
    private static final By SIMILAR_OPTION = AppiumBy.accessibilityId("Similar");

    // Step 12 - CONFIRMED exact content-desc, INCLUDING the leading space
    // (" Male") - matched exactly as captured, not trimmed.
    private static final By MALE_OPTION = AppiumBy.accessibilityId(" Male");

    // Step 14 - CONFIRMED.
    private static final By EVENINGS_OPTION = AppiumBy.accessibilityId("Evenings");

    // Step 16 - CONFIRMED.
    private static final By AGE_31_40_OPTION = AppiumBy.accessibilityId("31 - 40");

    // Step 17 - TODO: both slider handles are clickable=false SVG thumbs -
    // dragged by scaled coordinate, not tapped. Bounds confirmed via
    // Inspector (left handle, right handle).
    private static final int SLIDER_LEFT_HANDLE_X1 = 181;
    private static final int SLIDER_LEFT_HANDLE_Y1 = 849;
    private static final int SLIDER_LEFT_HANDLE_X2 = 251;
    private static final int SLIDER_LEFT_HANDLE_Y2 = 919;
    private static final int SLIDER_RIGHT_HANDLE_X1 = 968;
    private static final int SLIDER_RIGHT_HANDLE_Y1 = 849;
    private static final int SLIDER_RIGHT_HANDLE_X2 = 1038;
    private static final int SLIDER_RIGHT_HANDLE_Y2 = 919;
    // Arbitrary drag distance (reference pixels) - no specific target price
    // range was given in the steps, this just exercises the drag gesture.
    private static final int SLIDER_DRAG_DISTANCE = 100;

    // Step 19 - CONFIRMED (hint attribute).
    private static final By EMAIL_FIELD = AppiumBy.xpath("//android.widget.EditText[@hint='Enter Your Mail']");

    // Step 20 - CONFIRMED.
    private static final By YES_PLEASE_CHECKBOX = AppiumBy.className("android.widget.CheckBox");

    // Step 22 - CONFIRMED.
    private static final By VIEW_MATCHES_BUTTON = AppiumBy.accessibilityId("VIEW MATCHES");

    // Step 23 - TODO: no locator captured for this heading; inferred from
    // the step description to anchor the results carousel scroll.
    private static final By MY_FRAGRANCE_MATCHES_HEADER =
            AppiumBy.xpath("//android.widget.TextView[@text='My Fragrance Matches']");

    // Step 24 - TODO: no locator captured at all; reusing the "Go back"
    // accessibility id confirmed reliable elsewhere in this app (TC018).
    private static final By GO_BACK = AppiumBy.accessibilityId("Go back");

    private static final String APP_PACKAGE = "com.thefragranceshop";

    public FragrancematchCTAPage() {
        this(com.tfs.driver.DriverManager.getDriver());
    }

    public FragrancematchCTAPage(AndroidDriver driver) {
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
    }

    // ---------------------------------------------------------------
    // Step 2 - Tap "Fragrancematch". Self-verifying retry, same fix applied
    // to the "scentaddict" CTA in TC020 for the same timing-race issue.
    // ---------------------------------------------------------------
    public void tapFragranceMatchCta() {
        System.out.println("[STEP 02] Select the 'Fragrancematch' button");
        for (int attempt = 1; attempt <= 3; attempt++) {
            tapAtScaledCoordinates(FRAGRANCE_MATCH_CTA_X1, FRAGRANCE_MATCH_CTA_Y1,
                    FRAGRANCE_MATCH_CTA_X2, FRAGRANCE_MATCH_CTA_Y2, REF_SCREEN_WIDTH, REF_SCREEN_HEIGHT);
            System.out.println("[ACTION] Tapped 'Fragrancematch' (attempt " + attempt + "/3).");

            if (waitForElement(FRAGRANCE_MATCH_HEADER, 5)) {
                System.out.println("[PASS] Fragrance Match screen opened.");
                return;
            }
            System.out.println("[WARN] Fragrance Match screen not visible within 5s (attempt "
                    + attempt + "/3) - the tap likely landed before the button was interactive yet.");
        }
        throw new org.openqa.selenium.TimeoutException(
                "Fragrance Match screen did not open after 3 attempts tapping the CTA.");
    }

    // ---------------------------------------------------------------
    // Step 3 - Read the Fragrance Match intro screen, scrolling to capture
    // its full content (it extends below the fold per the screenshot).
    // ---------------------------------------------------------------
    public void readFragranceMatchIntroScreen() {
        System.out.println("[STEP 03] Read Fragrance Match intro screen, scrolling to capture full content");
        Set<String> seen = new LinkedHashSet<>(collectVisibleText());
        for (int i = 0; i < 6; i++) {
            int before = seen.size();
            scrollDown();
            seen.addAll(collectVisibleText());
            if (seen.size() == before) {
                System.out.println("[INFO] No new content after scroll " + (i + 1) + " - reached the bottom.");
                break;
            }
        }
        printCollected("Fragrance Match intro screen", seen);
        scrollToTop();
    }

    // ---------------------------------------------------------------
    // Step 4 - Tap "FOR MYSELF" (via its clickable ancestor)
    // ---------------------------------------------------------------
    public void tapForMyself() {
        System.out.println("[STEP 04] Select 'FOR MYSELF'");
        WebElement forMyself = wait.until(ExpectedConditions.elementToBeClickable(FOR_MYSELF_CLICKABLE));
        System.out.println("[ACTION] Tapping 'FOR MYSELF'.");
        forMyself.click();
    }

    // ---------------------------------------------------------------
    // Step 5 - Read the "search for a fragrance" screen
    // ---------------------------------------------------------------
    public void readSearchFragranceScreen() {
        System.out.println("[STEP 05] Read 'search for a fragrance' screen content");
        printCollected("Search for a fragrance screen", collectVisibleText());
    }

    // ---------------------------------------------------------------
    // Step 6-8 - Tap the search field, type "Rabanne", select the suggestion.
    //
    // FIXED (two real bugs, not just a retry-count issue):
    //  1) A real run showed Step 5's content read capturing only 1 line
    //     ("Fragrance Match") instead of the full screen - meaning this
    //     screen was still loading when the code raced ahead into typing.
    //     Now waits for the search field to actually be present AND for
    //     the NEXT/BACK buttons to exist before proceeding, confirming the
    //     screen has genuinely settled.
    //  2) waitForSuggestionSelected() had a fallback checking whether NEXT
    //     was "enabled" - but React Native apps very often leave the
    //     native enabled attribute true even on a visually-disabled
    //     button, controlling it purely through styling/onPress logic.
    //     That fallback gave a FALSE POSITIVE (declared success
    //     immediately even though the tap never registered), which is
    //     exactly what happened in a real run. Removed - now verifies
    //     purely via the suggestion's own "selected" state, and dumps
    //     diagnostic info if that never becomes true.
    // ---------------------------------------------------------------
    public void searchAndSelectRabanne(String query) {
        System.out.println("[STEP 06] Tap the 'Search for a fragrance' input field");
        waitForScreenToSettle(8);
        WebElement field = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_FRAGRANCE_FIELD));
        System.out.println("[ACTION] Tapping search field.");
        field.click();

        System.out.println("[STEP 07] Type '" + query + "'");
        System.out.println("[ACTION] Typing '" + query + "'.");
        field.sendKeys(query);

        System.out.println("[ACTION] Waiting for suggestion results to render.");
        boolean suggestionsRendered = waitForElement(RABANNE_SUGGESTION, 8);
        if (!suggestionsRendered) {
            printAllVisibleTextAndDescs("Screen state - Rabanne suggestion never rendered after typing");
            throw new org.openqa.selenium.TimeoutException(
                    "Rabanne suggestion never appeared after typing '" + query + "'.");
        }

        // FIXED (this is the third attempt at a verification signal, and
        // the previous two are both now disproven by real evidence):
        //  1) the "selected" attribute - a diagnostic dump showed it's
        //     never set true by this app, tap or no tap.
        //  2) "the suggestion list collapses to 1 item on selection" - a
        //     real screenshot disproved this: the selected item gets a
        //     black border + small "X" icon, but all 6 other items stay
        //     fully visible. Neither the border nor the X icon is exposed
        //     via content-desc or any attribute this tree reveals.
        // Rather than guess a fourth visual cue that might also be
        // unreadable, this now ties verification to the one thing that
        // actually matters and that IS provably reliable
        // (clickButtonWithVerification's content-change check, confirmed
        // working for every other NEXT/DONE transition in this quiz):
        // does NEXT genuinely advance the screen afterward. The Rabanne
        // tap and the NEXT tap are folded into one retry loop - if NEXT
        // doesn't advance, the Rabanne tap is assumed to have missed, and
        // BOTH are retried together.
        System.out.println("[STEP 08] Select the Rabanne suggestion, confirming via NEXT actually advancing");
        for (int attempt = 1; attempt <= 3; attempt++) {
            WebElement suggestion = wait.until(ExpectedConditions.elementToBeClickable(RABANNE_SUGGESTION));
            org.openqa.selenium.Rectangle rect = suggestion.getRect();
            System.out.println("[ACTION] Tapping Rabanne suggestion at its live bounds "
                    + "(attempt " + attempt + "/3).");
            tapAtRealCoordinates(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);

            try {
                Thread.sleep(600); // brief settle before checking/tapping NEXT
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            try {
                clickButtonWithVerification(NEXT_BUTTON, "NEXT", 1);
                System.out.println("[PASS] NEXT advanced the screen - Rabanne selection is confirmed.");
                return;
            } catch (org.openqa.selenium.TimeoutException e) {
                System.out.println("[WARN] NEXT did not advance after selecting Rabanne (attempt "
                        + attempt + "/3) - the selection tap likely missed. Retrying both.");
            }
        }
        printAllVisibleTextAndDescs("Screen state after 3 failed Rabanne selection + NEXT attempts");
        throw new org.openqa.selenium.TimeoutException(
                "Could not select Rabanne and advance past this screen after 3 attempts.");
    }

    /**
     * Waits for this screen to have genuinely finished rendering - the
     * search field AND the NEXT/BACK navigation both present - rather than
     * proceeding the instant the field alone appears. A real run showed
     * the screen still loading (only the header text visible) when the
     * code raced ahead.
     */
    private void waitForScreenToSettle(int timeoutSeconds) {
        try {
            new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofMillis(400))
                    .until(d -> !d.findElements(SEARCH_FRAGRANCE_FIELD).isEmpty()
                            && !d.findElements(NEXT_BUTTON).isEmpty());
        } catch (org.openqa.selenium.TimeoutException e) {
            System.out.println("[WARN] Screen did not fully settle within " + timeoutSeconds
                    + "s - proceeding anyway, subsequent steps may be affected.");
        }
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
                    lines.add("content-desc: " + desc.trim() + " | selected=" + el.getAttribute("selected"));
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

    // ---------------------------------------------------------------
    // Step 9 - Tap "NEXT". FIXED: same timing-race pattern confirmed for
    // the "scentaddict"/"Fragrancematch" CTAs - a real run showed the
    // screen content completely unchanged after tapping NEXT (still
    // showing the same search-suggestions screen), meaning the tap landed
    // before the button was actually interactive yet. Since NEXT/DONE are
    // reused at nearly every step of this 24-step quiz, this is fixed
    // generically via clickButtonWithVerification() rather than patched
    // per call site.
    // ---------------------------------------------------------------
    public void clickNext() {
        clickButtonWithVerification(NEXT_BUTTON, "NEXT", 3);
    }

    // ---------------------------------------------------------------
    // Step 10 - Read the Similar/Different screen, select "Similar"
    // ---------------------------------------------------------------
    public void readAndSelectSimilar() {
        System.out.println("[STEP 10] Read Similar/Different screen, select 'Similar'");
        printCollected("Similar/Different screen", collectVisibleText());
        WebElement similar = wait.until(ExpectedConditions.elementToBeClickable(SIMILAR_OPTION));
        System.out.println("[ACTION] Tapping 'Similar'.");
        similar.click();
    }

    // ---------------------------------------------------------------
    // Step 12 - Read the Male/Female screen, select "Male"
    // ---------------------------------------------------------------
    public void readAndSelectMale() {
        System.out.println("[STEP 12] Read Male/Female screen, select 'Male'");
        printCollected("Male/Female screen", collectVisibleText());
        WebElement male = wait.until(ExpectedConditions.elementToBeClickable(MALE_OPTION));
        System.out.println("[ACTION] Tapping 'Male'.");
        male.click();
    }

    // ---------------------------------------------------------------
    // Step 14 - Read the time-of-day screen, select "Evenings"
    // ---------------------------------------------------------------
    public void readAndSelectEvenings() {
        System.out.println("[STEP 14] Read time-of-day screen, select 'Evenings'");
        printCollected("Time of day screen", collectVisibleText());
        WebElement evenings = wait.until(ExpectedConditions.elementToBeClickable(EVENINGS_OPTION));
        System.out.println("[ACTION] Tapping 'Evenings'.");
        evenings.click();
    }

    // ---------------------------------------------------------------
    // Step 16 - Read the age-range screen, select "31 - 40"
    // ---------------------------------------------------------------
    public void readAndSelectAgeRange() {
        System.out.println("[STEP 16] Read age-range screen, select '31 - 40'");
        printCollected("Age range screen", collectVisibleText());
        WebElement age = wait.until(ExpectedConditions.elementToBeClickable(AGE_31_40_OPTION));
        System.out.println("[ACTION] Tapping '31 - 40'.");
        age.click();
    }

    // ---------------------------------------------------------------
    // Step 17 - Read the price-range screen, drag both slider handles
    // ---------------------------------------------------------------
    public void readAndAdjustPriceRangeSlider() {
        System.out.println("[STEP 17] Read price-range screen, adjust the slider");
        printCollected("Price range screen", collectVisibleText());

        System.out.println("[ACTION] Dragging left slider handle inward.");
        dragBetweenScaledCoordinates(
                SLIDER_LEFT_HANDLE_X1, SLIDER_LEFT_HANDLE_Y1, SLIDER_LEFT_HANDLE_X2, SLIDER_LEFT_HANDLE_Y2,
                SLIDER_DRAG_DISTANCE, 0, REF_SCREEN_WIDTH, REF_SCREEN_HEIGHT);

        System.out.println("[ACTION] Dragging right slider handle inward.");
        dragBetweenScaledCoordinates(
                SLIDER_RIGHT_HANDLE_X1, SLIDER_RIGHT_HANDLE_Y1, SLIDER_RIGHT_HANDLE_X2, SLIDER_RIGHT_HANDLE_Y2,
                -SLIDER_DRAG_DISTANCE, 0, REF_SCREEN_WIDTH, REF_SCREEN_HEIGHT);
    }

    // ---------------------------------------------------------------
    // Step 18 - Tap "DONE" (same self-verifying fix as clickNext())
    // ---------------------------------------------------------------
    public void clickDone() {
        clickButtonWithVerification(DONE_BUTTON, "DONE", 3);
    }

    // ---------------------------------------------------------------
    // Step 19-20 - Read the email screen, enter email, tick "Yes, please"
    // ---------------------------------------------------------------
    public void enterEmailAndOptIn(String email) {
        System.out.println("[STEP 19] Read email screen content");
        printCollected("Email screen", collectVisibleText());

        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(EMAIL_FIELD));
        System.out.println("[ACTION] Entering email: " + email);
        emailField.click();
        emailField.sendKeys(email);

        System.out.println("[STEP 20] Tick the 'Yes, please' marketing checkbox");
        WebElement checkbox = wait.until(ExpectedConditions.elementToBeClickable(YES_PLEASE_CHECKBOX));
        System.out.println("[ACTION] Tapping 'Yes, please' checkbox.");
        checkbox.click();
    }

    // ---------------------------------------------------------------
    // Step 21 - Dismiss the on-screen keyboard.
    // The captured locator was a mis-captured full-screen container (same
    // recurring pattern seen elsewhere in this project) - using the
    // Android back key instead, which reliably dismisses the keyboard
    // without navigating away from the current screen.
    // ---------------------------------------------------------------
    public void closeKeyboard() {
        System.out.println("[STEP 21] Close the keyboard");
        try {
            driver.pressKey(new KeyEvent(AndroidKey.BACK));
            System.out.println("[ACTION] Sent Android BACK key to dismiss the keyboard.");
        } catch (Exception e) {
            System.out.println("[WARN] Could not dismiss keyboard: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Step 22 - Tap "VIEW MATCHES".
    //
    // FIXED (this was the actual root cause of "app closes without ever
    // reaching the matches screen"): this was still using an unverified
    // WebElement.click() - the exact same broken pattern already proven
    // unreliable for NEXT/DONE/the Rabanne suggestion elsewhere in this
    // file. A real run showed the screen after tapping VIEW MATCHES was
    // identical to the email screen still - the tap never registered, so
    // the flow proceeded straight to closing the app without ever
    // genuinely reaching the results screen. Now uses the same
    // clickButtonWithVerification() (raw coordinate tap + content-change
    // check + retry) already proven reliable for every other transition
    // in this quiz.
    // ---------------------------------------------------------------
    public void clickViewMatches() {
        System.out.println("[STEP 22] Select 'VIEW MATCHES'");
        clickButtonWithVerification(VIEW_MATCHES_BUTTON, "VIEW MATCHES", 3);
    }

    // ---------------------------------------------------------------
    // Step 23 - Read the results screen in full, and scroll its product
    // carousel both directions.
    //
    // NOTE - HONESTLY FLAGGED: because clickViewMatches() never actually
    // worked in any run until the fix above, this screen's real content
    // and structure have never been captured. The full-page vertical read
    // below is the same technique already proven working elsewhere, so
    // it's reliable regardless of this screen's specific layout. The
    // horizontal carousel scroll is a best-effort addition using a
    // generic screen-region drag (NOT anchored to any confirmed heading
    // text, since none is known yet) - if the console output from this
    // run shows a specific heading for the matches/product section, that
    // should be used to properly anchor this the way DiscoverMorePage.java
    // does, rather than this generic region guess.
    // ---------------------------------------------------------------
    // ---------------------------------------------------------------
    // Step 23 - Read the results screen in full, and scroll its product
    // carousel both directions.
    //
    // FIXED: a real run confirmed clickViewMatches() now genuinely works
    // (content did change), but what we landed on and immediately read was
    // just 3 lines - "Fragrance Match" plus an inspirational quote/
    // attribution - and the page wasn't even scrollable on the first
    // attempt. That's consistent with a loading/interstitial screen shown
    // while the real personalized matches are still being fetched (likely
    // an async API call), not the actual results content. Same pattern as
    // the earlier "search for a fragrance" screen being read before it had
    // finished rendering. Now waits for the visible content to keep
    // growing beyond that initial quote screen before reading, rather than
    // reading the instant ANY change is detected.
    // ---------------------------------------------------------------
    public void readResultsAndScrollMatchesCarousel() {
        System.out.println("[STEP 23] Read Fragrance Match results screen, scrolling to capture full content");

        waitForResultsToFinishLoading(15);

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
        printCollected("Fragrance Match results screen (full)", seen);

        System.out.println("[ACTION] Attempting to scroll the product carousel both directions "
                + "(best-effort - no confirmed anchor/locator for this section yet).");
        for (int i = 0; i < 3; i++) {
            dragGenericCarouselRegion("left");
        }
        for (int i = 0; i < 3; i++) {
            dragGenericCarouselRegion("right");
        }
    }

    /**
     * Drags a finger horizontally across a generic mid-screen region - used
     * only as a best-effort attempt for the results screen's product
     * carousel, since (unlike DiscoverMorePage.java's carousel, which has a
     * confirmed heading to anchor to) this screen's real structure has
     * never been captured in a successful run. Replace with a properly
     * anchored version once the actual heading/section is confirmed.
     */
    /**
     * Waits for the results screen's visible content to keep growing
     * beyond whatever is showing right after VIEW MATCHES advances (a
     * real run showed just a 3-line quote/attribution at that point) -
     * covers an async fetch of the actual personalized matches. If content
     * never grows within the timeout, proceeds anyway and logs that this
     * may genuinely be the full screen rather than still-loading.
     */
    private void waitForResultsToFinishLoading(int timeoutSeconds) {
        int initialCount = collectVisibleText().size();
        System.out.println("[ACTION] Waiting for results content to finish loading "
                + "(currently " + initialCount + " line(s) visible).");
        try {
            new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofMillis(500))
                    .until(d -> collectVisibleText().size() > initialCount);
            System.out.println("[PASS] Additional results content loaded ("
                    + collectVisibleText().size() + " line(s) now visible).");
        } catch (org.openqa.selenium.TimeoutException e) {
            System.out.println("[WARN] Results content did not grow beyond " + initialCount
                    + " line(s) within " + timeoutSeconds + "s - proceeding with what's currently "
                    + "visible (this may genuinely be the full screen, or the matches API may not "
                    + "have returned any results for this account/quiz combination).");
        }
    }

    private void dragGenericCarouselRegion(String direction) {
        org.openqa.selenium.Dimension screenSize = driver.manage().window().getSize();
        int centerY = (int) (screenSize.height * 0.55);
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

    // ---------------------------------------------------------------
    // Step 24 - Tap Back, close the app
    // ---------------------------------------------------------------
    public void clickBack() {
        System.out.println("[STEP 24] Tap 'Back' to return to the previous screen");
        if (isDisplayedSafely(GO_BACK)) {
            WebElement back = wait.until(ExpectedConditions.elementToBeClickable(GO_BACK));
            System.out.println("[ACTION] Tapping 'Go back'.");
            back.click();
            return;
        }
        System.out.println("[WARN] 'Go back' accessibility id not found - falling back to Android back button.");
        driver.navigate().back();
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
     * Taps a button (by accessibility id) and verifies the screen's visible
     * text actually changed afterward - not just that the tap didn't throw.
     * Retries up to maxAttempts times if the content is unchanged, since
     * this app has repeatedly shown a timing race where a tap lands before
     * the target is truly interactive, silently doing nothing. Used for
     * "NEXT"/"DONE", which are reused at nearly every step of this quiz.
     */
    // NOTE: switched from button.click() to a raw coordinate tap - .click()
    // has never once been confirmed working for NEXT in any run's logs so
    // far, and raw coordinate taps are the technique proven reliable
    // elsewhere in this app's custom touchables (scentaddict/Fragrancematch
    // CTAs, the Rabanne suggestion). Removing this as a possible variable.
    private void clickButtonWithVerification(By locator, String label, int maxAttempts) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            List<String> before = collectVisibleText();
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(locator));
            org.openqa.selenium.Rectangle rect = button.getRect();
            System.out.println("[ACTION] Tapping '" + label + "' at its live bounds "
                    + "(attempt " + attempt + "/" + maxAttempts + ").");
            tapAtRealCoordinates(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);

            if (waitForContentChange(before, 5)) {
                System.out.println("[PASS] Screen advanced after tapping '" + label + "'.");
                return;
            }
            System.out.println("[WARN] Screen content unchanged after tapping '" + label + "' (attempt "
                    + attempt + "/" + maxAttempts + ") - the tap likely landed before the button was "
                    + "interactive yet. Retrying.");
        }
        throw new org.openqa.selenium.TimeoutException(
                "'" + label + "' tap did not advance the screen after " + maxAttempts + " attempts.");
    }

    private boolean waitForContentChange(List<String> before, int timeoutSeconds) {
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
        for (int i = 0; i < 6; i++) {
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
     * Taps a real, already-actual-device screen coordinate (no reference
     * scaling needed, unlike tapAtScaledCoordinates() below) - used when
     * the coordinate comes from an element's live getRect() rather than a
     * captured reference bounds. Raw PointerInput tap, not
     * WebElement.click(), matching the technique proven reliable elsewhere
     * in this app for custom touchable components.
     */
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

    /**
     * Drags a finger from the center of a reference-bounds rectangle by a
     * given reference-pixel delta, both scaled to the actual device screen.
     * Used for the price-range slider handles, which are non-clickable SVG
     * thumbs requiring a real drag gesture rather than a tap.
     */
    private void dragBetweenScaledCoordinates(int refX1, int refY1, int refX2, int refY2,
                                              int deltaXRef, int deltaYRef,
                                              int refWidth, int refHeight) {
        org.openqa.selenium.Dimension screenSize = driver.manage().window().getSize();
        double xRatio = (double) screenSize.width / refWidth;
        double yRatio = (double) screenSize.height / refHeight;

        int startX = (int) Math.round(((refX1 + refX2) / 2.0) * xRatio);
        int startY = (int) Math.round(((refY1 + refY2) / 2.0) * yRatio);
        int endX = (int) Math.round((((refX1 + refX2) / 2.0) + deltaXRef) * xRatio);
        int endY = (int) Math.round((((refY1 + refY2) / 2.0) + deltaYRef) * yRatio);

        org.openqa.selenium.interactions.PointerInput finger =
                new org.openqa.selenium.interactions.PointerInput(
                        org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
        org.openqa.selenium.interactions.Sequence drag =
                new org.openqa.selenium.interactions.Sequence(finger, 0);
        drag.addAction(finger.createPointerMove(Duration.ZERO,
                org.openqa.selenium.interactions.PointerInput.Origin.viewport(), startX, startY));
        drag.addAction(finger.createPointerDown(
                org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
        drag.addAction(finger.createPointerMove(Duration.ofMillis(300),
                org.openqa.selenium.interactions.PointerInput.Origin.viewport(), endX, endY));
        drag.addAction(finger.createPointerUp(
                org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(java.util.Collections.singletonList(drag));
        System.out.println("[ACTION] Dragged from (" + startX + "," + startY + ") to (" + endX + "," + endY + ").");
    }

    /**
     * Drags a finger horizontally across a carousel area, computed from a
     * given anchor element's current on-screen position - same technique
     * proven reliable in DiscoverMorePage.java for this app's custom
     * horizontal lists (mobile:scrollGesture was found unreliable for
     * these; a raw touch drag works regardless of whether Appium correctly
     * identifies the underlying widget as "scrollable").
     */
    private void dragCarouselOnce(By anchorLocator, String direction) {
        List<WebElement> anchors = driver.findElements(anchorLocator);
        org.openqa.selenium.Dimension screenSize = driver.manage().window().getSize();

        int centerY;
        if (!anchors.isEmpty()) {
            org.openqa.selenium.Rectangle r = anchors.get(0).getRect();
            centerY = r.getY() + r.getHeight() + 300;
        } else {
            centerY = (int) (screenSize.height * 0.5);
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
}