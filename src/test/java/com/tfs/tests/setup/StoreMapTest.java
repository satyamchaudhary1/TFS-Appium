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

        boolean permissionAllowed = mapPage.allowLocationPermissionIfDisplayed();
        System.out.println("[RESULT] Location permission handled: "
                + (permissionAllowed ? "While using the app clicked" : "Already granted"));

        Assert.assertTrue(mapPage.isMapScreenDisplayed(),
                "Map screen/search field/Google Map did not load after opening Store Map.");

        String postcode = ConfigManager.getOrDefault("map.search.postcode", "M12 1WE");
        mapPage.searchByPostcode(postcode);
        mapPage.expandStoreDetailsSheet();

        Assert.assertTrue(mapPage.isStoreDetailsSheetDisplayed(),
                "Store details bottom sheet did not open after postcode search: " + postcode);

        List<StoreInfo> stores = mapPage.collectAllStores();
        Assert.assertFalse(stores.isEmpty(),
                "No dynamic store results were returned for postcode: " + postcode);
        Assert.assertTrue(mapPage.everyStoreHasRequiredData(stores),
                "One or more stores are missing name/address/postcode/distance/hours/Call/Directions.");

        Assert.assertTrue(mapPage.validateAllStoreHours(stores),
                "Weekly hours or Hours/Call/Directions controls failed for one or more stores.");
        Assert.assertTrue(mapPage.validateAllStoreCallAndDirections(stores),
                "Call Store or Directions failed for one or more stores.");
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