package com.tfs.driver;

import com.tfs.config.ConfigManager;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import java.net.URI;
import java.time.Duration;

public final class DriverFactory {

    private DriverFactory() {
    }

    public static synchronized void start() {

        // Do not create another driver if one already exists
        if (DriverManager.hasDriver()) {
            return;
        }

        try {

            String appiumUrl =
                    ConfigManager.require("appium.url");

            String platformName =
                    ConfigManager.getOrDefault(
                            "platform.name",
                            "Android"
                    );

            String automationName =
                    ConfigManager.getOrDefault(
                            "automation.name",
                            "UiAutomator2"
                    );

            String deviceName =
                    ConfigManager.getOrDefault(
                            "device.name",
                            "Android Device"
                    );

            String udid =
                    ConfigManager.get(
                            "device.udid"
                    );

            String appPackage =
                    ConfigManager.require(
                            "app.package"
                    );

            String appActivity =
                    ConfigManager.require(
                            "app.activity"
                    );

            long newCommandTimeout =
                    ConfigManager.getLong(
                            "new.command.timeout.seconds",
                            120
                    );


            UiAutomator2Options options =
                    new UiAutomator2Options();

            options.setPlatformName(
                    platformName
            );

            options.setAutomationName(
                    automationName
            );

            options.setDeviceName(
                    deviceName
            );

            if (udid != null
                    && !udid.isBlank()) {

                options.setUdid(
                        udid.trim()
                );
            }


            /*
             * IMPORTANT
             *
             * TC_001 handles APK installation.
             *
             * UI tests should launch the already
             * installed application.
             *
             * Therefore we DO NOT use:
             *
             * options.setApp(...)
             */

            options.setAppPackage(
                    appPackage
            );

            options.setAppActivity(
                    appActivity
            );

            options.setNoReset(true);

            options.setFullReset(false);

            options.setNewCommandTimeout(
                    Duration.ofSeconds(
                            newCommandTimeout
                    )
            );


            AndroidDriver driver =
                    new AndroidDriver(
                            URI.create(
                                    appiumUrl
                            ).toURL(),
                            options
                    );

            DriverManager.setDriver(
                    driver
            );

            System.out.println(
                    "AndroidDriver started successfully."
            );

            System.out.println(
                    "Package: " + appPackage
            );

            System.out.println(
                    "Activity: " + appActivity
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to start AndroidDriver.",
                    e
            );
        }
    }
}