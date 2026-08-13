package com.tfs.pages;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public final class LocatorRepository {
    private static final Properties PROPS = load();
    private LocatorRepository() {}

    public static By by(String key) {
        String raw = PROPS.getProperty(key, "").trim();
        if (raw.isBlank()) throw new IllegalStateException("Locator is not configured: " + key);
        int i = raw.indexOf('=');
        if (i <= 0) throw new IllegalStateException("Invalid locator format: " + raw);
        String type = raw.substring(0, i).trim().toLowerCase();
        String value = raw.substring(i + 1).trim();
        return switch (type) {
            case "id" -> By.id(value);
            case "xpath" -> By.xpath(value);
            case "accessibility" -> AppiumBy.accessibilityId(value);
            case "androiduiautomator" -> AppiumBy.androidUIAutomator(value);
            default -> throw new IllegalStateException("Unsupported locator type: " + type);
        };
    }

    private static Properties load() {
        Properties p = new Properties();
        Path file = Path.of("src/test/resources/config/locators.properties");
        if (!Files.exists(file)) return p;
        try (InputStream in = Files.newInputStream(file)) {
            p.load(in); return p;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load locators.properties", e);
        }
    }
}
