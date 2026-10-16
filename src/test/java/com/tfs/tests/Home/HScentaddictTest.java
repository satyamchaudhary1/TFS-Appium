package com.tfs.tests.Home;

import com.tfs.annotations.TestInfo;
import com.tfs.config.ConfigManager;
import com.tfs.flows.AuthStartupFlow;
import com.tfs.pages.HScentaddictPage;
import com.tfs.pages.LoginPage;
import com.tfs.tests.BaseTest;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * TC018 - Scentaddict Get Started -> WebView -> Close tab
 * CODE_VERSION: TC018-SCENTADDICT-STEPS-1-3-V2-H-PREFIX
 */
public class HScentaddictTest extends BaseTest {

    private HScentaddictPage scentaddictPage;

    @BeforeMethod(alwaysRun = true)
    public void loginAndInitPage() throws Exception {

        System.out.println("[TC018 SETUP] Preparing authenticated session...");

        String email    = ConfigManager.require("login.email");
        String password = ConfigManager.require("login.password");

        new AuthStartupFlow().navigateToAuthLanding();

        LoginPage loginPage = new LoginPage();
        loginPage.clickSignIn();
        loginPage.enterEmail(email);
        loginPage.enterPassword(password);
        loginPage.clickLogin();

        if (loginPage.isLoginErrorDisplayed()) {
            String message = loginPage.getLoginErrorMessage();
            loginPage.closeLoginErrorPopup();
            Assert.fail("[TC018 SETUP] Login failed: " + message);
        }

        loginPage.handlePostLoginPopups();

        Assert.assertTrue(
                loginPage.isHomeScreenDisplayed(),
                "[TC018 SETUP] Home screen not displayed after login."
        );

        System.out.println("[TC018 SETUP] Login successful. On Home screen.");

        scentaddictPage = new HScentaddictPage();
        System.out.println("[TC018 CODE VERSION] " + HScentaddictPage.CODE_VERSION);
    }

    @TestInfo(
            id = "TC018",
            title = "Scentaddict Get Started and Close Tab",
            module = "MobileStories",
            requiresDriver = true
    )
    @Test(
            groups = { "regression" },
            description = "TC018 - Scentaddict Get Started -> WebView -> Close tab"
    )
    public void tc018_scentaddictFlow() {

        try {
            scentaddictPage.scrollToAndClickScentaddictGetStarted();   // Step 1
            scentaddictPage.verifyScentaddictWebViewOpened();          // Step 2
            scentaddictPage.closeTabAndReturnToApp();                  // Step 3

            System.out.println("[PASS] TC018 completed successfully.");

        } catch (Exception e) {
            System.err.println("[FAIL] TC018 failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}