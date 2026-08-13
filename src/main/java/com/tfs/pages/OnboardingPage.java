package com.tfs.pages;

import com.tfs.driver.DriverManager;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class OnboardingPage extends BasePage {

    private final AndroidDriver appDriver;

    private static final By NEXT_BUTTON =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Next\")"
            );

    private static final By DONE_BUTTON =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Done\")"
            );


    public OnboardingPage() {

        super();

        this.appDriver =
                DriverManager.getDriver();
    }


    public boolean isOnboardingDisplayed() {

        return pageContainsAny(
                "Next",
                "Done",
                "This button will show you",
                "Join MyTFS"
        );
    }


    public void clickNextIfDisplayed() {

        if (!pageContainsAny("Next")) {
            return;
        }

        try {

            safeClick(
                    NEXT_BUTTON,
                    5
            );

            System.out.println(
                    "[PASS] Onboarding Next clicked."
            );

            pause(1500);

        } catch (Exception e) {

            System.out.println(
                    "[INFO] Onboarding Next was not clickable."
            );
        }
    }


    public void clickDoneIfDisplayed() {

        if (!pageContainsAny("Done")) {
            return;
        }

        try {

            safeClick(
                    DONE_BUTTON,
                    5
            );

            System.out.println(
                    "[PASS] Onboarding Done clicked."
            );

            pause(2000);

        } catch (Exception e) {

            System.out.println(
                    "[INFO] Onboarding Done was not clickable."
            );
        }
    }


    public void handleOnboardingIfDisplayed() {

        System.out.println(
                "[ACTION] Checking onboarding."
        );

        /*
         * Supports more than one Next coach mark.
         */
        for (int i = 1; i <= 6; i++) {

            if (pageContainsAny("Done")) {

                clickDoneIfDisplayed();

                break;
            }

            if (pageContainsAny("Next")) {

                clickNextIfDisplayed();

                continue;
            }

            break;
        }
    }


    public boolean isHomeScreenDisplayed() {

        System.out.println(
                "[STEP] Checking Home screen."
        );

        long endTime =
                System.currentTimeMillis()
                        + 30000;

        while (System.currentTimeMillis()
                < endTime) {

            try {

                String source =
                        appDriver.getPageSource();

                int matches = 0;

                if (source.contains("Shop")) {
                    matches++;
                }

                if (source.contains("Search")) {
                    matches++;
                }

                if (source.contains("Basket")) {
                    matches++;
                }

                if (source.contains("Account")) {
                    matches++;
                }

                if (source.contains("MyTFS")) {
                    matches++;
                }

                if (source.contains("Women's")) {
                    matches++;
                }

                if (source.contains("Men's")) {
                    matches++;
                }

                if (source.contains("Gift Sets")) {
                    matches++;
                }

                if (source.contains("New In")) {
                    matches++;
                }

                if (matches >= 2) {

                    return true;
                }

            } catch (Exception ignored) {
            }

            pause(500);
        }

        return false;
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
                    "Unable to click onboarding element.",
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