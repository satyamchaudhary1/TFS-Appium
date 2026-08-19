package com.tfs.pages;

import com.tfs.driver.DriverManager;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomePage extends BasePage {

    private static final String HOME_PAGE_BUILD =
            "TC006_B07_KEYBOARD_BACKSPACE_FIX_V9_2026_08_16";

    private final AndroidDriver appDriver;

    private static final int MAX_BANNERS = 10;

    private final List<String> detectedBannerSignatures =
            new ArrayList<>();

    private boolean bannerCycleVerified = false;


    // ==========================================================
    // LOCATORS
    // ==========================================================

    private static final By IMAGE_VIEWS =
            AppiumBy.className(
                    "android.widget.ImageView"
            );

    private static final By TEXT_VIEWS =
            AppiumBy.className(
                    "android.widget.TextView"
            );

    private static final By EDIT_TEXT_FIELDS =
            AppiumBy.className(
                    "android.widget.EditText"
            );


    /*
     * Exact Search input from Appium Inspector / page hierarchy:
     * class      = android.widget.EditText
     * index      = 1
     * hint       = Search...
     * clickable  = true
     * focusable  = true
     * bounds     = [144,74][603,158]
     *
     * Do NOT include text="Search..." in the locator because the text
     * changes to the entered query while searching.
     */
    private static final By SEARCH_INPUT_EXACT =
            AppiumBy.xpath(
                    "//android.widget.EditText[@index='1' and @hint='Search...' and @clickable='true' and @focusable='true' and @bounds='[144,74][603,158]']"
            );

    private static final By SEARCH_ACCESSIBILITY =
            AppiumBy.accessibilityId(
                    "Search"
            );

    private static final By SEARCH_DESCRIPTION =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().descriptionContains(\"Search\")"
            );

    /*
     * Exact Search Clear/X element from Appium Inspector:
     * class      = android.view.ViewGroup
     * index      = 2
     * clickable  = true
     * focusable  = true
     * bounds     = [603,106][624,127]
     *
     * No resource-id, text, or content-desc is exposed.
     */
    private static final By SEARCH_CLEAR_X =
            AppiumBy.xpath(
                    "//android.view.ViewGroup[@index='2' and @clickable='true' and @focusable='true' and @bounds='[603,106][624,127]']"
            );


    // ==========================================================
    // CONSTRUCTOR
    // ==========================================================

    public HomePage() {

        super();

        this.appDriver =
                DriverManager.getDriver();

        System.out.println();
        System.out.println(
                "[HOME PAGE BUILD] "
                        + HOME_PAGE_BUILD
        );
    }


    // ==========================================================
    // EXISTING METHOD - KEEP
    // ==========================================================

    public boolean isHomeDisplayed() {

        /*
         * METHOD 1:
         * Keep the existing framework locator for backward compatibility.
         */
        try {

            if (isDisplayed(
                    "home.screen"
            )) {

                return true;
            }

        } catch (Exception ignored) {
        }


        /*
         * METHOD 2:
         * The current APK exposes the Home promotional area as a large
         * android.widget.ImageView. If that banner is visible, the Home
         * screen is considered ready even when the old home.screen locator
         * is not exposed by the current UI hierarchy.
         */
        WebElement banner =
                findMainBanner();


        return banner != null;
    }


    // ==========================================================
    // COMPATIBILITY METHOD USED BY TC_006
    // ==========================================================

    public boolean isHomeScreenDisplayed() {

        System.out.println();
        System.out.println(
                "[STEP] Checking Home screen."
        );


        long endTime =
                System.currentTimeMillis()
                        + 30000;


        int attempt =
                0;


        while (System.currentTimeMillis()
                < endTime) {

            attempt++;


            boolean legacyHomeLocator =
                    false;


            try {

                legacyHomeLocator =
                        isDisplayed(
                                "home.screen"
                        );

            } catch (Exception ignored) {
            }


            WebElement banner =
                    findMainBanner();


            boolean bannerDisplayed =
                    banner != null;


            if (legacyHomeLocator
                    || bannerDisplayed) {

                System.out.println(
                        "[PASS] Home screen displayed."
                );


                System.out.println(
                        "[HOME] Existing home.screen locator : "
                                + (
                                legacyHomeLocator
                                        ? "FOUND"
                                        : "NOT FOUND"
                        )
                );


                System.out.println(
                        "[HOME] Promotional Banner           : "
                                + (
                                bannerDisplayed
                                        ? "FOUND"
                                        : "NOT FOUND"
                        )
                );


                if (bannerDisplayed) {

                    try {

                        System.out.println(
                                "[HOME] Banner Class                : "
                                        + safeClassAttribute(banner)
                        );


                        System.out.println(
                                "[HOME] Banner Displayed            : "
                                        + banner.isDisplayed()
                        );


                        System.out.println(
                                "[HOME] Banner Bounds               : "
                                        + banner.getRect()
                        );

                    } catch (Exception ignored) {
                    }
                }


                System.out.println(
                        "[HOME] Detection Attempt           : "
                                + attempt
                );


                System.out.println(
                        "[RESULT] Home Screen Displayed : PASS"
                );


                return true;
            }


            pause(
                    500
            );
        }


        System.out.println();
        System.out.println(
                "[FAIL] Home screen was not detected within 30 seconds."
        );


        boolean finalLegacyStatus =
                false;


        try {

            finalLegacyStatus =
                    isDisplayed(
                            "home.screen"
                    );

        } catch (Exception ignored) {
        }


        WebElement finalBanner =
                findMainBanner();


        System.out.println(
                "[DEBUG] home.screen locator : "
                        + finalLegacyStatus
        );


        System.out.println(
                "[DEBUG] Promotional Banner  : "
                        + (
                        finalBanner != null
                )
        );


        try {

            Dimension screen =
                    appDriver
                            .manage()
                            .window()
                            .getSize();


            System.out.println(
                    "[DEBUG] Screen Size          : "
                            + screen.getWidth()
                            + " x "
                            + screen.getHeight()
            );

        } catch (Exception ignored) {
        }


        printVisibleTextDiagnostics();


        System.out.println(
                "[RESULT] Home Screen Displayed : FAIL"
        );


        return false;
    }


    // ==========================================================
    // PROMOTIONAL BANNER DISPLAY
    // ==========================================================

    public boolean isPromotionalBannerDisplayed() {

        System.out.println(
                "[STEP] Checking Promotional Banner."
        );

        System.out.println(
                "[EXPECTED] A visible Home promotional banner should be present."
        );

        Rectangle rect =
                getFreshBannerRectangle(
                        20
                );

        if (rect == null) {

            System.out.println(
                    "[ACTUAL] Promotional Banner : NOT FOUND"
            );

            System.out.println(
                    "[RESULT] Promotional Banner Displayed : FAIL"
            );

            return false;
        }

        System.out.println(
                "[ACTUAL] Promotional Banner : FOUND"
        );

        System.out.println(
                "[INFO] Banner Class         : android.widget.ImageView"
        );

        System.out.println(
                "[INFO] Banner Bounds        : "
                        + rectangleToString(
                        rect
                )
        );

        System.out.println(
                "[RESULT] Promotional Banner Displayed : PASS"
        );

        return true;
    }


    // ==========================================================
    // AUTOMATIC BANNER MOVEMENT
    // ==========================================================

    public boolean verifyAutomaticBannerMovement() {

        System.out.println();
        System.out.println(
                "[STEP] Checking automatic Promotional Banner movement."
        );

        System.out.println(
                "[EXPECTED] Banner visual should change automatically without a manual swipe."
        );

        String initialSignature =
                captureStableBannerSignature(
                        10
                );

        if (initialSignature == null) {

            System.out.println(
                    "[ACTUAL] Initial stable banner signature : NOT AVAILABLE"
            );

            System.out.println(
                    "[RESULT] Automatic Banner Movement : FAIL"
            );

            return false;
        }

        System.out.println(
                "[INFO] Initial Banner Signature : "
                        + initialSignature
        );

        System.out.println(
                "[ACTION] Waiting for automatic banner movement."
        );

        String currentSignature =
                waitForStableBannerChange(
                        initialSignature,
                        20
                );

        if (currentSignature == null) {

            System.out.println(
                    "[ACTUAL] Banner signature did not change within timeout."
            );

            System.out.println(
                    "[RESULT] Automatic Banner Movement : FAIL"
            );

            return false;
        }

        System.out.println(
                "[INFO] Previous Signature : "
                        + initialSignature
        );

        System.out.println(
                "[INFO] Current Signature  : "
                        + currentSignature
        );

        System.out.println(
                "[PASS] Promotional Banner changed automatically."
        );

        System.out.println(
                "[RESULT] Automatic Banner Movement : PASS"
        );

        return true;
    }


    // ==========================================================
    // DYNAMIC BANNER COUNT + CYCLIC ORDER
    // ==========================================================

    public int detectBannerCountAndVerifyCycle() {

        System.out.println();
        System.out.println(
                "[STEP] Detecting Promotional Banner count dynamically."
        );

        System.out.println(
                "[EXPECTED] Count must come from the current carousel. No fixed count is used."
        );

        detectedBannerSignatures.clear();
        bannerCycleVerified = false;

        String firstSignature =
                captureStableBannerSignature(
                        12
                );

        if (firstSignature == null) {

            throw new AssertionError(
                    "Unable to capture stable signature for Banner 1."
            );
        }

        detectedBannerSignatures.add(
                firstSignature
        );

        System.out.println(
                "[BANNER 1] Detected"
        );

        System.out.println(
                "[BANNER 1] Signature : "
                        + firstSignature
        );

        String currentSignature =
                firstSignature;

        int safetyAttempts =
                Math.max(
                        MAX_BANNERS * 2,
                        12
                );

        for (int attempt = 1;
             attempt <= safetyAttempts;
             attempt++) {

            System.out.println(
                    "[ACTION] Swipe attempt "
                            + attempt
                            + " - moving to next banner."
            );

            swipeBannerLeft();

            String nextSignature =
                    waitForStableBannerChange(
                            currentSignature,
                            10
                    );

            if (nextSignature == null) {

                System.out.println(
                        "[INFO] No stable different banner detected after swipe attempt "
                                + attempt
                                + "."
                );

                break;
            }

            System.out.println(
                    "[INFO] Previous Banner Signature : "
                            + currentSignature
            );

            System.out.println(
                    "[INFO] New Banner Signature      : "
                            + nextSignature
            );

            if (nextSignature.equals(
                    firstSignature
            )) {

                bannerCycleVerified = true;

                System.out.println(
                        "[PASS] Banner carousel returned to Banner 1."
                );

                break;
            }

            if (detectedBannerSignatures.contains(
                    nextSignature
            )) {

                System.out.println(
                        "[INFO] Signature already detected. It will not be counted twice."
                );

                currentSignature =
                        nextSignature;

                continue;
            }

            detectedBannerSignatures.add(
                    nextSignature
            );

            System.out.println(
                    "[BANNER "
                            + detectedBannerSignatures.size()
                            + "] Detected"
            );

            System.out.println(
                    "[BANNER "
                            + detectedBannerSignatures.size()
                            + "] Signature : "
                            + nextSignature
            );

            currentSignature =
                    nextSignature;
        }

        System.out.println();
        System.out.println(
                "[INFO] Unique Banner Signatures:"
        );

        for (int i = 0;
             i < detectedBannerSignatures.size();
             i++) {

            System.out.println(
                    "[INFO] Banner "
                            + (i + 1)
                            + " = "
                            + detectedBannerSignatures.get(i)
            );
        }

        System.out.println(
                "[ACTUAL] Dynamic Promotional Banner Count : "
                        + detectedBannerSignatures.size()
        );

        System.out.println(
                "[ACTUAL] Carousel Returned To First Banner : "
                        + (bannerCycleVerified ? "YES" : "NO")
        );

        System.out.println(
                "[RESULT] Cyclic Banner Rotation : "
                        + (bannerCycleVerified ? "PASS" : "FAIL")
        );

        return detectedBannerSignatures.size();
    }


    public boolean wasLastCycleVerified() {

        return bannerCycleVerified;
    }


    // ==========================================================
    // VALIDATE NAVIGATION FOR ALL BANNERS
    // ==========================================================

    public void validateEveryBannerNavigation() {

        if (detectedBannerSignatures.isEmpty()) {

            throw new AssertionError(
                    "Banner detection must run before navigation validation."
            );
        }

        System.out.println();
        System.out.println(
                "[STEP] Validating navigation for every detected banner."
        );

        System.out.println(
                "[INFO] Banners to validate : "
                        + detectedBannerSignatures.size()
        );

        for (int i = 0;
             i < detectedBannerSignatures.size();
             i++) {

            int bannerNumber =
                    i + 1;

            String expectedSignature =
                    detectedBannerSignatures.get(
                            i
                    );

            System.out.println();
            System.out.println(
                    "=================================================="
            );

            System.out.println(
                    "BANNER "
                            + bannerNumber
                            + " NAVIGATION"
            );

            System.out.println(
                    "=================================================="
            );

            System.out.println(
                    "[EXPECTED] Banner Signature : "
                            + expectedSignature
            );

            boolean positioned =
                    moveToBanner(
                            expectedSignature
                    );

            System.out.println(
                    "[ACTUAL] Banner Positioned : "
                            + (positioned ? "YES" : "NO")
            );

            if (!positioned) {

                throw new AssertionError(
                        "Unable to position carousel on Banner "
                                + bannerNumber
                                + " with signature "
                                + expectedSignature
                );
            }

            System.out.println(
                    "[PASS] Banner "
                            + bannerNumber
                            + " displayed."
            );

            String beforeSignature =
                    getPageSignature();

            System.out.println(
                    "[INFO] Page Signature Before CTA : "
                            + beforeSignature
            );

            System.out.println(
                    "[ACTION] Opening Banner "
                            + bannerNumber
                            + " CTA / Shop Now."
            );

            tapCurrentBannerCTA();

            boolean destinationOpened =
                    waitForBannerDestination(beforeSignature);

            System.out.println(
                    "[ACTUAL] Destination Opened : "
                            + (destinationOpened ? "YES" : "NO")
            );

            if (!destinationOpened) {

                throw new AssertionError(
                        "Banner "
                                + bannerNumber
                                + " did not open its destination."
                );
            }

            System.out.println(
                    "[PASS] Banner "
                            + bannerNumber
                            + " destination opened."
            );

            System.out.println(
                    "[DESTINATION] "
                            + getVisibleScreenText()
            );

            navigateBackToHome();

            boolean homeDisplayed =
                    isHomeDisplayed();

            System.out.println(
                    "[ACTUAL] Home Displayed After Return : "
                            + (homeDisplayed ? "YES" : "NO")
            );

            if (!homeDisplayed) {

                throw new AssertionError(
                        "Home screen was not displayed after Banner "
                                + bannerNumber
                );
            }

            System.out.println(
                    "[PASS] Returned to Home after Banner "
                            + bannerNumber
                            + "."
            );
        }
    }


    // ==========================================================
    // OPEN SEARCH
    // ==========================================================

    public void openSearch() {

        System.out.println(
                "[ACTION] Opening Home Page Search."
        );

        if (clickIfDisplayed(
                SEARCH_ACCESSIBILITY
        )) {

            if (waitForSearchInput(
                    8
            )) {

                System.out.println(
                        "[PASS] Search opened using accessibility locator."
                );

                return;
            }
        }

        if (clickIfDisplayed(
                SEARCH_DESCRIPTION
        )) {

            if (waitForSearchInput(
                    8
            )) {

                System.out.println(
                        "[PASS] Search opened using description locator."
                );

                return;
            }
        }

        /*
         * Temporary fallback until exact Inspector locator
         * for Search icon is available.
         */
        Dimension screen =
                appDriver.manage()
                        .window()
                        .getSize();

        int x =
                (int) (
                        screen.getWidth()
                                * 0.90
                );

        int y =
                (int) (
                        screen.getHeight()
                                * 0.07
                );

        System.out.println(
                "[INFO] Exact Search locator not available."
        );

        System.out.println(
                "[INFO] Search fallback tap : "
                        + x
                        + ", "
                        + y
        );

        appDriver.executeScript(
                "mobile: clickGesture",
                Map.of(
                        "x", x,
                        "y", y
                )
        );

        if (!waitForSearchInput(
                10
        )) {

            printVisibleTextDiagnostics();

            throw new AssertionError(
                    "Search screen did not open."
            );
        }

        System.out.println(
                "[PASS] Search screen opened."
        );
    }


    // ==========================================================
    // SEARCH SCREEN DISPLAYED
    // ==========================================================

    public boolean isSearchScreenDisplayed() {

        boolean displayed =
                waitForSearchInput(
                        15
                );

        System.out.println(
                "[RESULT] Search Screen Displayed : "
                        + (displayed ? "PASS" : "FAIL")
        );

        return displayed;
    }


    // ==========================================================
    // CLEAR SEARCH DIRECTLY
    // Used only to reset data between case-insensitivity checks.
    // ==========================================================

    public void clearSearchFieldDirectly() {

        System.out.println(
                "[ACTION] Resetting Search field directly."
        );

        WebElement input =
                getSearchInput();

        String beforeValue;

        try {

            beforeValue =
                    input.getText();

        } catch (Exception e) {

            beforeValue =
                    "<UNAVAILABLE>";
        }

        System.out.println(
                "[INFO] Search value before reset : "
                        + beforeValue
        );

        input.click();

        try {

            input.clear();

        } catch (Exception e) {

            throw new AssertionError(
                    "Unable to reset Search field.",
                    e
            );
        }

        pause(
                300
        );

        String afterValue;

        try {

            afterValue =
                    getSearchInput()
                            .getText();

        } catch (Exception e) {

            afterValue =
                    "<UNAVAILABLE>";
        }

        System.out.println(
                "[INFO] Search value after reset  : "
                        + afterValue
        );

        System.out.println(
                "[PASS] Search field reset completed."
        );
    }


    // ==========================================================
    // ENTER SEARCH TEXT
    // ==========================================================

    public void enterSearchText(
            String value
    ) {

        System.out.println(
                "[ACTION] Entering Search value: "
                        + value
        );

        WebElement input =
                getSearchInput();

        input.click();

        try {

            input.clear();

        } catch (Exception ignored) {
        }

        input.sendKeys(
                value
        );

        pause(
                700
        );

        String actualValue =
                input.getText();

        System.out.println(
                "[INFO] Search Input Value : "
                        + actualValue
        );

        if (actualValue.isBlank()) {

            throw new AssertionError(
                    "Search value was not entered."
            );
        }

        System.out.println(
                "[PASS] Search value entered."
        );
    }


    // ==========================================================
    // RELEVANT AUTO-SUGGESTIONS
    // ==========================================================

    public boolean areRelevantSuggestionsDisplayed(
            String keyword
    ) {

        System.out.println(
                "[STEP] Waiting for relevant Search suggestions for: "
                        + keyword
        );

        long endTime =
                System.currentTimeMillis()
                        + 20000;

        while (System.currentTimeMillis()
                < endTime) {

            List<String> suggestions =
                    getRelevantSuggestionTexts(
                            keyword
                    );

            if (!suggestions.isEmpty()) {

                System.out.println(
                        "[PASS] Relevant Search suggestions displayed."
                );

                System.out.println(
                        "[INFO] Suggestion Count : "
                                + suggestions.size()
                );

                for (int i = 0;
                     i < suggestions.size();
                     i++) {

                    System.out.println(
                            "[SUGGESTION "
                                    + (i + 1)
                                    + "] "
                                    + suggestions.get(i)
                    );
                }

                return true;
            }

            pause(
                    400
            );
        }

        System.out.println(
                "[FAIL] Relevant Search suggestions were not displayed."
        );

        return false;
    }


    // ==========================================================
    // SELECT FIRST RELEVANT SUGGESTION
    // ==========================================================

    public String selectFirstRelevantSuggestion(
            String keyword
    ) {

        System.out.println(
                "[ACTION] Selecting first relevant suggestion for: "
                        + keyword
        );

        WebElement input =
                getSearchInput();

        int inputBottom =
                input.getRect().getY()
                        + input.getRect().getHeight();

        List<WebElement> candidates =
                appDriver.findElements(
                        TEXT_VIEWS
                );

        WebElement bestCandidate = null;

        int bestY =
                Integer.MAX_VALUE;

        for (WebElement element : candidates) {

            try {

                String text =
                        element.getText();

                if (!element.isDisplayed()
                        ||
                        text.isBlank()
                        ||
                        !text.toLowerCase()
                                .contains(
                                        keyword.toLowerCase()
                                )) {

                    continue;
                }

                int y =
                        element.getRect()
                                .getY();

                if (y <= inputBottom) {
                    continue;
                }

                if (y < bestY) {

                    bestY =
                            y;

                    bestCandidate =
                            element;
                }

            } catch (Exception ignored) {
            }
        }

        if (bestCandidate == null) {

            printVisibleTextDiagnostics();

            throw new AssertionError(
                    "Relevant Search suggestion was not found for: "
                            + keyword
            );
        }

        String selectedText =
                safeText(
                        bestCandidate
                );

        System.out.println(
                "[INFO] Suggestion Selected : "
                        + selectedText
        );

        String before =
                getPageSignature();

        tapElementCenter(
                bestCandidate
        );

        if (pageChangeTimedOut(before)) {

            throw new AssertionError(
                    "Suggestion was selected but page did not change."
            );
        }

        System.out.println(
                "[PASS] Suggestion selected successfully."
        );

        return selectedText;
    }


    // ==========================================================
    // SEARCH RESULTS / PLP
    // ==========================================================

    public boolean isSearchResultsDisplayed(
            String keyword
    ) {

        System.out.println(
                "[STEP] Checking Search Results / PLP."
        );

        long endTime =
                System.currentTimeMillis()
                        + 20000;

        while (System.currentTimeMillis()
                < endTime) {

            try {

                String source =
                        appDriver.getPageSource()
                                .toLowerCase();

                boolean keywordPresent =
                        source.contains(
                                keyword.toLowerCase()
                        );

                boolean resultMarker =
                        source.contains(
                                "filter"
                        )
                                ||
                                source.contains(
                                        "sort"
                                )
                                ||
                                source.contains(
                                        "results"
                                )
                                ||
                                source.contains(
                                        "product"
                                );

                if (keywordPresent
                        && resultMarker) {

                    System.out.println(
                            "[PASS] Search Results / PLP displayed."
                    );

                    return true;
                }

            } catch (Exception ignored) {
            }

            pause(
                    500
            );
        }

        System.out.println(
                "[FAIL] Search Results / PLP was not detected."
        );

        printVisibleTextDiagnostics();

        return false;
    }


    // ==========================================================
    // RELEVANT PRODUCT COUNT
    // ==========================================================

    public int getRelevantProductCount(
            String keyword
    ) {

        Set<String> products =
                new LinkedHashSet<>();

        String lowerKeyword =
                keyword.toLowerCase();

        for (WebElement element :
                appDriver.findElements(
                        TEXT_VIEWS
                )) {

            try {

                String text =
                        element.getText();

                if (element.isDisplayed()
                        &&
                        !text.isBlank()
                        &&
                        text.toLowerCase()
                                .contains(
                                        lowerKeyword
                                )) {

                    products.add(
                            text.trim()
                    );
                }

            } catch (Exception ignored) {
            }
        }

        System.out.println(
                "[INFO] Relevant Product Count : "
                        + products.size()
        );

        int index = 1;

        for (String product : products) {

            System.out.println(
                    "[PRODUCT "
                            + index
                            + "] "
                            + product
            );

            index++;
        }

        return products.size();
    }


    // ==========================================================
    // OPEN FIRST RELEVANT PRODUCT
    // ==========================================================

    public String openFirstRelevantProduct(
            String keyword
    ) {

        System.out.println(
                "[ACTION] Opening first relevant product."
        );

        Dimension screen =
                appDriver.manage()
                        .window()
                        .getSize();

        WebElement candidate = null;

        int bestY =
                Integer.MAX_VALUE;

        for (WebElement element :
                appDriver.findElements(
                        TEXT_VIEWS
                )) {

            try {

                String text =
                        element.getText();

                if (!element.isDisplayed()
                        ||
                        !text.toLowerCase()
                                .contains(
                                        keyword.toLowerCase()
                                )) {

                    continue;
                }

                Rectangle rect =
                        element.getRect();

                /*
                 * Ignore page/header text.
                 */
                if (rect.getY()
                        < screen.getHeight()
                        * 0.20) {

                    continue;
                }

                if (rect.getY()
                        < bestY) {

                    bestY =
                            rect.getY();

                    candidate =
                            element;
                }

            } catch (Exception ignored) {
            }
        }

        if (candidate == null) {

            printVisibleTextDiagnostics();

            throw new AssertionError(
                    "Relevant product was not found."
            );
        }

        String productName =
                safeText(
                        candidate
                );

        System.out.println(
                "[INFO] Product Selected : "
                        + productName
        );

        String before =
                getPageSignature();

        tapElementCenter(
                candidate
        );

        if (pageChangeTimedOut(before)) {

            throw new AssertionError(
                    "Product was tapped but navigation did not occur."
            );
        }

        System.out.println(
                "[PASS] Product navigation completed."
        );

        return productName;
    }


    // ==========================================================
    // PDP VALIDATION
    // ==========================================================

    public boolean isProductDetailPageDisplayed() {

        System.out.println(
                "[STEP] Checking Product Detail Page."
        );

        long endTime =
                System.currentTimeMillis()
                        + 20000;

        while (System.currentTimeMillis()
                < endTime) {

            try {

                String source =
                        appDriver.getPageSource()
                                .toLowerCase();

                if (source.contains(
                        "add to bag"
                )
                        ||
                        source.contains(
                                "add to basket"
                        )
                        ||
                        source.contains(
                                "product details"
                        )
                        ||
                        source.contains(
                                "description"
                        )) {

                    System.out.println(
                            "[PASS] Product Detail Page displayed."
                    );

                    System.out.println(
                            "[PDP] "
                                    + getVisibleScreenText()
                    );

                    return true;
                }

            } catch (Exception ignored) {
            }

            pause(
                    500
            );
        }

        System.out.println(
                "[FAIL] Product Detail Page was not detected."
        );

        printVisibleTextDiagnostics();

        return false;
    }


    // ==========================================================
    // NAVIGATE BACK HOME
    // ==========================================================

    public void navigateBackToHome() {

        System.out.println(
                "[ACTION] Returning to Home screen."
        );

        for (int attempt = 1;
             attempt <= 6;
             attempt++) {

            if (isHomeDisplayed()) {

                System.out.println(
                        "[PASS] Home screen reached."
                );

                return;
            }

            appDriver.navigate()
                    .back();

            pause(
                    1000
            );
        }

        if (!isHomeDisplayed()) {

            throw new AssertionError(
                    "Unable to return to Home screen."
            );
        }

        System.out.println(
                "[PASS] Home screen reached."
        );
    }


    // ==========================================================
    // CLEAR / X SEARCH FUNCTIONALITY
    // ==========================================================





    public boolean isSearchFieldEmpty() {

        WebElement input =
                findSearchInputNow();

        if (input == null) {

            System.out.println(
                    "[CHECK] Search Field : temporarily not found - retry required"
            );

            return false;
        }

        try {

            String value =
                    input.getText();

            String hint =
                    input.getAttribute(
                            "hint"
                    );

            String normalizedValue =
                    value == null
                            ? ""
                            : value.trim();

            String normalizedHint =
                    hint == null
                            ? ""
                            : hint.trim();

            boolean blankValue =
                    normalizedValue.isBlank();

            boolean confirmedPlaceholderState =
                    normalizedValue.equalsIgnoreCase(
                            "Search..."
                    )
                            &&
                            normalizedHint.equalsIgnoreCase(
                                    "Search..."
                            );

            boolean cleared =
                    blankValue
                            ||
                            confirmedPlaceholderState;

            System.out.println(
                    "[CHECK] Search Field Value   : "
                            + value
            );

            System.out.println(
                    "[CHECK] Search Field Hint    : "
                            + hint
            );

            System.out.println(
                    "[CHECK] Search Field Cleared : "
                            + (cleared ? "YES" : "NO")
            );

            return cleared;

        } catch (Exception e) {

            System.out.println(
                    "[WARN] Unable to read Search field while checking cleared state : "
                            + e.getClass()
                            .getSimpleName()
                            + " - "
                            + e.getMessage()
            );

            return false;
        }
    }
    private String getSearchInputValueNow() {

        WebElement input =
                findSearchInputNow();

        if (input == null) {

            return "<NOT FOUND>";
        }

        try {

            return input.getText();

        } catch (Exception e) {

            return "<UNAVAILABLE>";
        }
    }


    public void clickClearSearch() {

        System.out.println();
        System.out.println(
                "[ACTION] Clearing Search field using Android keyboard Backspace."
        );

        System.out.println(
                "[EXPECTED] Search value should be removed using Android DEL/Backspace key events."
        );

        System.out.println(
                "[INFO] Clear Method : AndroidKey.DEL"
        );

        String beforeValue =
                getSearchInputValueNow();

        System.out.println(
                "[INFO] Search Value Before Clear : "
                        + beforeValue
        );

        if ("<NOT FOUND>".equals(
                beforeValue
        )) {

            throw new AssertionError(
                    "Search input field was not available before keyboard clear validation."
            );
        }

        /*
         * If the field is already blank / showing the Search placeholder,
         * there is nothing left to remove.
         */
        if (isSearchFieldEmpty()) {

            System.out.println(
                    "[PASS] Search field is already clear."
            );

            System.out.println(
                    "[RESULT] Clear Search Using Keyboard : PASS"
            );

            return;
        }

        WebElement input =
                getSearchInput();

        try {

            String focused =
                    input.getAttribute(
                            "focused"
                    );

            System.out.println(
                    "[INFO] Search Field Focused Before Backspace : "
                            + focused
            );

            /*
             * enterSearchText() leaves the Search EditText focused after
             * entering Dior. Do not click inside the text field again when it
             * is already focused, because another tap can move the caret into
             * the middle of the text.
             */
            if (!Boolean.parseBoolean(
                    focused
            )) {

                System.out.println(
                        "[ACTION] Search field is not focused. Focusing Search input."
                );

                input.click();

                pause(
                        300
                );

                System.out.println(
                        "[PASS] Search input focus action completed."
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "[WARN] Unable to read/focus Search field before Backspace : "
                            + e.getClass()
                            .getSimpleName()
                            + " - "
                            + e.getMessage()
            );
        }

        /*
         * Use enough DEL events to remove the complete current value.
         * A few retry events are allowed in case Android drops a key event.
         * We stop immediately as soon as the field becomes blank / Search....
         */
        int visibleCharacterCount =
                beforeValue == null
                        ? 0
                        : beforeValue.length();

        int maximumBackspaces =
                Math.max(
                        visibleCharacterCount + 5,
                        10
                );

        System.out.println(
                "[INFO] Visible Character Count : "
                        + visibleCharacterCount
        );

        System.out.println(
                "[INFO] Maximum Backspace Attempts : "
                        + maximumBackspaces
        );

        for (int attempt = 1;
             attempt <= maximumBackspaces;
             attempt++) {

            System.out.println(
                    "[ACTION] Backspace "
                            + attempt
                            + "/"
                            + maximumBackspaces
            );

            try {

                appDriver.pressKey(
                        new KeyEvent(
                                AndroidKey.DEL
                        )
                );

            } catch (Exception e) {

                System.out.println(
                        "[WARN] Backspace key event failed on attempt "
                                + attempt
                                + " : "
                                + e.getClass()
                                .getSimpleName()
                                + " - "
                                + e.getMessage()
                );

                continue;
            }

            pause(
                    150
            );

            String currentValue =
                    getSearchInputValueNow();

            System.out.println(
                    "[INFO] Search Value After Backspace "
                            + attempt
                            + " : "
                            + currentValue
            );

            if (isSearchFieldEmpty()) {

                System.out.println(
                        "[ACTUAL] Search Field Cleared : YES"
                );

                System.out.println(
                        "[PASS] Search field cleared successfully using Android keyboard Backspace."
                );

                System.out.println(
                        "[RESULT] Clear Search Using Keyboard : PASS"
                );

                return;
            }
        }

        String finalValue =
                getSearchInputValueNow();

        System.out.println(
                "[ACTUAL] Final Search Value : "
                        + finalValue
        );

        System.out.println(
                "[ACTUAL] Search Field Cleared : NO"
        );

        printVisibleTextDiagnostics();

        throw new AssertionError(
                "Android keyboard Backspace did not clear the Search field. Current Search value: "
                        + finalValue
        );
    }

    // ==========================================================
    // GET SUGGESTION TEXTS
    // ==========================================================

    private List<String> getRelevantSuggestionTexts(
            String keyword
    ) {

        Set<String> suggestions =
                new LinkedHashSet<>();

        String lowerKeyword =
                keyword.toLowerCase();

        for (WebElement element :
                appDriver.findElements(
                        TEXT_VIEWS
                )) {

            try {

                String text =
                        element.getText();

                if (element.isDisplayed()
                        &&
                        !text.isBlank()
                        &&
                        text.toLowerCase()
                                .contains(
                                        lowerKeyword
                                )) {

                    suggestions.add(
                            text.trim()
                    );
                }

            } catch (Exception ignored) {
            }
        }

        return new ArrayList<>(
                suggestions
        );
    }


    // ==========================================================
    // FIND MAIN PROMOTIONAL BANNER
    // ==========================================================

    private WebElement findMainBanner() {

        try {

            Dimension screen =
                    appDriver
                            .manage()
                            .window()
                            .getSize();


            List<WebElement> images =
                    appDriver.findElements(
                            IMAGE_VIEWS
                    );


            WebElement bestCandidate =
                    null;


            long largestArea =
                    0;


            for (WebElement image : images) {

                try {

                    if (!image.isDisplayed()) {

                        continue;
                    }


                    Rectangle rect =
                            image.getRect();


                    int width =
                            rect.getWidth();


                    int height =
                            rect.getHeight();


                    int y =
                            rect.getY();


                    long area =
                            (long) width
                                    * height;


                    /*
                     * Current Inspector data shows the Home promotional
                     * creative as a large ImageView occupying most of the
                     * screen width. Use proportional checks so this stays
                     * device-resolution independent.
                     */
                    boolean almostFullWidth =
                            width
                                    >= screen.getWidth()
                                    * 0.80;


                    boolean substantialHeight =
                            height
                                    >= screen.getHeight()
                                    * 0.20;


                    boolean upperPartOfScreen =
                            y
                                    < screen.getHeight()
                                    * 0.45;


                    if (almostFullWidth
                            &&
                            substantialHeight
                            &&
                            upperPartOfScreen
                            &&
                            area > largestArea) {

                        bestCandidate =
                                image;


                        largestArea =
                                area;
                    }

                } catch (Exception ignored) {
                }
            }


            return bestCandidate;

        } catch (Exception e) {

            return null;
        }
    }


    // ==========================================================
    // FRESH BANNER RECTANGLE - STALE SAFE
    // ==========================================================

    private Rectangle getFreshBannerRectangle(
            int timeoutSeconds
    ) {

        long endTime =
                System.currentTimeMillis()
                        + timeoutSeconds * 1000L;

        int attempt =
                0;

        while (System.currentTimeMillis()
                < endTime) {

            attempt++;

            try {

                WebElement banner =
                        findMainBanner();

                if (banner == null) {

                    System.out.println(
                            "[INFO] Fresh Banner Rect Attempt "
                                    + attempt
                                    + " : banner not found."
                    );

                    pause(
                            150
                    );

                    continue;
                }

                Rectangle rect =
                        banner.getRect();

                System.out.println(
                        "[INFO] Fresh Banner Rect Attempt "
                                + attempt
                                + " : "
                                + rectangleToString(
                                rect
                        )
                );

                return rect;

            } catch (Exception e) {

                System.out.println(
                        "[WARN] Fresh Banner Rect Attempt "
                                + attempt
                                + " failed : "
                                + e.getClass()
                                .getSimpleName()
                );

                pause(
                        150
                );
            }
        }

        return null;
    }


    // ==========================================================
    // BANNER SCREENSHOT SIGNATURE - NO CACHED ELEMENT
    // ==========================================================

    private String captureBannerVisualSignature(
            Rectangle rect
    ) {

        try {

            if (rect == null) {

                return null;
            }

            byte[] screenshot =
                    appDriver.getScreenshotAs(
                            OutputType.BYTES
                    );

            BufferedImage fullImage =
                    ImageIO.read(
                            new ByteArrayInputStream(
                                    screenshot
                            )
                    );

            if (fullImage == null) {

                return null;
            }

            int x =
                    Math.max(
                            0,
                            rect.getX()
                    );

            int y =
                    Math.max(
                            0,
                            rect.getY()
                    );

            int width =
                    Math.min(
                            rect.getWidth(),
                            fullImage.getWidth() - x
                    );

            int height =
                    Math.min(
                            rect.getHeight(),
                            fullImage.getHeight() - y
                    );

            if (width <= 10
                    ||
                    height <= 10) {

                return null;
            }

            BufferedImage bannerImage =
                    fullImage.getSubimage(
                            x,
                            y,
                            width,
                            height
                    );

            return createBannerVisualHash(
                    bannerImage
            );

        } catch (Exception e) {

            System.out.println(
                    "[WARN] Banner screenshot signature capture failed : "
                            + e.getClass()
                            .getSimpleName()
                            + " - "
                            + e.getMessage()
            );

            return null;
        }
    }


    private String createBannerVisualHash(
            BufferedImage image
    ) {

        if (image == null
                ||
                image.getWidth() <= 0
                ||
                image.getHeight() <= 0) {

            return null;
        }

        int[] sample =
                new int[64];

        int total =
                0;

        int index =
                0;

        for (int row = 0;
             row < 8;
             row++) {

            for (int column = 0;
                 column < 8;
                 column++) {

                int x =
                        Math.min(
                                image.getWidth() - 1,
                                column
                                        * image.getWidth()
                                        / 8
                                        +
                                        image.getWidth()
                                                / 16
                        );

                int y =
                        Math.min(
                                image.getHeight() - 1,
                                row
                                        * image.getHeight()
                                        / 8
                                        +
                                        image.getHeight()
                                                / 16
                        );

                int rgb =
                        image.getRGB(
                                x,
                                y
                        );

                int red =
                        (rgb >> 16)
                                & 255;

                int green =
                        (rgb >> 8)
                                & 255;

                int blue =
                        rgb
                                & 255;

                int gray =
                        (
                                red
                                        + green
                                        + blue
                        ) / 3;

                sample[index++] =
                        gray;

                total +=
                        gray;
            }
        }

        int average =
                total
                        / sample.length;

        long hash =
                0L;

        for (int value : sample) {

            hash <<= 1;

            if (value >= average) {

                hash |= 1L;
            }
        }

        return String.format(
                "%016X",
                hash
        );
    }


    private String captureStableBannerSignature(
            int timeoutSeconds
    ) {

        Rectangle rect =
                getFreshBannerRectangle(
                        5
                );

        if (rect == null) {

            System.out.println(
                    "[FAIL] Stable banner capture could not obtain banner bounds."
            );

            return null;
        }

        System.out.println(
                "[INFO] Stable Capture Banner Bounds : "
                        + rectangleToString(
                        rect
                )
        );

        long endTime =
                System.currentTimeMillis()
                        + timeoutSeconds * 1000L;

        Map<String, Integer> observations =
                new java.util.LinkedHashMap<>();

        int readNumber =
                0;

        while (System.currentTimeMillis()
                < endTime) {

            readNumber++;

            String signature =
                    captureBannerVisualSignature(
                            rect
                    );

            if (signature == null
                    ||
                    signature.length() != 16) {

                System.out.println(
                        "[INFO] Banner Stable Read "
                                + readNumber
                                + " : INVALID / TRANSITION - ignored"
                );

                pause(
                        120
                );

                continue;
            }

            int count =
                    observations.getOrDefault(
                            signature,
                            0
                    )
                            + 1;

            observations.put(
                    signature,
                    count
            );

            System.out.println(
                    "[INFO] Banner Stable Read "
                            + readNumber
                            + " : "
                            + signature
                            + " (observation "
                            + count
                            + "/2)"
            );

            /*
             * Two matching full-banner screenshots are enough to confirm
             * the current banner. We do NOT require three consecutive reads
             * because the carousel can rotate before a third screenshot.
             */
            if (count >= 2) {

                System.out.println(
                        "[PASS] Stable Banner Signature : "
                                + signature
                );

                return signature;
            }

            pause(
                    120
            );
        }

        System.out.println(
                "[FAIL] No banner signature was observed twice within "
                        + timeoutSeconds
                        + " seconds."
        );

        return null;
    }


    private String waitForStableBannerChange(
            String previous,
            int timeoutSeconds
    ) {

        Rectangle rect =
                getFreshBannerRectangle(
                        5
                );

        if (rect == null) {

            System.out.println(
                    "[FAIL] Banner-change capture could not obtain banner bounds."
            );

            return null;
        }

        long endTime =
                System.currentTimeMillis()
                        + timeoutSeconds * 1000L;

        Map<String, Integer> observations =
                new java.util.LinkedHashMap<>();

        int readNumber =
                0;

        while (System.currentTimeMillis()
                < endTime) {

            readNumber++;

            String signature =
                    captureBannerVisualSignature(
                            rect
                    );

            if (signature == null
                    ||
                    signature.length() != 16) {

                System.out.println(
                        "[INFO] Changed Banner Read "
                                + readNumber
                                + " : INVALID / TRANSITION - ignored"
                );

                pause(
                        120
                );

                continue;
            }

            if (signature.equals(
                    previous
            )) {

                System.out.println(
                        "[INFO] Changed Banner Read "
                                + readNumber
                                + " : still previous banner "
                                + signature
                );

                pause(
                        120
                );

                continue;
            }

            int count =
                    observations.getOrDefault(
                            signature,
                            0
                    )
                            + 1;

            observations.put(
                    signature,
                    count
            );

            System.out.println(
                    "[INFO] Changed Banner Read "
                            + readNumber
                            + " : "
                            + signature
                            + " (observation "
                            + count
                            + "/2)"
            );

            if (count >= 2) {

                System.out.println(
                        "[PASS] Stable changed Banner Signature : "
                                + signature
                );

                return signature;
            }

            pause(
                    120
            );
        }

        System.out.println(
                "[INFO] No stable changed banner detected within "
                        + timeoutSeconds
                        + " seconds."
        );

        return null;
    }


    private String rectangleToString(
            Rectangle rect
    ) {

        if (rect == null) {

            return "<NOT AVAILABLE>";
        }

        return "[x="
                + rect.getX()
                + ", y="
                + rect.getY()
                + ", width="
                + rect.getWidth()
                + ", height="
                + rect.getHeight()
                + "]";
    }


// ==========================================================
    // BANNER VISUAL SIGNATURE
    // ==========================================================

// ==========================================================
    // SWIPE BANNER LEFT
    // ==========================================================

    private void swipeBannerLeft() {

        Rectangle rect =
                getFreshBannerRectangle(
                        8
                );

        if (rect == null) {

            throw new AssertionError(
                    "Promotional Banner was not found for swipe."
            );
        }

        System.out.println(
                "[ACTION] Swiping Promotional Banner LEFT."
        );

        System.out.println(
                "[INFO] Swipe Region   : "
                        + rectangleToString(
                        rect
                )
        );

        System.out.println(
                "[INFO] Swipe Direction: left"
        );

        System.out.println(
                "[INFO] Swipe Percent  : 0.75"
        );

        appDriver.executeScript(
                "mobile: swipeGesture",
                Map.of(
                        "left",
                        rect.getX(),
                        "top",
                        rect.getY(),
                        "width",
                        rect.getWidth(),
                        "height",
                        rect.getHeight(),
                        "direction",
                        "left",
                        "percent",
                        0.75
                )
        );

        System.out.println(
                "[PASS] Swipe gesture sent successfully."
        );

        pause(
                500
        );
    }


// ==========================================================
    // MOVE TO PARTICULAR BANNER
    // ==========================================================

    private boolean moveToBanner(
            String expectedSignature
    ) {

        int maxAttempts =
                Math.max(
                        8,
                        detectedBannerSignatures.size() * 3
                );

        System.out.println(
                "[INFO] Target Banner Signature : "
                        + expectedSignature
        );

        System.out.println(
                "[INFO] Maximum Position Attempts : "
                        + maxAttempts
        );

        String currentSignature =
                captureStableBannerSignature(
                        5
                );

        System.out.println(
                "[INFO] Initial Current Signature : "
                        + currentSignature
        );

        if (expectedSignature.equals(
                currentSignature
        )) {

            System.out.println(
                    "[PASS] Expected banner is already positioned."
            );

            return true;
        }

        for (int attempt = 1;
             attempt <= maxAttempts;
             attempt++) {

            System.out.println(
                    "[INFO] Position Attempt "
                            + attempt
                            + "/"
                            + maxAttempts
            );

            if (currentSignature == null) {

                System.out.println(
                        "[WARN] Current banner signature unavailable. Capturing again."
                );

                currentSignature =
                        captureStableBannerSignature(
                                3
                        );

                System.out.println(
                        "[INFO] Re-captured Current Signature : "
                                + currentSignature
                );

                if (expectedSignature.equals(
                        currentSignature
                )) {

                    System.out.println(
                            "[PASS] Expected banner positioned after re-capture."
                    );

                    return true;
                }

                if (currentSignature == null) {

                    System.out.println(
                            "[WARN] Banner signature still unavailable. Retrying next attempt."
                    );

                    continue;
                }
            }

            System.out.println(
                    "[INFO] Current Signature  : "
                            + currentSignature
            );

            System.out.println(
                    "[INFO] Expected Signature : "
                            + expectedSignature
            );

            swipeBannerLeft();

            String changedSignature =
                    waitForStableBannerChange(
                            currentSignature,
                            8
                    );

            System.out.println(
                    "[INFO] Signature Reached After Swipe : "
                            + changedSignature
            );

            /*
             * IMPORTANT:
             * waitForStableBannerChange() has already confirmed that this
             * banner is stable. If it is the expected banner, return
             * immediately.
             *
             * Do NOT call captureStableBannerSignature() again here.
             * The carousel is auto-rotating and a second verification can
             * allow the target banner to move away before we return.
             */
            if (expectedSignature.equals(
                    changedSignature
            )) {

                System.out.println(
                        "[PASS] Expected banner reached after swipe on attempt "
                                + attempt
                                + "."
                );

                System.out.println(
                        "[INFO] No second banner verification performed to avoid auto-rotation race."
                );

                return true;
            }

            if (changedSignature == null) {

                System.out.println(
                        "[WARN] Swipe completed but no stable changed banner was captured."
                );

                currentSignature =
                        null;

                continue;
            }

            currentSignature =
                    changedSignature;
        }

        System.out.println(
                "[FAIL] Unable to position expected banner signature : "
                        + expectedSignature
        );

        return false;
    }


    // ==========================================================
    // BANNER CTA
    // ==========================================================

    private void tapCurrentBannerCTA() {

        Rectangle rect =
                getFreshBannerRectangle(
                        8
                );

        if (rect == null) {

            throw new AssertionError(
                    "Current Promotional Banner was not found."
            );
        }

        int x =
                rect.getX()
                        + rect.getWidth() / 2;

        int y =
                rect.getY()
                        +
                        (int) (
                                rect.getHeight()
                                        * 0.78
                        );

        System.out.println(
                "[INFO] Current Banner Bounds : "
                        + rectangleToString(
                        rect
                )
        );

        System.out.println(
                "[INFO] Banner CTA Position   : "
                        + x
                        + ", "
                        + y
        );

        System.out.println(
                "[ACTION] Sending CTA click gesture."
        );

        appDriver.executeScript(
                "mobile: clickGesture",
                Map.of(
                        "x", x,
                        "y", y
                )
        );

        System.out.println(
                "[PASS] Banner CTA click gesture sent."
        );

        pause(
                800
        );
    }


    // ==========================================================
    // BANNER DESTINATION
    // ==========================================================

    private boolean waitForBannerDestination(
            String beforeSignature
    ) {

        long end =
                System.currentTimeMillis()
                        + 15000L;

        while (System.currentTimeMillis()
                < end) {

            String currentSignature =
                    getPageSignature();

            boolean changed =
                    !currentSignature.equals(
                            beforeSignature
                    );

            boolean homeBannerGone =
                    findMainBanner()
                            == null;

            if (changed
                    &&
                    homeBannerGone) {

                return true;
            }

            pause(
                    500
            );
        }

        return false;
    }


    // ==========================================================
    // SEARCH INPUT
    // ==========================================================

    private WebElement findSearchInputNow() {

        /*
         * METHOD 1 - EXACT:
         * Latest Appium hierarchy confirms this exact EditText.
         */
        try {

            List<WebElement> exactFields =
                    appDriver.findElements(
                            SEARCH_INPUT_EXACT
                    );

            for (WebElement field : exactFields) {

                try {

                    if (field.isDisplayed()) {

                        return field;
                    }

                } catch (Exception ignored) {
                }
            }

        } catch (Exception ignored) {
        }


        /*
         * METHOD 2 - COMPATIBILITY FALLBACK:
         * Keep the generic EditText lookup for earlier Search flow behaviour.
         */
        try {

            List<WebElement> fields =
                    appDriver.findElements(
                            EDIT_TEXT_FIELDS
                    );

            for (WebElement field : fields) {

                try {

                    if (field.isDisplayed()) {

                        return field;
                    }

                } catch (Exception ignored) {
                }
            }

        } catch (Exception ignored) {
        }


        return null;
    }


    private WebElement getSearchInput() {

        long end =
                System.currentTimeMillis()
                        + 20000L;

        int attempt =
                0;

        while (System.currentTimeMillis()
                < end) {

            attempt++;

            WebElement field =
                    findSearchInputNow();

            if (field != null) {

                return field;
            }

            pause(
                    250
            );
        }

        System.out.println(
                "[FAIL] Search input field was not found."
        );

        System.out.println(
                "[LOCATOR] Exact Search Input : //android.widget.EditText[@index='1' and @hint='Search...' and @clickable='true' and @focusable='true' and @bounds='[144,74][603,158]']"
        );

        System.out.println(
                "[INFO] Search Input Attempts : "
                        + attempt
        );

        throw new AssertionError(
                "Search input field was not found."
        );
    }


    private boolean waitForSearchInput(
            int timeoutSeconds
    ) {

        long end =
                System.currentTimeMillis()
                        + timeoutSeconds * 1000L;

        while (System.currentTimeMillis()
                < end) {

            if (findSearchInputNow() != null) {

                return true;
            }

            pause(
                    250
            );
        }

        return false;
    }


    private boolean waitForSearchFieldEmpty() {

        long end =
                System.currentTimeMillis()
                        + 5000L;

        while (System.currentTimeMillis()
                < end) {

            if (isSearchFieldEmpty()) {

                return true;
            }

            pause(
                    250
            );
        }

        return false;
    }


    // ==========================================================
    // GENERIC PAGE CHANGE
    // ==========================================================

    private boolean pageChangeTimedOut(
            String before
    ) {

        long end =
                System.currentTimeMillis()
                        + 15000L;

        while (System.currentTimeMillis()
                < end) {

            if (!getPageSignature()
                    .equals(
                            before
                    )) {

                return false;
            }

            pause(
                    400
            );
        }

        return true;
    }


    private String getPageSignature() {

        try {

            return Integer.toHexString(
                    appDriver.getPageSource()
                            .hashCode()
            );

        } catch (Exception e) {

            return "UNKNOWN";
        }
    }


    // ==========================================================
    // VISIBLE SCREEN TEXT
    // ==========================================================

    private String getVisibleScreenText() {

        Set<String> values =
                new LinkedHashSet<>();

        for (WebElement element :
                appDriver.findElements(
                        TEXT_VIEWS
                )) {

            if (values.size()
                    >= 6) {

                break;
            }

            try {

                String text =
                        element.getText();

                if (element.isDisplayed()
                        &&
                        !text.isBlank()) {

                    values.add(
                            text.trim()
                    );
                }

            } catch (Exception ignored) {
            }
        }

        if (values.isEmpty()) {

            return "<No visible screen text>";
        }

        return String.join(
                " | ",
                values
        );
    }


    // ==========================================================
    // CLICK IF DISPLAYED
    // ==========================================================

    private boolean clickIfDisplayed(
            By locator
    ) {

        try {

            List<WebElement> elements =
                    appDriver.findElements(
                            locator
                    );

            for (WebElement element : elements) {

                try {

                    if (!element.isDisplayed()) {
                        continue;
                    }

                    try {

                        element.click();

                    } catch (Exception e) {

                        tapElementCenter(
                                element
                        );
                    }

                    pause(
                            500
                    );

                    return true;

                } catch (Exception ignored) {
                }
            }

        } catch (Exception ignored) {
        }

        return false;
    }


    // ==========================================================
    // TAP ELEMENT CENTER
    // ==========================================================

    private void tapElementCenter(
            WebElement element
    ) {

        Rectangle rect =
                element.getRect();

        int x =
                rect.getX()
                        + rect.getWidth() / 2;

        int y =
                rect.getY()
                        + rect.getHeight() / 2;

        appDriver.executeScript(
                "mobile: clickGesture",
                Map.of(
                        "x", x,
                        "y", y
                )
        );

        pause(
                600
        );
    }


    // ==========================================================
    // DIAGNOSTIC UI TEXT
    // ==========================================================

    private void printVisibleTextDiagnostics() {

        System.out.println();
        System.out.println(
                "--------------- VISIBLE UI TEXT ---------------"
        );

        int count = 0;

        for (WebElement element :
                appDriver.findElements(
                        TEXT_VIEWS
                )) {

            if (count >= 25) {
                break;
            }

            try {

                String text =
                        element.getText();

                if (element.isDisplayed()
                        &&
                        !text.isBlank()) {

                    System.out.println(
                            "[UI] "
                                    + text
                                    + " | "
                                    + element.getRect()
                    );

                    count++;
                }

            } catch (Exception ignored) {
            }
        }

        System.out.println(
                "-----------------------------------------------"
        );
    }


    // ==========================================================
    // SAFE ATTRIBUTE
    // ==========================================================

    private String safeClassAttribute(
            WebElement element
    ) {

        try {

            return element.getAttribute(
                    "class"
            );

        } catch (Exception e) {

            return "<NOT AVAILABLE>";
        }
    }


    // ==========================================================
    // SAFE TEXT
    // ==========================================================

    private String safeText(
            WebElement element
    ) {

        try {

            String value =
                    element.getText();

            return value == null
                    ? ""
                    : value.trim();

        } catch (Exception e) {

            return "";
        }
    }


    // ==========================================================
    // PAUSE
    // ==========================================================

    private void pause(
            long milliseconds
    ) {

        try {

            Thread.sleep(
                    milliseconds
            );

        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();

            throw new IllegalStateException(
                    "Home Page automation interrupted.",
                    e
            );
        }
    }
}
