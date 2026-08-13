package com.tfs.isolation;

import com.tfs.config.ConfigManager;
import com.tfs.utils.AdbUtils;

public final class TestIsolationManager {

    public enum Mode {
        FULL_APP_RESET,
        RESTART_APP,
        NONE
    }

    private static final int CLEAR_DATA_MAX_ATTEMPTS = 3;
    private static final long RETRY_WAIT_MS = 2000;

    private TestIsolationManager() {
    }


    public static void beforeUiTest() {

        String packageName =
                ConfigManager.get("app.package");

        Mode isolationMode =
                mode();


        System.out.println();
        System.out.println(
                "=================================================="
        );

        System.out.println(
                "[ISOLATION] Preparing application for next test"
        );

        System.out.println(
                "[ISOLATION] Mode    : " + isolationMode
        );

        System.out.println(
                "[ISOLATION] Package : " + packageName
        );

        System.out.println(
                "=================================================="
        );


        if (packageName == null
                || packageName.isBlank()
                || isolationMode == Mode.NONE) {

            System.out.println(
                    "[ISOLATION] No application reset required."
            );

            return;
        }


        // ======================================================
        // FORCE STOP
        // ======================================================

        System.out.println(
                "[ISOLATION] Force stopping application..."
        );


        AdbUtils.forceStop(
                packageName
        );


        System.out.println(
                "[ISOLATION] Application force stopped."
        );


        waitForDevice(
                1200
        );


        // ======================================================
        // FULL APP RESET
        // ======================================================

        if (isolationMode == Mode.FULL_APP_RESET) {

            clearAppDataWithRetry(
                    packageName
            );
        }


        System.out.println(
                "[ISOLATION] Application preparation completed."
        );

        System.out.println(
                "=================================================="
        );
    }


    private static void clearAppDataWithRetry(
            String packageName
    ) {

        RuntimeException lastException = null;


        for (int attempt = 1;
             attempt <= CLEAR_DATA_MAX_ATTEMPTS;
             attempt++) {

            System.out.println(
                    "[ISOLATION] Clear app data attempt "
                            + attempt
                            + "/"
                            + CLEAR_DATA_MAX_ATTEMPTS
            );


            try {

                /*
                 * Force-stop again before retrying.
                 * This helps when the previous Appium session
                 * is still finishing its teardown.
                 */
                AdbUtils.forceStop(
                        packageName
                );


                waitForDevice(
                        800
                );


                AdbUtils.clearAppData(
                        packageName
                );


                System.out.println(
                        "[ISOLATION] App data cleared successfully."
                );


                return;


            } catch (RuntimeException e) {

                lastException = e;


                System.out.println(
                        "[ISOLATION] Clear app data attempt "
                                + attempt
                                + " failed."
                );


                System.out.println(
                        "[ISOLATION] Reason: "
                                + e.getMessage()
                );


                if (attempt
                        < CLEAR_DATA_MAX_ATTEMPTS) {

                    System.out.println(
                            "[ISOLATION] Waiting "
                                    + RETRY_WAIT_MS
                                    + " ms before retry..."
                    );


                    waitForDevice(
                            RETRY_WAIT_MS
                    );
                }
            }
        }


        throw new IllegalStateException(
                "Unable to clear application data after "
                        + CLEAR_DATA_MAX_ATTEMPTS
                        + " attempts.",
                lastException
        );
    }


    public static Mode mode() {

        String value =
                ConfigManager.get(
                        "isolation.mode"
                );


        if (value == null
                || value.isBlank()) {

            return Mode.FULL_APP_RESET;
        }


        try {

            return Mode.valueOf(
                    value.trim()
                            .toUpperCase()
            );


        } catch (IllegalArgumentException e) {

            throw new IllegalStateException(
                    "Invalid isolation.mode: "
                            + value
                            + ". Supported values are "
                            + "FULL_APP_RESET, RESTART_APP, NONE."
            );
        }
    }


    private static void waitForDevice(
            long milliseconds
    ) {

        try {

            Thread.sleep(
                    milliseconds
            );


        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();


            throw new IllegalStateException(
                    "Test isolation wait was interrupted.",
                    e
            );
        }
    }
}