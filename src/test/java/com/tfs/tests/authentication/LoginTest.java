package com.tfs.tests.authentication;

import com.tfs.annotations.TestInfo;
import com.tfs.config.ConfigManager;
import com.tfs.flows.AuthStartupFlow;
import com.tfs.pages.LoginPage;
import com.tfs.tests.BaseTest;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @Test(
            groups = {
                    "smoke",
                    "regression",
                    "authentication"
            },
            priority = 3
    )
    @TestInfo(
            id = "TC_003",
            title = "Login / Sign Up Page - Login with valid credentials",
            module = "Authentication"
    )
    public void TC_003_loginWithValidCredentials() {

        System.out.println();
        System.out.println("==================================================");
        System.out.println("TC_003 - LOGIN WITH VALID CREDENTIALS");
        System.out.println("==================================================");


        // ==========================================================
        // TEST DATA
        // ==========================================================

        String email =
                ConfigManager.require(
                        "login.email"
                );

        String password =
                ConfigManager.require(
                        "login.password"
                );


        System.out.println();
        System.out.println("---------------- TEST DATA ----------------");
        System.out.println("[DATA] Email    : " + email);
        System.out.println("[DATA] Password : ********");
        System.out.println("-------------------------------------------");


        // ==========================================================
        // STEP 01 - APPLICATION LAUNCH
        // ==========================================================

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("[STEP] 01 - Launch Application");
        System.out.println("--------------------------------------------------");

        /*
         * AndroidDriver and application launch
         * are handled by BaseTest.
         */
        System.out.println(
                "[PASS] Application launched successfully."
        );


        // ==========================================================
        // STEP 02 - PREPARE AUTHENTICATION LANDING
        // ==========================================================

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println(
                "[STEP] 02 - Prepare Authentication Landing Screen"
        );
        System.out.println("--------------------------------------------------");


        /*
         * AuthStartupFlow permanently handles:
         *
         * - Android Media Permission
         * - Auth Video loading
         * - Continue button
         * - Data Collection popup
         * - Waiting until Sign In / Create Account screen is ready
         *
         * TC_003 does NOT validate the Auth Video.
         * Auth Video validation belongs to TC_002.
         */
        AuthStartupFlow startupFlow =
                new AuthStartupFlow();


        startupFlow
                .navigateToAuthLanding();


        System.out.println(
                "[PASS] Authentication Landing Screen ready."
        );


        // ==========================================================
        // LOGIN PAGE
        // ==========================================================

        LoginPage loginPage =
                new LoginPage();


        // ==========================================================
        // STEP 03 - VALIDATE SIGN IN BUTTON
        // ==========================================================

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("[STEP] 03 - Validate Sign In Button");
        System.out.println("--------------------------------------------------");


        boolean signInDisplayed =
                loginPage
                        .isSignInButtonDisplayed();


        System.out.println(
                "[RESULT] Sign In Button Displayed : "
                        + (
                        signInDisplayed
                                ? "PASS"
                                : "FAIL"
                )
        );


        Assert.assertTrue(
                signInDisplayed,
                "Sign In button was not displayed."
        );


        // ==========================================================
        // STEP 04 - CLICK SIGN IN
        // ==========================================================

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("[STEP] 04 - Open Login Screen");
        System.out.println("--------------------------------------------------");


        System.out.println(
                "[ACTION] Clicking Sign In."
        );


        loginPage
                .clickSignIn();


        System.out.println(
                "[PASS] Sign In clicked successfully."
        );


        // ==========================================================
        // STEP 05 - VALIDATE LOGIN SCREEN
        // ==========================================================

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("[STEP] 05 - Validate Login Screen");
        System.out.println("--------------------------------------------------");


        boolean loginScreenDisplayed =
                loginPage
                        .isLoginScreenDisplayed();


        System.out.println(
                "[RESULT] Login Screen Displayed : "
                        + (
                        loginScreenDisplayed
                                ? "PASS"
                                : "FAIL"
                )
        );


        Assert.assertTrue(
                loginScreenDisplayed,
                "Login screen was not displayed."
        );


        // ==========================================================
        // STEP 06 - ENTER EMAIL
        // ==========================================================

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("[STEP] 06 - Enter Email");
        System.out.println("--------------------------------------------------");


        System.out.println(
                "[ACTION] Entering email: "
                        + email
        );


        loginPage
                .enterEmail(
                        email
                );


        System.out.println(
                "[PASS] Email entered successfully."
        );


        // ==========================================================
        // STEP 07 - ENTER PASSWORD
        // ==========================================================

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("[STEP] 07 - Enter Password");
        System.out.println("--------------------------------------------------");


        System.out.println(
                "[ACTION] Entering password."
        );


        loginPage
                .enterPassword(
                        password
                );


        System.out.println(
                "[PASS] Password entered successfully."
        );


        // ==========================================================
        // STEP 08 - CLICK LOG IN
        // ==========================================================

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("[STEP] 08 - Click Log In");
        System.out.println("--------------------------------------------------");


        System.out.println(
                "[ACTION] Clicking Log In."
        );


        loginPage
                .clickLogin();


        System.out.println(
                "[PASS] Log In action completed."
        );


        // ==========================================================
        // STEP 09 - CHECK LOGIN ERROR
        // ==========================================================

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("[STEP] 09 - Check Login Result");
        System.out.println("--------------------------------------------------");


        if (loginPage
                .isLoginErrorDisplayed()) {


            String message =
                    loginPage
                            .getLoginErrorMessage();


            System.out.println(
                    "[FAIL] Login error displayed."
            );


            System.out.println(
                    "[APP MESSAGE] "
                            + message
            );


            loginPage
                    .closeLoginErrorPopup();


            Assert.fail(
                    "Login failed: "
                            + message
            );
        }


        System.out.println(
                "[PASS] No login error displayed."
        );


        // ==========================================================
        // STEP 10 - HANDLE OPTIONAL POST LOGIN POPUPS
        // ==========================================================

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println(
                "[STEP] 10 - Handle Optional Post-Login Popups"
        );
        System.out.println("--------------------------------------------------");


        /*
         * Handles optional items such as:
         * - Biometric popup
         * - Coach marks
         *
         * Only when displayed.
         */
        loginPage
                .handlePostLoginPopups();


        System.out.println(
                "[PASS] Post-login popup handling completed."
        );


        // ==========================================================
        // STEP 11 - VALIDATE HOME SCREEN
        // ==========================================================

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("[STEP] 11 - Validate Home Screen");
        System.out.println("--------------------------------------------------");


        boolean homeDisplayed =
                loginPage
                        .isHomeScreenDisplayed();


        System.out.println(
                "[RESULT] Home Screen Displayed : "
                        + (
                        homeDisplayed
                                ? "PASS"
                                : "FAIL"
                )
        );


        Assert.assertTrue(
                homeDisplayed,
                "Home screen was not displayed after valid login."
        );


        // ==========================================================
        // FINAL RESULT
        // ==========================================================

        System.out.println();
        System.out.println("==================================================");
        System.out.println("TC_003 RESULT: PASS");
        System.out.println("==================================================");
        System.out.println("Login Email   : " + email);
        System.out.println("Reached Screen: HOME");
        System.out.println("==================================================");
    }
}