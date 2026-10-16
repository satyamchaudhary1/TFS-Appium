package com.tfs.tests.setup;

import com.tfs.annotations.TestInfo;
import com.tfs.config.ConfigManager;
import com.tfs.flows.AuthStartupFlow;
import com.tfs.pages.LoginPage;
import com.tfs.pages.StoreMapPage;
import com.tfs.pages.StoreMapPage.StoreInfo;
import com.tfs.tests.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class StoreMapTest extends BaseTest {

    @Test(groups = {"regression", "homepage", "map"}, priority = 8)
    @TestInfo(id = "TC_008", title = "Home page - Store Map", module = "Home Page")
    public void TC_008_verifyDefaultFirstStoreThenCloseApp() {
        StoreMapPage mapPage = new StoreMapPage();
        ensureLoggedInHome(mapPage);

        mapPage.openMapFromHome();
        mapPage.allowLocationPermissionIfDisplayed();

        Assert.assertTrue(mapPage.isMapScreenDisplayed(),
                "Map screen/search field/Google Map was not displayed.");

        String postcode = ConfigManager.getOrDefault("map.search.postcode", "M17 1WE");
        mapPage.searchByPostcode(postcode);

        Assert.assertTrue(mapPage.isStoreDetailsSheetDisplayed(),
                "Default first-store popup was not displayed for postcode: " + postcode);

        StoreInfo firstStore = mapPage.getFirstVisibleStore();
        mapPage.printFirstStore(firstStore);

        Assert.assertTrue(mapPage.hasRequiredFirstStoreData(firstStore),
                "First store is missing details or Hours/Call/Directions controls.");

        Assert.assertTrue(mapPage.clickBackAndCloseApp(),
                "Map Back action or APK termination failed.");
    }

    private void ensureLoggedInHome(StoreMapPage mapPage) {
        if (mapPage.isHomePageDisplayedQuickly()) return;

        new AuthStartupFlow().navigateToAuthLanding();
        LoginPage loginPage = new LoginPage();
        loginPage.clickSignIn();
        Assert.assertTrue(loginPage.isLoginScreenDisplayed(), "Login screen was not displayed.");
        loginPage.enterEmail(ConfigManager.require("login.email"));
        loginPage.enterPassword(ConfigManager.require("login.password"));
        loginPage.clickLogin();
        if (loginPage.isLoginErrorDisplayed()) {
            Assert.fail("Login failed: " + loginPage.getLoginErrorMessage());
        }
        loginPage.handlePostLoginPopups();
        Assert.assertTrue(loginPage.isHomeScreenDisplayed(),
                "Home screen was not displayed after login.");
    }
}