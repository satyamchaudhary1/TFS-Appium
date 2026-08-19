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

        this.driver = DriverManager.getDriver();
        this.splashPage = new SplashPage();
        this.loginPage = new LoginPage();
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
                System.currentTimeMillis() + 45000;

        while (System.currentTimeMillis() < timeout) {

            // --------------------------------------------------
            // MEDIA PERMISSION
            // --------------------------------------------------

            if (isDisplayed(MEDIA_PERMISSION_ALLOW)) {

                System.out.println(
                        "[STARTUP] Android media permission detected."
                );

                click(
                        MEDIA_PERMISSION_ALLOW,
                        "Android Media Permission Allow"
                );

                pause(700);
                continue;
            }

            // --------------------------------------------------
            // VIDEO READY
            // --------------------------------------------------

            if (isDisplayed(AUTH_VIDEO)) {

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

            pause(500);
        }

        throw new AssertionError(
                "Auth Video was not displayed within 45 seconds."
        );
    }


    // ==========================================================
    // TC_003 + TC_004
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
                System.currentTimeMillis() + 60000;

        boolean permissionHandled = false;
        boolean continueHandled = false;
        boolean dataCollectionHandled = false;

        while (System.currentTimeMillis() < timeout) {

            // ==================================================
            // 1. ANDROID MEDIA PERMISSION
            // ==================================================

            if (isDisplayed(MEDIA_PERMISSION_ALLOW)) {

                System.out.println(
                        "[STARTUP] Media permission displayed."
                );

                click(
                        MEDIA_PERMISSION_ALLOW,
                        "Allow"
                );

                permissionHandled = true;

                System.out.println(
                        "[STARTUP] Media permission handled."
                );

                pause(800);
                continue;
            }


            // ==================================================
            // 2. AUTH CONTINUE BUTTON
            // ==================================================

            if (isDisplayed(CONTINUE_BUTTON)) {

                System.out.println(
                        "[STARTUP] Auth Continue displayed."
                );

                splashPage.clickContinue();

                continueHandled = true;

                System.out.println(
                        "[STARTUP] Auth Continue handled."
                );

                pause(800);
                continue;
            }


            // ==================================================
            // 3. DATA COLLECTION POPUP
            // ==================================================

            /*
             * This check must happen before the AUTH_VIDEO check.
             * The video can remain present in the background while
             * the popup is displayed.
             */
            if (isDisplayed(DATA_COLLECTION_ACCEPT)) {

                System.out.println(
                        "[STARTUP] Data Collection popup displayed."
                );

                boolean accepted =
                        loginPage.acceptDataCollectionIfDisplayed();

                if (accepted) {

                    dataCollectionHandled = true;

                    System.out.println(
                            "[STARTUP] Data Collection handled."
                    );
                }

                pause(600);
                continue;
            }


            // ==================================================
            // 4. AUTH LANDING READY
            // ==================================================

            /*
             * This check must happen before AUTH_VIDEO.
             *
             * AUTH_VIDEO remains displayed as the background of
             * the final authentication landing screen. If the
             * video is checked first, the loop continues forever
             * even though Sign In and Create Account are visible.
             */
            if (isAuthLandingDisplayed()) {

                System.out.println();
                System.out.println(
                        "[PASS] Authentication Landing Screen ready."
                );

                System.out.println(
                        "[STARTUP] Permission handled     : "
                                + yesNo(permissionHandled)
                );

                System.out.println(
                        "[STARTUP] Auth Continue handled : "
                                + yesNo(continueHandled)
                );

                System.out.println(
                        "[STARTUP] Data Collection handled: "
                                + yesNo(dataCollectionHandled)
                );

                System.out.println(
                        "=================================================="
                );

                return;
            }


            // ==================================================
            // 5. VIDEO IS PLAYING / SCREEN IS LOADING
            // ==================================================

            if (isDisplayed(AUTH_VIDEO)) {

                pause(500);
                continue;
            }


            // ==================================================
            // 6. NOTHING ACTIONABLE YET
            // ==================================================

            pause(500);
        }


        // ======================================================
        // TIMEOUT DEBUG INFORMATION
        // ======================================================

        System.out.println();
        System.out.println(
                "[FAIL] Authentication Landing Screen was not ready."
        );

        System.out.println(
                "[DEBUG] Video displayed          : "
                        + isDisplayed(AUTH_VIDEO)
        );

        System.out.println(
                "[DEBUG] Continue displayed       : "
                        + isDisplayed(CONTINUE_BUTTON)
        );

        System.out.println(
                "[DEBUG] Permission displayed     : "
                        + isDisplayed(MEDIA_PERMISSION_ALLOW)
        );

        System.out.println(
                "[DEBUG] Data Collection displayed: "
                        + isDisplayed(DATA_COLLECTION_ACCEPT)
        );

        System.out.println(
                "[DEBUG] Sign In displayed        : "
                        + isDisplayed(SIGN_IN_BUTTON)
        );

        System.out.println(
                "[DEBUG] Create Account displayed : "
                        + isDisplayed(CREATE_ACCOUNT_BUTTON)
        );

        throw new AssertionError(
                "Authentication Landing Screen was not ready "
                        + "within 60 seconds."
        );
    }


    // ==========================================================
    // AUTH LANDING CHECK
    // ==========================================================

    public boolean isAuthLandingDisplayed() {

        /*
         * Both primary actions should be visible before the
         * authentication landing screen is considered ready.
         */
        return isDisplayed(SIGN_IN_BUTTON)
                && isDisplayed(CREATE_ACCOUNT_BUTTON);
    }


    // ==========================================================
    // QUICK DISPLAY CHECK
    // ==========================================================

    private boolean isDisplayed(By locator) {

        try {

            List<WebElement> elements =
                    driver.findElements(locator);

            if (elements.isEmpty()) {
                return false;
            }

            for (WebElement element : elements) {

                try {

                    if (element.isDisplayed()) {
                        return true;
                    }

                } catch (Exception ignored) {

                    /*
                     * Ignore stale or temporarily unavailable
                     * elements and check the next matched element.
                     */
                }
            }

            return false;

        } catch (Exception exception) {

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
                driver.findElements(locator);

        if (elements.isEmpty()) {

            throw new AssertionError(
                    elementName + " was not found."
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
            pause(400);

            /*
             * If the element disappears after clicking,
             * the normal click was successful.
             */
            if (!isDisplayed(locator)) {

                System.out.println(
                        "[STARTUP] "
                                + elementName
                                + " clicked successfully."
                );

                return;
            }

        } catch (Exception exception) {

            System.out.println(
                    "[STARTUP] Normal click failed for "
                            + elementName
                            + ". Trying gesture click."
            );
        }


        // ------------------------------------------------------
        // RELOCATE ELEMENT BEFORE FALLBACK GESTURE
        // ------------------------------------------------------

        elements =
                driver.findElements(locator);

        /*
         * The element disappearing means the original click
         * succeeded even if the transition took additional time.
         */
        if (elements.isEmpty()) {

            System.out.println(
                    "[STARTUP] "
                            + elementName
                            + " disappeared after click."
            );

            return;
        }

        element =
                elements.get(0);

        int centerX =
                element.getRect().getX()
                        + element.getRect().getWidth() / 2;

        int centerY =
                element.getRect().getY()
                        + element.getRect().getHeight() / 2;

        driver.executeScript(
                "mobile: clickGesture",
                Map.of(
                        "x", centerX,
                        "y", centerY
                )
        );

        pause(500);

        System.out.println(
                "[STARTUP] Gesture click sent for "
                        + elementName
                        + "."
        );
    }


    // ==========================================================
    // LOGGING HELPER
    // ==========================================================

    private String yesNo(boolean value) {

        return value
                ? "YES"
                : "NOT REQUIRED";
    }


    // ==========================================================
    // SYNCHRONIZATION HELPER
    // ==========================================================

    private void pause(long milliseconds) {

        try {

            Thread.sleep(milliseconds);

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Startup synchronization interrupted.",
                    exception
            );
        }
    }
}