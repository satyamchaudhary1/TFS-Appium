package com.tfs.tests.MobileStories;

import com.tfs.annotations.TestInfo;
import com.tfs.config.ConfigManager;
import com.tfs.flows.AuthStartupFlow;
import com.tfs.pages.GiftSetsPage;
import com.tfs.pages.GiftSetsPage.ProductInfo;
import com.tfs.pages.LoginPage;
import com.tfs.tests.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** TC011: complete fresh-login Gift Sets PLP journey. */
public class GiftSetsTest extends BaseTest {

    @Test(groups = {"regression", "homepage", "giftsets", "plp"}, priority = 11)
    @TestInfo(id = "TC_011", title = "Gift Sets PLP, Show More, Sort and Filter",
            module = "Home Page")
    public void TC_011_verifyGiftSetsPlpShowMoreSortAndFilter() {
        GiftSetsPage giftSetsPage = new GiftSetsPage();
        String currentStep = "Initialize TC011";
        long executionStartedAt = System.nanoTime();

        try {
            currentStep = "Fresh login and reach Home page";
            step(0, currentStep);
            performFreshLogin();

            currentStep = "Select Gift Sets category from Home page";
            step(1, currentStep);
            Assert.assertTrue(giftSetsPage.isGiftSetsQuickAccessDisplayed(),
                    "Gift Sets quick-access category was not displayed on Home page.");
            giftSetsPage.openGiftSetsCategory();

            currentStep = "Validate Gift Sets total product count";
            step(2, currentStep);
            Assert.assertTrue(giftSetsPage.isGiftSetsPlpDisplayed(),
                    "All Gift Sets PLP was not displayed.");
            int totalProducts = giftSetsPage.getTotalProductCount();
            Assert.assertTrue(totalProducts > 0,
                    "Gift Sets total product count must be greater than zero.");

            currentStep = "Validate Gift Sets product details";
            step(3, currentStep);
            List<ProductInfo> products = giftSetsPage.validateAndPrintFirstTwoProducts();
            Assert.assertEquals(products.size(), 2,
                    "Exactly two Gift Sets products should be validated.");

            currentStep = "Scroll to Show More and load new products";
            step(4, currentStep);
            int newlyLoadedProducts = giftSetsPage.clickShowMoreAndVerifyNewProducts(1);
            Assert.assertTrue(newlyLoadedProducts > 0,
                    "Show More did not load any new Gift Sets products.");

            currentStep = "Return to PLP top and select Sort By";
            step(5, currentStep);
            giftSetsPage.openSortAndValidateOptions();

            currentStep = "Validate Sort By options";
            step(6, currentStep);
            // All expected options are validated by openSortAndValidateOptions().

            currentStep = "Apply Price low-to-high and validate product count";
            step(7, currentStep);
            giftSetsPage.selectLowToHighAndValidate();

            currentStep = "Open Filter functionality";
            step(8, currentStep);
            giftSetsPage.openFilter();

            currentStep = "Select Armani and show results";
            step(9, currentStep);
            giftSetsPage.selectArmaniAndShowResults();

            currentStep = "Validate filtered PLP and product count";
            step(10, currentStep);
            int filteredCount = giftSetsPage.validateArmaniFilteredResults();

            currentStep = "Navigate back to Home page";
            step(11, currentStep);
            Assert.assertTrue(giftSetsPage.goBackHome(),
                    "Unable to navigate from All Gift Sets PLP to Home page.");

            currentStep = "Close the application";
            step(12, currentStep);
            Assert.assertTrue(giftSetsPage.closeApplication(),
                    "The TFS application did not close successfully.");

            System.out.println("==================================================");
            System.out.println("[PASS] TC011 Steps 1-12 completed successfully.");
            System.out.println("[RESULT] Total Gift Sets products : " + totalProducts);
            System.out.println("[RESULT] New products detected    : " + newlyLoadedProducts);
            System.out.println("[RESULT] Filtered Armani count    : " + filteredCount);
            System.out.println("==================================================");
        } catch (AssertionError | RuntimeException failure) {
            System.out.println("==================================================");
            System.out.println("[TC011 FAILED]");
            System.out.println("[FAILED STEP] " + currentStep);
            System.out.println("[ERROR] " + failure.getMessage());
            System.out.println("==================================================");
            throw failure;
        } finally {
            long elapsedSeconds =
                    (System.nanoTime() - executionStartedAt) / 1_000_000_000L;
            System.out.println("[TC011 TEST METHOD TIME] " + (elapsedSeconds / 60)
                    + " minute(s) " + (elapsedSeconds % 60) + " second(s)");
        }
    }

    private void performFreshLogin() {
        System.out.println("[STARTUP] TC011 fresh-login flow started.");
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
        System.out.println("[PASS] TC011 fresh login completed successfully.");
    }

    private void step(int number, String description) {
        String label = number == 0 ? "LOGIN" : String.format("%02d", number);
        System.out.println("[STEP " + label + "] " + description);
    }
}
