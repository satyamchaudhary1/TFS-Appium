package com.tfs.tests.SideMenu;

import com.tfs.annotations.TestInfo;
import com.tfs.config.ConfigManager;
import com.tfs.flows.AuthStartupFlow;
import com.tfs.pages.BrandsCTAPage;
import com.tfs.pages.LoginPage;
import com.tfs.tests.BaseTest;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC023 - Side Menu -> Brands tab -> All Brands list -> scroll to and
 * select "Zadig & Voltaire" -> Brand Details page (read full content +
 * Bestsellers carousel) -> Discover Gifting -> Gifting PLP -> back -> Hero
 * Banner -> full PLP -> back x3 -> Mega Menu -> close app.
 * <p>
 * NOTE: originally requested as "Test Case 22", but TC022 was already used
 * for the EveryHuman CTA test - numbered as TC023 (next in sequence)
 * instead.
 * <p>
 * PART 1 (Authentication) mirrors LoginTest.java's TC_003 exactly, the same
 * way TC017-TC022's PART 1 does, so this test can run standalone from a
 * logged-out state. Placed under com.tfs.tests.SideMenu, matching
 * TC019-TC022's package.
 */
public class BrandsCTATest extends BaseTest {

    @Test
    @TestInfo(id = "TC023", title = "Side Menu Brands - All Brands, Brand Details, PLPs", module = "SideMenu", requiresDriver = true)
    public void TC_023_verifyBrandsFlow() {

        // ==========================================================
        // TEST DATA
        // ==========================================================
        String email = ConfigManager.require("login.email");
        String password = ConfigManager.require("login.password");

        System.out.println();
        System.out.println("==================================================");
        System.out.println("TC_023 - SIDE MENU BRANDS FLOW");
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

        BrandsCTAPage brandsPage = new BrandsCTAPage();
        long startTime = System.currentTimeMillis();

        try {
            // Step 1-2: Side Menu -> Brands tab.
            brandsPage.openSideMenu();
            brandsPage.scrollToAndTapBrandsTab();

            // Step 3-4: scroll to and select "Zadig & Voltaire".
            brandsPage.scrollAndSelectZadigVoltaire();

            // Step 5: read Brand Details page fully + Bestsellers carousel.
            brandsPage.readBrandDetailsPageFully();

            // Step 6-7: Discover Gifting -> Gifting PLP -> its Bestsellers carousel.
            brandsPage.tapDiscoverGifting();
            brandsPage.checkGiftingPlpBestsellersCarousel();

            // Step 8: back to Brand Details, scroll to top, tap Hero Banner.
            brandsPage.goBackScrollToTopAndTapHeroBanner();

            // Step 9: read the full PLP, then back to Brand Details.
            brandsPage.readFullPlpThenGoBack();

            // Step 10-11: back to All Brands list, then back to Mega Menu.
            brandsPage.goBackToAllBrandsList();
            brandsPage.goBackToMegaMenu();

            // Step 12: close the app.
            brandsPage.closeApp();

            long durationSec = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println();
            System.out.println("==================================================");
            System.out.println("[TC023 PASSED]");
            System.out.println("[TC023 TEST METHOD TIME] " + (durationSec / 60) + " minute(s) "
                    + (durationSec % 60) + " second(s)");
            System.out.println("==================================================");

        } catch (Exception | AssertionError e) {
            long durationSec = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println();
            System.out.println("==================================================");
            System.out.println("[TC023 FAILED]");
            System.out.println("[ERROR TYPE] " + e.getClass().getSimpleName());
            System.out.println("[ERROR DETAILS] " + e.getMessage());
            System.out.println("[TC023 TEST METHOD TIME] " + (durationSec / 60) + " minute(s) "
                    + (durationSec % 60) + " second(s)");
            System.out.println("==================================================");
            throw e;
        }
    }
}