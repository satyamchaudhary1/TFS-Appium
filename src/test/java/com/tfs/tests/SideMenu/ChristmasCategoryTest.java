package com.tfs.tests.SideMenu;

import com.tfs.annotations.TestInfo;
import com.tfs.config.ConfigManager;
import com.tfs.flows.AuthStartupFlow;
import com.tfs.pages.LoginPage;
import com.tfs.pages.SideMenuChristmasPage;
import com.tfs.tests.BaseTest;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC029 - Side Menu -> "Christmas" category -> "Women's Christmas" -> "Gift Sets
 * for Her" PLP -> Show More -> Sort by "Product Name : Z-A" -> Filter (Armani,
 * GBP 50.0-100.0) -> show results -> back -> collapse Women's Christmas ->
 * expand and collapse Men's Christmas and Discover The Perfect Gift -> collapse
 * Christmas -> Home icon -> close the app.
 * <p>
 * Requested as "Test Case 30"; numbered TC029 (next after TC028, the Gifting
 * category test).
 * <p>
 * PART 1 (Authentication) mirrors LoginTest.java's TC_003 exactly, the same
 * way the earlier Side Menu tests do. Placed under com.tfs.tests.SideMenu.
 */
public class ChristmasCategoryTest extends BaseTest {

    @Test
    @TestInfo(id = "TC029", title = "Side Menu Christmas category - Gift Sets for Her PLP, sort, filter and sub-categories", module = "SideMenu", requiresDriver = true)
    public void TC_029_verifyChristmasCategoryFlow() {

        // ==========================================================
        // TEST DATA
        // ==========================================================
        String email = ConfigManager.require("login.email");
        String password = ConfigManager.require("login.password");

        System.out.println();
        System.out.println("==================================================");
        System.out.println("TC_025 - SIDE MENU CHRISTMAS CATEGORY FLOW");
        System.out.println("==================================================");
        System.out.println();
        System.out.println("---------------- TEST DATA ----------------");
        System.out.println("[DATA] Email    : " + email);
        System.out.println("[DATA] Password : ********");
        System.out.println("-------------------------------------------");

        // ==========================================================
        // PART 1 - AUTHENTICATION (mirrors LoginTest.TC_003)
        // ==========================================================
        System.out.println();
        System.out.println("==================================================");
        System.out.println("PART 1 - AUTHENTICATION");
        System.out.println("==================================================");

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("[STEP] 01 - Launch Application");
        System.out.println("--------------------------------------------------");
        System.out.println("[PASS] Application launched successfully.");

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("[STEP] 02 - Prepare Authentication Landing Screen");
        System.out.println("--------------------------------------------------");
        AuthStartupFlow startupFlow = new AuthStartupFlow();
        startupFlow.navigateToAuthLanding();
        System.out.println("[PASS] Authentication Landing Screen ready.");

        LoginPage loginPage = new LoginPage();

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("[STEP] 03 - Validate Sign In Button");
        System.out.println("--------------------------------------------------");
        boolean signInDisplayed = loginPage.isSignInButtonDisplayed();
        System.out.println("[RESULT] Sign In Button Displayed : " + (signInDisplayed ? "PASS" : "FAIL"));
        Assert.assertTrue(signInDisplayed, "Sign In button was not displayed.");

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("[STEP] 04 - Open Login Screen");
        System.out.println("--------------------------------------------------");
        System.out.println("[ACTION] Clicking Sign In.");
        loginPage.clickSignIn();
        System.out.println("[PASS] Sign In clicked successfully.");

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("[STEP] 05 - Validate Login Screen");
        System.out.println("--------------------------------------------------");
        boolean loginScreenDisplayed = loginPage.isLoginScreenDisplayed();
        System.out.println("[RESULT] Login Screen Displayed : " + (loginScreenDisplayed ? "PASS" : "FAIL"));
        Assert.assertTrue(loginScreenDisplayed, "Login screen was not displayed.");

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("[STEP] 06 - Enter Email");
        System.out.println("--------------------------------------------------");
        System.out.println("[ACTION] Entering email: " + email);
        loginPage.enterEmail(email);
        System.out.println("[PASS] Email entered successfully.");

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("[STEP] 07 - Enter Password");
        System.out.println("--------------------------------------------------");
        System.out.println("[ACTION] Entering password.");
        loginPage.enterPassword(password);
        System.out.println("[PASS] Password entered successfully.");

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("[STEP] 08 - Click Log In");
        System.out.println("--------------------------------------------------");
        System.out.println("[ACTION] Clicking Log In.");
        loginPage.clickLogin();
        System.out.println("[PASS] Log In action completed.");

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("[STEP] 09 - Check Login Result");
        System.out.println("--------------------------------------------------");
        if (loginPage.isLoginErrorDisplayed()) {
            String message = loginPage.getLoginErrorMessage();
            System.out.println("[FAIL] Login error displayed.");
            System.out.println("[APP MESSAGE] " + message);
            loginPage.closeLoginErrorPopup();
            Assert.fail("Login failed: " + message);
        }
        System.out.println("[PASS] No login error displayed.");

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("[STEP] 10 - Handle Optional Post-Login Popups");
        System.out.println("--------------------------------------------------");
        loginPage.handlePostLoginPopups();
        System.out.println("[PASS] Post-login popup handling completed.");

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("[STEP] 11 - Validate Home Screen");
        System.out.println("--------------------------------------------------");
        boolean homeDisplayed = loginPage.isHomeScreenDisplayed();
        System.out.println("[RESULT] Home Screen Displayed : " + (homeDisplayed ? "PASS" : "FAIL"));
        Assert.assertTrue(homeDisplayed, "Home screen was not displayed after valid login.");

        // ==========================================================
        // PART 2 - SIDE MENU CHRISTMAS CATEGORY FLOW
        // ==========================================================
        System.out.println();
        System.out.println("==================================================");
        System.out.println("PART 2 - SIDE MENU CHRISTMAS CATEGORY FLOW");
        System.out.println("==================================================");

        SideMenuChristmasPage christmasPage = new SideMenuChristmasPage();
        long startTime = System.currentTimeMillis();

        try {
            // Step 1-3: Side Menu -> print categories -> Christmas -> count sub-categories.
            christmasPage.openSideMenu();
            christmasPage.printCategoriesAndSelectChristmas();
            christmasPage.countAndPrintSubCategories();

            // Step 4-6: Women's Christmas -> Gift Sets for Her -> read the PLP.
            christmasPage.expandWomensChristmasAndSelectGiftSetsForHer();
            christmasPage.readGiftSetsForHerPlpFully();

            // Step 7: Show More.
            christmasPage.tapShowMore();

            // Step 8-9: back to the top, Sort by -> Product Name : Z-A.
            christmasPage.scrollToTopAndSortByProductNameZtoA();

            // Step 10-14: Filter (Armani, GBP 50.0-100.0) -> show results.
            christmasPage.applyArmaniAndPriceFilter();

            // Step 15: read the sorted + filtered PLP.
            christmasPage.readFilteredPlpFully();

            // Step 16-17: back arrow, collapse Women's Christmas.
            christmasPage.tapBackArrowToReturn("returning from the filtered PLP to the Women's Christmas list");
            christmasPage.collapseWomensChristmas();

            // Step 18-21: the other two sub-categories.
            christmasPage.expandScrollAndCollapseMensChristmas();
            christmasPage.expandReadAndCollapseDiscoverPerfectGift();

            // Step 22: collapse the main Christmas row.
            christmasPage.collapseChristmasMainCategory();

            // Step 23: Home icon in the bottom navigation.
            christmasPage.tapHomeNavIconAndVerifyHomePage();

            // Step 24: close the app.
            christmasPage.closeApp();

            long durationSec = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println();
            System.out.println("==================================================");
            System.out.println("[TC029 PASSED]");
            System.out.println("[TC029 TEST METHOD TIME] " + (durationSec / 60) + " minute(s) "
                    + (durationSec % 60) + " second(s)");
            System.out.println("==================================================");

        } catch (Exception | AssertionError e) {
            long durationSec = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println();
            System.out.println("==================================================");
            System.out.println("[TC029 FAILED]");
            System.out.println("[ERROR TYPE] " + e.getClass().getSimpleName());
            System.out.println("[ERROR DETAILS] " + e.getMessage());
            System.out.println("[TC029 TEST METHOD TIME] " + (durationSec / 60) + " minute(s) "
                    + (durationSec % 60) + " second(s)");
            System.out.println("==================================================");
            throw e;
        }
    }
}