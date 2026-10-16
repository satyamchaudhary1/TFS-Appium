package com.tfs.tests.MobileStories;

import com.tfs.annotations.TestInfo;
import com.tfs.config.ConfigManager;
import com.tfs.flows.AuthStartupFlow;
import com.tfs.pages.NewInPage;
import com.tfs.pages.NewInPage.ProductInfo;
import com.tfs.pages.LoginPage;
import com.tfs.tests.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** TC012: complete fresh-login New In PLP journey. */
public class NewInTest extends BaseTest {

    @Test(groups = {"regression", "homepage", "newin", "plp"}, priority = 12)
    @TestInfo(id = "TC_012", title = "New In PLP, Show More, Sort and Filter",
            module = "Home Page")
    public void TC_012_verifyNewInPlpShowMoreSortAndFilter() {
        NewInPage newInPage = new NewInPage();
        String currentStep = "Initialize TC012";
        long executionStartedAt = System.nanoTime();

        try {
            currentStep = "Fresh login and reach Home page";
            step(0, currentStep);
            performFreshLogin();

            currentStep = "Select New In category from Home page";
            step(1, currentStep);
            Assert.assertTrue(newInPage.isNewInQuickAccessDisplayed(),
                    "New In quick-access category was not displayed on Home page.");
            newInPage.openNewInCategory();

            if (newInPage.isEmptyCategoryDisplayed()) {
                executeEmptyCategoryJourney(newInPage);
                return;
            }

            currentStep = "Validate New In total product count";
            step(2, currentStep);
            Assert.assertTrue(newInPage.isNewInPlpDisplayed(),
                    "New In PLP was not displayed.");
            int totalProducts = newInPage.getTotalProductCount();
            Assert.assertTrue(totalProducts > 0,
                    "New In total product count must be greater than zero.");

            currentStep = "Validate New In product details";
            step(3, currentStep);
            List<ProductInfo> products = newInPage.validateAndPrintFirstTwoProducts();
            Assert.assertEquals(products.size(), 2,
                    "Exactly two New In products should be validated.");

            currentStep = "Scroll to Show More and load new products";
            step(4, currentStep);
            int newlyLoadedProducts = newInPage.clickShowMoreAndVerifyNewProducts(1);
            Assert.assertTrue(newlyLoadedProducts > 0,
                    "Show More did not load any new New In products.");

            currentStep = "Return to PLP top and select Sort By";
            step(5, currentStep);
            newInPage.openSortAndValidateOptions();

            currentStep = "Validate Sort By options";
            step(6, currentStep);
            // All expected options are validated by openSortAndValidateOptions().

            currentStep = "Apply Price low-to-high and validate product count";
            step(7, currentStep);
            newInPage.selectLowToHighAndValidate();

            currentStep = "Open Filter functionality";
            step(8, currentStep);
            newInPage.openFilter();

            currentStep = "Select Armani and show results";
            step(9, currentStep);
            newInPage.selectArmaniAndShowResults();

            currentStep = "Validate filtered PLP and product count";
            step(10, currentStep);
            int filteredCount = newInPage.validateArmaniFilteredResults();

            currentStep = "Navigate back to Home page";
            step(11, currentStep);
            Assert.assertTrue(newInPage.goBackHome(),
                    "Unable to navigate from New In PLP to Home page.");

            currentStep = "Close the application";
            step(12, currentStep);
            Assert.assertTrue(newInPage.closeApplication(),
                    "The TFS application did not close successfully.");

            System.out.println("==================================================");
            System.out.println("[PASS] TC012 Steps 1-12 completed successfully.");
            System.out.println("[RESULT] Total New In products : " + totalProducts);
            System.out.println("[RESULT] New products detected    : " + newlyLoadedProducts);
            System.out.println("[RESULT] Filtered Armani count    : " + filteredCount);
            System.out.println("==================================================");
        } catch (AssertionError | RuntimeException failure) {
            System.out.println("==================================================");
            System.out.println("[TC012 FAILED]");
            System.out.println("[FAILED STEP] " + currentStep);
            System.out.println("[ERROR TYPE] " + failure.getClass().getSimpleName());
            System.out.println("[ERROR DETAILS] " + meaningfulMessage(failure));
            System.out.println("==================================================");
            throw failure;
        } finally {
            long elapsedSeconds =
                    (System.nanoTime() - executionStartedAt) / 1_000_000_000L;
            System.out.println("[TC012 TEST METHOD TIME] " + (elapsedSeconds / 60)
                    + " minute(s) " + (elapsedSeconds % 60) + " second(s)");
        }
    }

    private void performFreshLogin() {
        System.out.println("[STARTUP] TC012 fresh-login flow started.");
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
        System.out.println("[PASS] TC012 fresh login completed successfully.");
    }

    private void step(int number, String description) {
        String label = number == 0 ? "LOGIN" : String.format("%02d", number);
        System.out.println("[STEP " + label + "] " + description);
    }

    private void executeEmptyCategoryJourney(NewInPage newInPage) {
        step(2, "Validate New In empty state and product count");
        int emptyCount = newInPage.validateEmptyCategory();

        step(3, "Validate BROWSE BESTSELLERS button");
        Assert.assertTrue(newInPage.isBrowseBestSellersDisplayedAndEnabled(),
                "BROWSE BESTSELLERS button was not displayed and enabled.");

        step(4, "Click BROWSE BESTSELLERS and validate Best Seller PLP");
        boolean bestSellerProductsFound =
                newInPage.clickBrowseBestSellersAndVerifyRedirect();
        if (!bestSellerProductsFound) {
            System.out.println("[INFO] Best Seller PLP contains 0 products. "
                    + "Proceeding directly to the Home navigation step.");
        }

        System.out.println("[SKIP][STEPS 05-10] New In-specific Show More, "
                + "Sort and Filter validations are not applicable because the "
                + "selected category contains 0 products.");

        step(11, "Navigate from Best Seller PLP back to Home page");
        Assert.assertTrue(newInPage.goBackFromBestSellerToHome(),
                "Unable to navigate from Best Seller PLP to Home page.");

        step(12, "Close the application");
        Assert.assertTrue(newInPage.closeApplication(),
                "The TFS application did not close successfully.");

        System.out.println("==================================================");
        System.out.println("[PASS] TC012 empty-category alternate journey completed.");
        System.out.println("[RESULT] New In product count     : " + emptyCount);
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
                ? "No error message was supplied. Review the first TC012 stack-trace line."
                : message;
    }
}
