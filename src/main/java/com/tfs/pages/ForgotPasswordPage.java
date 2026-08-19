package com.tfs.pages;

import com.tfs.driver.DriverManager;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class ForgotPasswordPage extends BasePage {

    private final AndroidDriver appDriver;


    // ==========================================================
    // LOCATORS
    // ==========================================================

    /*
     * Inspector confirmed:
     *
     * class     = android.widget.TextView
     * text      = Forgot your password
     * clickable = false
     */
    private static final By FORGOT_PASSWORD_LINK =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().text(\"Forgot your password\")"
            );


    /*
     * If the actual TextView itself is not clickable,
     * find the first clickable ancestor.
     */
    private static final By FORGOT_PASSWORD_CLICKABLE_PARENT =
            By.xpath(
                    "//*[@text='Forgot your password']/ancestor::*[@clickable='true'][1]"
            );


    private static final By INPUT_FIELDS =
            AppiumBy.className(
                    "android.widget.EditText"
            );


    private static final By RESET_PASSWORD_BUTTON =
            AppiumBy.accessibilityId(
                    "Reset Password"
            );


    private static final By RESET_PASSWORD_TEXT =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().text(\"Reset Password\")"
            );


    private static final By POPUP_OKAY_UPPER =
            AppiumBy.accessibilityId(
                    "OKAY"
            );


    private static final By POPUP_OKAY_TITLE =
            AppiumBy.accessibilityId(
                    "Okay"
            );


    private static final By ANDROID_OK_BUTTON =
            By.id(
                    "android:id/button1"
            );


    // ==========================================================
    // CONSTRUCTOR
    // ==========================================================

    public ForgotPasswordPage() {

        super();

        this.appDriver =
                DriverManager.getDriver();
    }


    // ==========================================================
    // FORGOT PASSWORD LINK DISPLAY
    // ==========================================================

    public boolean isForgotPasswordLinkDisplayed() {

        System.out.println(
                "[ACTION] Looking for exact Forgot your password link."
        );


        long endTime =
                System.currentTimeMillis()
                        + 20000;


        while (System.currentTimeMillis()
                < endTime) {

            try {

                List<WebElement> elements =
                        appDriver.findElements(
                                FORGOT_PASSWORD_LINK
                        );


                if (!elements.isEmpty()) {

                    WebElement element =
                            elements.get(0);


                    if (element.isDisplayed()) {

                        System.out.println(
                                "[PASS] Forgot your password link detected."
                        );


                        printElementDetails(
                                "Forgot your password",
                                element
                        );


                        return true;
                    }
                }


            } catch (Exception ignored) {
            }


            pause(
                    300
            );
        }


        System.out.println(
                "[FAIL] Forgot your password link was not detected."
        );


        return false;
    }


    // ==========================================================
    // CLICK FORGOT PASSWORD
    // ==========================================================

    public void clickForgotPassword() {

        System.out.println();
        System.out.println(
                "[ACTION] Clicking Forgot your password."
        );


        WebElement textElement =
                waitForVisibleElement(
                        FORGOT_PASSWORD_LINK,
                        20
                );


        if (textElement == null) {

            throw new AssertionError(
                    "Forgot your password link was not found."
            );
        }


        printElementDetails(
                "Forgot your password",
                textElement
        );


        // ======================================================
        // ATTEMPT 1 - CLICK TEXT DIRECTLY IF CLICKABLE
        // ======================================================

        try {

            String clickable =
                    textElement.getAttribute(
                            "clickable"
                    );


            if ("true".equalsIgnoreCase(
                    clickable
            )) {

                System.out.println(
                        "[ACTION] Link is clickable. Using normal click."
                );


                textElement.click();


                if (waitForForgotPasswordScreenQuick(
                        5
                )) {

                    System.out.println(
                            "[PASS] Forgot your password clicked successfully."
                    );

                    return;
                }
            }


        } catch (Exception e) {

            System.out.println(
                    "[INFO] Direct link click was not available."
            );
        }


        // ======================================================
        // ATTEMPT 2 - CLICK CLICKABLE PARENT
        // ======================================================

        try {

            List<WebElement> parents =
                    appDriver.findElements(
                            FORGOT_PASSWORD_CLICKABLE_PARENT
                    );


            if (!parents.isEmpty()) {

                WebElement parent =
                        parents.get(0);


                if (parent.isDisplayed()) {

                    System.out.println(
                            "[INFO] TextView itself is not clickable."
                    );


                    System.out.println(
                            "[ACTION] Clicking clickable parent element."
                    );


                    printElementDetails(
                            "Forgot Password clickable parent",
                            parent
                    );


                    parent.click();


                    if (waitForForgotPasswordScreenQuick(
                            5
                    )) {

                        System.out.println(
                                "[PASS] Forgot your password clicked using parent element."
                        );

                        return;
                    }
                }
            }


        } catch (Exception e) {

            System.out.println(
                    "[INFO] Clickable parent was not available."
            );
        }


        // ======================================================
        // ATTEMPT 3 - ELEMENT CENTER GESTURE
        // ======================================================

        System.out.println(
                "[ACTION] Using element-center gesture."
        );


        /*
         * Re-locate before gesture to avoid stale reference.
         */
        textElement =
                waitForVisibleElement(
                        FORGOT_PASSWORD_LINK,
                        5
                );


        if (textElement == null) {

            throw new AssertionError(
                    "Forgot your password element disappeared before gesture click."
            );
        }


        int centerX =
                textElement
                        .getRect()
                        .getX()
                        +
                        textElement
                                .getRect()
                                .getWidth()
                                / 2;


        int centerY =
                textElement
                        .getRect()
                        .getY()
                        +
                        textElement
                                .getRect()
                                .getHeight()
                                / 2;


        System.out.println(
                "[INFO] Element center X : "
                        + centerX
        );


        System.out.println(
                "[INFO] Element center Y : "
                        + centerY
        );


        appDriver.executeScript(
                "mobile: clickGesture",
                Map.of(
                        "x", centerX,
                        "y", centerY
                )
        );


        System.out.println(
                "[INFO] Gesture click sent."
        );


        // ======================================================
        // FINAL DESTINATION VERIFICATION
        // ======================================================

        if (!waitForForgotPasswordScreenQuick(
                10
        )) {

            throw new AssertionError(
                    "Forgot your password was detected and tapped, "
                            + "but Reset Password screen did not open."
            );
        }


        System.out.println(
                "[PASS] Forgot your password clicked successfully using gesture."
        );
    }


    // ==========================================================
    // FORGOT PASSWORD SCREEN
    // ==========================================================

    public boolean isForgotPasswordScreenDisplayed() {

        System.out.println(
                "[ACTION] Validating Reset Password screen."
        );


        boolean displayed =
                waitForForgotPasswordScreenQuick(
                        30
                );


        if (displayed) {

            System.out.println(
                    "[PASS] Reset Password screen displayed."
            );


            List<WebElement> fields =
                    appDriver.findElements(
                            INPUT_FIELDS
                    );


            System.out.println(
                    "[INFO] Input field count : "
                            + fields.size()
            );


            System.out.println(
                    "[INFO] Reset Password button detected : "
                            + (
                            isElementDisplayed(
                                    RESET_PASSWORD_BUTTON
                            )
                                    ||
                                    isElementDisplayed(
                                            RESET_PASSWORD_TEXT
                                    )
                    )
            );


        } else {

            System.out.println(
                    "[FAIL] Reset Password screen was not displayed."
            );
        }


        return displayed;
    }


    // ==========================================================
    // ENTER EMAIL
    // ==========================================================

    public void enterEmail(
            String email
    ) {

        System.out.println(
                "[ACTION] Entering email: "
                        + email
        );


        WebElement emailField =
                getInputField(
                        0
                );


        printElementDetails(
                "Forgot Password Email",
                emailField
        );


        emailField.click();


        try {

            emailField.clear();

            System.out.println(
                    "[INFO] Existing email value cleared."
            );

        } catch (Exception ignored) {

            System.out.println(
                    "[INFO] Email field was already empty."
            );
        }


        emailField.sendKeys(
                email
        );


        String enteredValue =
                emailField.getText();


        System.out.println(
                "[INFO] Email field value after entry: "
                        + enteredValue
        );


        if (enteredValue == null
                || enteredValue.trim().isEmpty()) {

            throw new AssertionError(
                    "Email value was not entered."
            );
        }


        System.out.println(
                "[PASS] Email entered successfully."
        );
    }


    // ==========================================================
    // RESET PASSWORD BUTTON
    // ==========================================================

    public boolean isResetPasswordButtonDisplayed() {

        System.out.println(
                "[ACTION] Checking Reset Password button."
        );


        boolean displayed =
                waitForEitherElement(
                        RESET_PASSWORD_BUTTON,
                        RESET_PASSWORD_TEXT,
                        20
                );


        if (displayed) {

            System.out.println(
                    "[PASS] Reset Password button displayed."
            );

        } else {

            System.out.println(
                    "[FAIL] Reset Password button was not displayed."
            );
        }


        return displayed;
    }


    // ==========================================================
    // CLICK RESET PASSWORD
    // ==========================================================

    public void clickResetPassword() {

        System.out.println(
                "[ACTION] Clicking Reset Password."
        );


        try {

            appDriver.hideKeyboard();

            System.out.println(
                    "[INFO] Keyboard hidden."
            );

        } catch (Exception ignored) {

            System.out.println(
                    "[INFO] Keyboard was already hidden."
            );
        }


        if (isElementDisplayed(
                RESET_PASSWORD_BUTTON
        )) {

            System.out.println(
                    "[INFO] Using Reset Password accessibility locator."
            );


            safeClick(
                    RESET_PASSWORD_BUTTON,
                    30
            );


        } else {

            System.out.println(
                    "[INFO] Using Reset Password text locator."
            );


            safeClick(
                    RESET_PASSWORD_TEXT,
                    30
            );
        }


        System.out.println(
                "[PASS] Reset Password click sent."
        );
    }


    // ==========================================================
    // USER NOT FOUND
    // ==========================================================

    public boolean isUserNotFoundPopupDisplayed() {

        System.out.println(
                "[ACTION] Waiting for User Not Found popup."
        );


        boolean displayed =
                waitForAnyText(
                        25,
                        "User Not Found",
                        "not registered",
                        "could not be fetched"
                );


        if (displayed) {

            System.out.println(
                    "[PASS] User Not Found popup displayed."
            );

        } else {

            System.out.println(
                    "[FAIL] User Not Found popup was not displayed."
            );
        }


        return displayed;
    }


    // ==========================================================
    // SUCCESS POPUP
    // ==========================================================

    public boolean isSuccessPopupDisplayed() {

        System.out.println(
                "[ACTION] Waiting for Reset Password success popup."
        );


        boolean displayed =
                waitForAnyText(
                        25,
                        "Send!",
                        "instructions",
                        "create a new password"
                );


        if (displayed) {

            System.out.println(
                    "[PASS] Reset Password success popup displayed."
            );

        } else {

            System.out.println(
                    "[FAIL] Reset Password success popup was not displayed."
            );
        }


        return displayed;
    }


    // ==========================================================
    // POPUP MESSAGE
    // ==========================================================

    public String getPopupMessage() {

        try {

            String source =
                    appDriver.getPageSource();


            if (source.contains(
                    "User Not Found"
            )) {

                return "User Not Found - It looks like this email ID is not registered with us.";
            }


            if (source.contains(
                    "Send!"
            )) {

                return "Send! - Reset password instructions popup displayed.";
            }


            if (source.contains(
                    "create a new password"
            )) {

                return "Reset password instructions to create a new password displayed.";
            }


        } catch (Exception ignored) {
        }


        return "Exact popup message could not be captured.";
    }


    // ==========================================================
    // CLOSE POPUP
    // ==========================================================

    public void clickOkayIfDisplayed() {

        System.out.println(
                "[ACTION] Looking for popup confirmation button."
        );


        if (isElementDisplayed(
                POPUP_OKAY_UPPER
        )) {

            System.out.println(
                    "[INFO] OKAY button detected."
            );


            safeClick(
                    POPUP_OKAY_UPPER,
                    10
            );


            System.out.println(
                    "[PASS] Popup closed using OKAY."
            );


            return;
        }


        if (isElementDisplayed(
                POPUP_OKAY_TITLE
        )) {

            System.out.println(
                    "[INFO] Okay button detected."
            );


            safeClick(
                    POPUP_OKAY_TITLE,
                    10
            );


            System.out.println(
                    "[PASS] Popup closed using Okay."
            );


            return;
        }


        if (isElementDisplayed(
                ANDROID_OK_BUTTON
        )) {

            System.out.println(
                    "[INFO] Android OK button detected."
            );


            safeClick(
                    ANDROID_OK_BUTTON,
                    10
            );


            System.out.println(
                    "[PASS] Popup closed using Android OK."
            );


            return;
        }


        System.out.println(
                "[INFO] Popup confirmation button was not displayed."
        );
    }


    // ==========================================================
    // BACK TO LOGIN
    // ==========================================================

    public void navigateBackToLogin() {

        System.out.println(
                "[ACTION] Navigating back to Login screen."
        );


        try {

            appDriver.hideKeyboard();

        } catch (Exception ignored) {
        }


        appDriver.navigate()
                .back();


        pause(
                800
        );


        System.out.println(
                "[PASS] Back navigation completed."
        );
    }


    // ==========================================================
    // QUICK FORGOT PASSWORD SCREEN CHECK
    // ==========================================================

    private boolean waitForForgotPasswordScreenQuick(
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
                                INPUT_FIELDS
                        );


                boolean emailFieldDisplayed =
                        !fields.isEmpty()
                                &&
                                fields.get(0)
                                        .isDisplayed();


                boolean resetDisplayed =
                        isElementDisplayed(
                                RESET_PASSWORD_BUTTON
                        )
                                ||
                                isElementDisplayed(
                                        RESET_PASSWORD_TEXT
                                );


                if (emailFieldDisplayed
                        && resetDisplayed) {

                    return true;
                }


            } catch (Exception ignored) {
            }


            pause(
                    300
            );
        }


        return false;
    }


    // ==========================================================
    // INPUT FIELD
    // ==========================================================

    private WebElement getInputField(
            int index
    ) {

        long endTime =
                System.currentTimeMillis()
                        + 20000;


        while (System.currentTimeMillis()
                < endTime) {

            try {

                List<WebElement> fields =
                        appDriver.findElements(
                                INPUT_FIELDS
                        );


                if (fields.size()
                        > index) {

                    WebElement element =
                            fields.get(
                                    index
                            );


                    if (element.isDisplayed()) {

                        return element;
                    }
                }


            } catch (Exception ignored) {
            }


            pause(
                    300
            );
        }


        throw new AssertionError(
                "Input field not found at index: "
                        + index
        );
    }


    // ==========================================================
    // WAIT FOR VISIBLE ELEMENT
    // ==========================================================

    private WebElement waitForVisibleElement(
            By locator,
            int timeoutSeconds
    ) {

        long endTime =
                System.currentTimeMillis()
                        + timeoutSeconds * 1000L;


        while (System.currentTimeMillis()
                < endTime) {

            try {

                List<WebElement> elements =
                        appDriver.findElements(
                                locator
                        );


                for (WebElement element : elements) {

                    try {

                        if (element.isDisplayed()) {

                            return element;
                        }

                    } catch (Exception ignored) {
                    }
                }


            } catch (Exception ignored) {
            }


            pause(
                    250
            );
        }


        return null;
    }


    // ==========================================================
    // ELEMENT DETAILS - CONSOLE
    // ==========================================================

    private void printElementDetails(
            String elementName,
            WebElement element
    ) {

        System.out.println();
        System.out.println(
                "--------------- ELEMENT DETAILS ---------------"
        );


        System.out.println(
                "[ELEMENT] Name       : "
                        + elementName
        );


        System.out.println(
                "[ELEMENT] Class      : "
                        + safeAttribute(
                        element,
                        "class"
                )
        );


        System.out.println(
                "[ELEMENT] Text       : "
                        + safeText(
                        element
                )
        );


        System.out.println(
                "[ELEMENT] Resource ID: "
                        + safeAttribute(
                        element,
                        "resource-id"
                )
        );


        System.out.println(
                "[ELEMENT] ContentDesc: "
                        + safeAttribute(
                        element,
                        "content-desc"
                )
        );


        System.out.println(
                "[ELEMENT] Clickable  : "
                        + safeAttribute(
                        element,
                        "clickable"
                )
        );


        System.out.println(
                "[ELEMENT] Enabled    : "
                        + safeAttribute(
                        element,
                        "enabled"
                )
        );


        try {

            System.out.println(
                    "[ELEMENT] Displayed  : "
                            + element.isDisplayed()
            );

        } catch (Exception e) {

            System.out.println(
                    "[ELEMENT] Displayed  : UNKNOWN"
            );
        }


        try {

            System.out.println(
                    "[ELEMENT] Bounds     : "
                            + element.getRect()
            );

        } catch (Exception e) {

            System.out.println(
                    "[ELEMENT] Bounds     : UNKNOWN"
            );
        }


        System.out.println(
                "-----------------------------------------------"
        );
        System.out.println();
    }


    // ==========================================================
    // SAFE ATTRIBUTE
    // ==========================================================

    private String safeAttribute(
            WebElement element,
            String name
    ) {

        try {

            String value =
                    element.getAttribute(
                            name
                    );


            return value == null
                    || value.isBlank()
                    ? "<EMPTY>"
                    : value;


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
                    || value.isBlank()
                    ? "<EMPTY>"
                    : value;


        } catch (Exception e) {

            return "<NOT AVAILABLE>";
        }
    }


    // ==========================================================
    // ELEMENT DISPLAY
    // ==========================================================

    private boolean isElementDisplayed(
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
    // WAIT FOR EITHER
    // ==========================================================

    private boolean waitForEitherElement(
            By first,
            By second,
            int timeoutSeconds
    ) {

        long endTime =
                System.currentTimeMillis()
                        + timeoutSeconds * 1000L;


        while (System.currentTimeMillis()
                < endTime) {

            if (isElementDisplayed(
                    first
            )
                    ||
                    isElementDisplayed(
                            second
                    )) {

                return true;
            }


            pause(
                    300
            );
        }


        return false;
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
    // WAIT FOR ANY TEXT
    // ==========================================================

    private boolean waitForAnyText(
            int timeoutSeconds,
            String... values
    ) {

        long endTime =
                System.currentTimeMillis()
                        + timeoutSeconds * 1000L;


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
                    350
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
                    "Forgot Password flow interrupted.",
                    e
            );
        }
    }
}