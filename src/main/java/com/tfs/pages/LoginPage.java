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

public class LoginPage extends BasePage {

    private final AndroidDriver driver;

    /*
     * Login input fields
     *
     * Index 0 = Email
     * Index 1 = Password
     */
    private static final By INPUT_FIELDS =
            AppiumBy.className("android.widget.EditText");


    /*
     * Initial Sign In button
     */
    private static final By SIGN_IN_ACCESSIBILITY =
            AppiumBy.accessibilityId("Sign In");

    private static final By SIGN_IN_XPATH =
            By.xpath(
                    "//*[@content-desc='Sign In' " +
                            "or @text='Sign In' " +
                            "or @content-desc='Sign in' " +
                            "or @text='Sign in']"
            );


    /*
     * Data Collection popup
     *
     * We support the common visible Accept labels.
     * Once we have the exact Inspector locator,
     * we can make this locator stricter.
     */
    private static final By DATA_COLLECTION_ACCEPT =
            By.id("android:id/button1");
    private static final By DATA_COLLECTION_TITLE =
            By.id("com.thefragranceshop:id/alert_title");

    private static final By DATA_COLLECTION_MESSAGE =
            By.id("android:id/message");



    private static final By DATA_COLLECTION_DECLINE =
            By.id("android:id/button2");

    /*
     * Final Log In button
     */
    private static final By LOGIN_BUTTON_ACCESSIBILITY =
            AppiumBy.accessibilityId("Log In");

    private static final By LOGIN_BUTTON_XPATH =
            By.xpath(
                    "//*[@content-desc='Log In' " +
                            "or @text='Log In']"
            );


    private static final By FORGOT_PASSWORD =
            AppiumBy.accessibilityId(
                    "Forgot your password"
            );


    /*
     * Android biometric popup
     */
    private static final By BIOMETRIC_OK_BUTTON =
            By.id("android:id/button1");


    /*
     * Login error popup
     */
    private static final By LOGIN_ERROR_POPUP =
            AppiumBy.accessibilityId(
                    "Error, Your email address or password are incorrect."
            );

    private static final By ERROR_OK_BUTTON =
            AppiumBy.accessibilityId("OKAY");


    /*
     * Coach marks
     */
    private static final By COACH_MARK_NEXT =
            By.xpath(
                    "//*[@content-desc='Next' or @text='Next']"
            );

    private static final By COACH_MARK_DONE =
            By.xpath(
                    "//*[@content-desc='Done' or @text='Done']"
            );


    public LoginPage() {

        super();

        this.driver =
                DriverManager.getDriver();
    }


    // ==========================================================
    // DATA COLLECTION
    // ==========================================================

    public boolean acceptDataCollectionIfDisplayed() {

        System.out.println(
                "[ACTION] Checking Data Collection popup..."
        );

        long endTime =
                System.currentTimeMillis() + 20000;

        while (System.currentTimeMillis() < endTime) {

            try {

                List<WebElement> acceptButtons =
                        driver.findElements(
                                DATA_COLLECTION_ACCEPT
                        );

                if (!acceptButtons.isEmpty()) {

                    WebElement acceptButton =
                            acceptButtons.get(0);

                    if (acceptButton.isDisplayed()
                            && acceptButton.isEnabled()) {

                        System.out.println(
                                "[PASS] Data Collection popup displayed."
                        );

                        acceptButton.click();

                        pause(1500);

                        System.out.println(
                                "[PASS] ACCEPT button clicked."
                        );

                        return true;
                    }
                }

            } catch (Exception ignored) {
            }

            pause(500);
        }

        System.out.println(
                "[INFO] Data Collection popup was not displayed."
        );

        return false;
    }
    // ==========================================================
    // SIGN IN ENTRY BUTTON
    // ==========================================================

    public boolean isSignInButtonDisplayed() {

        return isDisplayed(
                SIGN_IN_ACCESSIBILITY
        ) || isDisplayed(
                SIGN_IN_XPATH
        );
    }


    public void clickSignIn() {

        System.out.println(
                "[ACTION] Clicking Sign In."
        );

        if (clickIfDisplayed(
                SIGN_IN_ACCESSIBILITY,
                15
        )) {

            System.out.println(
                    "[PASS] Sign In clicked."
            );

            pause(1500);

            return;
        }


        if (clickIfDisplayed(
                SIGN_IN_XPATH,
                10
        )) {

            System.out.println(
                    "[PASS] Sign In clicked using fallback locator."
            );

            pause(1500);

            return;
        }


        throw new AssertionError(
                "Sign In button was not displayed."
        );
    }


    // ==========================================================
    // LOGIN SCREEN VALIDATION
    // ==========================================================

    public boolean isLoginScreenDisplayed() {

        System.out.println(
                "[STEP] Checking Login screen."
        );

        long endTime =
                System.currentTimeMillis() + 30000;

        while (System.currentTimeMillis() < endTime) {

            try {

                String source =
                        driver.getPageSource();

                boolean hasEmail =
                        source.contains("Email");

                boolean hasPassword =
                        source.contains("Password");

                boolean hasLogin =
                        source.contains("Log In");

                if (hasEmail
                        && hasPassword
                        && hasLogin) {

                    return true;
                }

            } catch (Exception ignored) {
            }

            pause(500);
        }

        return false;
    }


    public boolean isForgotPasswordDisplayed() {

        return isDisplayed(
                FORGOT_PASSWORD
        ) || pageContains(
                "Forgot your password"
        );
    }


    // ==========================================================
    // EMAIL / PASSWORD
    // ==========================================================

    public void enterEmail(
            String email
    ) {

        System.out.println(
                "[ACTION] Entering email."
        );

        WebElement emailField =
                getInputField(0);

        emailField.click();

        try {
            emailField.clear();
        } catch (Exception ignored) {
        }

        emailField.sendKeys(
                email
        );

        System.out.println(
                "[PASS] Email entered."
        );
    }


    public void enterPassword(
            String password
    ) {

        System.out.println(
                "[ACTION] Entering password."
        );

        WebElement passwordField =
                getInputField(1);

        passwordField.click();

        try {
            passwordField.clear();
        } catch (Exception ignored) {
        }

        passwordField.sendKeys(
                password
        );

        System.out.println(
                "[PASS] Password entered."
        );
    }


    public void clearFields() {

        List<WebElement> fields =
                driver.findElements(
                        INPUT_FIELDS
                );

        for (WebElement field : fields) {

            try {

                field.click();
                field.clear();

            } catch (Exception ignored) {
            }
        }
    }


    // ==========================================================
    // LOG IN
    // ==========================================================

    public void clickLogin() {

        System.out.println(
                "[ACTION] Clicking Log In button..."
        );

        /*
         * Sometimes Android keyboard remains active after
         * entering the password and can interfere with the tap.
         */
        try {

            driver.hideKeyboard();

            System.out.println(
                    "[INFO] Keyboard hidden before Log In."
            );

            pause(
                    500
            );

        } catch (Exception ignored) {

            System.out.println(
                    "[INFO] Keyboard was already hidden."
            );
        }


        /*
         * Attempt 1 - always locate a FRESH Log In element.
         */
        WebElement loginButton =
                findFreshLoginButton(
                        15
                );

        if (loginButton == null) {

            throw new AssertionError(
                    "Log In button was not found."
            );
        }


        try {

            loginButton.click();

            System.out.println(
                    "[INFO] Log In click sent."
            );

        } catch (Exception e) {

            System.out.println(
                    "[WARN] Normal Log In click failed : "
                            + e.getClass()
                            .getSimpleName()
                            + " - "
                            + e.getMessage()
            );
        }


        /*
         * Give the app enough time to process the first click.
         * Do not reuse the original WebElement after this point,
         * because React Native may re-render the Log In button.
         */
        pause(
                3000
        );


        if (!isLoginScreenCurrentlyDisplayed()
                || isLoginErrorDisplayed()) {

            System.out.println(
                    "[PASS] Log In button clicked successfully."
            );

            return;
        }


        /*
         * Attempt 2 - fallback gesture.
         *
         * IMPORTANT:
         * Re-find the button before EVERY gesture attempt.
         * The old element may be stale after the first click.
         */
        System.out.println(
                "[INFO] Normal click did not trigger Login. Retrying with fresh-element gesture."
        );


        Exception lastGestureException =
                null;

        for (int attempt = 1;
             attempt <= 3;
             attempt++) {

            /*
             * Login may finish while we are preparing the fallback.
             */
            if (!isLoginScreenCurrentlyDisplayed()
                    || isLoginErrorDisplayed()) {

                System.out.println(
                        "[PASS] Login transition completed before fallback gesture."
                );

                return;
            }


            System.out.println(
                    "[INFO] Gesture Retry Attempt : "
                            + attempt
                            + "/3"
            );


            WebElement freshLoginButton =
                    findFreshLoginButton(
                            5
                    );


            if (freshLoginButton == null) {

                System.out.println(
                        "[INFO] Fresh Log In element not found on gesture attempt "
                                + attempt
                                + ". Re-checking page transition."
                );

                pause(
                        500
                );

                if (!isLoginScreenCurrentlyDisplayed()
                        || isLoginErrorDisplayed()) {

                    System.out.println(
                            "[PASS] Login transition completed."
                    );

                    return;
                }

                continue;
            }


            try {

                org.openqa.selenium.Rectangle loginRect =
                        freshLoginButton.getRect();

                int centerX =
                        loginRect.getX()
                                + loginRect.getWidth() / 2;

                int centerY =
                        loginRect.getY()
                                + loginRect.getHeight() / 2;


                System.out.println(
                        "[INFO] Fresh Log In Bounds : [x="
                                + loginRect.getX()
                                + ", y="
                                + loginRect.getY()
                                + ", width="
                                + loginRect.getWidth()
                                + ", height="
                                + loginRect.getHeight()
                                + "]"
                );

                System.out.println(
                        "[INFO] Fresh Log In Center : "
                                + centerX
                                + ", "
                                + centerY
                );


                driver.executeScript(
                        "mobile: clickGesture",
                        java.util.Map.of(
                                "x",
                                centerX,
                                "y",
                                centerY
                        )
                );


                System.out.println(
                        "[INFO] Gesture click sent to FRESH Log In button."
                );


                pause(
                        2500
                );


                if (!isLoginScreenCurrentlyDisplayed()
                        || isLoginErrorDisplayed()) {

                    System.out.println(
                            "[PASS] Log In button clicked successfully."
                    );

                    return;
                }


                System.out.println(
                        "[INFO] Login screen still displayed after gesture attempt "
                                + attempt
                                + "."
                );

            } catch (org.openqa.selenium.StaleElementReferenceException e) {

                lastGestureException =
                        e;

                System.out.println(
                        "[WARN] Log In element became stale on gesture attempt "
                                + attempt
                                + ". Re-finding element before retry."
                );

                pause(
                        400
                );

            } catch (Exception e) {

                lastGestureException =
                        e;

                System.out.println(
                        "[WARN] Gesture attempt "
                                + attempt
                                + " failed : "
                                + e.getClass()
                                .getSimpleName()
                                + " - "
                                + e.getMessage()
                );

                pause(
                        400
                );
            }
        }


        /*
         * One final page-state check before declaring failure.
         */
        if (!isLoginScreenCurrentlyDisplayed()
                || isLoginErrorDisplayed()) {

            System.out.println(
                    "[PASS] Log In action completed."
            );

            return;
        }


        System.out.println(
                "[FAIL] Log In button was tapped but Login screen did not change."
        );


        if (lastGestureException != null) {

            throw new AssertionError(
                    "Unable to click Log In button after fresh-element retries.",
                    lastGestureException
            );
        }


        throw new AssertionError(
                "Log In button did not perform login. Login screen is still displayed."
        );
    }

    // ==========================================================
    // LOGIN ERROR
    // ==========================================================

    public boolean isLoginErrorDisplayed() {

        try {

            List<WebElement> errors =
                    driver.findElements(
                            LOGIN_ERROR_POPUP
                    );

            if (!errors.isEmpty()
                    && errors.get(0).isDisplayed()) {

                return true;
            }

        } catch (Exception ignored) {
        }


        return pageContains(
                "Your email address or password are incorrect"
        );
    }


    public String getLoginErrorMessage() {

        if (isLoginErrorDisplayed()) {

            return "Your email address or password are incorrect.";
        }

        return "Login error message not displayed.";
    }


    public void closeLoginErrorPopup() {

        if (!isLoginErrorDisplayed()) {
            return;
        }


        System.out.println(
                "[APP MESSAGE] "
                        + getLoginErrorMessage()
        );


        if (clickIfDisplayed(
                ERROR_OK_BUTTON,
                10
        )) {

            pause(1000);

            System.out.println(
                    "[PASS] Login error popup closed."
            );
        }
    }


    // ==========================================================
    // BIOMETRIC POPUP
    // ==========================================================

    public void handleBiometricPopupIfDisplayed() {

        try {

            if (!pageContains(
                    "Biometric Authentication"
            )) {

                System.out.println(
                        "[INFO] Biometric popup not displayed."
                );

                return;
            }


            System.out.println(
                    "[INFO] Biometric popup displayed."
            );


            if (clickIfDisplayed(
                    BIOMETRIC_OK_BUTTON,
                    5
            )) {

                System.out.println(
                        "[PASS] Biometric popup closed."
                );

                pause(1000);
            }

        } catch (Exception e) {

            System.out.println(
                    "[INFO] Biometric popup not displayed."
            );
        }
    }


    // ==========================================================
    // HOME COACH MARKS
    // ==========================================================

    public void handleHomeCoachMarksIfDisplayed() {

        System.out.println(
                "[ACTION] Checking home coach marks."
        );


        for (int i = 1; i <= 6; i++) {

            try {

                if (clickIfDisplayed(
                        COACH_MARK_NEXT,
                        2
                )) {

                    System.out.println(
                            "[INFO] Coach mark Next clicked."
                    );

                    pause(800);

                    continue;
                }


                if (clickIfDisplayed(
                        COACH_MARK_DONE,
                        2
                )) {

                    System.out.println(
                            "[INFO] Coach mark Done clicked."
                    );

                    pause(800);

                    break;
                }


                /*
                 * Fallback only if coach-mark text exists
                 * but is not directly clickable.
                 *
                 * Uses screen percentage instead of
                 * hardcoded device coordinates.
                 */
                if (pageContains("Next")) {

                    tapScreenPercentage(
                            0.80,
                            0.20
                    );

                    pause(800);

                    continue;
                }


                if (pageContains("Done")) {

                    tapScreenPercentage(
                            0.75,
                            0.85
                    );

                    pause(800);

                    break;
                }


                break;

            } catch (Exception ignored) {

                break;
            }
        }
    }


    public void handlePostLoginPopups() {

        handleBiometricPopupIfDisplayed();

        handleHomeCoachMarksIfDisplayed();

        handleBiometricPopupIfDisplayed();
    }


    // ==========================================================
    // HOME SCREEN VALIDATION
    // ==========================================================

    public boolean isHomeScreenDisplayed() {

        System.out.println(
                "[STEP] Checking Home screen."
        );


        long endTime =
                System.currentTimeMillis() + 30000;


        while (System.currentTimeMillis() < endTime) {

            try {

                String source =
                        driver.getPageSource();


                boolean hasWomen =
                        source.contains("Women's");

                boolean hasMen =
                        source.contains("Men's");

                boolean hasGiftSets =
                        source.contains("Gift Sets");

                boolean hasNewIn =
                        source.contains("New In");


                /*
                 * We don't require every item because
                 * screen content may differ by viewport.
                 */
                int matches = 0;

                if (hasWomen) {
                    matches++;
                }

                if (hasMen) {
                    matches++;
                }

                if (hasGiftSets) {
                    matches++;
                }

                if (hasNewIn) {
                    matches++;
                }


                if (matches >= 2) {

                    return true;
                }

            } catch (Exception ignored) {
            }


            pause(500);
        }


        return false;
    }


    private WebElement findFreshLoginButton(
            int timeoutSeconds
    ) {

        /*
         * Always return a newly located element.
         * Never cache/reuse the Log In WebElement across a UI re-render.
         */
        try {

            WebDriverWait wait =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(
                                    timeoutSeconds
                            )
                    );

            return wait.until(
                    ExpectedConditions.elementToBeClickable(
                            LOGIN_BUTTON_ACCESSIBILITY
                    )
            );

        } catch (Exception accessibilityException) {

            try {

                WebDriverWait wait =
                        new WebDriverWait(
                                driver,
                                Duration.ofSeconds(
                                        timeoutSeconds
                                )
                        );

                return wait.until(
                        ExpectedConditions.elementToBeClickable(
                                LOGIN_BUTTON_XPATH
                        )
                );

            } catch (Exception xpathException) {

                return null;
            }
        }
    }


    // ==========================================================
    // PRIVATE HELPERS
    // ==========================================================

    private WebElement getInputField(
            int index
    ) {

        WebDriverWait wait =
                new WebDriverWait(
                        driver,
                        Duration.ofSeconds(20)
                );


        wait.until(
                d -> d.findElements(INPUT_FIELDS).size()
                        > index
        );


        List<WebElement> fields =
                driver.findElements(
                        INPUT_FIELDS
                );


        if (fields.size() <= index) {

            throw new AssertionError(
                    "Input field not found at index: "
                            + index
            );
        }


        return fields.get(index);
    }


    private boolean clickIfDisplayed(
            By locator,
            int timeoutSeconds
    ) {

        try {

            WebDriverWait wait =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(
                                    timeoutSeconds
                            )
                    );


            WebElement element =
                    wait.until(
                            ExpectedConditions
                                    .elementToBeClickable(
                                            locator
                                    )
                    );


            element.click();

            return true;

        } catch (Exception e) {

            return false;
        }
    }


    private boolean isDisplayed(
            By locator
    ) {

        try {

            List<WebElement> elements =
                    driver.findElements(
                            locator
                    );


            return !elements.isEmpty()
                    && elements.get(0)
                    .isDisplayed();

        } catch (Exception e) {

            return false;
        }
    }


    private boolean pageContains(
            String text
    ) {

        try {

            return driver.getPageSource()
                    .contains(text);

        } catch (Exception e) {

            return false;
        }
    }


    private void tapScreenPercentage(
            double xPercentage,
            double yPercentage
    ) {

        Dimension size =
                driver.manage()
                        .window()
                        .getSize();


        int x =
                (int) (
                        size.width
                                * xPercentage
                );

        int y =
                (int) (
                        size.height
                                * yPercentage
                );


        driver.executeScript(
                "mobile: clickGesture",
                java.util.Map.of(
                        "x", x,
                        "y", y
                )
        );
    }


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
    private boolean isLoginScreenCurrentlyDisplayed() {

        try {

            String source =
                    driver.getPageSource();

            return source.contains("Login to your account")
                    && source.contains("Log In");

        } catch (Exception e) {

            return false;
        }
    }
}