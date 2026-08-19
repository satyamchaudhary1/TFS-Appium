package com.tfs.tests.authentication;

import com.tfs.annotations.TestInfo;
import com.tfs.config.ConfigManager;
import com.tfs.flows.AuthStartupFlow;
import com.tfs.pages.ForgotPasswordPage;
import com.tfs.pages.LoginPage;
import com.tfs.tests.BaseTest;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ForgotPasswordTest extends BaseTest {

    private String currentStep =
            "Test initialization";


    @Test(
            groups = {
                    "regression",
                    "authentication"
            },
            priority = 5
    )
    @TestInfo(
            id = "TC_005",
            title = "Forgot Password - Registered and Unregistered Email",
            module = "Authentication"
    )
    public void TC_005_verifyForgotPasswordScenarios() {

        String registeredEmail = null;
        String unregisteredEmail = null;


        try {

            printHeader();


            // ======================================================
            // TEST DATA
            // ======================================================

            registeredEmail =
                    ConfigManager.require(
                            "forgot.password.registered.email"
                    );


            unregisteredEmail =
                    generateUnregisteredEmail();


            printTestData(
                    registeredEmail,
                    unregisteredEmail
            );


            // ======================================================
            // STEP 01 - APPLICATION LAUNCH
            // ======================================================

            currentStep =
                    "01 - Launch Application";


            printStep(
                    currentStep
            );


            /*
             * Driver launch is already handled by BaseTest.
             */
            printPass(
                    "Application launched successfully."
            );


            // ======================================================
            // STEP 02 - PREPARE AUTH LANDING
            // ======================================================

            currentStep =
                    "02 - Prepare Authentication Landing Screen";


            printStep(
                    currentStep
            );


            AuthStartupFlow startupFlow =
                    new AuthStartupFlow();


            startupFlow
                    .navigateToAuthLanding();


            printPass(
                    "Authentication Landing Screen ready."
            );


            // ======================================================
            // STEP 03 - OPEN LOGIN
            // ======================================================

            currentStep =
                    "03 - Open Login Screen";


            printStep(
                    currentStep
            );


            LoginPage loginPage =
                    new LoginPage();


            boolean signInDisplayed =
                    loginPage
                            .isSignInButtonDisplayed();


            printResult(
                    "Sign In Button Displayed",
                    signInDisplayed
            );


            Assert.assertTrue(
                    signInDisplayed,
                    "Sign In button was not displayed."
            );


            loginPage
                    .clickSignIn();


            boolean loginScreenDisplayed =
                    loginPage
                            .isLoginScreenDisplayed();


            printResult(
                    "Login Screen Displayed",
                    loginScreenDisplayed
            );


            Assert.assertTrue(
                    loginScreenDisplayed,
                    "Login screen was not displayed."
            );


            // ======================================================
            // STEP 04 - OPEN FORGOT PASSWORD
            // ======================================================

            currentStep =
                    "04 - Open Forgot Password Screen";


            printStep(
                    currentStep
            );


            ForgotPasswordPage forgotPage =
                    new ForgotPasswordPage();


            boolean forgotLinkDisplayed =
                    forgotPage
                            .isForgotPasswordLinkDisplayed();


            printResult(
                    "Forgot Password Link Displayed",
                    forgotLinkDisplayed
            );


            Assert.assertTrue(
                    forgotLinkDisplayed,
                    "Forgot Password link was not displayed."
            );


            forgotPage
                    .clickForgotPassword();


            boolean forgotScreenDisplayed =
                    forgotPage
                            .isForgotPasswordScreenDisplayed();


            printResult(
                    "Forgot Password Screen Displayed",
                    forgotScreenDisplayed
            );


            Assert.assertTrue(
                    forgotScreenDisplayed,
                    "Forgot Password screen was not displayed."
            );


            // ======================================================
            // STEP 05 - UNREGISTERED EMAIL
            // ======================================================

            currentStep =
                    "05 - Validate Unregistered Email";


            printStep(
                    currentStep
            );


            System.out.println(
                    "[ACTION] Using unique unregistered email: "
                            + unregisteredEmail
            );


            forgotPage
                    .enterEmail(
                            unregisteredEmail
                    );


            forgotPage
                    .clickResetPassword();


            boolean userNotFoundDisplayed =
                    forgotPage
                            .isUserNotFoundPopupDisplayed();


            printResult(
                    "User Not Found Popup Displayed",
                    userNotFoundDisplayed
            );


            Assert.assertTrue(
                    userNotFoundDisplayed,
                    "User Not Found popup was not displayed for unregistered email."
            );


            System.out.println(
                    "[APP MESSAGE] "
                            + forgotPage.getPopupMessage()
            );


            printPass(
                    "Unregistered email validation successful."
            );


            // ======================================================
            // STEP 06 - CLOSE USER NOT FOUND POPUP
            // ======================================================

            currentStep =
                    "06 - Close User Not Found Popup";


            printStep(
                    currentStep
            );


            forgotPage
                    .clickOkayIfDisplayed();


            boolean screenStillDisplayed =
                    forgotPage
                            .isForgotPasswordScreenDisplayed();


            printResult(
                    "Forgot Password Screen Displayed After Popup",
                    screenStillDisplayed
            );


            Assert.assertTrue(
                    screenStillDisplayed,
                    "Forgot Password screen was not visible after closing User Not Found popup."
            );


            // ======================================================
            // STEP 07 - RESET FOR POSITIVE SCENARIO
            // ======================================================

            currentStep =
                    "07 - Reopen Forgot Password Screen";


            printStep(
                    currentStep
            );


            forgotPage
                    .navigateBackToLogin();


            boolean loginReturned =
                    loginPage
                            .isLoginScreenDisplayed();


            printResult(
                    "Login Screen Displayed After Back",
                    loginReturned
            );


            Assert.assertTrue(
                    loginReturned,
                    "Login screen was not displayed after Back navigation."
            );


            boolean forgotLinkAgain =
                    forgotPage
                            .isForgotPasswordLinkDisplayed();


            Assert.assertTrue(
                    forgotLinkAgain,
                    "Forgot Password link was not displayed after returning to Login."
            );


            forgotPage
                    .clickForgotPassword();


            boolean forgotScreenAgain =
                    forgotPage
                            .isForgotPasswordScreenDisplayed();


            printResult(
                    "Forgot Password Screen Reopened",
                    forgotScreenAgain
            );


            Assert.assertTrue(
                    forgotScreenAgain,
                    "Forgot Password screen was not displayed for positive scenario."
            );


            // ======================================================
            // STEP 08 - REGISTERED EMAIL
            // ======================================================

            currentStep =
                    "08 - Validate Registered Email";


            printStep(
                    currentStep
            );


            System.out.println(
                    "[ACTION] Entering registered email: "
                            + registeredEmail
            );


            forgotPage
                    .enterEmail(
                            registeredEmail
                    );


            forgotPage
                    .clickResetPassword();


            boolean successPopupDisplayed =
                    forgotPage
                            .isSuccessPopupDisplayed();


            printResult(
                    "Forgot Password Success Popup Displayed",
                    successPopupDisplayed
            );


            Assert.assertTrue(
                    successPopupDisplayed,
                    "Forgot Password success popup was not displayed for registered email."
            );


            System.out.println(
                    "[APP MESSAGE] "
                            + forgotPage.getPopupMessage()
            );


            printPass(
                    "Registered email Forgot Password validation successful."
            );


            // ======================================================
            // STEP 09 - CLOSE SUCCESS POPUP
            // ======================================================

            currentStep =
                    "09 - Close Success Popup";


            printStep(
                    currentStep
            );


            forgotPage
                    .clickOkayIfDisplayed();


            printPass(
                    "Forgot Password success popup handled."
            );


            // ======================================================
            // FINAL RESULT
            // ======================================================

            printFinalPass(
                    registeredEmail,
                    unregisteredEmail
            );


        } catch (AssertionError | RuntimeException e) {


            printFinalFail(
                    currentStep,
                    e.getMessage(),
                    registeredEmail,
                    unregisteredEmail
            );


            throw e;
        }
    }


    // ==========================================================
    // UNIQUE UNREGISTERED EMAIL
    // ==========================================================

    private String generateUnregisteredEmail() {

        return "notregistered"
                + System.currentTimeMillis()
                + "@gmail.com";
    }


    // ==========================================================
    // CONSOLE
    // ==========================================================

    private void printHeader() {

        System.out.println();
        System.out.println(
                "=================================================="
        );

        System.out.println(
                "TC_005 - FORGOT PASSWORD"
        );

        System.out.println(
                "=================================================="
        );
    }


    private void printTestData(
            String registeredEmail,
            String unregisteredEmail
    ) {

        System.out.println();
        System.out.println(
                "---------------- TEST DATA ----------------"
        );

        System.out.println(
                "[DATA] Registered Email   : "
                        + registeredEmail
        );

        System.out.println(
                "[DATA] Unregistered Email : "
                        + unregisteredEmail
        );

        System.out.println(
                "-------------------------------------------"
        );
    }


    private void printStep(
            String step
    ) {

        System.out.println();
        System.out.println(
                "--------------------------------------------------"
        );

        System.out.println(
                "[STEP] "
                        + step
        );

        System.out.println(
                "--------------------------------------------------"
        );
    }


    private void printPass(
            String message
    ) {

        System.out.println(
                "[PASS] "
                        + message
        );
    }


    private void printResult(
            String name,
            boolean result
    ) {

        System.out.println(
                "[RESULT] "
                        + name
                        + " : "
                        + (
                        result
                                ? "PASS"
                                : "FAIL"
                )
        );
    }


    private void printFinalPass(
            String registeredEmail,
            String unregisteredEmail
    ) {

        System.out.println();
        System.out.println(
                "=================================================="
        );

        System.out.println(
                "TC_005 RESULT: PASS"
        );

        System.out.println(
                "=================================================="
        );

        System.out.println(
                "Unregistered Email Validation : PASS"
        );

        System.out.println(
                "Registered Email Validation   : PASS"
        );

        System.out.println(
                "Registered Email Used         : "
                        + registeredEmail
        );

        System.out.println(
                "Unregistered Email Used       : "
                        + unregisteredEmail
        );

        System.out.println(
                "=================================================="
        );
    }


    private void printFinalFail(
            String failedStep,
            String reason,
            String registeredEmail,
            String unregisteredEmail
    ) {

        System.out.println();
        System.out.println(
                "=================================================="
        );

        System.out.println(
                "TC_005 RESULT: FAIL"
        );

        System.out.println(
                "=================================================="
        );

        System.out.println(
                "Failed Step : "
                        + failedStep
        );

        System.out.println(
                "Reason      : "
                        + reason
        );


        if (registeredEmail != null) {

            System.out.println(
                    "Registered Email   : "
                            + registeredEmail
            );
        }


        if (unregisteredEmail != null) {

            System.out.println(
                    "Unregistered Email : "
                            + unregisteredEmail
            );
        }


        System.out.println(
                "=================================================="
        );
    }
}