package com.tfs.tests.authentication;

import com.tfs.annotations.TestInfo;
import com.tfs.config.ConfigManager;
import com.tfs.pages.DeliveryDetailsPage;
import com.tfs.pages.LoginPage;
import com.tfs.pages.MembershipPage;
import com.tfs.pages.NotificationPermissionPage;
import com.tfs.pages.OnboardingPage;
import com.tfs.pages.SignUpPage;
import com.tfs.pages.SplashPage;
import com.tfs.tests.BaseTest;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SignUpTest extends BaseTest {

    private String currentStep = "Test initialization";

    @Test(
            groups = {
                    "smoke",
                    "regression",
                    "authentication"
            },
            priority = 4
    )
    @TestInfo(
            id = "TC_004",
            title = "Create Account and Navigate to Home",
            module = "Authentication"
    )
    public void TC_004_createAccountWithValidDetails() {

        String email = null;
        String phone = null;

        try {

            printHeader();


            // ==================================================
            // TEST DATA
            // ==================================================

            String fullName =
                    ConfigManager.require(
                            "signup.fullname"
                    );

            String password =
                    ConfigManager.require(
                            "signup.password"
                    );

            String postcode =
                    ConfigManager.require(
                            "signup.postcode"
                    );

            String addressLine1 =
                    ConfigManager.require(
                            "signup.address.line1"
                    );


            // ONE UNIQUE VALUE FOR THIS EXECUTION
            long runId =
                    System.currentTimeMillis();


            email =
                    generateUniqueEmail(
                            runId
                    );


            phone =
                    generateUniquePhone(
                            runId
                    );


            printTestData(
                    fullName,
                    email,
                    phone,
                    postcode,
                    addressLine1
            );


            // ==================================================
            // STEP 01 - APPLICATION LAUNCH
            // ==================================================

            currentStep =
                    "01 - Launch Application";

            printStep(
                    currentStep
            );

            printPass(
                    "Application launched successfully."
            );


            SplashPage splashPage =
                    new SplashPage();


            // ==================================================
// STEP 02 - MEDIA PERMISSION
// ==================================================

            currentStep =
                    "02 - Handle Android Media Permission";

            printStep(
                    currentStep
            );

            boolean permissionHandled =
                    splashPage
                            .handleMediaPermissionIfPresent();


            if (permissionHandled) {

                printPass(
                        "Android media permission handled successfully."
                );

            } else {

                printInfo(
                        "Media permission was not detected on first check."
                );

                printInfo(
                        "Waiting briefly and checking permission one more time."
                );

                try {

                    Thread.sleep(3000);

                } catch (InterruptedException e) {

                    Thread.currentThread()
                            .interrupt();
                }


                boolean permissionHandledOnRetry =
                        splashPage
                                .handleMediaPermissionIfPresent();


                if (permissionHandledOnRetry) {

                    printPass(
                            "Android media permission handled on retry."
                    );

                } else {

                    printInfo(
                            "Media permission is not present. Continuing."
                    );
                }
            }


// ==================================================
// STEP 03 - AUTH VIDEO / CONTINUE
// ==================================================

            currentStep =
                    "03 - Handle Auth Video Screen";

            printStep(
                    currentStep
            );


            /*
             * TC_002 separately validates the video.
             *
             * TC_004 only needs to navigate through it
             * when it is displayed.
             */
            boolean videoDisplayed =
                    splashPage
                            .isVideoDisplayed();


            if (videoDisplayed) {

                printPass(
                        "Auth video screen displayed."
                );


                boolean continueDisplayed =
                        splashPage
                                .isContinueDisplayed();


                printResult(
                        "Continue Button Displayed",
                        continueDisplayed
                );


                Assert.assertTrue(
                        continueDisplayed,
                        "Continue button was not displayed on Auth Video screen."
                );


                printAction(
                        "Clicking Continue."
                );


                splashPage
                        .clickContinue();


                printPass(
                        "Continue clicked successfully."
                );


            } else {

                printInfo(
                        "Auth video screen is not displayed. Continuing Sign Up prerequisite flow."
                );
            }


            // ==================================================
            // STEP 04 - DATA COLLECTION
            // ==================================================

            currentStep =
                    "04 - Handle Data Collection";

            printStep(
                    currentStep
            );


            LoginPage loginPage =
                    new LoginPage();


            boolean dataCollectionAccepted =
                    loginPage
                            .acceptDataCollectionIfDisplayed();


            if (dataCollectionAccepted) {

                printPass(
                        "Data Collection popup displayed and ACCEPT clicked."
                );

            } else {

                printInfo(
                        "Data Collection popup was not displayed/already handled."
                );
            }


            // ==================================================
            // STEP 05 - CREATE ACCOUNT BUTTON
            // ==================================================

            currentStep =
                    "05 - Open Create Account";

            printStep(
                    currentStep
            );


            SignUpPage signUpPage =
                    new SignUpPage();


            boolean createAccountDisplayed =
                    signUpPage
                            .isCreateAccountButtonDisplayed();


            printResult(
                    "Create Account Button Displayed",
                    createAccountDisplayed
            );


            Assert.assertTrue(
                    createAccountDisplayed,
                    "Create Account button was not displayed."
            );


            printAction(
                    "Clicking Create Account."
            );


            signUpPage
                    .clickCreateAccount();


            printPass(
                    "Create Account clicked successfully."
            );


            // ==================================================
            // STEP 06 - CREATE ACCOUNT FORM
            // ==================================================

            currentStep =
                    "06 - Validate Create Account Form";

            printStep(
                    currentStep
            );


            boolean createAccountScreen =
                    signUpPage
                            .isCreateAccountScreenDisplayed();


            printResult(
                    "Create Account Screen Displayed",
                    createAccountScreen
            );


            Assert.assertTrue(
                    createAccountScreen,
                    "Create Account screen was not displayed."
            );


            validateField(
                    "Full Name",
                    signUpPage
                            .isFullNameFieldDisplayed()
            );


            validateField(
                    "Email",
                    signUpPage
                            .isEmailFieldDisplayed()
            );


            validateField(
                    "Password",
                    signUpPage
                            .isPasswordFieldDisplayed()
            );


            validateField(
                    "Confirm Password",
                    signUpPage
                            .isConfirmPasswordFieldDisplayed()
            );


            validateField(
                    "Phone Number",
                    signUpPage
                            .isPhoneNumberFieldDisplayed()
            );


            // ==================================================
            // STEP 07 - ENTER REGISTRATION DETAILS
            // ==================================================

            currentStep =
                    "07 - Enter Registration Details";

            printStep(
                    currentStep
            );


            printAction(
                    "Entering Full Name: "
                            + fullName
            );


            signUpPage
                    .enterFullName(
                            fullName
                    );


            printPass(
                    "Full Name entered."
            );


            printAction(
                    "Entering UNIQUE Email: "
                            + email
            );


            signUpPage
                    .enterEmail(
                            email
                    );


            printPass(
                    "Unique Email entered."
            );


            printAction(
                    "Entering Password."
            );


            signUpPage
                    .enterPassword(
                            password
                    );


            printPass(
                    "Password entered."
            );


            printAction(
                    "Entering Confirm Password."
            );


            signUpPage
                    .enterConfirmPassword(
                            password
                    );


            printPass(
                    "Confirm Password entered."
            );


            printAction(
                    "Entering UNIQUE Phone Number: "
                            + phone
            );


            signUpPage
                    .enterPhoneNumber(
                            phone
                    );


            printPass(
                    "Unique Phone Number entered."
            );


            // ==================================================
            // STEP 08 - REGISTER
            // ==================================================

            currentStep =
                    "08 - Register Account";

            printStep(
                    currentStep
            );


            boolean registerDisplayed =
                    signUpPage
                            .isRegisterButtonDisplayed();


            printResult(
                    "Register Button Displayed",
                    registerDisplayed
            );


            Assert.assertTrue(
                    registerDisplayed,
                    "Register and Enter Delivery Info button was not displayed."
            );


            boolean registerEnabled =
                    signUpPage
                            .isRegisterButtonEnabled();


            printResult(
                    "Register Button Enabled",
                    registerEnabled
            );


            Assert.assertTrue(
                    registerEnabled,
                    "Register button was disabled."
            );


            System.out.println();
            System.out.println(
                    "[ACCOUNT DATA BEFORE REGISTRATION]"
            );

            System.out.println(
                    "Email : "
                            + email
            );

            System.out.println(
                    "Phone : "
                            + phone
            );


            printAction(
                    "Clicking Register and Enter Delivery Info."
            );


            signUpPage
                    .clickRegister();


            // ==================================================
            // USER ALREADY EXISTS CHECK
            // ==================================================

            if (signUpPage
                    .isUserAlreadyExistsDisplayed()) {

                System.out.println();
                System.out.println(
                        "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
                );

                System.out.println(
                        "[APP ERROR] USER ALREADY EXISTS"
                );

                System.out.println(
                        "Email Used : "
                                + email
                );

                System.out.println(
                        "Phone Used : "
                                + phone
                );

                System.out.println(
                        "Both values were generated uniquely for this execution."
                );

                System.out.println(
                        "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
                );


                Assert.fail(
                        "Application displayed User Already Exists "
                                + "even though unique account data was generated. "
                                + "Email="
                                + email
                                + ", Phone="
                                + phone
                );
            }


            printPass(
                    "Account registration accepted."
            );


            // ==================================================
            // STEP 09 - DELIVERY SCREEN
            // ==================================================

            currentStep =
                    "09 - Validate Delivery Search Screen";

            printStep(
                    currentStep
            );


            DeliveryDetailsPage deliveryPage =
                    new DeliveryDetailsPage();


            boolean deliveryScreenDisplayed =
                    deliveryPage
                            .isDeliverySearchScreenDisplayed();


            printResult(
                    "Delivery Search Screen Displayed",
                    deliveryScreenDisplayed
            );


            Assert.assertTrue(
                    deliveryScreenDisplayed,
                    "Delivery postcode search screen was not displayed."
            );


            // ==================================================
            // STEP 10 - ENTER POSTCODE
            // ==================================================

            currentStep =
                    "10 - Enter Postcode";

            printStep(
                    currentStep
            );


            printAction(
                    "Entering postcode: "
                            + postcode
            );


            deliveryPage
                    .enterPostcode(
                            postcode
                    );


            printPass(
                    "Postcode entered successfully."
            );


            // ==================================================
            // STEP 11 - ADDRESS SUGGESTIONS
            // ==================================================

            currentStep =
                    "11 - Validate Address Suggestions";

            printStep(
                    currentStep
            );


            boolean addressSuggestions =
                    deliveryPage
                            .areAddressSuggestionsDisplayed();


            printResult(
                    "Address Suggestions Displayed",
                    addressSuggestions
            );


            Assert.assertTrue(
                    addressSuggestions,
                    "Address suggestions were not displayed."
            );


            // ==================================================
            // STEP 12 - SELECT ADDRESS
            // ==================================================

            currentStep =
                    "12 - Select Address";

            printStep(
                    currentStep
            );


            deliveryPage
                    .selectFirstAddressSuggestion();


            boolean addressFormDisplayed =
                    deliveryPage
                            .isManualAddressFormDisplayed();


            printResult(
                    "Address Details Form Displayed",
                    addressFormDisplayed
            );


            Assert.assertTrue(
                    addressFormDisplayed,
                    "Address Details form was not displayed after selecting address."
            );


            // ==================================================
            // STEP 13 - MANDATORY ADDRESS LINE 1
            // ==================================================

            currentStep =
                    "13 - Validate Mandatory Address Line 1";

            printStep(
                    currentStep
            );


            boolean addressLine1Displayed =
                    deliveryPage
                            .isAddressLine1FieldDisplayed();


            printResult(
                    "Mandatory Address Line 1 Field Displayed",
                    addressLine1Displayed
            );


            Assert.assertTrue(
                    addressLine1Displayed,
                    "Mandatory Address Line 1 field was not displayed."
            );


            printAction(
                    "Checking Address Line 1."
            );


            deliveryPage
                    .enterAddressLine1IfEmpty(
                            addressLine1
                    );


            boolean addressLine1Value =
                    deliveryPage
                            .isAddressLine1ValueEntered();


            printResult(
                    "Mandatory Address Line 1 Value Entered",
                    addressLine1Value
            );


            Assert.assertTrue(
                    addressLine1Value,
                    "Mandatory Address Line 1 was empty."
            );


            // ==================================================
            // STEP 14 - PICK MEMBERSHIP
            // ==================================================

            currentStep =
                    "14 - Pick Membership";

            printStep(
                    currentStep
            );


            boolean pickMembershipDisplayed =
                    deliveryPage
                            .isPickMembershipDisplayed();


            printResult(
                    "Pick Membership Button Displayed",
                    pickMembershipDisplayed
            );


            Assert.assertTrue(
                    pickMembershipDisplayed,
                    "Pick Membership button was not displayed."
            );


            deliveryPage
                    .clickPickMembership();


            printPass(
                    "Pick Membership clicked successfully."
            );


            // ==================================================
            // STEP 15 - MEMBERSHIP
            // ==================================================

            currentStep =
                    "15 - Handle Membership";

            printStep(
                    currentStep
            );


            MembershipPage membershipPage =
                    new MembershipPage();


            if (membershipPage
                    .isMembershipScreenDisplayed()) {


                printPass(
                        "Membership screen displayed."
                );


                boolean skipDisplayed =
                        membershipPage
                                .isSkipButtonDisplayed();


                printResult(
                        "Membership Skip Button Displayed",
                        skipDisplayed
                );


                Assert.assertTrue(
                        skipDisplayed,
                        "Skip button was not displayed on Membership screen."
                );


                printAction(
                        "Skipping Membership."
                );


                membershipPage
                        .clickSkip();


                printPass(
                        "Membership skipped successfully."
                );


            } else {

                printInfo(
                        "Membership screen was not displayed. Continuing."
                );
            }


            // ==================================================
            // STEP 16 - NOTIFICATION SCREEN
            // ==================================================

            currentStep =
                    "16 - Handle Notification Permission";

            printStep(
                    currentStep
            );


            NotificationPermissionPage notificationPage =
                    new NotificationPermissionPage();


            if (notificationPage
                    .isNotificationScreenDisplayed()) {


                printPass(
                        "Notification Permission screen displayed."
                );


                if (notificationPage
                        .isSkipForNowDisplayed()) {


                    printAction(
                            "Clicking Skip for now."
                    );


                    notificationPage
                            .clickSkipForNow();


                    printPass(
                            "Notification permission skipped."
                    );


                } else {

                    printInfo(
                            "Skip for now was not displayed."
                    );
                }


            } else {

                printInfo(
                        "Notification screen was not displayed. Continuing."
                );
            }


            // ==================================================
            // STEP 17 - ONBOARDING
            // ==================================================

            currentStep =
                    "17 - Handle Onboarding";

            printStep(
                    currentStep
            );


            OnboardingPage onboardingPage =
                    new OnboardingPage();


            onboardingPage
                    .handleOnboardingIfDisplayed();


            printPass(
                    "Onboarding handling completed."
            );


            // ==================================================
            // STEP 18 - HOME SCREEN
            // ==================================================

            currentStep =
                    "18 - Validate Home Screen";

            printStep(
                    currentStep
            );


            boolean homeDisplayed =
                    onboardingPage
                            .isHomeScreenDisplayed();


            printResult(
                    "Home Screen Displayed",
                    homeDisplayed
            );


            Assert.assertTrue(
                    homeDisplayed,
                    "Home screen was not displayed after Sign Up."
            );


            // ==================================================
            // FINAL PASS
            // ==================================================

            printFinalPass(
                    email,
                    phone
            );


        } catch (AssertionError | RuntimeException e) {


            printFinalFail(
                    currentStep,
                    e.getMessage(),
                    email,
                    phone
            );


            throw e;
        }
    }


    // ==========================================================
    // UNIQUE EMAIL
    // ==========================================================

    private String generateUniqueEmail(
            long runId
    ) {

        return "satyamtest"
                + runId
                + "@gmail.com";
    }


    // ==========================================================
    // UNIQUE PHONE
    // ==========================================================

    private String generateUniquePhone(
            long runId
    ) {

        String timestamp =
                String.valueOf(
                        runId
                );


        String lastEightDigits =
                timestamp.substring(
                        timestamp.length() - 8
                );


        return "98"
                + lastEightDigits;
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
                "TC_004 - COMPLETE SIGN UP TO HOME FLOW"
        );

        System.out.println(
                "=================================================="
        );
    }


    private void printTestData(
            String fullName,
            String email,
            String phone,
            String postcode,
            String addressLine1
    ) {

        System.out.println();
        System.out.println(
                "---------------- TEST DATA ----------------"
        );

        System.out.println(
                "[DATA] Full Name     : "
                        + fullName
        );

        System.out.println(
                "[DATA] Unique Email  : "
                        + email
        );

        System.out.println(
                "[DATA] Unique Phone  : "
                        + phone
        );

        System.out.println(
                "[DATA] Postcode      : "
                        + postcode
        );

        System.out.println(
                "[DATA] Address Line1 : "
                        + addressLine1
        );

        System.out.println(
                "[DATA] Password      : ********"
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


    private void printAction(
            String message
    ) {

        System.out.println(
                "[ACTION] "
                        + message
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


    private void printInfo(
            String message
    ) {

        System.out.println(
                "[INFO] "
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


    private void validateField(
            String fieldName,
            boolean displayed
    ) {

        printResult(
                fieldName + " Field Displayed",
                displayed
        );


        Assert.assertTrue(
                displayed,
                fieldName
                        + " field was not displayed."
        );
    }


    private void printFinalPass(
            String email,
            String phone
    ) {

        System.out.println();
        System.out.println(
                "=================================================="
        );

        System.out.println(
                "TC_004 RESULT: PASS"
        );

        System.out.println(
                "=================================================="
        );

        System.out.println(
                "Created Email : "
                        + email
        );

        System.out.println(
                "Created Phone : "
                        + phone
        );

        System.out.println(
                "Reached Screen: HOME"
        );

        System.out.println(
                "=================================================="
        );
    }


    private void printFinalFail(
            String failedStep,
            String reason,
            String email,
            String phone
    ) {

        System.out.println();
        System.out.println(
                "=================================================="
        );

        System.out.println(
                "TC_004 RESULT: FAIL"
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


        if (email != null) {

            System.out.println(
                    "Email Used  : "
                            + email
            );
        }


        if (phone != null) {

            System.out.println(
                    "Phone Used  : "
                            + phone
            );
        }


        System.out.println(
                "=================================================="
        );
    }
}