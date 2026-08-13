package com.tfs.pages;

import com.tfs.driver.DriverManager;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class DeliveryDetailsPage extends BasePage {

    private final AndroidDriver appDriver;


    // ==========================================================
    // LOCATORS
    // ==========================================================

    private static final By EDIT_TEXT_FIELDS =
            AppiumBy.className(
                    "android.widget.EditText"
            );


    private static final By MANUAL_ADDRESS_LINK =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Enter your address manually\")"
            );


    private static final By PICK_MEMBERSHIP_BUTTON =
            AppiumBy.accessibilityId(
                    "Pick Membership"
            );


    // ==========================================================
    // CONSTRUCTOR
    // ==========================================================

    public DeliveryDetailsPage() {

        super();

        this.appDriver =
                DriverManager.getDriver();
    }


    // ==========================================================
    // DELIVERY POSTCODE SEARCH SCREEN
    // ==========================================================

    public boolean isDeliverySearchScreenDisplayed() {

        System.out.println();
        System.out.println(
                "[STEP] Checking Delivery Postcode Search screen."
        );


        boolean displayed =
                waitForAnyText(
                        40,
                        "Your delivery details",
                        "Search for a Postcode",
                        "Enter your address manually"
                );


        if (displayed) {

            System.out.println(
                    "[PASS] Delivery Postcode Search screen displayed."
            );

        } else {

            System.out.println(
                    "[FAIL] Delivery Postcode Search screen was not displayed."
            );
        }


        return displayed;
    }


    // ==========================================================
    // ENTER POSTCODE
    // ==========================================================

    public void enterPostcode(
            String postcode
    ) {

        System.out.println(
                "[ACTION] Entering postcode: "
                        + postcode
        );


        List<WebElement> fields =
                appDriver.findElements(
                        EDIT_TEXT_FIELDS
                );


        if (fields.isEmpty()) {

            throw new AssertionError(
                    "Postcode input field was not found on Delivery Details screen."
            );
        }


        WebElement postcodeInput =
                fields.get(0);


        postcodeInput.click();


        try {

            postcodeInput.clear();

        } catch (Exception ignored) {
        }


        postcodeInput.sendKeys(
                postcode
        );


        System.out.println(
                "[INFO] Postcode entered. Waiting for address lookup results."
        );


        /*
         * Do not use a long fixed sleep here.
         * Step 11 waits dynamically for the real result.
         */
        pause(
                800
        );


        System.out.println(
                "[PASS] Postcode entered successfully: "
                        + postcode
        );
    }


    // ==========================================================
    // ADDRESS SUGGESTIONS
    // ==========================================================

    public boolean areAddressSuggestionsDisplayed() {

        System.out.println(
                "[STEP] Waiting for actual address suggestions."
        );


        WebElement firstSuggestion =
                waitForFirstAddressSuggestion(
                        45
                );


        if (firstSuggestion != null) {

            String value =
                    getElementValue(
                            firstSuggestion
                    );


            System.out.println(
                    "[PASS] Address suggestions displayed."
            );


            if (!value.isBlank()) {

                System.out.println(
                        "[INFO] First address suggestion: "
                                + value
                );
            }


            return true;
        }


        System.out.println(
                "[FAIL] Address suggestions were not displayed."
        );


        printAddressSuggestionDiagnostics();


        return false;
    }


    // ==========================================================
    // SELECT FIRST ADDRESS SUGGESTION
    // ==========================================================

    public void selectFirstAddressSuggestion() {

        System.out.println();
        System.out.println(
                "[ACTION] Selecting first visible address suggestion."
        );


        WebElement suggestion =
                waitForFirstAddressSuggestion(
                        30
                );


        if (suggestion == null) {

            printAddressSuggestionDiagnostics();


            throw new AssertionError(
                    "No visible address suggestion was available to select."
            );
        }


        String suggestionText =
                getElementValue(
                        suggestion
                );


        System.out.println(
                "[INFO] First visible address result: "
                        + (
                        suggestionText.isBlank()
                                ? "<NO TEXT>"
                                : suggestionText
                )
        );


        /*
         * First try the real Appium element.
         */
        boolean clickSent =
                clickAddressSuggestionElement(
                        suggestion
                );


        if (clickSent) {

            System.out.println(
                    "[INFO] Address suggestion click sent."
            );
        }


        /*
         * PASS only after the destination screen
         * is actually displayed.
         */
        if (isManualAddressFormDisplayed()) {

            System.out.println(
                    "[PASS] First address suggestion selected successfully."
            );

            return;
        }


        // ======================================================
        // RETRY USING CURRENT ELEMENT BOUNDS
        // ======================================================

        System.out.println(
                "[INFO] Address Details form did not open after first click."
        );

        System.out.println(
                "[ACTION] Retrying first address suggestion using element center."
        );


        suggestion =
                waitForFirstAddressSuggestion(
                        10
                );


        if (suggestion != null) {

            tapElementCenter(
                    suggestion,
                    "first address suggestion"
            );
        }


        if (isManualAddressFormDisplayed()) {

            System.out.println(
                    "[PASS] First address suggestion selected successfully on retry."
            );

            return;
        }


        System.out.println(
                "[FAIL] Address suggestion was detected but selection did not open Address Details form."
        );


        throw new AssertionError(
                "First address suggestion was found but could not be selected."
        );
    }


    // ==========================================================
    // WAIT FOR FIRST REAL ADDRESS SUGGESTION
    // ==========================================================

    private WebElement waitForFirstAddressSuggestion(
            int timeoutSeconds
    ) {

        long endTime =
                System.currentTimeMillis()
                        + timeoutSeconds * 1000L;


        while (System.currentTimeMillis()
                < endTime) {

            try {

                List<WebElement> fields =
                        appDriver.findElements(
                                EDIT_TEXT_FIELDS
                        );


                /*
                 * Search screen must have a postcode/search field.
                 */
                if (fields.isEmpty()) {

                    /*
                     * It is possible the result was already selected
                     * and we reached the Address Details form.
                     */
                    if (isAddressDetailsFormVisibleQuick()) {

                        return null;
                    }


                    pause(
                            400
                    );

                    continue;
                }


                WebElement postcodeField =
                        fields.get(0);


                int postcodeFieldBottom =
                        postcodeField
                                .getRect()
                                .getY()
                                +
                                postcodeField
                                        .getRect()
                                        .getHeight();


                Dimension screenSize =
                        appDriver
                                .manage()
                                .window()
                                .getSize();


                /*
                 * Suggestions appear below the postcode field.
                 *
                 * The keyboard occupies the bottom portion of the
                 * screen, so we exclude that area from candidates.
                 */
                int suggestionAreaBottom =
                        (int) (
                                screenSize.getHeight()
                                        * 0.68
                        );


                List<WebElement> candidates =
                        appDriver.findElements(
                                By.xpath(
                                        "//*[@text!='' or @content-desc!='']"
                                )
                        );


                WebElement bestCandidate = null;

                int bestY =
                        Integer.MAX_VALUE;


                for (WebElement element : candidates) {

                    try {

                        if (!element.isDisplayed()) {

                            continue;
                        }


                        String value =
                                getElementValue(
                                        element
                                );


                        if (value == null
                                || value.isBlank()) {

                            continue;
                        }


                        String normalized =
                                value.trim();


                        /*
                         * Ignore permanent screen controls/text.
                         */
                        if (isNonAddressResultText(
                                normalized
                        )) {

                            continue;
                        }


                        int elementX =
                                element
                                        .getRect()
                                        .getX();


                        int elementY =
                                element
                                        .getRect()
                                        .getY();


                        int elementWidth =
                                element
                                        .getRect()
                                        .getWidth();


                        int elementHeight =
                                element
                                        .getRect()
                                        .getHeight();


                        int elementBottom =
                                elementY
                                        + elementHeight;


                        /*
                         * Result must be BELOW the search field.
                         */
                        if (elementY
                                <= postcodeFieldBottom) {

                            continue;
                        }


                        /*
                         * Result must be ABOVE the keyboard area.
                         */
                        if (elementBottom
                                >= suggestionAreaBottom) {

                            continue;
                        }


                        /*
                         * Ignore tiny icons such as location pins.
                         */
                        if (elementWidth
                                < 90) {

                            continue;
                        }


                        /*
                         * Ignore extremely short decorative elements.
                         */
                        if (elementHeight
                                < 15) {

                            continue;
                        }


                        /*
                         * Ignore elements completely outside
                         * visible screen width.
                         */
                        if (elementX
                                >= screenSize.getWidth()) {

                            continue;
                        }


                        /*
                         * Choose the TOPMOST valid result.
                         * This becomes our first address suggestion.
                         */
                        if (elementY
                                < bestY) {

                            bestCandidate =
                                    element;

                            bestY =
                                    elementY;
                        }


                    } catch (Exception ignored) {
                    }
                }


                if (bestCandidate != null) {

                    String resultText =
                            getElementValue(
                                    bestCandidate
                            );


                    System.out.println(
                            "[INFO] Address result detected: "
                                    + resultText
                    );


                    System.out.println(
                            "[INFO] Address result bounds: "
                                    + bestCandidate.getRect()
                    );


                    return bestCandidate;
                }


            } catch (Exception ignored) {
            }


            pause(
                    500
            );
        }


        return null;
    }


    // ==========================================================
    // NON ADDRESS TEXT FILTER
    // ==========================================================

    private boolean isNonAddressResultText(
            String value
    ) {

        String normalized =
                value
                        .trim()
                        .toLowerCase();


        /*
         * These are known fixed controls/text from
         * the Delivery postcode page or Android keyboard.
         *
         * We do NOT hardcode actual address values.
         */
        return normalized.equals(
                "your delivery details"
        )
                ||
                normalized.equals(
                        "delivery details"
                )
                ||
                normalized.equals(
                        "search for a postcode"
                )
                ||
                normalized.equals(
                        "enter your address manually"
                )
                ||
                normalized.equals(
                        "pick membership"
                )
                ||
                normalized.equals(
                        "the fragrance shop"
                )
                ||
                normalized.equals(
                        "spray a little happiness"
                )
                ||
                normalized.equals(
                        "done"
                )
                ||
                normalized.equals(
                        "next"
                );
    }


    // ==========================================================
    // CLICK ADDRESS SUGGESTION ELEMENT
    // ==========================================================

    private boolean clickAddressSuggestionElement(
            WebElement element
    ) {

        try {

            element.click();

            pause(
                    800
            );


            return true;


        } catch (Exception e) {

            System.out.println(
                    "[INFO] Normal address suggestion click failed."
            );


            try {

                tapElementCenter(
                        element,
                        "address suggestion"
                );


                return true;


            } catch (Exception ignored) {

                return false;
            }
        }
    }


    // ==========================================================
    // TAP ELEMENT CENTER
    // ==========================================================

    private void tapElementCenter(
            WebElement element,
            String elementName
    ) {

        int centerX =
                element
                        .getRect()
                        .getX()
                        +
                        element
                                .getRect()
                                .getWidth()
                                / 2;


        int centerY =
                element
                        .getRect()
                        .getY()
                        +
                        element
                                .getRect()
                                .getHeight()
                                / 2;


        System.out.println(
                "[INFO] "
                        + elementName
                        + " center: "
                        + centerX
                        + ", "
                        + centerY
        );


        appDriver.executeScript(
                "mobile: clickGesture",
                Map.of(
                        "x", centerX,
                        "y", centerY
                )
        );


        pause(
                800
        );
    }


    // ==========================================================
    // ELEMENT VALUE
    // ==========================================================

    private String getElementValue(
            WebElement element
    ) {

        try {

            String text =
                    element.getText();


            if (text != null
                    && !text.trim().isEmpty()) {

                return text.trim();
            }


        } catch (Exception ignored) {
        }


        try {

            String contentDescription =
                    element.getAttribute(
                            "content-desc"
                    );


            if (contentDescription != null
                    && !contentDescription.trim().isEmpty()) {

                return contentDescription.trim();
            }


        } catch (Exception ignored) {
        }


        try {

            String contentDescription =
                    element.getAttribute(
                            "contentDescription"
                    );


            if (contentDescription != null
                    && !contentDescription.trim().isEmpty()) {

                return contentDescription.trim();
            }


        } catch (Exception ignored) {
        }


        return "";
    }


    // ==========================================================
    // ADDRESS SUGGESTION DIAGNOSTICS
    // ==========================================================

    private void printAddressSuggestionDiagnostics() {

        System.out.println();
        System.out.println(
                "[DIAGNOSTIC] Address suggestion detection details:"
        );


        try {

            Dimension size =
                    appDriver
                            .manage()
                            .window()
                            .getSize();


            System.out.println(
                    "[DIAGNOSTIC] Screen Size: "
                            + size.width
                            + " x "
                            + size.height
            );


        } catch (Exception e) {

            System.out.println(
                    "[DIAGNOSTIC] Unable to read screen size."
            );
        }


        try {

            List<WebElement> fields =
                    appDriver.findElements(
                            EDIT_TEXT_FIELDS
                    );


            System.out.println(
                    "[DIAGNOSTIC] EditText count: "
                            + fields.size()
            );


            if (!fields.isEmpty()) {

                String value =
                        fields.get(0)
                                .getText();


                System.out.println(
                        "[DIAGNOSTIC] Search/Postcode value: "
                                + (
                                value == null
                                        ? "<NULL>"
                                        : value
                        )
                );
            }


        } catch (Exception e) {

            System.out.println(
                    "[DIAGNOSTIC] Unable to inspect postcode field."
            );
        }


        try {

            List<WebElement> textElements =
                    appDriver.findElements(
                            By.xpath(
                                    "//*[@text!='' or @content-desc!='']"
                            )
                    );


            System.out.println(
                    "[DIAGNOSTIC] Visible text/content-desc elements:"
            );


            int printed =
                    0;


            for (WebElement element : textElements) {

                if (printed
                        >= 20) {

                    break;
                }


                try {

                    if (!element.isDisplayed()) {

                        continue;
                    }


                    String value =
                            getElementValue(
                                    element
                            );


                    if (value.isBlank()) {

                        continue;
                    }


                    System.out.println(
                            "  - "
                                    + value
                                    + " | "
                                    + element.getRect()
                    );


                    printed++;


                } catch (Exception ignored) {
                }
            }


        } catch (Exception e) {

            System.out.println(
                    "[DIAGNOSTIC] Unable to inspect visible address elements."
            );
        }
    }


    // ==========================================================
    // MANUAL ADDRESS LINK
    // ==========================================================

    public void clickManualAddressLink() {

        System.out.println(
                "[ACTION] Clicking Enter your address manually."
        );


        safeClick(
                MANUAL_ADDRESS_LINK,
                20
        );


        pause(
                1500
        );


        System.out.println(
                "[PASS] Enter your address manually clicked."
        );
    }


    // ==========================================================
    // ADDRESS DETAILS FORM
    // ==========================================================

    public boolean isManualAddressFormDisplayed() {

        System.out.println(
                "[STEP] Checking Delivery Details address form."
        );


        long endTime =
                System.currentTimeMillis()
                        + 30000;


        while (System.currentTimeMillis()
                < endTime) {

            if (isAddressDetailsFormVisibleQuick()) {

                List<WebElement> fields =
                        appDriver.findElements(
                                EDIT_TEXT_FIELDS
                        );


                System.out.println(
                        "[PASS] Delivery Details address form displayed."
                );


                System.out.println(
                        "[INFO] Address fields found: "
                                + fields.size()
                );


                return true;
            }


            pause(
                    500
            );
        }


        System.out.println(
                "[FAIL] Delivery Details address form was not displayed."
        );


        return false;
    }


    // ==========================================================
    // QUICK ADDRESS DETAILS FORM CHECK
    // ==========================================================

    private boolean isAddressDetailsFormVisibleQuick() {

        try {

            List<WebElement> fields =
                    appDriver.findElements(
                            EDIT_TEXT_FIELDS
                    );


            /*
             * Inspector confirmed Delivery Details form:
             *
             * 0 = Address Line 1
             * 1 = Address Line 2
             * 2 = BFPO / Town
             * 3 = Country
             * 4 = Postcode
             */
            boolean addressFieldsDisplayed =
                    fields.size()
                            >= 5;


            boolean pickMembershipDisplayed =
                    isElementDisplayedQuick(
                            PICK_MEMBERSHIP_BUTTON
                    );


            return addressFieldsDisplayed
                    && pickMembershipDisplayed;


        } catch (Exception e) {

            return false;
        }
    }


    // ==========================================================
    // ADDRESS LINE 1 DISPLAYED
    // ==========================================================

    public boolean isAddressLine1FieldDisplayed() {

        System.out.println(
                "[STEP] Checking mandatory Address Line 1 field."
        );


        long endTime =
                System.currentTimeMillis()
                        + 20000;


        while (System.currentTimeMillis()
                < endTime) {

            try {

                List<WebElement> fields =
                        appDriver.findElements(
                                EDIT_TEXT_FIELDS
                        );


                if (!fields.isEmpty()) {

                    WebElement addressLine1Field =
                            fields.get(0);


                    if (addressLine1Field.isDisplayed()
                            && addressLine1Field.isEnabled()) {

                        System.out.println(
                                "[PASS] Mandatory Address Line 1 field displayed."
                        );


                        System.out.println(
                                "[INFO] Address Line 1 locator: EditText index 0"
                        );


                        return true;
                    }
                }


            } catch (Exception ignored) {
            }


            pause(
                    500
            );
        }


        System.out.println(
                "[FAIL] Mandatory Address Line 1 field was not displayed."
        );


        return false;
    }


    // ==========================================================
    // MANDATORY ADDRESS LINE 1 - ENTER VALUE
    // ==========================================================

    public void enterAddressLine1IfEmpty(
            String addressLine1
    ) {

        System.out.println(
                "[ACTION] Checking mandatory Address Line 1 value."
        );


        List<WebElement> fields =
                appDriver.findElements(
                        EDIT_TEXT_FIELDS
                );


        if (fields.isEmpty()) {

            throw new AssertionError(
                    "Mandatory Address Line 1 field was not found."
            );
        }


        /*
         * Inspector confirmed:
         * First EditText = Address Line 1
         */
        WebElement addressLine1Field =
                fields.get(0);


        String currentValue =
                addressLine1Field.getText();


        System.out.println(
                "[INFO] Current Address Line 1 value: "
                        + (
                        currentValue == null
                                || currentValue.isBlank()
                                ? "<EMPTY>"
                                : currentValue
                )
        );


        if (currentValue == null
                || currentValue.trim().isEmpty()) {


            System.out.println(
                    "[ACTION] Address Line 1 is mandatory and empty."
            );


            System.out.println(
                    "[ACTION] Entering Address Line 1: "
                            + addressLine1
            );


            addressLine1Field.click();


            try {

                addressLine1Field.clear();

            } catch (Exception ignored) {
            }


            addressLine1Field.sendKeys(
                    addressLine1
            );


            pause(
                    800
            );


            try {

                appDriver.hideKeyboard();

            } catch (Exception ignored) {
            }


            /*
             * Re-find field after keyboard interaction.
             */
            fields =
                    appDriver.findElements(
                            EDIT_TEXT_FIELDS
                    );


            if (fields.isEmpty()) {

                throw new AssertionError(
                        "Address Line 1 field disappeared after data entry."
                );
            }


            String enteredValue =
                    fields.get(0)
                            .getText();


            System.out.println(
                    "[INFO] Address Line 1 after entry: "
                            + enteredValue
            );


            if (enteredValue == null
                    || enteredValue.trim().isEmpty()) {

                throw new AssertionError(
                        "Mandatory Address Line 1 value was not entered."
                );
            }


            System.out.println(
                    "[PASS] Mandatory Address Line 1 entered successfully."
            );


        } else {

            System.out.println(
                    "[PASS] Address Line 1 already contains value: "
                            + currentValue
            );
        }
    }


    // ==========================================================
    // MANDATORY ADDRESS LINE 1 - VALUE VALIDATION
    // ==========================================================

    public boolean isAddressLine1ValueEntered() {

        System.out.println(
                "[STEP] Validating mandatory Address Line 1 value."
        );


        List<WebElement> fields =
                appDriver.findElements(
                        EDIT_TEXT_FIELDS
                );


        if (fields.isEmpty()) {

            System.out.println(
                    "[FAIL] Address Line 1 field was not found."
            );


            return false;
        }


        String value =
                fields.get(0)
                        .getText();


        boolean valid =
                value != null
                        && !value.trim().isEmpty();


        if (valid) {

            System.out.println(
                    "[PASS] Mandatory Address Line 1 value: "
                            + value
            );


        } else {

            System.out.println(
                    "[FAIL] Mandatory Address Line 1 is empty."
            );
        }


        return valid;
    }


    // ==========================================================
    // PICK MEMBERSHIP BUTTON
    // ==========================================================

    public boolean isPickMembershipDisplayed() {

        System.out.println(
                "[STEP] Checking Pick Membership button."
        );


        try {

            boolean displayed =
                    new WebDriverWait(
                            appDriver,
                            Duration.ofSeconds(
                                    20
                            )
                    ).until(
                            ExpectedConditions
                                    .visibilityOfElementLocated(
                                            PICK_MEMBERSHIP_BUTTON
                                    )
                    ).isDisplayed();


            if (displayed) {

                System.out.println(
                        "[PASS] Pick Membership button displayed."
                );
            }


            return displayed;


        } catch (Exception e) {


            boolean displayed =
                    waitForAnyText(
                            5,
                            "Pick Membership"
                    );


            if (displayed) {

                System.out.println(
                        "[PASS] Pick Membership text displayed."
                );


            } else {

                System.out.println(
                        "[FAIL] Pick Membership button was not displayed."
                );
            }


            return displayed;
        }
    }


    // ==========================================================
    // CLICK PICK MEMBERSHIP
    // ==========================================================

    public void clickPickMembership() {

        System.out.println();
        System.out.println(
                "[ACTION] Clicking Pick Membership."
        );


        try {

            appDriver.hideKeyboard();

        } catch (Exception ignored) {
        }


        safeClick(
                PICK_MEMBERSHIP_BUTTON,
                30
        );


        pause(
                2500
        );


        System.out.println(
                "[PASS] Pick Membership clicked successfully."
        );
    }


    // ==========================================================
    // QUICK ELEMENT DISPLAY CHECK
    // ==========================================================

    private boolean isElementDisplayedQuick(
            By locator
    ) {

        try {

            List<WebElement> elements =
                    appDriver.findElements(
                            locator
                    );


            for (WebElement element : elements) {

                try {

                    if (element.isDisplayed()) {

                        return true;
                    }

                } catch (Exception ignored) {
                }
            }


            return false;


        } catch (Exception e) {

            return false;
        }
    }


    // ==========================================================
    // SAFE CLICK
    // ==========================================================

    private void safeClick(
            By locator,
            int timeoutSeconds
    ) {

        try {

            WebElement element =
                    new WebDriverWait(
                            appDriver,
                            Duration.ofSeconds(
                                    timeoutSeconds
                            )
                    ).until(
                            ExpectedConditions
                                    .elementToBeClickable(
                                            locator
                                    )
                    );


            element.click();


        } catch (Exception e) {

            throw new AssertionError(
                    "Unable to click element: "
                            + locator,
                    e
            );
        }
    }


    // ==========================================================
    // WAIT FOR TEXT
    // ==========================================================

    private boolean waitForAnyText(
            int timeoutSeconds,
            String... values
    ) {

        long endTime =
                System.currentTimeMillis()
                        +
                        timeoutSeconds * 1000L;


        while (System.currentTimeMillis()
                < endTime) {

            try {

                String source =
                        appDriver.getPageSource();


                for (String value : values) {

                    if (source.contains(
                            value
                    )) {

                        return true;
                    }
                }


            } catch (Exception ignored) {
            }


            pause(
                    500
            );
        }


        return false;
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
                    "Test interrupted.",
                    e
            );
        }
    }
}