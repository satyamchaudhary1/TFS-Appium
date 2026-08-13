package com.tfs.pages;

import com.tfs.driver.DriverManager;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class MembershipPage extends BasePage {

    private final AndroidDriver appDriver;

    private static final By SKIP_BUTTON =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Skip\")"
            );

    private static final By SELECT_MEMBERSHIP_BUTTON =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Select Membership\")"
            );


    public MembershipPage() {

        super();

        this.appDriver =
                DriverManager.getDriver();
    }


    public boolean isMembershipScreenDisplayed() {

        return pageContainsAny(
                "Be the first to get the newest fragrances",
                "TFS UNLIMITED",
                "Select Membership",
                "Skip"
        );
    }


    public boolean isSkipButtonDisplayed() {

        return pageContainsAny(
                "Skip"
        );
    }


    public boolean isSelectMembershipDisplayed() {

        return pageContainsAny(
                "Select Membership"
        );
    }


    public void clickSkip() {

        System.out.println(
                "[ACTION] Clicking Skip on Membership page."
        );

        safeClick(
                SKIP_BUTTON,
                30
        );

        pause(5000);

        System.out.println(
                "[PASS] Membership skipped."
        );
    }


    public void clickSelectMembership() {

        safeClick(
                SELECT_MEMBERSHIP_BUTTON,
                30
        );

        pause(5000);
    }


    private void safeClick(
            By locator,
            int timeoutSeconds
    ) {

        try {

            WebElement element =
                    new WebDriverWait(
                            appDriver,
                            Duration.ofSeconds(
                                    timeoutSeconds
                            )
                    ).until(
                            ExpectedConditions
                                    .elementToBeClickable(
                                            locator
                                    )
                    );

            element.click();

        } catch (Exception e) {

            throw new AssertionError(
                    "Unable to click Membership element.",
                    e
            );
        }
    }


    private boolean pageContainsAny(
            String... values
    ) {

        try {

            String source =
                    appDriver.getPageSource();

            for (String value : values) {

                if (source.contains(value)) {
                    return true;
                }
            }

        } catch (Exception ignored) {
        }

        return false;
    }


    private void pause(long milliseconds) {

        try {

            Thread.sleep(milliseconds);

        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();

            throw new IllegalStateException(
                    "Test interrupted.",
                    e
            );
        }
    }
}