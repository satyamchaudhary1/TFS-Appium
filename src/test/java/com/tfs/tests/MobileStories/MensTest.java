package com.tfs.tests.MobileStories;

import com.tfs.annotations.TestInfo;
import com.tfs.config.ConfigManager;
import com.tfs.flows.AuthStartupFlow;
import com.tfs.pages.LoginPage;
import com.tfs.pages.MensPage;
import com.tfs.pages.MensPage.ProductInfo;
import com.tfs.tests.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * TC010 Men's PLP Steps 1-13.
 *
 * Uses the normal BaseTest lifecycle so every execution starts from a clean
 * application state and performs the complete login and TC010 journey.
 */
public class MensTest extends BaseTest {

    @Test(groups = {"regression", "homepage", "mens", "plp"}, priority = 10)
    @TestInfo(id = "TC_010", title = "Men's PLP, Show More, Sort and Filter",
            module = "Home Page")
    public void TC_010_verifyMensPlpAndShowMoreTwice() {
        MensPage mensPage = new MensPage();
        String currentStep = "Initialize TC010";
        long executionStartedAt = System.nanoTime();

        try {
            currentStep = "Login and reach Home page";
            System.out.println("[STEP] " + currentStep);
            ensureLoggedInHome();

            currentStep = "Select Men's category from Home page";
            System.out.println("[STEP] " + currentStep);
            Assert.assertTrue(mensPage.isMensQuickAccessDisplayed(),
                    "Men's quick-access category was not displayed on Home page.");
            mensPage.openMensCategory();

            currentStep = "Verify Men's PLP and total product count";
            System.out.println("[STEP] " + currentStep);
            Assert.assertTrue(mensPage.isMensPlpDisplayed(),
                    "Men's Fragrances PLP was not displayed.");
            int totalProducts = mensPage.getTotalProductCount();
            Assert.assertTrue(totalProducts > 0,
                    "Men's total product count must be greater than zero.");

            currentStep = "Validate and print first 2 initial products";
            System.out.println("[STEP] " + currentStep);
            List<ProductInfo> products = mensPage.validateAndPrintFirstTwoProducts();
            Assert.assertEquals(products.size(), 2,
                    "Exactly two initial products should be validated and printed.");

            currentStep = "Scroll and click first Show More";
            System.out.println("[STEP] " + currentStep);
            int firstBatch = mensPage.clickShowMoreAndVerifyNewProducts(1);
            Assert.assertTrue(firstBatch > 0,
                    "First Show More did not load any newly detected product.");

            currentStep = "Scroll and click second Show More";
            System.out.println("[STEP] " + currentStep);
            int secondBatch = mensPage.clickShowMoreAndVerifyNewProducts(2);
            Assert.assertTrue(secondBatch > 0,
                    "Second Show More did not load any newly detected product.");

            currentStep = "Return to top and validate Sort By options";
            System.out.println("[STEP] " + currentStep);
            mensPage.openSortAndValidateOptions();

            currentStep = "Apply Price low-to-high and validate sorted PLP";
            System.out.println("[STEP] " + currentStep);
            mensPage.selectLowToHighAndValidate();

            currentStep = "Open Filter screen";
            System.out.println("[STEP] " + currentStep);
            mensPage.openFilter();

            currentStep = "Apply 1667 filter and validate results";
            System.out.println("[STEP] " + currentStep);
            int filteredCount = mensPage.apply1667FilterAndValidate();
            Assert.assertTrue(filteredCount > 0,
                    "1667 filter returned no Men's products.");

            currentStep = "Navigate back to Home page";
            System.out.println("[STEP] " + currentStep);
            Assert.assertTrue(mensPage.goBackHome(),
                    "Unable to navigate from Men's PLP to Home page.");

            currentStep = "Close the APK";
            System.out.println("[STEP] " + currentStep);
            Assert.assertTrue(mensPage.closeApplication(),
                    "The TFS APK did not close successfully.");

            System.out.println("==================================================");
            System.out.println("[PASS] TC010 Steps 1-13 completed successfully.");
            System.out.println("[RESULT] Total PLP products : " + totalProducts);
            System.out.println("[RESULT] First load detected : " + firstBatch);
            System.out.println("[RESULT] Second load detected: " + secondBatch);
            System.out.println("[RESULT] Filtered 1667 count : " + filteredCount);
            System.out.println("==================================================");
        } catch (AssertionError | RuntimeException failure) {
            System.out.println("==================================================");
            System.out.println("[TC010 FAILED]");
            System.out.println("[FAILED STEP] " + currentStep);
            System.out.println("[ERROR] " + failure.getMessage());
            System.out.println("==================================================");
            throw failure;
        } finally {
            long elapsedSeconds =
                    (System.nanoTime() - executionStartedAt) / 1_000_000_000L;
            System.out.println("[TC010 TEST METHOD TIME] " + (elapsedSeconds / 60)
                    + " minute(s) " + (elapsedSeconds % 60) + " second(s)");
        }
    }

    private void ensureLoggedInHome() {
        System.out.println("[STARTUP] Fresh-login flow started.");
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
        System.out.println("[PASS] Fresh login completed successfully.");
    }
}
