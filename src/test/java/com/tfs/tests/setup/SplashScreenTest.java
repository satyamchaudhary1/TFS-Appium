package com.tfs.tests.setup;
import com.tfs.flows.AuthStartupFlow;
import com.tfs.annotations.TestInfo;
import com.tfs.pages.SplashPage;
import com.tfs.tests.BaseTest;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SplashScreenTest extends BaseTest {

    @Test(
            groups = {
                    "smoke",
                    "regression",
                    "setup"
            },
            priority = 2
    )
    @TestInfo(
            id = "TC_002",
            title = "Video Play on Auth Screen",
            module = "Setup"
    )
    public void TC_002_authVideoValidation() {

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "TC_002 - Auth Screen Video Validation"
        );

        System.out.println(
                "=============================================="
        );

        SplashPage splashPage =
                new SplashPage();


        // STEP 1
        // Handle Android permission if displayed

        AuthStartupFlow startupFlow =
                new AuthStartupFlow();

        startupFlow
                .waitForAuthVideoReady();


        boolean videoDisplayed =
                splashPage
                        .isVideoDisplayed();

        System.out.println(
                "Video Displayed: "
                        + (
                        videoDisplayed
                                ? "PASS"
                                : "FAIL"
                )
        );

        Assert.assertTrue(
                videoDisplayed,
                "Auth screen video is not displayed."
        );

        System.out.println(
                "Video Displayed: "
                        + (videoDisplayed ? "PASS" : "FAIL")
        );

        Assert.assertTrue(
                videoDisplayed,
                "Auth screen video is not displayed."
        );


        // STEP 3
        // Verify video is autoplaying

        boolean videoAutoplay =
                splashPage.isVideoAutoplaying();

        System.out.println(
                "Video Autoplay: "
                        + (videoAutoplay ? "PASS" : "FAIL")
        );

        Assert.assertTrue(
                videoAutoplay,
                "Auth screen video is displayed but is not autoplaying."
        );


        // STEP 4
        // Verify expected auth screen loaded

        boolean continueDisplayed =
                splashPage.isContinueDisplayed();

        System.out.println(
                "Continue Button Displayed: "
                        + (continueDisplayed ? "PASS" : "FAIL")
        );

        Assert.assertTrue(
                continueDisplayed,
                "Continue button is not displayed on auth video screen."
        );


        System.out.println(
                "=============================================="
        );

        System.out.println(
                "TC_002 RESULT: PASS"
        );

        System.out.println(
                "=============================================="
        );
    }
}