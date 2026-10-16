package com.tfs.tests.SideMenu;

import com.tfs.annotations.TestInfo;
import com.tfs.config.ConfigManager;
import com.tfs.flows.AuthStartupFlow;
import com.tfs.pages.SearchFieldPage;
import com.tfs.pages.LoginPage;
import com.tfs.tests.BaseTest;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC019 - Search "DIOR" from the bottom nav -> select "dior" suggestion ->
 * DIOR PLP -> Show More -> Sort By (Price: low to high) -> Filter
 * (Brand=DIOR, Price=£20-£39.99) -> show results -> verify filtered PLP ->
 * Back -> close app.
 * <p>
 * PART 1 (Authentication) mirrors LoginTest.java's TC_003 exactly, the same
 * way MyTfsMembershipTest.java's and DiscoverMoreTest.java's PART 1 does,
 * so this test can run standalone from a logged-out state.
 */
public class SearchFieldTest extends BaseTest {

    @Test
    @TestInfo(id = "TC019", title = "Search DIOR, PLP sort and filter", module = "Home", requiresDriver = true)
    public void TC_019_verifyDiorSearchSortAndFilterFlow() {

        // ==========================================================
        // TEST DATA
        // ==========================================================
        String email = ConfigManager.require("login.email");
        String password = ConfigManager.require("login.password");

        System.out.println();
        System.out.println("==================================================");
        System.out.println("TC_019 - DIOR SEARCH, SORT AND FILTER FLOW");
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
        // PART 2 - DIOR SEARCH, SORT AND FILTER FLOW
        // ==========================================================
        System.out.println();
        System.out.println("==================================================");
        System.out.println("PART 2 - DIOR SEARCH, SORT AND FILTER FLOW");
        System.out.println("==================================================");

        SearchFieldPage plpPage = new SearchFieldPage();
        long startTime = System.currentTimeMillis();

        try {
            // Step 1-3: open Search, type DIOR, select the "dior" suggestion.
            plpPage.openSearch();
            plpPage.searchAndSelectDior("DIOR");

            // Step 4: confirm DIOR PLP is displayed.
            plpPage.verifyDiorPlpDisplayed();

            // Step 5-6: scroll to Show More, tap it.
            plpPage.scrollToShowMoreButton();
            plpPage.clickShowMore();

            // Step 7-8: back to top, Sort By -> Price: low to high.
            plpPage.scrollToTopAndSortByPriceLowToHigh();

            // Step 9-11: Filter -> Brand=DIOR, Price=£20-£39.99 -> show results.
            plpPage.clickFilter();
            plpPage.selectDiorBrandAndPriceRangeFilter();
            plpPage.clickShowResults();

            // Step 12: verify the filtered PLP content.
            plpPage.verifyFilteredPlpAndScroll();

            // Step 13-14: back, close the app.
            plpPage.clickBack();
            plpPage.closeApp();

            long durationSec = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println();
            System.out.println("==================================================");
            System.out.println("[TC019 PASSED]");
            System.out.println("[TC019 TEST METHOD TIME] " + (durationSec / 60) + " minute(s) "
                    + (durationSec % 60) + " second(s)");
            System.out.println("==================================================");

        } catch (Exception | AssertionError e) {
            long durationSec = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println();
            System.out.println("==================================================");
            System.out.println("[TC019 FAILED]");
            System.out.println("[ERROR TYPE] " + e.getClass().getSimpleName());
            System.out.println("[ERROR DETAILS] " + e.getMessage());
            System.out.println("[TC019 TEST METHOD TIME] " + (durationSec / 60) + " minute(s) "
                    + (durationSec % 60) + " second(s)");
            System.out.println("==================================================");
            throw e;
        }
    }
}