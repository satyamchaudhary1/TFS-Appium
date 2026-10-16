package com.tfs.tests.MobileStories;

import com.tfs.annotations.TestInfo;
import com.tfs.config.ConfigManager;
import com.tfs.flows.AuthStartupFlow;
import com.tfs.pages.NichePage;
import com.tfs.pages.NichePage.ProductInfo;
import com.tfs.pages.LoginPage;
import com.tfs.tests.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** TC016: complete fresh-login Niche PLP journey. */
public class NicheTest extends BaseTest {

    @Test(groups = {"regression", "homepage", "niche", "plp"}, priority = 16)
    @TestInfo(id = "TC_016", title = "Niche PLP, Show More, Sort and Filter",
            module = "Home Page")
    public void TC_016_verifyNichePlpShowMoreSortAndFilter() {
        NichePage nichePage = new NichePage();
        String currentStep = "Initialize TC016";
        long executionStartedAt = System.nanoTime();

        try {
            currentStep = "Fresh login and reach Home page";
            step(0, currentStep);
            performFreshLogin();

            currentStep = "Scroll Mobile Stories horizontally to Niche";
            step(1, currentStep);
            nichePage.scrollMobileStoriesHorizontallyToNiche();

            currentStep = "Select Niche category from Home page";
            step(2, currentStep);
            Assert.assertTrue(nichePage.isNicheQuickAccessDisplayed(),
                    "Niche quick-access category was not displayed on Home page.");
            nichePage.openNicheCategory();

            if (nichePage.isEmptyCategoryDisplayed()) {
                executeEmptyCategoryJourney(nichePage);
                return;
            }

            currentStep = "Validate Niche total product count";
            step(3, currentStep);
            Assert.assertTrue(nichePage.isNichePlpDisplayed(),
                    "Niche PLP was not displayed.");
            int totalProducts = nichePage.getTotalProductCount();
            Assert.assertTrue(totalProducts > 0,
                    "Niche total product count must be greater than zero.");

            currentStep = "Validate Niche product details";
            step(4, currentStep);
            List<ProductInfo> products = nichePage.validateAndPrintFirstTwoProducts();
            Assert.assertEquals(products.size(), 2,
                    "Exactly two Niche products should be validated.");

            currentStep = "Scroll to Show More and load new products";
            step(5, currentStep);
            int newlyLoadedProducts = nichePage.clickShowMoreAndVerifyNewProducts(1);
            Assert.assertTrue(newlyLoadedProducts > 0,
                    "Show More did not load any new Niche products.");

            currentStep = "Return to PLP top and select Sort By";
            step(6, currentStep);
            nichePage.openSortAndValidateOptions();

            currentStep = "Validate Sort By options";
            step(7, currentStep);
            // All expected options are validated by openSortAndValidateOptions().

            currentStep = "Apply Product Name Z-A and validate product count";
            step(8, currentStep);
            nichePage.selectProductNameZToAAndValidate();

            currentStep = "Open Filter functionality";
            step(9, currentStep);
            nichePage.openFilter();

            currentStep = "Select Etro and show results";
            step(10, currentStep);
            nichePage.selectEtroAndShowResults();

            currentStep = "Validate filtered PLP and product count";
            step(11, currentStep);
            int filteredCount = nichePage.validateEtroFilteredResults();

            currentStep = "Navigate back to Home page";
            step(12, currentStep);
            Assert.assertTrue(nichePage.goBackHome(),
                    "Unable to navigate from Niche PLP to Home page.");

            currentStep = "Close the application";
            step(13, currentStep);
            Assert.assertTrue(nichePage.closeApplication(),
                    "The TFS application did not close successfully.");

            System.out.println("==================================================");
            System.out.println("[PASS] TC016 Steps 1-13 completed successfully.");
            System.out.println("[RESULT] Total Niche products : " + totalProducts);
            System.out.println("[RESULT] New products detected    : " + newlyLoadedProducts);
            System.out.println("[RESULT] Filtered Etro count    : " + filteredCount);
            System.out.println("==================================================");
        } catch (AssertionError | RuntimeException failure) {
            System.out.println("==================================================");
            System.out.println("[TC016 FAILED]");
            System.out.println("[FAILED STEP] " + currentStep);
            System.out.println("[ERROR TYPE] " + failure.getClass().getSimpleName());
            System.out.println("[ERROR DETAILS] " + meaningfulMessage(failure));
            System.out.println("==================================================");
            throw failure;
        } finally {
            long elapsedSeconds =
                    (System.nanoTime() - executionStartedAt) / 1_000_000_000L;
            System.out.println("[TC016 TEST METHOD TIME] " + (elapsedSeconds / 60)
                    + " minute(s) " + (elapsedSeconds % 60) + " second(s)");
        }
    }

    private void performFreshLogin() {
        System.out.println("[STARTUP] TC016 fresh-login flow started.");
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
        System.out.println("[PASS] TC016 fresh login completed successfully.");
    }

    private void step(int number, String description) {
        String label = number == 0 ? "LOGIN" : String.format("%02d", number);
        System.out.println("[STEP " + label + "] " + description);
    }

    private void executeEmptyCategoryJourney(NichePage nichePage) {
        step(3, "Validate Niche empty state and product count");
        int emptyCount = nichePage.validateEmptyCategory();

        System.out.println("[SKIP][STEPS 04-11] Niche product details, Show More, "
                + "Sort and Filter validations are not applicable because the "
                + "selected category contains 0 products.");

        Assert.assertTrue(nichePage.isBrowseBestSellersDisplayedAndEnabled(),
                "BROWSE BESTSELLERS must be displayed and enabled for an empty Niche category.");
        boolean bestSellerProductsFound =
                nichePage.clickBrowseBestSellersAndVerifyRedirect();
        System.out.println("[INFO] Best Seller products found after redirection: "
                + bestSellerProductsFound);

        step(12, "Navigate from Best Seller PLP back to Home page");
        Assert.assertTrue(nichePage.goBackFromBestSellerToHome(),
                "Unable to navigate from empty Niche PLP to Home page.");

        step(13, "Close the application");
        Assert.assertTrue(nichePage.closeApplication(),
                "The TFS application did not close successfully.");

        System.out.println("==================================================");
        System.out.println("[PASS] TC016 empty-category alternate journey completed.");
        System.out.println("[RESULT] Niche product count     : " + emptyCount);
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
                ? "No error message was supplied. Review the first TC016 stack-trace line."
                : message;
    }
}
