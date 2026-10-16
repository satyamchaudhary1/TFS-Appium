package com.tfs.tests.MobileStories;

import com.tfs.annotations.TestInfo;
import com.tfs.config.ConfigManager;
import com.tfs.flows.AuthStartupFlow;
import com.tfs.pages.LoginPage;
import com.tfs.pages.WomensPage;
import com.tfs.pages.WomensPage.ProductInfo;
import com.tfs.tests.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** TC009 complete Steps 1–13. */
public class WomensTest extends BaseTest {

    @Test(groups = {"regression", "homepage", "womens", "plp"}, priority = 9)
    @TestInfo(id = "TC_009", title = "Women's PLP, Show More, Sort and Filter",
            module = "Home Page")
    public void TC_009_verifyWomensPlpAndShowMoreTwice() {
        WomensPage womensPage = new WomensPage();
        String currentStep = "Initialize TC009";
        long executionStartedAt = System.nanoTime();

        try {
            currentStep = "Login and reach Home page";
            System.out.println("[STEP] " + currentStep);
            ensureLoggedInHome(womensPage);

            currentStep = "Select Women's category from Home page";
            System.out.println("[STEP] " + currentStep);
            Assert.assertTrue(womensPage.isWomensQuickAccessDisplayed(),
                    "Women's quick-access category was not displayed on Home page.");
            womensPage.openWomensCategory();

            currentStep = "Verify Women's PLP and total product count";
            System.out.println("[STEP] " + currentStep);
            Assert.assertTrue(womensPage.isWomensPlpDisplayed(),
                    "Women's Fragrances PLP was not displayed.");
            int totalProducts = womensPage.getTotalProductCount();
            Assert.assertTrue(totalProducts > 0,
                    "Women's total product count must be greater than zero.");

            currentStep = "Validate and print first 2 initial products";
            System.out.println("[STEP] " + currentStep);
            List<ProductInfo> products = womensPage.validateAndPrintFirstTwoProducts();
            Assert.assertEquals(products.size(), 2,
                    "Exactly two initial products should be validated and printed.");

            currentStep = "Scroll and click first Show More";
            System.out.println("[STEP] " + currentStep);
            int firstBatch = womensPage.clickShowMoreAndVerifyNewProducts(1);
            Assert.assertTrue(firstBatch > 0,
                    "First Show More did not load any newly detected product.");

            currentStep = "Scroll and click second Show More";
            System.out.println("[STEP] " + currentStep);
            int secondBatch = womensPage.clickShowMoreAndVerifyNewProducts(2);
            Assert.assertTrue(secondBatch > 0,
                    "Second Show More did not load any newly detected product.");

            currentStep = "Return to PLP top and validate Sort By options";
            System.out.println("[STEP] " + currentStep);
            womensPage.openSortAndValidateOptions();

            currentStep = "Apply Price low-to-high and validate products/count";
            System.out.println("[STEP] " + currentStep);
            womensPage.selectLowToHighAndValidate();

            currentStep = "Open Filter screen";
            System.out.println("[STEP] " + currentStep);
            womensPage.openFilter();

            currentStep = "Apply 100bon filter and validate filtered PLP/count";
            System.out.println("[STEP] " + currentStep);
            int filteredCount = womensPage.apply100bonFilterAndValidate();
            Assert.assertTrue(filteredCount > 0,
                    "100bon filtered product count must be greater than zero.");

            currentStep = "Go back to Home page";
            System.out.println("[STEP] " + currentStep);
            Assert.assertTrue(womensPage.goBackHome(),
                    "Unable to return from Women's PLP to Home page.");

            currentStep = "Close the application";
            System.out.println("[STEP] " + currentStep);
            Assert.assertTrue(womensPage.closeApplication(),
                    "The TFS application did not close successfully.");

            System.out.println("==================================================");
            System.out.println("[PASS] TC009 Steps 1–13 completed successfully.");
            System.out.println("[RESULT] Total PLP products : " + totalProducts);
            System.out.println("[RESULT] First load detected : " + firstBatch);
            System.out.println("[RESULT] Second load detected: " + secondBatch);
            System.out.println("[RESULT] 100bon filtered count: " + filteredCount);
            System.out.println("==================================================");
        } catch (AssertionError | RuntimeException failure) {
            System.out.println("==================================================");
            System.out.println("[TC009 FAILED]");
            System.out.println("[FAILED STEP] " + currentStep);
            System.out.println("[ERROR] " + failure.getMessage());
            System.out.println("==================================================");
            throw failure;
        } finally {
            long elapsedSeconds = (System.nanoTime() - executionStartedAt) / 1_000_000_000L;
            System.out.println("[TC009 EXECUTION TIME] " + (elapsedSeconds / 60)
                    + " minute(s) " + (elapsedSeconds % 60) + " second(s)");
        }
    }

    private void ensureLoggedInHome(WomensPage womensPage) {
        if (womensPage.isHomePageDisplayedQuickly()) {
            System.out.println("[INFO] Home page already displayed; login flow skipped.");
            return;
        }

        new AuthStartupFlow().navigateToAuthLanding();
        LoginPage loginPage = new LoginPage();
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
    }
}