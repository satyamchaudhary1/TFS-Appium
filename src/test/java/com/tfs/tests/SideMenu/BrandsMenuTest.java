package com.tfs.tests.SideMenu;

import com.tfs.annotations.TestInfo;
import com.tfs.config.ConfigManager;
import com.tfs.flows.AuthStartupFlow;
import com.tfs.pages.LoginPage;
import com.tfs.pages.SideMenuBrandsPage;
import com.tfs.tests.BaseTest;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC030 - Side Menu -> "Brands" -> All Brands list (read and count) -> index
 * letters D and R -> "Rabanne" -> brand page (carousel, tabs, toggle, video,
 * About) -> "Discover Gifting" -> Rabanne PLP (brand check) -> Sort by "Price :
 * low to high" -> Filter (Body Lotion, Single) -> show results -> back x3 ->
 * Home icon -> close the app.
 * <p>
 * Requested as "Test Case 31"; numbered TC030 (next after TC029, the Christmas
 * category test).
 * <p>
 * PART 1 (Authentication) mirrors LoginTest.java's TC_003 exactly, the same
 * way the earlier Side Menu tests do. Placed under com.tfs.tests.SideMenu.
 */
public class BrandsMenuTest extends BaseTest {

    @Test
    @TestInfo(id = "TC030", title = "Side Menu Brands - All Brands list, Rabanne brand page, PLP sort and filter", module = "SideMenu", requiresDriver = true)
    public void TC_030_verifyBrandsMenuFlow() {

        // ==========================================================
        // TEST DATA
        // ==========================================================
        String email = ConfigManager.require("login.email");
        String password = ConfigManager.require("login.password");

        System.out.println();
        System.out.println("==================================================");
        System.out.println("TC_030 - SIDE MENU BRANDS FLOW");
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
        // PART 2 - SIDE MENU BRANDS FLOW
        // ==========================================================
        System.out.println();
        System.out.println("==================================================");
        System.out.println("PART 2 - SIDE MENU BRANDS FLOW");
        System.out.println("==================================================");

        SideMenuBrandsPage brandsPage = new SideMenuBrandsPage();
        long startTime = System.currentTimeMillis();

        try {
            // Step 1-2: Side Menu -> print categories -> Brands.
            brandsPage.openSideMenu();
            brandsPage.printCategoriesAndSelectBrands();

            // Step 3: read and count the whole All Brands list.
            brandsPage.readAllBrandsAndCount();

            // Step 4-7: index letters D and R.
            brandsPage.selectIndexLetter("D", "STEP 04-05");
            brandsPage.selectIndexLetter("R", "STEP 06-07");

            // Step 8: Rabanne.
            brandsPage.selectRabanneBrand();

            // Step 9-12: brand page - carousel, top tabs, bottom toggle.
            brandsPage.readBrandPageAndSelectWomensTab();
            brandsPage.selectMensTab();
            brandsPage.checkWomensListThenSwitchToMensToggle();

            // Step 13: video and About Rabanne.
            brandsPage.playBrandVideoAndReadAbout();

            // Step 14-15: Discover Gifting -> Rabanne PLP, brand check.
            brandsPage.tapDiscoverGifting();
            brandsPage.readRabannePlpAndVerifyBrand();

            // Step 16-17: back to the top, Sort by -> Price : low to high.
            brandsPage.scrollToTopAndSortByPriceLowToHigh();

            // Step 18-23: Filter (Body Lotion, Single) -> show results.
            brandsPage.applyBodyLotionAndSingleFilter();

            // Step 24: read the sorted + filtered PLP.
            brandsPage.readFilteredPlpFully();

            // Step 25-27: three back-arrow taps.
            brandsPage.backFromFilteredPlp();
            brandsPage.backFromBrandPage();
            brandsPage.backFromBrandsList();

            // Step 28: Home icon in the bottom navigation.
            brandsPage.tapHomeNavIconAndVerifyHomePage();

            // Step 29: close the app.
            brandsPage.closeApp();

            long durationSec = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println();
            System.out.println("==================================================");
            System.out.println("[TC030 PASSED]");
            System.out.println("[TC030 TEST METHOD TIME] " + (durationSec / 60) + " minute(s) "
                    + (durationSec % 60) + " second(s)");
            System.out.println("==================================================");

        } catch (Exception | AssertionError e) {
            long durationSec = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println();
            System.out.println("==================================================");
            System.out.println("[TC030 FAILED]");
            System.out.println("[ERROR TYPE] " + e.getClass().getSimpleName());
            System.out.println("[ERROR DETAILS] " + e.getMessage());
            System.out.println("[TC030 TEST METHOD TIME] " + (durationSec / 60) + " minute(s) "
                    + (durationSec % 60) + " second(s)");
            System.out.println("==================================================");
            throw e;
        }
    }
}