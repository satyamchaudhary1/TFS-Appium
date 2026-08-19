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

import java.util.List;

public class StoreMapTest extends BaseTest {

    @Test(groups = {"regression", "homepage", "map"}, priority = 8)
    @TestInfo(
            id = "TC_008",
            title = "Home page - Store Map",
            module = "Home Page")
    public void TC_008_verifyStoreMapJourney() {
        StoreMapPage mapPage = new StoreMapPage();
        ensureLoggedInHome(mapPage);

        mapPage.openMapFromHome();

        boolean permissionDenied = mapPage.denyLocationPermissionIfDisplayed();
        System.out.println("[RESULT] Location permission deny handled: " + permissionDenied);
        Assert.assertTrue(permissionDenied,
                "Location permission dialog was not displayed. Reset Location permission " +
                        "for The Fragrance Shop before running this denial scenario.");

        Assert.assertTrue(mapPage.isMapScreenDisplayed(),
                "Map screen/search field/Google Map did not load after opening Store Map.");

        String postcode = ConfigManager.getOrDefault("map.search.postcode", "M12 1WE");
        mapPage.searchByPostcode(postcode);

        Assert.assertTrue(mapPage.isStoreDetailsSheetDisplayed(),
                "Store details bottom sheet did not open after postcode search: " + postcode);

        List<StoreInfo> stores = mapPage.collectAllStores();
        Assert.assertFalse(stores.isEmpty(),
                "No dynamic store results were returned for postcode: " + postcode);
        Assert.assertTrue(mapPage.everyStoreHasRequiredData(stores),
                "One or more stores are missing name/address/postcode/distance/hours/Call/Directions.");

        Assert.assertTrue(mapPage.expandAndValidateFirstStoreHours(),
                "Weekly store hours were not displayed after expanding the first store.");
        Assert.assertTrue(mapPage.validateFirstStoreCallAndDirections(),
                "Call Store or Directions did not open a valid external handler.");
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