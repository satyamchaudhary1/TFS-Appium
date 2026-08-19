package com.tfs.flows;

import com.tfs.driver.DriverManager;
import com.tfs.pages.LoginPage;
import com.tfs.pages.SplashPage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Map;

public class AuthStartupFlow {

    private final AndroidDriver driver;

    private final SplashPage splashPage;
    private final LoginPage loginPage;


    // ==========================================================
    // STARTUP LOCATORS
    // ==========================================================

    private static final By MEDIA_PERMISSION_ALLOW =
            By.id(
                    "com.android.permissioncontroller:id/permission_allow_button"
            );


    private static final By AUTH_VIDEO =
            By.id(
                    "com.thefragranceshop:id/exo_content_frame"
            );


    private static final By CONTINUE_BUTTON =
            AppiumBy.accessibilityId(
                    "Continue"
            );


    private static final By DATA_COLLECTION_ACCEPT =
            By.id(
                    "android:id/button1"
            );


    private static final By SIGN_IN_BUTTON =
            AppiumBy.accessibilityId(
                    "Sign In"
            );


    private static final By CREATE_ACCOUNT_BUTTON =
            AppiumBy.accessibilityId(
                    "Create Account"
            );


    // ==========================================================
    // CONSTRUCTOR
    // ==========================================================

    public AuthStartupFlow() {

        this.driver =
                DriverManager.getDriver();

        this.splashPage =
                new SplashPage();

        this.loginPage =
                new LoginPage();
    }


    // ==========================================================
    // TC_002
    // WAIT FOR AUTH VIDEO
    // ==========================================================

    public void waitForAuthVideoReady() {

        System.out.println();
        System.out.println(
                "[STARTUP] Waiting for Auth Video screen."
        );


        long timeout =
                System.currentTimeMillis()
                        + 45000;


        while (System.currentTimeMillis()
                < timeout) {


            // --------------------------------------------------
            // MEDIA PERMISSION
            // --------------------------------------------------

            if (isDisplayed(
                    MEDIA_PERMISSION_ALLOW
            )) {

                System.out.println(
                        "[STARTUP] Android media permission detected."
                );


                click(
                        MEDIA_PERMISSION_ALLOW,
                        "Android Media Permission Allow"
                );


                pause(
                        700
                );


                /*
                 * Continue polling after permission.
                 * Do not immediately assume the video
                 * is already rendered.
                 */
                continue;
            }


            // --------------------------------------------------
            // VIDEO READY
            // --------------------------------------------------

            if (isDisplayed(
                    AUTH_VIDEO
            )) {

                System.out.println(
                        "[STARTUP] Auth video is ready."
                );

                return;
            }


            // --------------------------------------------------
            // UNEXPECTED AUTH LANDING
            // --------------------------------------------------

            if (isAuthLandingDisplayed()) {

                throw new AssertionError(
                        "Application reached Authentication landing "
                                + "without displaying the Auth Video."
                );
            }


            pause(
                    500
            );
        }


        throw new AssertionError(
                "Auth Video was not displayed within 45 seconds."
        );
    }


    // ==========================================================
    // TC_003 + TC_004 + TC_005 + FUTURE TESTS
    // NAVIGATE TO AUTH LANDING
    // ==========================================================

    public void navigateToAuthLanding() {

        System.out.println();
        System.out.println(
                "=================================================="
        );

        System.out.println(
                "[STARTUP] Preparing Authentication Landing Screen"
        );

        System.out.println(
                "=================================================="
        );


        long timeout =
                System.currentTimeMillis()
                        + 60000;


        boolean permissionHandled =
                false;

        boolean continueHandled =
                false;

        boolean dataCollectionHandled =
                false;


        while (System.currentTimeMillis()
                < timeout) {


            // ==================================================
            // 1. ANDROID MEDIA PERMISSION
            // ==================================================

            if (isDisplayed(
                    MEDIA_PERMISSION_ALLOW
            )) {

                System.out.println(
                        "[STARTUP] Media permission displayed."
                );


                click(
                        MEDIA_PERMISSION_ALLOW,
                        "Allow"
                );


                permissionHandled =
                        true;


                System.out.println(
                        "[STARTUP] Media permission handled."
                );


                pause(
                        800
                );


                continue;
            }


            // ==================================================
            // 2. AUTH CONTINUE BUTTON
            // ==================================================

            if (isDisplayed(
                    CONTINUE_BUTTON
            )) {

                System.out.println(
                        "[STARTUP] Auth Continue displayed."
                );


                splashPage
                        .clickContinue();


                continueHandled =
                        true;


                System.out.println(
                        "[STARTUP] Auth Continue handled."
                );


                pause(
                        800
                );


                continue;
            }


            // ==================================================
            // 3. DATA COLLECTION
            // ==================================================

            /*
             * Handle the popup before treating the underlying
             * Authentication Landing screen as ready.
             */
            if (isDisplayed(
                    DATA_COLLECTION_ACCEPT
            )) {

                System.out.println(
                        "[STARTUP] Data Collection popup displayed."
                );


                boolean accepted =
                        loginPage
                                .acceptDataCollectionIfDisplayed();


                if (accepted) {

                    dataCollectionHandled =
                            true;


                    System.out.println(
                            "[STARTUP] Data Collection handled."
                    );
                }


                pause(
                        600
                );


                continue;
            }


            // ==================================================
            // 4. AUTHENTICATION LANDING READY
            // ==================================================

            /*
             * IMPORTANT:
             *
             * Sign In / Create Account is the actual final state
             * required by TC_003, TC_004, TC_005 and later tests.
             *
             * Check this BEFORE AUTH_VIDEO.
             *
             * ExoPlayer can remain visible in Appium XML even
             * after the real Authentication Landing screen has
             * already appeared.
             */
            boolean signInDisplayed =
                    isDisplayed(
                            SIGN_IN_BUTTON
                    );


            boolean createAccountDisplayed =
                    isDisplayed(
                            CREATE_ACCOUNT_BUTTON
                    );


            if (signInDisplayed
                    || createAccountDisplayed) {

                System.out.println();
                System.out.println(
                        "[PASS] Authentication Landing Screen ready."
                );


                System.out.println(
                        "[STARTUP] Sign In displayed        : "
                                + (
                                signInDisplayed
                                        ? "YES"
                                        : "NO"
                        )
                );


                System.out.println(
                        "[STARTUP] Create Account displayed : "
                                + (
                                createAccountDisplayed
                                        ? "YES"
                                        : "NO"
                        )
                );


                System.out.println(
                        "[STARTUP] Permission handled       : "
                                + yesNo(
                                permissionHandled
                        )
                );


                System.out.println(
                        "[STARTUP] Auth Continue handled    : "
                                + yesNo(
                                continueHandled
                        )
                );


                System.out.println(
                        "[STARTUP] Data Collection handled  : "
                                + yesNo(
                                dataCollectionHandled
                        )
                );


                System.out.println(
                        "=================================================="
                );


                return;
            }


            // ==================================================
            // 5. AUTH VIDEO STILL PRESENT
            // ==================================================

            /*
             * Video can legitimately be displayed while waiting
             * for Continue.
             *
             * It can also remain in the Appium hierarchy after
             * Continue/Data Collection have already completed.
             *
             * That is why the Landing check above always gets
             * priority over this condition.
             */
            if (isDisplayed(
                    AUTH_VIDEO
            )) {

                pause(
                        500
                );


                continue;
            }


            // ==================================================
            // 6. APPLICATION STILL LOADING
            // ==================================================

            pause(
                    500
            );
        }


        // ======================================================
        // TIMEOUT DIAGNOSTICS
        // ======================================================

        boolean videoDisplayed =
                isDisplayed(
                        AUTH_VIDEO
                );


        boolean continueDisplayed =
                isDisplayed(
                        CONTINUE_BUTTON
                );


        boolean permissionDisplayed =
                isDisplayed(
                        MEDIA_PERMISSION_ALLOW
                );


        boolean dataCollectionDisplayed =
                isDisplayed(
                        DATA_COLLECTION_ACCEPT
                );


        boolean signInDisplayed =
                isDisplayed(
                        SIGN_IN_BUTTON
                );


        boolean createAccountDisplayed =
                isDisplayed(
                        CREATE_ACCOUNT_BUTTON
                );


        System.out.println();
        System.out.println(
                "[FAIL] Authentication Landing Screen was not ready."
        );


        System.out.println(
                "[DEBUG] Video displayed          : "
                        + videoDisplayed
        );


        System.out.println(
                "[DEBUG] Continue displayed       : "
                        + continueDisplayed
        );


        System.out.println(
                "[DEBUG] Permission displayed     : "
                        + permissionDisplayed
        );


        System.out.println(
                "[DEBUG] Data Collection displayed: "
                        + dataCollectionDisplayed
        );


        System.out.println(
                "[DEBUG] Sign In displayed        : "
                        + signInDisplayed
        );


        System.out.println(
                "[DEBUG] Create Account displayed : "
                        + createAccountDisplayed
        );


        /*
         * Final protection for an element appearing at the
         * exact timeout boundary.
         */
        if (signInDisplayed
                || createAccountDisplayed) {

            System.out.println();
            System.out.println(
                    "[PASS] Authentication Landing Screen became ready at timeout boundary."
            );


            return;
        }


        throw new AssertionError(
                "Authentication Landing Screen was not ready "
                        + "within 60 seconds."
        );
    }


    // ==========================================================
    // AUTH LANDING CHECK
    // ==========================================================

    public boolean isAuthLandingDisplayed() {

        return isDisplayed(
                SIGN_IN_BUTTON
        )
                ||
                isDisplayed(
                        CREATE_ACCOUNT_BUTTON
                );
    }


    // ==========================================================
    // QUICK DISPLAY CHECK
    // ==========================================================

    private boolean isDisplayed(
            By locator
    ) {

        try {

            List<WebElement> elements =
                    driver.findElements(
                            locator
                    );


            if (elements.isEmpty()) {

                return false;
            }


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

    private void click(
            By locator,
            String elementName
    ) {

        List<WebElement> elements =
                driver.findElements(
                        locator
                );


        if (elements.isEmpty()) {

            throw new AssertionError(
                    elementName
                            + " was not found."
            );
        }


        WebElement element =
                elements.get(0);


        System.out.println(
                "[STARTUP] Clicking "
                        + elementName
                        + "."
        );


        try {

            element.click();


            pause(
                    400
            );


            /*
             * If the element disappeared after click,
             * consider the click successful.
             */
            if (!isDisplayed(
                    locator
            )) {

                System.out.println(
                        "[STARTUP] "
                                + elementName
                                + " clicked successfully."
                );


                return;
            }


        } catch (Exception e) {

            System.out.println(
                    "[STARTUP] Normal click failed for "
                            + elementName
                            + ". Trying gesture."
            );
        }


        /*
         * Re-locate before fallback because the first
         * WebElement may have become stale.
         */
        elements =
                driver.findElements(
                        locator
                );


        if (elements.isEmpty()) {

            return;
        }


        element =
                elements.get(0);


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
                "[STARTUP] "
                        + elementName
                        + " center: "
                        + centerX
                        + ", "
                        + centerY
        );


        driver.executeScript(
                "mobile: clickGesture",
                Map.of(
                        "x", centerX,
                        "y", centerY
                )
        );


        pause(
                500
        );


        System.out.println(
                "[STARTUP] Gesture click sent for "
                        + elementName
                        + "."
        );
    }


    // ==========================================================
    // HELPERS
    // ==========================================================

    private String yesNo(
            boolean value
    ) {

        return value
                ? "YES"
                : "NOT REQUIRED";
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
                    "Startup synchronization interrupted.",
                    e
            );
        }
    }
}