package com.tfs.tests.Basket;

import com.tfs.annotations.TestInfo;
import com.tfs.config.ConfigManager;
import com.tfs.flows.AuthStartupFlow;
import com.tfs.pages.LoginPage;
import com.tfs.pages.LoginUserBasketPage;
import com.tfs.tests.BaseTest;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC031 - Home "Men's" tile -> Men's Fragrances PLP -> 5th product -> PDP
 * (MyTFS membership, first variant, gift options) -> basket (read, quantity 2)
 * -> Secure Checkout -> Express Delivery -> Pay by Card -> Credit Card details
 * -> Pay -> order confirmation -> close the app.
 * <p>
 * Requested as "Test Case 32"; numbered TC031 (next after TC030, the Brands test).
 * <p>
 * REAL MONEY GUARD: Pay is tapped only when framework.properties has
 * payment.submit.enabled=true; otherwise the card details are entered and the
 * test stops short of paying.
 * <p>
 * PART 1 (Authentication) mirrors LoginTest.java's TC_003 exactly, the same
 * way the earlier tests do. Placed under com.tfs.tests.Purchase.
 */
public class LoginUserBasketTest extends BaseTest {

    @Test
    @TestInfo(id = "TC031", title = "Purchase flow - Men's PLP, PDP, gift options, basket, checkout, card payment", module = "Purchase", requiresDriver = true)
    public void TC_031_verifyPurchaseFlow() {

        // ==========================================================
        // TEST DATA
        // ==========================================================
        String email = ConfigManager.require("login.email");
        String password = ConfigManager.require("login.password");

        // Card data from the test steps (the config file's cvv differs; the steps say 737).
        final String cardNumber = "5555444433331111";
        final String cardExpiry = "0330";
        final String cardCvc = "737";

        // Pay is only tapped when this is explicitly "true" (default: not tapped - it places a real order).
        boolean submitEnabled = false;
        try {
            submitEnabled = "true".equalsIgnoreCase(String.valueOf(ConfigManager.get("payment.submit.enabled")).trim());
        } catch (Exception ignored) {
            // key missing - stay safe
        }

        System.out.println();
        System.out.println("==================================================");
        System.out.println("TC_031 - PURCHASE FLOW");
        System.out.println("==================================================");
        System.out.println();
        System.out.println("---------------- TEST DATA ----------------");
        System.out.println("[DATA] Email    : " + email);
        System.out.println("[DATA] Password : ********");
        System.out.println("[DATA] Card     : **** **** **** 1111");
        System.out.println("[DATA] Pay tap  : " + (submitEnabled ? "ENABLED (a real order will be placed)" : "DISABLED (payment.submit.enabled is not true)"));
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
        // PART 2 - PURCHASE FLOW
        // ==========================================================
        System.out.println();
        System.out.println("==================================================");
        System.out.println("PART 2 - PURCHASE FLOW");
        System.out.println("==================================================");

        LoginUserBasketPage purchasePage = new LoginUserBasketPage();
        long startTime = System.currentTimeMillis();

        try {
            // Step 1-3: Men's tile -> PLP -> 5th product.
            purchasePage.selectMenCategoryFromHome();
            purchasePage.readMensPlp();
            purchasePage.selectProductByPosition(5);

            // Step 5-7: PDP - membership, first variant, gift options window.
            purchasePage.readPdpAndOpenMembershipInfo();
            purchasePage.addMembership();
            purchasePage.selectFirstVariant();
            purchasePage.openGiftOptions();

            // Step 8-13: Occasion Bag (Peach) and Gift Bags (Medium).
            purchasePage.selectOccasionBagPeach();
            purchasePage.selectGiftBagMedium();

            // Step 15: order total -> Add and Continue -> basket.
            purchasePage.printOrderTotalAndAddAndContinue();

            // Step 16-19: read the basket, quantity 2, read it again.
            purchasePage.readBasketFully("Basket (quantity 1)");
            purchasePage.changeQuantityToTwo();
            purchasePage.readBasketFully("Basket (quantity 2)");

            // Step 20-21: Secure Checkout and read the checkout page.
            purchasePage.secureCheckout();
            purchasePage.readCheckoutFully();

            // Step 22-24: Express Delivery.
            purchasePage.selectExpressDelivery();

            // Step 25-30: Pay by Card -> Continue -> Credit Card details.
            purchasePage.selectPayByCardAndContinue();
            purchasePage.selectCreditCardAndEnterDetails(cardNumber, cardExpiry, cardCvc);

            // Step 31: Pay (only when enabled) and the order confirmation.
            purchasePage.payAndVerifyOrder(submitEnabled);

            // Step 32: close the app.
            purchasePage.closeApp();

            long durationSec = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println();
            System.out.println("==================================================");
            System.out.println("[TC031 PASSED]");
            System.out.println("[TC031 TEST METHOD TIME] " + (durationSec / 60) + " minute(s) "
                    + (durationSec % 60) + " second(s)");
            System.out.println("==================================================");

        } catch (Exception | AssertionError e) {
            long durationSec = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println();
            System.out.println("==================================================");
            System.out.println("[TC031 FAILED]");
            System.out.println("[ERROR TYPE] " + e.getClass().getSimpleName());
            System.out.println("[ERROR DETAILS] " + e.getMessage());
            System.out.println("[TC031 TEST METHOD TIME] " + (durationSec / 60) + " minute(s) "
                    + (durationSec % 60) + " second(s)");
            System.out.println("==================================================");
            throw e;
        }
    }
}