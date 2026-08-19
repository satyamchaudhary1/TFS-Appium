package com.tfs.pages;

import com.tfs.driver.DriverManager;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class SignUpPage extends BasePage {

    private final AndroidDriver driver;

    // ==========================================================
    // LANDING / CREATE ACCOUNT
    // ==========================================================

    private static final By CREATE_ACCOUNT_ACCESSIBILITY =
            AppiumBy.accessibilityId("Create Account");

    private static final By CREATE_ACCOUNT_FALLBACK =
            By.xpath(
                    "//*[@content-desc='Create Account' " +
                            "or @text='Create Account']"
            );


    // ==========================================================
    // CREATE ACCOUNT FORM
    // ==========================================================

    private static final By INPUT_FIELDS =
            AppiumBy.className(
                    "android.widget.EditText"
            );

    private static final int FIELD_FULL_NAME = 0;
    private static final int FIELD_EMAIL = 1;
    private static final int FIELD_PASSWORD = 2;
    private static final int FIELD_CONFIRM_PASSWORD = 3;
    private static final int FIELD_PHONE = 4;


    // ==========================================================
    // REGISTER BUTTON
    // ==========================================================

    private static final By REGISTER_BUTTON =
            AppiumBy.accessibilityId(
                    "Register and Enter Delivery Info"
            );


    public SignUpPage() {

        super();

        this.driver =
                DriverManager.getDriver();
    }


    // ==========================================================
    // CREATE ACCOUNT ENTRY
    // ==========================================================

    public boolean isCreateAccountButtonDisplayed() {

        System.out.println(
                "[STEP] Waiting for Create Account button."
        );

        System.out.println(
                "[EXPECTED] Authentication landing screen should finish rendering and Create Account should become visible."
        );

        try {

            WebDriverWait wait =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(30)
                    );

            Boolean displayed =
                    wait.until(
                            d -> {

                                /*
                                 * Always locate fresh elements on every poll.
                                 * The Auth landing screen can re-render after
                                 * the Data Collection popup is accepted.
                                 */
                                try {

                                    List<WebElement> accessibilityMatches =
                                            d.findElements(
                                                    CREATE_ACCOUNT_ACCESSIBILITY
                                            );

                                    for (WebElement element : accessibilityMatches) {

                                        try {

                                            if (element.isDisplayed()) {

                                                System.out.println(
                                                        "[INFO] Create Account found using accessibility id."
                                                );

                                                return true;
                                            }

                                        } catch (org.openqa.selenium.StaleElementReferenceException ignored) {

                                            /*
                                             * The landing screen re-rendered.
                                             * WebDriverWait will poll again and
                                             * locate a fresh element.
                                             */
                                        }
                                    }


                                    List<WebElement> fallbackMatches =
                                            d.findElements(
                                                    CREATE_ACCOUNT_FALLBACK
                                            );

                                    for (WebElement element : fallbackMatches) {

                                        try {

                                            if (element.isDisplayed()) {

                                                System.out.println(
                                                        "[INFO] Create Account found using fallback locator."
                                                );

                                                return true;
                                            }

                                        } catch (org.openqa.selenium.StaleElementReferenceException ignored) {

                                            /*
                                             * Re-render race. Retry with a
                                             * newly located element.
                                             */
                                        }
                                    }

                                } catch (org.openqa.selenium.StaleElementReferenceException ignored) {

                                    /*
                                     * Entire lookup became stale during the
                                     * React Native re-render. Retry safely.
                                     */
                                } catch (Exception ignored) {

                                    /*
                                     * Temporary Appium/UI lookup issue.
                                     * Continue polling until timeout.
                                     */
                                }

                                return false;
                            }
                    );


            boolean result =
                    Boolean.TRUE.equals(
                            displayed
                    );


            System.out.println(
                    "[RESULT] Create Account Button Ready : "
                            + (result ? "PASS" : "FAIL")
            );


            return result;

        } catch (TimeoutException e) {

            System.out.println(
                    "[FAIL] Create Account button was not displayed within 30 seconds after Data Collection."
            );

            return false;

        } catch (Exception e) {

            System.out.println(
                    "[FAIL] Create Account visibility check failed : "
                            + e.getClass()
                            .getSimpleName()
                            + " - "
                            + e.getMessage()
            );

            return false;
        }
    }


    public void clickCreateAccount() {

        System.out.println(
                "[ACTION] Clicking Create Account."
        );

        if (clickIfDisplayed(
                CREATE_ACCOUNT_ACCESSIBILITY,
                15
        )) {

            System.out.println(
                    "[PASS] Create Account clicked."
            );

            pause(1500);

            return;
        }


        if (clickIfDisplayed(
                CREATE_ACCOUNT_FALLBACK,
                10
        )) {

            System.out.println(
                    "[PASS] Create Account clicked using fallback locator."
            );

            pause(1500);

            return;
        }


        throw new AssertionError(
                "Create Account button was not displayed."
        );
    }


    // ==========================================================
    // CREATE ACCOUNT SCREEN
    // ==========================================================

    public boolean isCreateAccountScreenDisplayed() {

        try {

            new WebDriverWait(
                    driver,
                    Duration.ofSeconds(20)
            ).until(
                    d -> d.findElements(INPUT_FIELDS)
                            .size() >= 5
            );

            return true;

        } catch (Exception e) {

            return false;
        }
    }


    // ==========================================================
    // FIELD DISPLAY VALIDATION
    // ==========================================================

    public boolean isFullNameFieldDisplayed() {

        return isFieldDisplayed(
                FIELD_FULL_NAME
        );
    }


    public boolean isEmailFieldDisplayed() {

        return isFieldDisplayed(
                FIELD_EMAIL
        );
    }


    public boolean isPasswordFieldDisplayed() {

        return isFieldDisplayed(
                FIELD_PASSWORD
        );
    }


    public boolean isConfirmPasswordFieldDisplayed() {

        return isFieldDisplayed(
                FIELD_CONFIRM_PASSWORD
        );
    }


    public boolean isPhoneNumberFieldDisplayed() {

        return isFieldDisplayed(
                FIELD_PHONE
        );
    }


    // ==========================================================
    // ENTER REGISTRATION DETAILS
    // ==========================================================

    public void enterFullName(
            String name
    ) {

        System.out.println(
                "[ACTION] Entering Full Name."
        );

        enterValue(
                FIELD_FULL_NAME,
                name
        );

        System.out.println(
                "[PASS] Full Name entered."
        );
    }


    public void enterEmail(
            String email
    ) {

        System.out.println(
                "[ACTION] Entering unique Email."
        );

        enterValue(
                FIELD_EMAIL,
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
                "[ACTION] Entering Password."
        );

        enterValue(
                FIELD_PASSWORD,
                password
        );

        System.out.println(
                "[PASS] Password entered."
        );
    }


    public void enterConfirmPassword(
            String password
    ) {

        System.out.println(
                "[ACTION] Entering Confirm Password."
        );

        enterValue(
                FIELD_CONFIRM_PASSWORD,
                password
        );

        System.out.println(
                "[PASS] Confirm Password entered."
        );
    }


    public void enterPhoneNumber(
            String phone
    ) {

        System.out.println(
                "[ACTION] Entering Phone Number."
        );

        enterValue(
                FIELD_PHONE,
                phone
        );

        System.out.println(
                "[PASS] Phone Number entered."
        );
    }


    // ==========================================================
    // REGISTER BUTTON
    // ==========================================================

    public boolean isRegisterButtonDisplayed() {

        System.out.println(
                "[ACTION] Looking for Register and Enter Delivery Info button."
        );

        /*
         * Phone field leaves Android keyboard open.
         * Hide it before looking for the button.
         */
        try {

            driver.hideKeyboard();

            System.out.println(
                    "[INFO] Keyboard hidden."
            );

            pause(700);

        } catch (Exception ignored) {
            // Keyboard may already be closed.
        }


        /*
         * First check current viewport.
         */
        if (isDisplayed(REGISTER_BUTTON)) {

            System.out.println(
                    "[PASS] Register button displayed."
            );

            return true;
        }


        /*
         * Button may be below the visible area.
         * Scroll down until it becomes visible.
         */
        System.out.println(
                "[INFO] Register button not visible yet. Scrolling down."
        );

        for (int i = 1; i <= 5; i++) {

            scrollDown();

            pause(600);

            if (isDisplayed(REGISTER_BUTTON)) {

                System.out.println(
                        "[PASS] Register button displayed after scroll."
                );

                return true;
            }
        }


        System.out.println(
                "[FAIL] Register button was not found after scrolling."
        );

        return false;
    }


    public boolean isRegisterButtonEnabled() {

        try {

            WebElement registerButton =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(15)
                    ).until(
                            ExpectedConditions
                                    .visibilityOfElementLocated(
                                            REGISTER_BUTTON
                                    )
                    );

            return registerButton.isEnabled();

        } catch (Exception e) {

            return false;
        }
    }


    public void clickRegister() {

        System.out.println(
                "[ACTION] Clicking Register and Enter Delivery Info."
        );

        try {

            driver.hideKeyboard();

        } catch (Exception ignored) {
        }


        if (!isDisplayed(REGISTER_BUTTON)) {

            for (int i = 1; i <= 5; i++) {

                scrollDown();

                pause(500);

                if (isDisplayed(REGISTER_BUTTON)) {
                    break;
                }
            }
        }


        try {

            WebDriverWait wait =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(15)
                    );

            WebElement registerButton =
                    wait.until(
                            ExpectedConditions.elementToBeClickable(
                                    REGISTER_BUTTON
                            )
                    );


            /*
             * Normal Appium click
             */
            registerButton.click();

            System.out.println(
                    "[INFO] Register click sent."
            );

            pause(2500);


            /*
             * Verify registration actually moved forward.
             */
            if (isDeliveryInformationScreenDisplayed()
                    || isUserAlreadyExistsDisplayed()) {

                System.out.println(
                        "[PASS] Register button clicked successfully."
                );

                return;
            }


            /*
             * Normal click did not cause navigation.
             * Retry at the actual button center.
             */
            System.out.println(
                    "[INFO] Normal Register click did not change screen. Retrying with gesture."
            );


            int centerX =
                    registerButton.getRect().getX()
                            + registerButton.getRect().getWidth() / 2;

            int centerY =
                    registerButton.getRect().getY()
                            + registerButton.getRect().getHeight() / 2;


            driver.executeScript(
                    "mobile: clickGesture",
                    java.util.Map.of(
                            "x", centerX,
                            "y", centerY
                    )
            );


            pause(3000);


            if (isDeliveryInformationScreenDisplayed()
                    || isUserAlreadyExistsDisplayed()) {

                System.out.println(
                        "[PASS] Register button clicked successfully."
                );

                return;
            }


            throw new AssertionError(
                    "Register button was clicked but registration did not continue."
            );



        } catch (AssertionError e) {

            throw e;

        } catch (Exception e) {

            throw new AssertionError(
                    "Unable to click Register and Enter Delivery Info button.",
                    e
            );
        }
    }

    // ==========================================================
    // DELIVERY SCREEN
    // ==========================================================

    public boolean isDeliveryInformationScreenDisplayed() {

        System.out.println(
                "[STEP] Checking Delivery Information screen."
        );


        long endTime =
                System.currentTimeMillis()
                        + 60000;


        while (System.currentTimeMillis()
                < endTime) {

            try {

                String source =
                        driver.getPageSource();


                if (source.contains(
                        "Your delivery details"
                )
                        ||
                        source.contains(
                                "Delivery Details"
                        )
                        ||
                        source.contains(
                                "Search for a Postcode"
                        )
                        ||
                        source.contains(
                                "Enter your address manually"
                        )
                        ||
                        source.contains(
                                "Pick Membership"
                        )) {

                    return true;
                }


                /*
                 * Fail sooner if an account-related
                 * validation is displayed.
                 */
                if (source.contains(
                        "User Already Exists"
                )
                        ||
                        source.contains(
                                "already registered"
                        )
                        ||
                        source.contains(
                                "already exists"
                        )) {

                    return false;
                }

            } catch (Exception ignored) {
            }


            pause(500);
        }


        return false;
    }


    // ==========================================================
    // EXISTING USER ERROR
    // ==========================================================

    public boolean isUserAlreadyExistsDisplayed() {

        try {

            String source =
                    driver.getPageSource();

            return source.contains(
                    "User Already Exists"
            )
                    ||
                    source.contains(
                            "already registered"
                    )
                    ||
                    source.contains(
                            "already exists"
                    );

        } catch (Exception e) {

            return false;
        }
    }


    // ==========================================================
    // PRIVATE HELPERS
    // ==========================================================

    private WebElement getInputField(
            int index
    ) {

        try {

            List<WebElement> fields =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(20)
                    ).until(
                            d -> {

                                List<WebElement> elements =
                                        d.findElements(
                                                INPUT_FIELDS
                                        );

                                return elements.size()
                                        > index
                                        ? elements
                                        : null;
                            }
                    );


            return fields.get(index);


        } catch (TimeoutException e) {

            int found =
                    driver.findElements(
                            INPUT_FIELDS
                    ).size();


            throw new AssertionError(
                    "Expected input field at index "
                            + index
                            + " but only "
                            + found
                            + " input fields were found.",
                    e
            );
        }
    }


    private boolean isFieldDisplayed(
            int index
    ) {

        try {

            WebElement field =
                    getInputField(index);

            return field.isDisplayed();

        } catch (Exception e) {

            return false;
        }
    }


    private void enterValue(
            int index,
            String value
    ) {

        WebElement field =
                getInputField(index);


        field.click();


        try {

            field.clear();

        } catch (Exception ignored) {
        }


        field.sendKeys(
                value
        );
    }


    private boolean clickIfDisplayed(
            By locator,
            int timeoutSeconds
    ) {

        try {

            WebElement element =
                    new WebDriverWait(
                            driver,
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
                    &&
                    elements.get(0)
                            .isDisplayed();


        } catch (Exception e) {

            return false;
        }
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
                    "Test execution interrupted.",
                    e
            );
        }
    }

    private void scrollDown() {

        try {

            org.openqa.selenium.Dimension size =
                    driver.manage()
                            .window()
                            .getSize();

            int left =
                    (int) (size.width * 0.10);

            int top =
                    (int) (size.height * 0.20);

            int width =
                    (int) (size.width * 0.80);

            int height =
                    (int) (size.height * 0.60);


            driver.executeScript(
                    "mobile: scrollGesture",
                    java.util.Map.of(
                            "left", left,
                            "top", top,
                            "width", width,
                            "height", height,
                            "direction", "down",
                            "percent", 0.75
                    )
            );

        } catch (Exception e) {

            System.out.println(
                    "[INFO] Scroll attempt could not be performed: "
                            + e.getMessage()
            );
        }
    }
}