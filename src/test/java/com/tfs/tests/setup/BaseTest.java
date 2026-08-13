package com.tfs.tests;

import com.tfs.annotations.TestInfo;
import com.tfs.driver.DriverFactory;
import com.tfs.driver.DriverManager;
import com.tfs.isolation.TestIsolationManager;
import com.tfs.listeners.TestResultListener;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import java.lang.reflect.Method;

@Listeners(TestResultListener.class)
public abstract class BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(
            Method method
    ) {

        TestInfo info =
                method.getAnnotation(
                        TestInfo.class
                );


        if (info == null) {

            throw new IllegalStateException(
                    "Missing @TestInfo on "
                            + method.getName()
            );
        }


        System.out.println();
        System.out.println(
                "=================================================="
        );

        System.out.println(
                "[TEST SETUP] "
                        + info.id()
                        + " - "
                        + info.title()
        );

        System.out.println(
                "=================================================="
        );


        if (info.requiresDriver()) {

            /*
             * First prepare a clean app state.
             */
            TestIsolationManager
                    .beforeUiTest();


            /*
             * Then create Appium driver.
             */
            DriverFactory.start();


            System.out.println(
                    "[TEST SETUP] AndroidDriver ready."
            );
        }
    }


    @AfterMethod(alwaysRun = true)
    public void afterMethod() {

        System.out.println();
        System.out.println(
                "[TEST CLEANUP] Closing Appium driver..."
        );


        try {

            DriverManager.quitDriver();


            System.out.println(
                    "[TEST CLEANUP] AndroidDriver closed."
            );


        } finally {

            /*
             * Give UiAutomator2/Appium a short time to
             * completely release the application before
             * the next test executes pm clear.
             */
            waitAfterDriverQuit();
        }
    }


    private void waitAfterDriverQuit() {

        try {

            System.out.println(
                    "[TEST CLEANUP] Waiting for Appium teardown..."
            );


            Thread.sleep(
                    2000
            );


            System.out.println(
                    "[TEST CLEANUP] Teardown wait completed."
            );


        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();


            throw new IllegalStateException(
                    "Interrupted while waiting for Appium teardown.",
                    e
            );
        }
    }
}