package com.tfs.tests.MobileStories;

import com.tfs.annotations.TestInfo;
import com.tfs.config.ConfigManager;
import com.tfs.flows.AuthStartupFlow;
import com.tfs.pages.SalePage;
import com.tfs.pages.SalePage.ProductInfo;
import com.tfs.pages.LoginPage;
import com.tfs.tests.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** TC013: complete fresh-login Sale PLP journey. */
public class SaleTest extends BaseTest {

    @Test(groups = {"regression", "homepage", "sale", "plp"}, priority = 13)
    @TestInfo(id = "TC_013", title = "Sale PLP, Show More, Sort and Filter",
            module = "Home Page")
    public void TC_013_verifySalePlpShowMoreSortAndFilter() {
        SalePage salePage = new SalePage();
        String currentStep = "Initialize TC013";
        long executionStartedAt = System.nanoTime();

        try {
            currentStep = "Fresh login and reach Home page";
            step(0, currentStep);
            performFreshLogin();

            currentStep = "Scroll Mobile Stories horizontally to Sale";
            step(1, currentStep);
            salePage.scrollMobileStoriesHorizontallyToSale();

            currentStep = "Select Sale category from Home page";
            step(2, currentStep);
            Assert.assertTrue(salePage.isSaleQuickAccessDisplayed(),
                    "Sale quick-access category was not displayed on Home page.");
            salePage.openSaleCategory();

            if (salePage.isEmptyCategoryDisplayed()) {
                executeEmptyCategoryJourney(salePage);
                return;
            }

            currentStep = "Validate Sale total product count";
            step(3, currentStep);
            Assert.assertTrue(salePage.isSalePlpDisplayed(),
                    "Sale PLP was not displayed.");
            int totalProducts = salePage.getTotalProductCount();
            Assert.assertTrue(totalProducts > 0,
                    "Sale total product count must be greater than zero.");

            currentStep = "Validate Sale product details";
            step(4, currentStep);
            List<ProductInfo> products = salePage.validateAndPrintFirstTwoProducts();
            Assert.assertEquals(products.size(), 2,
                    "Exactly two Sale products should be validated.");

            currentStep = "Scroll to Show More and load new products";
            step(5, currentStep);
            int newlyLoadedProducts = salePage.clickShowMoreAndVerifyNewProducts(1);
            Assert.assertTrue(newlyLoadedProducts > 0,
                    "Show More did not load any new Sale products.");

            currentStep = "Return to PLP top and select Sort By";
            step(6, currentStep);
            salePage.openSortAndValidateOptions();

            currentStep = "Validate Sort By options";
            step(7, currentStep);
            // All expected options are validated by openSortAndValidateOptions().

            currentStep = "Apply Price low-to-high and validate product count";
            step(8, currentStep);
            salePage.selectLowToHighAndValidate();

            currentStep = "Open Filter functionality";
            step(9, currentStep);
            salePage.openFilter();

            currentStep = "Select Aery and show results";
            step(10, currentStep);
            salePage.selectAeryAndShowResults();

            currentStep = "Validate filtered PLP and product count";
            step(11, currentStep);
            int filteredCount = salePage.validateAeryFilteredResults();

            currentStep = "Navigate back to Home page";
            step(12, currentStep);
            Assert.assertTrue(salePage.goBackHome(),
                    "Unable to navigate from Sale PLP to Home page.");

            currentStep = "Close the application";
            step(13, currentStep);
            Assert.assertTrue(salePage.closeApplication(),
                    "The TFS application did not close successfully.");

            System.out.println("==================================================");
            System.out.println("[PASS] TC013 Steps 1-13 completed successfully.");
            System.out.println("[RESULT] Total Sale products : " + totalProducts);
            System.out.println("[RESULT] New products detected    : " + newlyLoadedProducts);
            System.out.println("[RESULT] Filtered Aery count    : " + filteredCount);
            System.out.println("==================================================");
        } catch (AssertionError | RuntimeException failure) {
            System.out.println("==================================================");
            System.out.println("[TC013 FAILED]");
            System.out.println("[FAILED STEP] " + currentStep);
            System.out.println("[ERROR TYPE] " + failure.getClass().getSimpleName());
            System.out.println("[ERROR DETAILS] " + meaningfulMessage(failure));
            System.out.println("==================================================");
            throw failure;
        } finally {
            long elapsedSeconds =
                    (System.nanoTime() - executionStartedAt) / 1_000_000_000L;
            System.out.println("[TC013 TEST METHOD TIME] " + (elapsedSeconds / 60)
                    + " minute(s) " + (elapsedSeconds % 60) + " second(s)");
        }
    }

    private void performFreshLogin() {
        System.out.println("[STARTUP] TC013 fresh-login flow started.");
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
        System.out.println("[PASS] TC013 fresh login completed successfully.");
    }

    private void step(int number, String description) {
        String label = number == 0 ? "LOGIN" : String.format("%02d", number);
        System.out.println("[STEP " + label + "] " + description);
    }

    private void executeEmptyCategoryJourney(SalePage salePage) {
        step(3, "Validate Sale empty state and product count");
        int emptyCount = salePage.validateEmptyCategory();

        step(4, "Validate BROWSE BESTSELLERS button");
        Assert.assertTrue(salePage.isBrowseBestSellersDisplayedAndEnabled(),
                "BROWSE BESTSELLERS button was not displayed and enabled.");

        step(5, "Click BROWSE BESTSELLERS and validate Best Seller PLP");
        boolean bestSellerProductsFound =
                salePage.clickBrowseBestSellersAndVerifyRedirect();
        if (!bestSellerProductsFound) {
            System.out.println("[INFO] Best Seller PLP contains 0 products. "
                    + "Proceeding directly to the Home navigation step.");
        }

        System.out.println("[SKIP][STEPS 06-11] Sale-specific Show More, "
                + "Sort and Filter validations are not applicable because the "
                + "selected category contains 0 products.");

        step(12, "Navigate from Best Seller PLP back to Home page");
        Assert.assertTrue(salePage.goBackFromBestSellerToHome(),
                "Unable to navigate from Best Seller PLP to Home page.");

        step(13, "Close the application");
        Assert.assertTrue(salePage.closeApplication(),
                "The TFS application did not close successfully.");

        System.out.println("==================================================");
        System.out.println("[PASS] TC013 empty-category alternate journey completed.");
        System.out.println("[RESULT] Sale product count     : " + emptyCount);
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
                ? "No error message was supplied. Review the first TC013 stack-trace line."
                : message;
    }
}
