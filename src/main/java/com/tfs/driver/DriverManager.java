package com.tfs.driver;

import io.appium.java_client.android.AndroidDriver;

public final class DriverManager {

    private static final ThreadLocal<AndroidDriver> DRIVER =
            new ThreadLocal<>();

    private DriverManager() {
    }

    public static void setDriver(
            AndroidDriver driver
    ) {

        DRIVER.set(driver);
    }


    public static AndroidDriver getDriver() {

        AndroidDriver driver =
                DRIVER.get();

        if (driver == null) {

            throw new IllegalStateException(
                    "AndroidDriver is not initialized."
            );
        }

        return driver;
    }


    public static boolean hasDriver() {

        return DRIVER.get() != null;
    }


    public static void quitDriver() {

        AndroidDriver driver =
                DRIVER.get();

        if (driver != null) {

            try {

                driver.quit();

            } finally {

                DRIVER.remove();
            }
        }
    }
}