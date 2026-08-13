package com.tfs.tests.setup;

import com.tfs.annotations.TestInfo;
import com.tfs.config.ConfigManager;
import com.tfs.utils.AdbUtils;
import com.tfs.utils.ApkUtils;
import com.tfs.utils.CommandExecutor;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ApkValidationTest {

    @Test(
            groups = {
                    "smoke",
                    "regression",
                    "setup"
            },
            priority = 1
    )
    @TestInfo(
            id = "TC_001",
            title = "Android APK Installation",
            module = "Setup"
    )
    public void TC_001_installation() {

        System.out.println(
                "===================================="
        );

        System.out.println(
                "TC_001 - Android APK Installation"
        );

        System.out.println(
                "===================================="
        );


        // STEP 1 - Validate APK

        String apkPath =
                ConfigManager.get("apk.path");

        Assert.assertTrue(
                ApkUtils.isApkAvailable(apkPath),
                "APK file not found: "
                        + ApkUtils.getAbsolutePath(apkPath)
        );

        String absoluteApkPath =
                ApkUtils.getAbsolutePath(apkPath);

        System.out.println(
                "APK Found: "
                        + absoluteApkPath
        );


        // STEP 2 - Validate ADB

        Assert.assertTrue(
                AdbUtils.isAdbAvailable(),
                "ADB is not available."
        );

        System.out.println(
                "ADB Available: YES"
        );


        // STEP 3 - Validate Android device

        String deviceId =
                AdbUtils.getConnectedDeviceId();

        Assert.assertNotNull(
                deviceId,
                "No Android device connected."
        );

        System.out.println(
                "Device ID: "
                        + deviceId
        );

        System.out.println(
                "Manufacturer: "
                        + AdbUtils.getManufacturer()
        );

        System.out.println(
                "Device Model: "
                        + AdbUtils.getDeviceModel()
        );

        System.out.println(
                "Android Version: "
                        + AdbUtils.getAndroidVersion()
        );


        // STEP 4 - Install APK

        CommandExecutor.CommandResult result =
                AdbUtils.installApk(
                        absoluteApkPath
                );

        System.out.println(
                "Installation Output:"
        );

        System.out.println(
                result.getOutput()
        );


        // STEP 5 - Validate installation

        boolean installedSuccessfully =
                result.isSuccess()
                        &&
                        result.getOutput()
                                .toLowerCase()
                                .contains("success");

        Assert.assertTrue(
                installedSuccessfully,
                "APK installation failed.\n"
                        + result.getOutput()
        );


        System.out.println(
                "===================================="
        );

        System.out.println(
                "TC_001 RESULT: PASS"
        );

        System.out.println(
                "===================================="
        );
    }
}