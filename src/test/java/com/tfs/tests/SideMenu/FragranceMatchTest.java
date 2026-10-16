package com.tfs.tests.SideMenu;

import com.tfs.annotations.TestInfo;
import com.tfs.config.ConfigManager;
import com.tfs.flows.AuthStartupFlow;
import com.tfs.pages.FragrancematchCTAPage;
import com.tfs.pages.LoginPage;
import com.tfs.tests.BaseTest;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC021 - Side Menu -> "Fragrancematch" CTA -> full Fragrance Match quiz
 * (For Myself -> search + pick a fragrance -> Similar -> Male -> Evenings
 * -> 31-40 -> price slider -> Done -> email + opt-in -> View Matches ->
 * results carousel) -> Back -> close app.
 * <p>
 * PART 1 (Authentication) mirrors LoginTest.java's TC_003 exactly, the same
 * way TC017-TC020's PART 1 does, so this test can run standalone from a
 * logged-out state.
 * <p>
 * Placed under com.tfs.tests.SideMenu, matching TC019/TC020's package.
 */
public class FragranceMatchTest extends BaseTest {

    @Test
    @TestInfo(id = "TC021", title = "Side Menu Fragrance Match quiz", module = "SideMenu", requiresDriver = true)
    public void TC_021_verifyFragranceMatchQuizFlow() {

        // ==========================================================
        // TEST DATA
        // ==========================================================
        String email = ConfigManager.require("login.email");
        String password = ConfigManager.require("login.password");

        System.out.println();
        System.out.println("==================================================");
        System.out.println("TC_021 - FRAGRANCE MATCH QUIZ FLOW");
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
        // PART 2 - FRAGRANCE MATCH QUIZ FLOW
        // ==========================================================
        System.out.println();
        System.out.println("==================================================");
        System.out.println("PART 2 - FRAGRANCE MATCH QUIZ FLOW");
        System.out.println("==================================================");

        FragrancematchCTAPage fragranceMatchPage = new FragrancematchCTAPage();
        long startTime = System.currentTimeMillis();

        try {
            // Step 1-2: open Side Menu, tap Fragrancematch CTA.
            fragranceMatchPage.openSideMenu();
            fragranceMatchPage.tapFragranceMatchCta();

            // Step 3: read the intro screen.
            fragranceMatchPage.readFragranceMatchIntroScreen();

            // Step 4: For Myself.
            fragranceMatchPage.tapForMyself();

            // Step 5-9: read search screen, search + select Rabanne
            // (NEXT is now tapped internally as part of confirming the
            // selection actually registered - see FragrancematchCTAPage).
            fragranceMatchPage.readSearchFragranceScreen();
            fragranceMatchPage.searchAndSelectRabanne("Rabanne");

            // Step 10-11: Similar, Next.
            fragranceMatchPage.readAndSelectSimilar();
            fragranceMatchPage.clickNext();

            // Step 12-13: Male, Next.
            fragranceMatchPage.readAndSelectMale();
            fragranceMatchPage.clickNext();

            // Step 14-15: Evenings, Next.
            fragranceMatchPage.readAndSelectEvenings();
            fragranceMatchPage.clickNext();

            // Step 16-17: 31-40, price slider.
            fragranceMatchPage.readAndSelectAgeRange();
            fragranceMatchPage.clickNext();
            fragranceMatchPage.readAndAdjustPriceRangeSlider();

            // Step 18: Done.
            fragranceMatchPage.clickDone();

            // Step 19-21: email, opt-in checkbox, close keyboard.
            fragranceMatchPage.enterEmailAndOptIn(email);
            fragranceMatchPage.closeKeyboard();

            // Step 22-23: View Matches, read results + scroll carousel.
            fragranceMatchPage.clickViewMatches();
            fragranceMatchPage.readResultsAndScrollMatchesCarousel();

            // Step 24: back, close app.
            fragranceMatchPage.clickBack();
            fragranceMatchPage.closeApp();

            long durationSec = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println();
            System.out.println("==================================================");
            System.out.println("[TC021 PASSED]");
            System.out.println("[TC021 TEST METHOD TIME] " + (durationSec / 60) + " minute(s) "
                    + (durationSec % 60) + " second(s)");
            System.out.println("==================================================");

        } catch (Exception | AssertionError e) {
            long durationSec = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println();
            System.out.println("==================================================");
            System.out.println("[TC021 FAILED]");
            System.out.println("[ERROR TYPE] " + e.getClass().getSimpleName());
            System.out.println("[ERROR DETAILS] " + e.getMessage());
            System.out.println("[TC021 TEST METHOD TIME] " + (durationSec / 60) + " minute(s) "
                    + (durationSec % 60) + " second(s)");
            System.out.println("==================================================");
            throw e;
        }
    }
}