package com.tfs.utils;

import java.io.File;

public final class ApkUtils {

    private ApkUtils() {
    }

    public static File getApkFile(
            String apkPath
    ) {

        if (apkPath == null ||
                apkPath.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "APK path is empty."
            );
        }

        return new File(apkPath);
    }


    public static boolean isApkAvailable(
            String apkPath
    ) {

        File apkFile =
                getApkFile(apkPath);

        return apkFile.exists()
                && apkFile.isFile()
                && apkFile.getName()
                .toLowerCase()
                .endsWith(".apk");
    }


    public static String getAbsolutePath(
            String apkPath
    ) {

        return getApkFile(apkPath)
                .getAbsolutePath();
    }


    public static String getApkName(
            String apkPath
    ) {

        return getApkFile(apkPath)
                .getName();
    }
}