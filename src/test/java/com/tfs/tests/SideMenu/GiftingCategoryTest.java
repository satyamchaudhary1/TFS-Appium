package com.tfs.tests.SideMenu;

import com.tfs.annotations.TestInfo;
import com.tfs.config.ConfigManager;
import com.tfs.flows.AuthStartupFlow;
import com.tfs.pages.LoginPage;
import com.tfs.pages.SideMenuGiftingPage;
import com.tfs.tests.BaseTest;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC028 - Side Menu -> "Gifting" category -> "Shop Gifts" -> "Gift Sets for
 * Her" PLP -> Show More -> Sort by "Price : low to high" -> Filter (Armani,
 * GBP 50.0-100.0) -> show results -> back -> collapse Shop Gifts -> expand and
 * collapse Gift Inspiration, Make It Special, Shop By Price -> Home icon ->
 * close the app.
 * <p>
 * Requested as "Test Case 29"; numbered TC028 (next after TC027, the Home
 * category test).
 * <p>
 * PART 1 (Authentication) mirrors LoginTest.java's TC_003 exactly, the same
 * way the earlier Side Menu tests do. Placed under com.tfs.tests.SideMenu.
 */
public class GiftingCategoryTest extends BaseTest {

    @Test
    @TestInfo(id = "TC028", title = "Side Menu Gifting category - Gift Sets for Her PLP, sort, filter and sub-categories", module = "SideMenu", requiresDriver = true)
    public void TC_028_verifyGiftingCategoryFlow() {

        // ==========================================================
        // TEST DATA
        // ==========================================================
        String email = ConfigManager.require("login.email");
        String password = ConfigManager.require("login.password");

        System.out.println();
        System.out.println("==================================================");
        System.out.println("TC_025 - SIDE MENU GIFTING CATEGORY FLOW");
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
        // PART 2 - SIDE MENU GIFTING CATEGORY FLOW
        // ==========================================================
        System.out.println();
        System.out.println("==================================================");
        System.out.println("PART 2 - SIDE MENU GIFTING CATEGORY FLOW");
        System.out.println("==================================================");

        SideMenuGiftingPage giftingPage = new SideMenuGiftingPage();
        long startTime = System.currentTimeMillis();

        try {
            // Step 1-3: Side Menu -> Gifting -> count sub-categories.
            giftingPage.openSideMenu();
            giftingPage.selectGiftingCategory();
            giftingPage.countAndPrintSubCategories();

            // Step 4-6: Shop Gifts -> Gift Sets for Her -> read the PLP.
            giftingPage.expandShopGiftsAndSelectGiftSetsForHer();
            giftingPage.readGiftSetsPlpFully();

            // Step 7: Show More.
            giftingPage.tapShowMore();

            // Step 8-9: back to the top, Sort by -> Price : low to high.
            giftingPage.scrollToTopAndSortByPriceLowToHigh();

            // Step 10-14: Filter (Armani, GBP 50.0-100.0) -> show results.
            giftingPage.applyArmaniAndPriceFilter();

            // Step 15: read the sorted + filtered PLP.
            giftingPage.readFilteredPlpFully();

            // Step 16-17: back arrow, collapse Shop Gifts.
            giftingPage.tapBackArrowToReturn("returning from the filtered PLP to the Shop Gifts list");
            giftingPage.collapseShopGifts();

            // Step 18-23: the other three sub-categories.
            giftingPage.expandScrollAndCollapseGiftInspiration();
            giftingPage.expandReadAndCollapseMakeItSpecial();
            giftingPage.expandReadAndCollapseShopByPrice();

            // Step 24: Home icon in the bottom navigation.
            giftingPage.tapHomeNavIconAndVerifyHomePage();

            // Step 25: close the app.
            giftingPage.closeApp();

            long durationSec = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println();
            System.out.println("==================================================");
            System.out.println("[TC028 PASSED]");
            System.out.println("[TC028 TEST METHOD TIME] " + (durationSec / 60) + " minute(s) "
                    + (durationSec % 60) + " second(s)");
            System.out.println("==================================================");

        } catch (Exception | AssertionError e) {
            long durationSec = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println();
            System.out.println("==================================================");
            System.out.println("[TC028 FAILED]");
            System.out.println("[ERROR TYPE] " + e.getClass().getSimpleName());
            System.out.println("[ERROR DETAILS] " + e.getMessage());
            System.out.println("[TC028 TEST METHOD TIME] " + (durationSec / 60) + " minute(s) "
                    + (durationSec % 60) + " second(s)");
            System.out.println("==================================================");
            throw e;
        }
    }
}