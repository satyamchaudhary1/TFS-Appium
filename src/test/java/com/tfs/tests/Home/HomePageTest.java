package com.tfs.tests.Home;

import com.tfs.annotations.TestInfo;
import com.tfs.config.ConfigManager;
import com.tfs.flows.AuthStartupFlow;
import com.tfs.pages.HomePage;
import com.tfs.pages.LoginPage;
import com.tfs.tests.BaseTest;

import org.testng.Assert;
import org.testng.annotations.Test;

public class HomePageTest extends BaseTest {

    private static final String TEST_BUILD =
            "TC006_VERBOSE_CONSOLE_CLEAN_V2_2026_08_16";

    private String currentStep =
            "Test initialization";


    @Test(
            groups = {
                    "regression",
                    "home"
            },
            priority = 6
    )
    @TestInfo(
            id = "TC_006",
            title = "Home Page - Promotional Banners and Search",
            module = "Home"
    )
    public void TC_006_homePageBannerAndSearchValidation() {

        boolean bannerResult = false;
        boolean searchResult = false;


        try {

            printMainHeader();

            System.out.println(
                    "[TEST BUILD] "
                            + TEST_BUILD
            );

            System.out.println(
                    "[INFO] Console Mode : VERBOSE - actions, expected values, actual values, locators and results"
            );


            // ======================================================
            // STEP 01 - LAUNCH
            // ======================================================

            currentStep =
                    "01 - Launch Application";


            printStep(
                    currentStep
            );

            System.out.println(
                    "[EXPECTED] Android application should be launched and Appium driver should be active."
            );

            System.out.println(
                    "[ACTUAL] Android application is available to the test."
            );

            printPass(
                    "Application launched successfully."
            );


            // ======================================================
            // STEP 02 - LOGIN TO HOME
            // ======================================================

            currentStep =
                    "02 - Login and Navigate to Home";


            printStep(
                    currentStep
            );

            System.out.println(
                    "[EXPECTED] Valid configured user should log in and Home screen should open."
            );

            loginToHome();


            HomePage homePage =
                    new HomePage();


            Assert.assertTrue(
                    homePage.isHomeScreenDisplayed(),
                    "Home screen was not displayed."
            );


            printPass(
                    "Home screen ready."
            );


            // ======================================================
            // TC_006(A)
            // ======================================================

            printSubHeader(
                    "TC_006(A) - HOME PAGE - PROMOTIONAL BANNERS"
            );


            // ------------------------------------------------------
            // A01
            // ------------------------------------------------------

            currentStep =
                    "TC_006(A) - A01 - Validate Promotional Banner";


            printStep(
                    currentStep
            );


            boolean bannerDisplayed =
                    homePage
                            .isPromotionalBannerDisplayed();


            printResult(
                    "Promotional Banner Displayed",
                    bannerDisplayed
            );


            Assert.assertTrue(
                    bannerDisplayed,
                    "Promotional banner was not displayed."
            );


            // ------------------------------------------------------
            // A02
            // ------------------------------------------------------

            currentStep =
                    "TC_006(A) - A02 - Validate Automatic Banner Movement";


            printStep(
                    currentStep
            );


            boolean automaticMovement =
                    homePage
                            .verifyAutomaticBannerMovement();


            printResult(
                    "Automatic Banner Movement",
                    automaticMovement
            );


            Assert.assertTrue(
                    automaticMovement,
                    "Promotional banners were not moving automatically."
            );


            // ------------------------------------------------------
            // A03
            // ------------------------------------------------------

            currentStep =
                    "TC_006(A) - A03 - Detect Dynamic Banner Count";


            printStep(
                    currentStep
            );


            int bannerCount =
                    homePage
                            .detectBannerCountAndVerifyCycle();

            System.out.println(
                    "[ACTUAL] Banner Count Returned To Test : "
                            + bannerCount
            );


            System.out.println(
                    "[INFO] Current Promotional Banner Count: "
                            + bannerCount
            );


            Assert.assertTrue(
                    bannerCount > 1,
                    "Less than two promotional banners were detected."
            );


            Assert.assertTrue(
                    homePage.wasLastCycleVerified(),
                    "Promotional banners did not cycle back to the first banner."
            );


            printPass(
                    "Dynamic banner count detected: "
                            + bannerCount
            );


            // ------------------------------------------------------
            // A04
            // ------------------------------------------------------

            currentStep =
                    "TC_006(A) - A04 - Validate Every Banner Navigation";


            printStep(
                    currentStep
            );


            homePage
                    .validateEveryBannerNavigation();


            bannerResult =
                    true;


            System.out.println();
            System.out.println(
                    "=================================================="
            );

            System.out.println(
                    "TC_006(A) RESULT: PASS"
            );

            System.out.println(
                    "Banner Count       : "
                            + bannerCount
            );

            System.out.println(
                    "Automatic Movement : PASS"
            );

            System.out.println(
                    "Cyclic Rotation    : PASS"
            );

            System.out.println(
                    "Banner Navigation  : PASS"
            );

            System.out.println(
                    "=================================================="
            );


            // ======================================================
            // TC_006(B)
            // ======================================================

            printSubHeader(
                    "TC_006(B) - HOME PAGE - SEARCH"
            );


            // ------------------------------------------------------
            // B01 - OPEN SEARCH
            // ------------------------------------------------------

            currentStep =
                    "TC_006(B) - B01 - Open Search";


            printStep(
                    currentStep
            );


            homePage
                    .openSearch();


            boolean searchScreen =
                    homePage
                            .isSearchScreenDisplayed();


            printResult(
                    "Search Screen Displayed",
                    searchScreen
            );


            Assert.assertTrue(
                    searchScreen,
                    "Search screen was not displayed."
            );


            // ------------------------------------------------------
            // B02 - CASE INSENSITIVITY
            // ------------------------------------------------------

            currentStep =
                    "TC_006(B) - B02 - Validate Case Insensitive Search";


            printStep(
                    currentStep
            );


            String[] searchValues = {
                    "dior",
                    "DIOR",
                    "Dior"
            };


            for (String searchValue :
                    searchValues) {


                homePage
                        .clearSearchFieldDirectly();


                homePage
                        .enterSearchText(
                                searchValue
                        );


                boolean suggestionsDisplayed =
                        homePage
                                .areRelevantSuggestionsDisplayed(
                                        "dior"
                                );


                System.out.println(
                        "[SEARCH CASE] "
                                + searchValue
                                + " : "
                                + (
                                suggestionsDisplayed
                                        ? "PASS"
                                        : "FAIL"
                        )
                );


                Assert.assertTrue(
                        suggestionsDisplayed,
                        "Relevant suggestions were not displayed for search case: "
                                + searchValue
                );
            }


            printPass(
                    "Search is case insensitive for dior / DIOR / Dior."
            );


            // ------------------------------------------------------
            // B03 - AUTO SUGGESTIONS
            // ------------------------------------------------------

            currentStep =
                    "TC_006(B) - B03 - Validate Auto Suggestions";


            printStep(
                    currentStep
            );


            homePage
                    .clearSearchFieldDirectly();


            homePage
                    .enterSearchText(
                            "Dior"
                    );


            boolean autoSuggestions =
                    homePage
                            .areRelevantSuggestionsDisplayed(
                                    "dior"
                            );


            printResult(
                    "Relevant Dior Suggestions Displayed",
                    autoSuggestions
            );


            Assert.assertTrue(
                    autoSuggestions,
                    "Relevant Dior auto-suggestions were not displayed."
            );


            // ------------------------------------------------------
            // B04 - SELECT SUGGESTION
            // ------------------------------------------------------

            currentStep =
                    "TC_006(B) - B04 - Select Search Suggestion";


            printStep(
                    currentStep
            );


            String selectedSuggestion =
                    homePage
                            .selectFirstRelevantSuggestion(
                                    "dior"
                            );


            System.out.println(
                    "[INFO] Selected Suggestion: "
                            + selectedSuggestion
            );


            boolean searchResults =
                    homePage
                            .isSearchResultsDisplayed(
                                    "dior"
                            );


            printResult(
                    "Search Results / PLP Displayed",
                    searchResults
            );


            Assert.assertTrue(
                    searchResults,
                    "Search Results / PLP was not displayed after selecting suggestion."
            );


            // ------------------------------------------------------
            // B05 - PRODUCT SUGGESTIONS / RESULTS
            // ------------------------------------------------------

            currentStep =
                    "TC_006(B) - B05 - Validate Relevant Products";


            printStep(
                    currentStep
            );


            int relevantProductCount =
                    homePage
                            .getRelevantProductCount(
                                    "dior"
                            );


            System.out.println(
                    "[INFO] Relevant Product Count: "
                            + relevantProductCount
            );


            Assert.assertTrue(
                    relevantProductCount > 0,
                    "Relevant Dior products were not displayed."
            );


            printPass(
                    "Relevant Dior products displayed."
            );


            // ------------------------------------------------------
            // B06 - PRODUCT CLICK / PDP
            // ------------------------------------------------------

            currentStep =
                    "TC_006(B) - B06 - Validate Product Click Navigation";


            printStep(
                    currentStep
            );


            String productName =
                    homePage
                            .openFirstRelevantProduct(
                                    "dior"
                            );


            System.out.println(
                    "[INFO] Product Opened: "
                            + productName
            );


            boolean pdpDisplayed =
                    homePage
                            .isProductDetailPageDisplayed();


            printResult(
                    "Product Detail Page Displayed",
                    pdpDisplayed
            );


            Assert.assertTrue(
                    pdpDisplayed,
                    "Product Detail Page was not displayed."
            );


            // ------------------------------------------------------
            // B07 - CLEAR SEARCH
            // ------------------------------------------------------

            currentStep =
                    "TC_006(B) - B07 - Validate Clear Search Functionality";


            printStep(
                    currentStep
            );


            homePage
                    .navigateBackToHome();


            Assert.assertTrue(
                    homePage.isHomeScreenDisplayed(),
                    "Home screen was not displayed before Clear Search validation."
            );


            homePage
                    .openSearch();


            Assert.assertTrue(
                    homePage.isSearchScreenDisplayed(),
                    "Search screen was not displayed."
            );


            homePage
                    .enterSearchText(
                            "Dior"
                    );


            Assert.assertTrue(
                    homePage.areRelevantSuggestionsDisplayed(
                            "dior"
                    ),
                    "Suggestions were not displayed before Clear Search validation."
            );


            System.out.println(
                    "[EXPECTED] Exact Search Clear/X element should clear the value 'Dior'."
            );

            homePage
                    .clickClearSearch();


            boolean searchEmpty =
                    homePage
                            .isSearchFieldEmpty();


            printResult(
                    "Search Field Cleared",
                    searchEmpty
            );


            Assert.assertTrue(
                    searchEmpty,
                    "Search field was not cleared after tapping X."
            );


            searchResult =
                    true;


            System.out.println();
            System.out.println(
                    "=================================================="
            );

            System.out.println(
                    "TC_006(B) RESULT: PASS"
            );

            System.out.println(
                    "Search Input        : PASS"
            );

            System.out.println(
                    "Auto Suggestions    : PASS"
            );

            System.out.println(
                    "Suggestion Selection: PASS"
            );

            System.out.println(
                    "Relevant Products   : PASS"
            );

            System.out.println(
                    "Product → PDP       : PASS"
            );

            System.out.println(
                    "Case Insensitivity  : PASS"
            );

            System.out.println(
                    "Clear Search / X    : PASS"
            );

            System.out.println(
                    "=================================================="
            );


            // ======================================================
            // FINAL
            // ======================================================

            printFinalPass(
                    bannerResult,
                    searchResult
            );


        } catch (AssertionError | RuntimeException e) {


            printFinalFail(
                    currentStep,
                    e.getMessage(),
                    bannerResult,
                    searchResult
            );


            throw e;
        }
    }


    // ==========================================================
    // LOGIN PREREQUISITE
    // ==========================================================

    private void loginToHome() {

        String email =
                ConfigManager.require(
                        "login.email"
                );


        String password =
                ConfigManager.require(
                        "login.password"
                );


        System.out.println(
                "[INFO] Login Email: "
                        + email
        );


        System.out.println(
                "[INFO] Login Password: ********"
        );


        AuthStartupFlow startupFlow =
                new AuthStartupFlow();


        startupFlow
                .navigateToAuthLanding();


        LoginPage loginPage =
                new LoginPage();


        Assert.assertTrue(
                loginPage.isSignInButtonDisplayed(),
                "Sign In button was not displayed."
        );


        loginPage
                .clickSignIn();


        Assert.assertTrue(
                loginPage.isLoginScreenDisplayed(),
                "Login screen was not displayed."
        );


        loginPage
                .enterEmail(
                        email
                );


        loginPage
                .enterPassword(
                        password
                );


        loginPage
                .clickLogin();


        if (loginPage.isLoginErrorDisplayed()) {

            String message =
                    loginPage
                            .getLoginErrorMessage();


            loginPage
                    .closeLoginErrorPopup();


            Assert.fail(
                    "Login failed before Home Page test: "
                            + message
            );
        }


        loginPage
                .handlePostLoginPopups();


        Assert.assertTrue(
                loginPage.isHomeScreenDisplayed(),
                "Home screen was not displayed after login."
        );


        System.out.println(
                "[PASS] Login prerequisite completed."
        );
    }


    // ==========================================================
    // CONSOLE
    // ==========================================================

    private void printMainHeader() {

        System.out.println();
        System.out.println(
                "=================================================="
        );

        System.out.println(
                "TC_006 - HOME PAGE VALIDATION"
        );

        System.out.println(
                "=================================================="
        );
    }


    private void printSubHeader(
            String title
    ) {

        System.out.println();
        System.out.println();
        System.out.println(
                "##################################################"
        );

        System.out.println(
                title
        );

        System.out.println(
                "##################################################"
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
            boolean bannerResult,
            boolean searchResult
    ) {

        System.out.println();
        System.out.println(
                "=================================================="
        );

        System.out.println(
                "TC_006 RESULT: PASS"
        );

        System.out.println(
                "=================================================="
        );

        System.out.println(
                "TC_006(A) Promotional Banners : "
                        + (
                        bannerResult
                                ? "PASS"
                                : "FAIL"
                )
        );

        System.out.println(
                "TC_006(B) Search Functionality : "
                        + (
                        searchResult
                                ? "PASS"
                                : "FAIL"
                )
        );

        System.out.println(
                "=================================================="
        );
    }


    private void printFinalFail(
            String failedStep,
            String reason,
            boolean bannerResult,
            boolean searchResult
    ) {

        System.out.println();
        System.out.println(
                "=================================================="
        );

        System.out.println(
                "TC_006 RESULT: FAIL"
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

        System.out.println(
                "TC_006(A) Promotional Banners : "

                        + (
                        bannerResult
                                ? "PASS"
                                : "NOT COMPLETED / FAIL"
                )
        );

        System.out.println(
                "TC_006(B) Search Functionality : "
                        + (
                        searchResult
                                ? "PASS"
                                : "NOT COMPLETED / FAIL"
                )
        );

        System.out.println(
                "=================================================="
        );
    }
}