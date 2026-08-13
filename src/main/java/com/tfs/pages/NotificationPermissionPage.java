package com.tfs.pages;

import com.tfs.driver.DriverManager;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class NotificationPermissionPage extends BasePage {

    private final AndroidDriver appDriver;

    private static final By ENABLE_NOTIFICATIONS_BUTTON =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Enable Notifications\")"
            );

    private static final By SKIP_FOR_NOW_LINK =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Skip for now\")"
            );


    public NotificationPermissionPage() {

        super();

        this.appDriver =
                DriverManager.getDriver();
    }


    public boolean isNotificationScreenDisplayed() {

        return pageContainsAny(
                "Don’t miss a thing",
                "Don't miss a thing",
                "allow notifications",
                "Enable Notifications",
                "Skip for now"
        );
    }


    public boolean isEnableNotificationsDisplayed() {

        return pageContainsAny(
                "Enable Notifications"
        );
    }


    public boolean isSkipForNowDisplayed() {

        return pageContainsAny(
                "Skip for now"
        );
    }


    public void clickSkipForNow() {

        System.out.println(
                "[ACTION] Clicking Skip for now."
        );

        safeClick(
                SKIP_FOR_NOW_LINK,
                30
        );

        pause(5000);

        System.out.println(
                "[PASS] Notification permission skipped."
        );
    }


    public void clickEnableNotifications() {

        safeClick(
                ENABLE_NOTIFICATIONS_BUTTON,
                30
        );

        pause(3000);
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
                    "Unable to click Notification element.",
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