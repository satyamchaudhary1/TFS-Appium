package com.tfs.tests.MobileStories;

import com.tfs.annotations.TestInfo;
import com.tfs.config.ConfigManager;
import com.tfs.flows.AuthStartupFlow;
import com.tfs.pages.FreeGiftsPage;
import com.tfs.pages.FreeGiftsPage.FreeSampleInfo;
import com.tfs.pages.LoginPage;
import com.tfs.tests.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** TC014: complete fresh-login Free Gifts PLP journey. */
public class FreeGiftsTest extends BaseTest {

    @Test(groups = {"regression", "homepage", "freegifts", "plp"}, priority = 14)
    @TestInfo(id = "TC_014", title = "Free Gifts PLP, Show More, Sort and Filter",
            module = "Home Page")
    public void TC_014_verifyFreeGiftsPlpShowMoreSortAndFilter() {
        FreeGiftsPage freeGiftsPage = new FreeGiftsPage();
        String currentStep = "Initialize TC014";
        long executionStartedAt = System.nanoTime();

        try {
            currentStep = "Fresh login and reach Home page";
            step(0, currentStep);
            performFreshLogin();

            currentStep = "Scroll Mobile Stories horizontally to Free Gifts";
            step(1, currentStep);
            freeGiftsPage.scrollMobileStoriesHorizontallyToFreeGifts();

            currentStep = "Select Free Gifts category from Home page";
            step(2, currentStep);
            Assert.assertTrue(freeGiftsPage.isFreeGiftsQuickAccessDisplayed(),
                    "Free Gifts quick-access category was not displayed on Home page.");
            freeGiftsPage.openFreeGiftsCategory();

            if (freeGiftsPage.isEmptyCategoryDisplayed()) {
                executeEmptyCategoryJourney(freeGiftsPage);
                return;
            }

            currentStep = "Validate Free Gifts total sample count";
            step(3, currentStep);
            Assert.assertTrue(freeGiftsPage.isFreeGiftsPlpDisplayed(),
                    "Free Gifts PLP was not displayed.");
            int totalSamples = freeGiftsPage.getTotalSampleCount();
            Assert.assertTrue(totalSamples > 0,
                    "Free Gifts total sample count must be greater than zero.");

            currentStep = "Validate Free Gifts sample details";
            step(4, currentStep);
            List<FreeSampleInfo> samples = freeGiftsPage.validateAndPrintFirstTwoSamples();
            Assert.assertEquals(samples.size(), 2,
                    "Exactly two free samples should be validated.");

            currentStep = "Scroll to Show More and load new free samples";
            step(5, currentStep);
            int newlyLoadedSamples = freeGiftsPage.clickShowMoreAndVerifyNewSamples(1);
            Assert.assertTrue(newlyLoadedSamples > 0,
                    "Show More did not load any new free samples.");

            currentStep = "Return to PLP top and select Sort By";
            step(6, currentStep);
            freeGiftsPage.openSortAndValidateOptions();

            currentStep = "Validate Sort By options";
            step(7, currentStep);
            // All expected options are validated by openSortAndValidateOptions().

            currentStep = "Apply Product Name A-Z and validate sample count";
            step(8, currentStep);
            freeGiftsPage.selectProductNameAToZAndValidate();

            currentStep = "Open Filter functionality";
            step(9, currentStep);
            freeGiftsPage.openFilter();

            currentStep = "Select Armani and show results";
            step(10, currentStep);
            freeGiftsPage.selectArmaniAndShowResults();

            currentStep = "Validate filtered free-sample PLP and sample count";
            step(11, currentStep);
            int filteredCount = freeGiftsPage.validateArmaniFilteredResults();

            currentStep = "Navigate back to Home page";
            step(12, currentStep);
            Assert.assertTrue(freeGiftsPage.goBackHome(),
                    "Unable to navigate from Free Gifts PLP to Home page.");

            currentStep = "Close the application";
            step(13, currentStep);
            Assert.assertTrue(freeGiftsPage.closeApplication(),
                    "The TFS application did not close successfully.");

            System.out.println("==================================================");
            System.out.println("[PASS] TC014 Steps 1-13 completed successfully.");
            System.out.println("[RESULT] Total free samples       : " + totalSamples);
            System.out.println("[RESULT] New samples detected     : " + newlyLoadedSamples);
            System.out.println("[RESULT] Filtered Armani count    : " + filteredCount);
            System.out.println("==================================================");
        } catch (AssertionError | RuntimeException failure) {
            System.out.println("==================================================");
            System.out.println("[TC014 FAILED]");
            System.out.println("[FAILED STEP] " + currentStep);
            System.out.println("[ERROR TYPE] " + failure.getClass().getSimpleName());
            System.out.println("[ERROR DETAILS] " + meaningfulMessage(failure));
            System.out.println("==================================================");
            throw failure;
        } finally {
            long elapsedSeconds =
                    (System.nanoTime() - executionStartedAt) / 1_000_000_000L;
            System.out.println("[TC014 TEST METHOD TIME] " + (elapsedSeconds / 60)
                    + " minute(s) " + (elapsedSeconds % 60) + " second(s)");
        }
    }

    private void performFreshLogin() {
        System.out.println("[STARTUP] TC014 fresh-login flow started.");
        AuthStartupFlow startupFlow = new AuthStartupFlow();
        LoginPage loginPage = new LoginPage();

        startupFlow.navigateToAuthLanding();
        loginPage.clickSignIn();
        Assert.assertTrue(loginPage.isLoginScreenDisplayed(),
                "Login screen was not displayed after clicking Sign In.");

        loginPage.enterEmail(ConfigManager.require("login.email"));
        loginPage.enterPassword(ConfigManager.require("login.password"));
        loginPage.clickLogin();

        if (loginPage.isLoginErrorDisplayed()) {
            Assert.fail("Login failed: " + loginPage.getLoginErrorMessage());
        }

        loginPage.handlePostLoginPopups();
        Assert.assertTrue(loginPage.isHomeScreenDisplayed(),
                "Home screen was not displayed after successful login.");
        System.out.println("[PASS] TC014 fresh login completed successfully.");
    }

    private void step(int number, String description) {
        String label = number == 0 ? "LOGIN" : String.format("%02d", number);
        System.out.println("[STEP " + label + "] " + description);
    }

    private void executeEmptyCategoryJourney(FreeGiftsPage freeGiftsPage) {
        step(3, "Validate Free Gifts empty state and sample count");
        int emptyCount = freeGiftsPage.validateEmptyCategory();

        step(4, "Validate BROWSE BESTSELLERS button");
        Assert.assertTrue(freeGiftsPage.isBrowseBestSellersDisplayedAndEnabled(),
                "BROWSE BESTSELLERS button was not displayed and enabled.");

        step(5, "Click BROWSE BESTSELLERS and validate Best Seller PLP");
        boolean bestSellerProductsFound =
                freeGiftsPage.clickBrowseBestSellersAndVerifyRedirect();
        if (!bestSellerProductsFound) {
            System.out.println("[INFO] Best Seller PLP contains 0 products. "
                    + "Proceeding directly to the Home navigation step.");
        }

        System.out.println("[SKIP][STEPS 06-11] Free Gifts-specific Show More, "
                + "Sort and Filter validations are not applicable because the "
                + "selected category contains 0 products.");

        step(12, "Navigate from Best Seller PLP back to Home page");
        Assert.assertTrue(freeGiftsPage.goBackFromBestSellerToHome(),
                "Unable to navigate from Best Seller PLP to Home page.");

        step(13, "Close the application");
        Assert.assertTrue(freeGiftsPage.closeApplication(),
                "The TFS application did not close successfully.");

        System.out.println("==================================================");
        System.out.println("[PASS] TC014 empty-category alternate journey completed.");
        System.out.println("[RESULT] Free Gifts sample count      : " + emptyCount);
        System.out.println("[RESULT] Best Seller redirection : PASS");
        System.out.println("==================================================");
    }

    private String meaningfulMessage(Throwable failure) {
        Throwable current = failure;
        while ((current.getMessage() == null || current.getMessage().isBlank())
                && current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return message == null || message.isBlank()
                ? "No error message was supplied. Review the first TC014 stack-trace line."
                : message;
    }
}
