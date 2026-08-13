package com.tfs.pages;

import com.tfs.driver.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;

public class SplashPage extends BasePage {

    /**
     * Handles Android media permission if it appears.
     *
     * Permission popup is Android system behaviour,
     * so TC_002 should not fail if it does not appear.
     */
    public boolean handleMediaPermissionIfPresent() {

        System.out.println(
                "Checking for Android media permission..."
        );

        long timeout =
                System.currentTimeMillis() + 20000;

        while (System.currentTimeMillis() < timeout) {

            try {

                List<WebElement> allowButtons =
                        DriverManager.getDriver()
                                .findElements(
                                        LocatorRepository.by(
                                                "permission.media.allow"
                                        )
                                );

                if (!allowButtons.isEmpty()) {

                    WebElement allowButton =
                            allowButtons.get(0);

                    if (allowButton.isDisplayed()
                            && allowButton.isEnabled()) {

                        System.out.println(
                                "Android media permission displayed."
                        );

                        allowButton.click();

                        System.out.println(
                                "Android media permission Allow clicked."
                        );

                        return true;
                    }
                }

            } catch (Exception ignored) {
                // Keep checking until timeout.
            }

            try {

                Thread.sleep(500);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                throw new IllegalStateException(
                        "Interrupted while waiting for permission.",
                        e
                );
            }
        }

        System.out.println(
                "Android media permission was not detected within 10 seconds."
        );

        return false;
    }

    /**
     * Verify the ExoPlayer video container is visible.
     */
    public boolean isVideoDisplayed() {

        return isDisplayed(
                "auth.video.player"
        );
    }


    /**
     * Verify Continue button is visible.
     *
     * This also gives us an additional marker that
     * the expected auth/video screen loaded.
     */
    public boolean isContinueDisplayed() {

        return isDisplayed(
                "auth.continue"
        );
    }


    /**
     * Verify the background video is actually changing.
     *
     * We capture the video area twice with a delay.
     * A meaningful pixel difference indicates that
     * the video is playing rather than showing
     * one static frame.
     */
    public boolean isVideoAutoplaying() {

        try {

            byte[] firstFrame =
                    captureVideoFrame();

            sleep(3000);

            byte[] secondFrame =
                    captureVideoFrame();

            double difference =
                    calculateFrameDifference(
                            firstFrame,
                            secondFrame
                    );

            System.out.println(
                    "Video frame difference: "
                            + difference
            );

            /*
             * A small threshold prevents insignificant
             * rendering noise from being treated as motion.
             */
            return difference > 2.0;

        } catch (Exception e) {

            System.out.println(
                    "Unable to validate video autoplay: "
                            + e.getMessage()
            );

            return false;
        }
    }


    /**
     * Capture only the video player area.
     */
    private byte[] captureVideoFrame() {

        WebElement video =
                element(
                        "auth.video.player"
                );

        return video.getScreenshotAs(
                OutputType.BYTES
        );
    }


    /**
     * Compare two video screenshots.
     */
    private double calculateFrameDifference(
            byte[] first,
            byte[] second
    ) throws Exception {

        BufferedImage image1 =
                ImageIO.read(
                        new ByteArrayInputStream(first)
                );

        BufferedImage image2 =
                ImageIO.read(
                        new ByteArrayInputStream(second)
                );

        if (image1 == null || image2 == null) {

            throw new IllegalStateException(
                    "Unable to read captured video frame."
            );
        }

        if (image1.getWidth() != image2.getWidth()
                || image1.getHeight() != image2.getHeight()) {

            return 100.0;
        }

        long totalDifference = 0;
        long samples = 0;

        /*
         * Sample pixels instead of checking every pixel.
         * Faster and enough for video movement detection.
         */
        int step = 8;

        for (int y = 0;
             y < image1.getHeight();
             y += step) {

            for (int x = 0;
                 x < image1.getWidth();
                 x += step) {

                int rgb1 =
                        image1.getRGB(x, y);

                int rgb2 =
                        image2.getRGB(x, y);

                int red1 =
                        (rgb1 >> 16) & 0xFF;

                int green1 =
                        (rgb1 >> 8) & 0xFF;

                int blue1 =
                        rgb1 & 0xFF;

                int red2 =
                        (rgb2 >> 16) & 0xFF;

                int green2 =
                        (rgb2 >> 8) & 0xFF;

                int blue2 =
                        rgb2 & 0xFF;

                totalDifference +=
                        Math.abs(red1 - red2);

                totalDifference +=
                        Math.abs(green1 - green2);

                totalDifference +=
                        Math.abs(blue1 - blue2);

                samples += 3;
            }
        }

        if (samples == 0) {
            return 0;
        }

        return (double) totalDifference
                / samples;
    }


    private void sleep(
            long milliseconds
    ) {

        try {

            Thread.sleep(milliseconds);

        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();

            throw new IllegalStateException(
                    "Thread interrupted while waiting.",
                    e
            );
        }
    }

    public void clickContinue() {

        System.out.println(
                "[ACTION] Clicking Continue."
        );

        try {

            WebElement continueButton =
                    DriverManager.getDriver()
                            .findElement(
                                    io.appium.java_client.AppiumBy
                                            .accessibilityId(
                                                    "Continue"
                                            )
                            );

            continueButton.click();

            System.out.println(
                    "[PASS] Continue clicked."
            );

        } catch (Exception e) {

            throw new AssertionError(
                    "Continue button was not displayed.",
                    e
            );
        }
    }
}