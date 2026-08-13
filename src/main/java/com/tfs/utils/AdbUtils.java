package com.tfs.utils;

import com.tfs.config.ConfigManager;

public final class AdbUtils {

    private AdbUtils() {
    }

    private static String adb() {
        return ConfigManager.getOrDefault(
                "adb.path",
                "adb"
        );
    }

    // Check whether ADB is installed/available
    public static boolean isAdbAvailable() {

        CommandExecutor.CommandResult result =
                CommandExecutor.execute(
                        adb(),
                        "version"
                );

        return result.isSuccess();
    }

    // Get connected Android device ID
    public static String getConnectedDeviceId() {

        // If UDID is explicitly configured, use it
        String configuredUdid =
                ConfigManager.get("device.udid");

        if (configuredUdid != null
                && !configuredUdid.isBlank()) {

            return configuredUdid.trim();
        }

        CommandExecutor.CommandResult result =
                CommandExecutor.execute(
                        adb(),
                        "devices"
                );

        if (!result.isSuccess()) {
            return null;
        }

        String[] lines =
                result.getOutput().split("\\R");

        for (String line : lines) {

            String cleanLine = line.trim();

            if (cleanLine.endsWith("\tdevice")) {

                String[] parts =
                        cleanLine.split("\\s+");

                if (parts.length >= 2) {
                    return parts[0];
                }
            }
        }

        return null;
    }

    // Compatibility method used by existing framework
    public static String resolveDeviceId() {
        return getConnectedDeviceId();
    }

    // Check whether Android device is connected
    public static boolean isDeviceConnected() {
        return getConnectedDeviceId() != null;
    }

    // Install APK
    public static CommandExecutor.CommandResult installApk(
            String apkAbsolutePath
    ) {

        String deviceId =
                getConnectedDeviceId();

        if (deviceId == null) {

            return new CommandExecutor.CommandResult(
                    -1,
                    "No Android device connected."
            );
        }

        return CommandExecutor.execute(
                adb(),
                "-s",
                deviceId,
                "install",
                "-r",
                apkAbsolutePath
        );
    }

    // Android OS version
    public static String getAndroidVersion() {

        return getDeviceProperty(
                "ro.build.version.release"
        );
    }

    // Compatibility method used by ReportManager
    public static String androidVersion() {
        return getAndroidVersion();
    }

    // Device manufacturer
    public static String getManufacturer() {

        return getDeviceProperty(
                "ro.product.manufacturer"
        );
    }

    // Device model
    public static String getDeviceModel() {

        return getDeviceProperty(
                "ro.product.model"
        );
    }

    // Force stop application
    public static void forceStop(
            String packageName
    ) {

        if (packageName == null
                || packageName.isBlank()) {
            return;
        }

        String deviceId =
                getConnectedDeviceId();

        if (deviceId == null) {

            throw new IllegalStateException(
                    "No Android device connected."
            );
        }

        CommandExecutor.CommandResult result =
                CommandExecutor.execute(
                        adb(),
                        "-s",
                        deviceId,
                        "shell",
                        "am",
                        "force-stop",
                        packageName
                );

        if (!result.isSuccess()) {

            throw new IllegalStateException(
                    "Unable to force stop application. "
                            + result.getOutput()
            );
        }
    }

    // Clear application data
    public static void clearAppData(
            String packageName
    ) {

        if (packageName == null
                || packageName.isBlank()) {
            return;
        }

        String deviceId =
                getConnectedDeviceId();

        if (deviceId == null) {

            throw new IllegalStateException(
                    "No Android device connected."
            );
        }

        CommandExecutor.CommandResult result =
                CommandExecutor.execute(
                        adb(),
                        "-s",
                        deviceId,
                        "shell",
                        "pm",
                        "clear",
                        packageName
                );

        if (!result.isSuccess()) {

            throw new IllegalStateException(
                    "Unable to clear application data. "
                            + result.getOutput()
            );
        }
    }

    // Read Android device property
    private static String getDeviceProperty(
            String property
    ) {

        String deviceId =
                getConnectedDeviceId();

        if (deviceId == null) {
            return "UNKNOWN";
        }

        CommandExecutor.CommandResult result =
                CommandExecutor.execute(
                        adb(),
                        "-s",
                        deviceId,
                        "shell",
                        "getprop",
                        property
                );

        if (!result.isSuccess()) {
            return "UNKNOWN";
        }

        return result.getOutput().trim();
    }
}